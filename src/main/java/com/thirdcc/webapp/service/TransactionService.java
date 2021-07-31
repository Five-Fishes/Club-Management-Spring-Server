package com.thirdcc.webapp.service;

import com.thirdcc.webapp.domain.enumeration.TransactionStatus;
import com.thirdcc.webapp.service.dto.EventBudgetTotalDTO;
import com.thirdcc.webapp.service.dto.TransactionDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.thirdcc.webapp.domain.Transaction}.
 */
public interface TransactionService {

    /**
     * Save a transaction.
     *
     * @param transactionDTO the entity to save.
     * @return the persisted transaction.
     */
    TransactionDTO save(MultipartFile imageFile, TransactionDTO transactionDTO) throws IOException;

    /**
     * Get all the transactions.
     *
     * @param pageable the pagination information.
     * @return the list of transactions.
     */
    Page<TransactionDTO> findAll(Pageable pageable);

    /**
     * Get the "id" transaction.
     *
     * @param id the id of the transaction.
     * @return the transaction.
     */
    Optional<TransactionDTO> findOne(Long id);

    /**
     * Delete the "id" transaction.
     *
     * @param id the id of the transaction.
     */
    void delete(Long id);

    /**
     * Get a list of transaction by specifying an eventId.
     *
     * @param eventId the eventId in use to find related transactions.
     * @param pageable the pagination information.
     * @return the list of transactions related to the specified eventId
     */
    Page<TransactionDTO> findAllByEventId(Long eventId, Pageable pageable);

    /**
     * Get the Event Budget total.
     *
     * @param eventId the eventId to get Budget Total.
     * @return the total EventBudgetTotalDTO.
     */
    EventBudgetTotalDTO findTotalTransactionByEventId(Long eventId);
    
    /**
     * Update transaction status by id.
     *
     * @param id the id of the entity.
     * @param transactionStatus the updated transactionStatus of the entity.
     * @return the persisted transaction.
     */
    public TransactionDTO updateTransactionStatus(Long id, TransactionStatus transactionStatus);
}
