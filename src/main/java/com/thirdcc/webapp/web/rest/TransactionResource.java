package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.domain.enumeration.TransactionStatus;
import com.thirdcc.webapp.security.AuthoritiesConstants;
import com.thirdcc.webapp.service.TransactionQueryService;
import com.thirdcc.webapp.service.TransactionService;
import com.thirdcc.webapp.service.criteria.TransactionCriteria;
import com.thirdcc.webapp.service.dto.EventBudgetTotalDTO;
import com.thirdcc.webapp.web.rest.errors.BadRequestAlertException;
import com.thirdcc.webapp.service.dto.TransactionDTO;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;

/**
 * REST controller for managing {@link com.thirdcc.webapp.domain.Transaction}.
 */
@RestController
@RequestMapping("/api")
public class TransactionResource {

    private final Logger log = LoggerFactory.getLogger(TransactionResource.class);

    private static final String ENTITY_NAME = "transaction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TransactionService transactionService;

    private final TransactionQueryService transactionQueryService;

    public TransactionResource(TransactionService transactionService, TransactionQueryService transactionQueryService) {
        this.transactionService = transactionService;
        this.transactionQueryService = transactionQueryService;
    }

    /**
     * {@code POST  /transactions} : Create a new transaction.
     *
     * @param transactionDTO the transactionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new transactionDTO, or with status {@code 400 (Bad Request)} if the transaction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/transactions")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") || @transactionSecurityExpression.transactionAccess(#transactionDTO.getEventId())")
    public ResponseEntity<TransactionDTO> createTransaction(@ModelAttribute TransactionDTO transactionDTO) throws URISyntaxException, IOException {

        log.debug("REST request to save Transaction : {}", transactionDTO);
        if (transactionDTO.getId() != null) {
            throw new BadRequestAlertException("A new transaction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TransactionDTO result = transactionService.save(transactionDTO.getMultipartFile(), transactionDTO);
        return ResponseEntity.created(new URI("/api/transactions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /transactions} : Updates an existing transaction.
     *
     * @param transactionDTO the transactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated transactionDTO,
     * or with status {@code 400 (Bad Request)} if the transactionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the transactionDTO couldn't be updated.
     * @throws IOException if IO error occurs is incorrect.
     */
    @PutMapping("/transactions")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") || @transactionSecurityExpression.transactionAccess(#transactionDTO.getEventId())")
    public ResponseEntity<TransactionDTO> updateTransaction(@ModelAttribute TransactionDTO transactionDTO) throws IOException {

        log.debug("REST request to update Transaction: {}", transactionDTO);
        if (transactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        TransactionDTO result = transactionService.save(transactionDTO.getMultipartFile(), transactionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, transactionDTO.getId().toString()))
            .body(result);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") || @managementTeamSecurityExpression.isCurrentAdministrator()")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(TransactionCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Transactions by criteria: {}", criteria);
        Page<TransactionDTO> page = transactionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
    
    @PutMapping("/transactions/{id}/status/{transactionStatus}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") || @managementTeamSecurityExpression.isCurrentAdministrator()")
    public ResponseEntity<TransactionDTO> updateTransactionStatus(@PathVariable Long id, @PathVariable TransactionStatus transactionStatus) {
        log.debug("REST request to update transaction: {} with status: {}", id, transactionStatus);
        TransactionDTO result = transactionService.updateTransactionStatus(id, transactionStatus);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .body(result);
    }

    @GetMapping("/transactions/count")
    public ResponseEntity<Long> countTransactions(TransactionCriteria criteria) {
        log.debug("REST request to count Transactions by criteria: {}", criteria);
        return ResponseEntity.ok().body(transactionQueryService.countByCriteria(criteria));
    }

    @GetMapping("/transactions/event/{eventId}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\") || @managementTeamSecurityExpression.isEventCrew(#eventId) || @managementTeamSecurityExpression.isCurrentAdministrator()")
    public ResponseEntity<List<TransactionDTO>> getAllTransactionsByEventId(@PathVariable Long eventId, Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get a page of Transactions by eventId: {}", eventId);
        Page<TransactionDTO> page = transactionService.findAllByEventId(eventId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/transactions/event/{eventId}/total")
    @PreAuthorize("@managementTeamSecurityExpression.isCurrentAdministrator() || @managementTeamSecurityExpression.isEventCrew(#eventId)")
    public ResponseEntity<EventBudgetTotalDTO> getTotalTransactionByEventId(@PathVariable Long eventId) {
        log.debug("REST request to get total transaction amount by event Id: {}", eventId);
        EventBudgetTotalDTO result = transactionService.findTotalTransactionByEventId(eventId);
        return ResponseEntity.ok()
            .body(result);
    }
}
