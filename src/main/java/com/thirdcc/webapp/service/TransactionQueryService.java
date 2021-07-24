package com.thirdcc.webapp.service;

import com.thirdcc.webapp.domain.*; // for static metamodels
import com.thirdcc.webapp.domain.Transaction;
import com.thirdcc.webapp.repository.TransactionRepository;
import com.thirdcc.webapp.repository.UserRepository;
import com.thirdcc.webapp.service.criteria.TransactionCriteria;
import com.thirdcc.webapp.service.dto.TransactionDTO;
import com.thirdcc.webapp.service.mapper.TransactionMapper;

import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Transaction} entities in the database.
 * The main input is a {@link TransactionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link TransactionDTO} or a {@link Page} of {@link TransactionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TransactionQueryService extends QueryService<Transaction> {

    private final Logger log = LoggerFactory.getLogger(TransactionQueryService.class);

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;

    private final TransactionMapper transactionMapper;

    public TransactionQueryService(TransactionRepository transactionRepository, TransactionMapper transactionMapper, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.userRepository = userRepository;
    }

    /**
     * Return a {@link List} of {@link TransactionDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> findByCriteria(TransactionCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionMapper.toDto(transactionRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link TransactionDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByCriteria(TransactionCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.findAll(specification, page).map(transactionMapper::toDto).map(this::mapUserName);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TransactionCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.count(specification);
    }

    /**
     * Function to convert {@link TransactionCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Transaction> createSpecification(TransactionCriteria criteria) {
        Specification<Transaction> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Transaction_.id));
            }
            if (criteria.getTitle() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTitle(), Transaction_.title));
            }
            if (criteria.getTransactionDate() != null) {
                specification = specification.and(buildSpecification(criteria.getTransactionDate(), Transaction_.transactionDate));
            }
            if (criteria.getTransactionType() != null) {
                specification = specification.and(buildSpecification(criteria.getTransactionType(), Transaction_.transactionType));
            }
            if (criteria.getTransactionStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getTransactionStatus(), Transaction_.transactionStatus));
            }
            if (criteria.getEventId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEventId(), Transaction_.eventId));
            }
            if (criteria.getTransactionAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTransactionAmount(), Transaction_.transactionAmount));
            }
            if (criteria.getImageLink() != null) {
                specification = specification.and(buildStringSpecification(criteria.getImageLink(), Transaction_.imageLink));
            }
            if (criteria.getClosedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getClosedBy(), Transaction_.closedBy));
            }
            if (criteria.getDescription() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescription(), Transaction_.description));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), Transaction_.createdBy));
            }
            if (criteria.getCreatedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), Transaction_.createdDate));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), Transaction_.lastModifiedBy));
            }
            if (criteria.getLastModifiedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), Transaction_.lastModifiedDate));
            }
        }
        return specification;
    }

    /**
     * Assign user name to the transactionDTO.
     *
     * @param transactionDTO to add on the user name prop.
     * */
    private TransactionDTO mapUserName(TransactionDTO transactionDTO) {
        Optional<User> dbUser = userRepository.findOneByLogin(transactionDTO.getCreatedBy());
        if(dbUser.isPresent()){
            User user = dbUser.get();
            transactionDTO.setCreatedBy(user.getFirstName() + " " + user.getLastName());
        }
        return transactionDTO;
    }
}
