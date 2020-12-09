package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.ClubmanagementApp;
import com.thirdcc.webapp.domain.Event;
import com.thirdcc.webapp.domain.Transaction;
import com.thirdcc.webapp.domain.enumeration.EventStatus;
import com.thirdcc.webapp.repository.EventRepository;
import com.thirdcc.webapp.repository.TransactionRepository;
import com.thirdcc.webapp.service.TransactionService;
import com.thirdcc.webapp.service.dto.TransactionDTO;
import com.thirdcc.webapp.service.mapper.TransactionMapper;
import com.thirdcc.webapp.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.thirdcc.webapp.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.thirdcc.webapp.domain.enumeration.TransactionType;
/**
 * Integration tests for the {@Link TransactionResource} REST controller.
 */
@SpringBootTest(classes = ClubmanagementApp.class)
public class TransactionResourceIT {

    private static final Long DEFAULT_EVENT_ID = 1L;
    private static final Long UPDATED_EVENT_ID = 2L;

    private static final Long DEFAULT_RECEIPT_ID = 1L;
    private static final Long UPDATED_RECEIPT_ID = 2L;

    private static final TransactionType DEFAULT_TYPE = TransactionType.INCOME;
    private static final TransactionType UPDATED_TYPE = TransactionType.EXPENSE;

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);

    private static final String DEFAULT_DETAILS = "TRANSACTION_DETAILS";
    private static final String UPDATED_DETAILS = "TRANSACTION_UPDATED_DETAILS";

    // Event Default data
    private static final String DEFAULT_EVENT_NAME = "DEFAULT_EVENT_NAME";
    private static final String DEFAULT_EVENT_DESCRIPTION = "DEFAULT_EVENT_DESCRIPTION";
    private static final String DEFAULT_EVENT_REMARKS = "DEFAULT_EVENT_REMARKS";
    private static final String DEFAULT_EVENT_VENUE = "DEFAULT_EVENT_VENUE";
    private static final Instant DEFAULT_EVENT_START_DATE = Instant.now().minus(5, ChronoUnit.DAYS);
    private static final Instant DEFAULT_EVENT_END_DATE = Instant.now().plus(5, ChronoUnit.DAYS);
    private static final BigDecimal DEFAULT_EVENT_FEE = new BigDecimal(2123);
    private static final EventStatus DEFAULT_EVENT_STATUS = EventStatus.OPEN;


    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restTransactionMockMvc;

    private Transaction transaction;

    @Autowired
    private EventRepository eventRepository;

    private Event event;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final TransactionResource transactionResource = new TransactionResource(transactionService);
        this.restTransactionMockMvc = MockMvcBuilders.standaloneSetup(transactionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createTransactionEntity() {
        return new Transaction()
            .eventId(DEFAULT_EVENT_ID)
            .receiptId(DEFAULT_RECEIPT_ID)
            .type(DEFAULT_TYPE)
            .amount(DEFAULT_AMOUNT)
            .details(DEFAULT_DETAILS);
    }

    public static Event createEventEntity() {
        return new Event()
            .name(DEFAULT_EVENT_NAME)
            .description(DEFAULT_EVENT_DESCRIPTION)
            .remarks(DEFAULT_EVENT_REMARKS)
            .startDate(DEFAULT_EVENT_START_DATE)
            .endDate(DEFAULT_EVENT_END_DATE)
            .fee(DEFAULT_EVENT_FEE)
            .status(DEFAULT_EVENT_STATUS)
            .venue(DEFAULT_EVENT_VENUE);
    }

    @BeforeEach
    public void initTest() {
        transaction = createTransactionEntity();
        event = createEventEntity();
    }

    @AfterEach
    public void cleanUp() {
        transactionRepository.deleteAll();
        eventRepository.deleteAll();
    }

//    @Test
//    public void createTransaction() throws Exception {
//        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
//
//        // Create the Transaction
//        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);
//        restTransactionMockMvc.perform(post("/api/transactions")
//            .contentType(TestUtil.APPLICATION_JSON_UTF8)
//            .content(TestUtil.convertObjectToJsonBytes(transactionDTO)))
//            .andExpect(status().isCreated());
//
//        // Validate the Transaction in the database
//        List<Transaction> transactionList = transactionRepository.findAll();
//        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
//        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
//        assertThat(testTransaction.getEventId()).isEqualTo(DEFAULT_EVENT_ID);
//        assertThat(testTransaction.getReceiptId()).isEqualTo(DEFAULT_RECEIPT_ID);
//        assertThat(testTransaction.getType()).isEqualTo(DEFAULT_TYPE);
//        assertThat(testTransaction.getAmount()).isEqualTo(DEFAULT_AMOUNT);
//        assertThat(testTransaction.getDetails()).isEqualTo(DEFAULT_DETAILS);
//    }
//
//    @Test
//    public void createTransactionWithExistingId() throws Exception {
//        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
//
//        // Create the Transaction with an existing ID
//        transaction.setId(1L);
//        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);
//
//        // An entity with an existing ID cannot be created, so this API call must fail
//        restTransactionMockMvc.perform(post("/api/transactions")
//            .contentType(TestUtil.APPLICATION_JSON_UTF8)
//            .content(TestUtil.convertObjectToJsonBytes(transactionDTO)))
//            .andExpect(status().isBadRequest());
//
//        // Validate the Transaction in the database
//        List<Transaction> transactionList = transactionRepository.findAll();
//        assertThat(transactionList).hasSize(databaseSizeBeforeCreate);
//    }


    @Test
    public void getAllTransactions() throws Exception {
        // Initialize the database
        Event savedEvent = initEventDB();
        transaction.setEventId(savedEvent.getId());
        Transaction savedTransaction = initTransactionDB();

        // Get all the transactionList
        restTransactionMockMvc.perform(get("/api/transactions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(savedTransaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].eventId").value(hasItem(savedTransaction.getEventId().intValue())))
            .andExpect(jsonPath("$.[*].receiptId").value(hasItem(savedTransaction.getReceiptId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(savedTransaction.getType().toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(savedTransaction.getAmount().doubleValue())))
            .andExpect(jsonPath("$.[*].details").value(hasItem(savedTransaction.getDetails())));
    }

    @Test
    public void getAllTransactionByEventId() throws Exception {
        // Initialize the database
        Event savedEvent = initEventDB();
        transaction.setEventId(savedEvent.getId());
        Transaction savedTransaction = initTransactionDB();

        // Get all the transactionList
        restTransactionMockMvc.perform(get("/api/transactions/event/{eventId}?sort=id,desc", savedEvent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(savedTransaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].eventId").value(hasItem(savedTransaction.getEventId().intValue())))
            .andExpect(jsonPath("$.[*].receiptId").value(hasItem(savedTransaction.getReceiptId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(savedTransaction.getType().toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(savedTransaction.getAmount().doubleValue())))
            .andExpect(jsonPath("$.[*].details").value(hasItem(savedTransaction.getDetails())));
    }

    @Test
    public void getAllTransactionByEventId_WithCancelledEvent_ShouldThrow400() throws Exception {
        // Initialize the database
        event.setStatus(EventStatus.CANCELLED);
        Event savedEvent = initEventDB();
        transaction.setEventId(savedEvent.getId());
        Transaction savedTransaction = initTransactionDB();

        // Get all the transactionList
        restTransactionMockMvc.perform(get("/api/transactions/event/{eventId}?sort=id,desc", savedEvent.getId()))
            .andExpect(status().isBadRequest());
    }
    
//    @Test
//    public void getTransaction() throws Exception {
//        // Initialize the database
//        transactionRepository.saveAndFlush(transaction);
//
//        // Get the transaction
//        restTransactionMockMvc.perform(get("/api/transactions/{id}", transaction.getId()))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
//            .andExpect(jsonPath("$.id").value(transaction.getId().intValue()))
//            .andExpect(jsonPath("$.eventId").value(DEFAULT_EVENT_ID.intValue()))
//            .andExpect(jsonPath("$.receiptId").value(DEFAULT_RECEIPT_ID.intValue()))
//            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
//            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
//            .andExpect(jsonPath("$.details").value(DEFAULT_DETAILS.toString()));
//    }
//
//    @Test
//    public void getNonExistingTransaction() throws Exception {
//        // Get the transaction
//        restTransactionMockMvc.perform(get("/api/transactions/{id}", Long.MAX_VALUE))
//            .andExpect(status().isNotFound());
//    }
//
//    @Test
//    public void updateTransaction() throws Exception {
//        // Initialize the database
//        transactionRepository.saveAndFlush(transaction);
//
//        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();
//
//        // Update the transaction
//        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).get();
//        // Disconnect from session so that the updates on updatedTransaction are not directly saved in db
//        em.detach(updatedTransaction);
//        updatedTransaction
//            .eventId(UPDATED_EVENT_ID)
//            .receiptId(UPDATED_RECEIPT_ID)
//            .type(UPDATED_TYPE)
//            .amount(UPDATED_AMOUNT)
//            .details(UPDATED_DETAILS);
//        TransactionDTO transactionDTO = transactionMapper.toDto(updatedTransaction);
//
//        restTransactionMockMvc.perform(put("/api/transactions")
//            .contentType(TestUtil.APPLICATION_JSON_UTF8)
//            .content(TestUtil.convertObjectToJsonBytes(transactionDTO)))
//            .andExpect(status().isOk());
//
//        // Validate the Transaction in the database
//        List<Transaction> transactionList = transactionRepository.findAll();
//        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
//        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
//        assertThat(testTransaction.getEventId()).isEqualTo(UPDATED_EVENT_ID);
//        assertThat(testTransaction.getReceiptId()).isEqualTo(UPDATED_RECEIPT_ID);
//        assertThat(testTransaction.getType()).isEqualTo(UPDATED_TYPE);
//        assertThat(testTransaction.getAmount()).isEqualTo(UPDATED_AMOUNT);
//        assertThat(testTransaction.getDetails()).isEqualTo(UPDATED_DETAILS);
//    }
//
//    @Test
//    public void updateNonExistingTransaction() throws Exception {
//        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();
//
//        // Create the Transaction
//        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);
//
//        // If the entity doesn't have an ID, it will throw BadRequestAlertException
//        restTransactionMockMvc.perform(put("/api/transactions")
//            .contentType(TestUtil.APPLICATION_JSON_UTF8)
//            .content(TestUtil.convertObjectToJsonBytes(transactionDTO)))
//            .andExpect(status().isBadRequest());
//
//        // Validate the Transaction in the database
//        List<Transaction> transactionList = transactionRepository.findAll();
//        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    public void deleteTransaction() throws Exception {
//        // Initialize the database
//        transactionRepository.saveAndFlush(transaction);
//
//        int databaseSizeBeforeDelete = transactionRepository.findAll().size();
//
//        // Delete the transaction
//        restTransactionMockMvc.perform(delete("/api/transactions/{id}", transaction.getId())
//            .accept(TestUtil.APPLICATION_JSON_UTF8))
//            .andExpect(status().isNoContent());
//
//        // Validate the database contains one less item
//        List<Transaction> transactionList = transactionRepository.findAll();
//        assertThat(transactionList).hasSize(databaseSizeBeforeDelete - 1);
//    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Transaction.class);
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        Transaction transaction2 = new Transaction();
        transaction2.setId(transaction1.getId());
        assertThat(transaction1).isEqualTo(transaction2);
        transaction2.setId(2L);
        assertThat(transaction1).isNotEqualTo(transaction2);
        transaction1.setId(null);
        assertThat(transaction1).isNotEqualTo(transaction2);
    }

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TransactionDTO.class);
        TransactionDTO transactionDTO1 = new TransactionDTO();
        transactionDTO1.setId(1L);
        TransactionDTO transactionDTO2 = new TransactionDTO();
        assertThat(transactionDTO1).isNotEqualTo(transactionDTO2);
        transactionDTO2.setId(transactionDTO1.getId());
        assertThat(transactionDTO1).isEqualTo(transactionDTO2);
        transactionDTO2.setId(2L);
        assertThat(transactionDTO1).isNotEqualTo(transactionDTO2);
        transactionDTO1.setId(null);
        assertThat(transactionDTO1).isNotEqualTo(transactionDTO2);
    }

    @Test
    public void testEntityFromId() {
        assertThat(transactionMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(transactionMapper.fromId(null)).isNull();
    }

    private Transaction initTransactionDB() {
        return transactionRepository.saveAndFlush(transaction);
    }

    private Event initEventDB() {
        return eventRepository.saveAndFlush(event);
    }
}
