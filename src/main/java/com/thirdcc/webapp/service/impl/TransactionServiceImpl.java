package com.thirdcc.webapp.service.impl;

import com.thirdcc.webapp.domain.Event;
import com.thirdcc.webapp.domain.enumeration.TransactionStatus;
import com.thirdcc.webapp.domain.enumeration.TransactionType;
import com.thirdcc.webapp.exception.BadRequestException;
import com.thirdcc.webapp.service.EventService;
import com.thirdcc.webapp.service.ImageStorageService;
import com.thirdcc.webapp.service.TransactionService;
import com.thirdcc.webapp.domain.Transaction;
import com.thirdcc.webapp.repository.TransactionRepository;
import com.thirdcc.webapp.service.dto.EventBudgetTotalDTO;
import com.thirdcc.webapp.service.dto.ImageStorageDTO;
import com.thirdcc.webapp.service.dto.TransactionDTO;
import com.thirdcc.webapp.service.mapper.TransactionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Transaction}.
 */
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final EventService eventService;

    private final ImageStorageService imageStorageService;

    public TransactionServiceImpl(
        TransactionRepository transactionRepository,
        TransactionMapper transactionMapper,
        EventService eventService,
        ImageStorageService imageStorageService
    ) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.eventService = eventService;
        this.imageStorageService = imageStorageService;
    }

    /**
     * Save a transaction.
     *
     * @param transactionDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public TransactionDTO save(MultipartFile multipartFile, TransactionDTO transactionDTO) throws IOException {
        log.debug("Request to save Transaction : {}", transactionDTO);

        // EventID is nullable because some INCOME/EXPENSE might not be associated to event (donation & etc)
        // - Check all required fields present
        // - transaction_amount
        //   - created_by
        //   - transaction_status
        //   - title
        // - Create object
        // - Save

        if (transactionDTO.getEventId() != null){
            // Check if there is existing event and the status is not cancelled
            Event ongoingEvent = eventService.findEventByIdAndNotCancelledStatus(transactionDTO.getEventId());
            if (ongoingEvent == null) {
                throw new BadRequestException("Event is cancelled");
            }
        }

        boolean isTransactionDTOValid = validateTransactionDTO(transactionDTO, multipartFile);

        // Throw exception on invalid transactionDTO
        if (!isTransactionDTOValid){
            throw new BadRequestException(
                "Transaction must include fields " +
                    "transaction_amount, " +
                    "transaction_status, " +
                    "title, " +
                    "image_link(expense) OR multipartFile(if no image_link)"
            );
        }

        boolean hasImageLink = transactionDTO.getImageLink() != null && !transactionDTO.getImageLink().isEmpty();
        boolean hasImageFile = multipartFile != null && !multipartFile.isEmpty();

        // Generate imageLink if required
        if ( !hasImageLink && hasImageFile ){
            // Create new Image
            ImageStorageDTO imageStorageDTO = new ImageStorageDTO();
            String imageLink = imageStorageService.save(imageStorageDTO, multipartFile).getImageUrl();

            log.debug("imageStorageDTO: {}",imageStorageDTO);
            log.debug("imageLink: {}",imageLink);

            // Set the new Image URL for this transaction
            transactionDTO.setImageLink(imageLink);
        }

        // Save the transaction
        Transaction transaction = transactionMapper.toEntity(transactionDTO);
        transaction = transactionRepository.save(transaction);
        return transactionMapper.toDto(transaction);
    }

    /**
     * Get all the transactions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Transactions");
        return transactionRepository.findAll(pageable)
            .map(transactionMapper::toDto);
    }


    /**
     * Get one transaction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionDTO> findOne(Long id) {
        log.debug("Request to get Transaction : {}", id);
        return transactionRepository.findById(id)
            .map(transactionMapper::toDto);
    }

    /**
     * Delete the transaction by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Transaction : {}", id);
        transactionRepository.deleteById(id);
    }
        
    /**
     * Update transaction status by id.
     *
     * @param id the id of the entity.
     * @param transactionStatus the updated transactionStatus of the entity.
     * @return the persisted transaction.
     */
    @Override
    public TransactionDTO updateTransactionStatus(Long id, TransactionStatus transactionStatus) {
        log.debug("Request to update Transaction : {} to status: {}", id, transactionStatus);
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("Transaction not exists: " + id));
        if(!TransactionStatus.PENDING.equals(transaction.getTransactionStatus())){
            throw new BadRequestException("Transaction is not pending, not allow to update");
        }
        transaction.setTransactionStatus(transactionStatus);
        return transactionMapper.toDto(
            transactionRepository.save(transaction)
        );
    }

    @Override
    public Page<TransactionDTO> findAllByEventId(Long eventId, Pageable pageable) {
        log.debug("Request to find all Transaction of event: {}", eventId);
        eventService.findEventByIdAndNotCancelledStatus(eventId);
        return transactionRepository.findAllByEventId(eventId, pageable)
            .map(transactionMapper::toDto);
    }

    @Override
    public EventBudgetTotalDTO findTotalTransactionByEventId(Long eventId) {
        log.debug("Request to get total transactions by event Id");
        EventBudgetTotalDTO eventBudgetTotalDTO = new EventBudgetTotalDTO();
        transactionRepository.findAllByEventId(eventId, Pageable.unpaged())
            .forEach(transaction -> {
                if (TransactionType.EXPENSE == transaction.getTransactionType()) {
                    eventBudgetTotalDTO.addTotalExpense(transaction.getTransactionAmount());
                } else if (TransactionType.INCOME == transaction.getTransactionType()) {
                    eventBudgetTotalDTO.addTotalIncome(transaction.getTransactionAmount());
                }
            });
        return eventBudgetTotalDTO;
    }

    /**
     * Validate on fields:
     * - title
     * - transaction_amount
     * - transaction_status
     * - file
     *
     * @param transactionDTO the TransactionDTO to validate.
     * @return is the TransactionDTO valid
     */
    private boolean validateTransactionDTO(TransactionDTO transactionDTO, MultipartFile multipartFile){

        boolean hasTitle = !transactionDTO.getTitle().isEmpty();
        boolean hasTransactionAmount = transactionDTO.getTransactionAmount() != null && transactionDTO.getTransactionAmount().intValue() >= 0;
        boolean hasTransactionStatus = transactionDTO.getTransactionStatus() != null;
        boolean hasImageLink = (transactionDTO.getImageLink() != null && !transactionDTO.getImageLink().isEmpty());
        boolean hasMultipartFile = multipartFile != null && !multipartFile.isEmpty();

        if (transactionDTO.getTransactionType() == TransactionType.EXPENSE){
            // if there is no image link, then it is a new transaction, thus must have an image file to be uploaded
            if (!hasImageLink){
                if (!hasMultipartFile) {
                    return false;
                }else {
                    return (hasTitle && hasMultipartFile && hasTransactionAmount && hasTransactionStatus);
                }
            }
            return (hasTitle && hasImageLink && hasTransactionAmount && hasTransactionStatus);
        }else{
            // if this is INCOME (DEBT), then image link is not necessary, because treasurer doesn't have to audit this transaction with receipt image
            return (hasTitle && hasTransactionAmount && hasTransactionStatus);
        }
    }

//    private ClaimDTO createClaimRecord(Transaction transaction) {
//        ClaimDTO claimDTO = new ClaimDTO();
//        claimDTO.setAmount(transaction.getTransactionAmount());
//        claimDTO.setStatus(ClaimStatus.OPEN);
//        claimDTO.setCreatedBy(transaction.getCreatedBy());
//        claimDTO.setCreatedDate(transaction.getCreatedDate());
//        claimDTO.setTransactionId(transaction.getId());
//        return claimService.save(claimDTO);
//    }
}
