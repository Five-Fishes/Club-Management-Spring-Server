package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.ClubmanagementApp;
import com.thirdcc.webapp.annotations.authorization.WithCurrentCCAdministrator;
import com.thirdcc.webapp.annotations.authorization.WithEventCrew;
import com.thirdcc.webapp.annotations.authorization.WithEventHead;
import com.thirdcc.webapp.annotations.authorization.WithNormalUser;
import com.thirdcc.webapp.annotations.init.InitYearSession;
import com.thirdcc.webapp.domain.*;
import com.thirdcc.webapp.domain.enumeration.*;
import com.thirdcc.webapp.exception.BadRequestException;
import com.thirdcc.webapp.repository.*;
import com.thirdcc.webapp.security.SecurityUtils;
import com.thirdcc.webapp.service.ImageStorageService;
import com.thirdcc.webapp.service.UserService;
import com.thirdcc.webapp.service.dto.EventBudgetTotalDTO;
import com.thirdcc.webapp.service.dto.ImageStorageDTO;
import com.thirdcc.webapp.service.dto.ReceiptDTO;
import com.thirdcc.webapp.service.dto.TransactionDTO;
import com.thirdcc.webapp.service.mapper.TransactionMapper;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.thirdcc.webapp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@Link TransactionResource} REST controller.
 */
@SpringBootTest(classes = ClubmanagementApp.class)
@AutoConfigureMockMvc
@WithMockUser(username = TransactionResourceIT.USERNAME, roles = "ADMIN")
@InitYearSession
public class TransactionResourceIT {

    public static final String USERNAME = "admin";
    private static final String ENTITY_API_URL = "/api/transactions";

    private static final Long DEFAULT_EVENT_ID = 1L;
    private static final Long SMALLER_EVENT_ID = DEFAULT_EVENT_ID - 1L;
    private static final Long UPDATED_EVENT_ID = 2L;

    private static final Long DEFAULT_RECEIPT_ID = 1L;
    private static final Long SMALLER_RECEIPT_ID = DEFAULT_RECEIPT_ID - 1L;
    private static final Long UPDATED_RECEIPT_ID = 2L;

    private static final TransactionType DEFAULT_TRANSACTION_TYPE = TransactionType.INCOME;
    private static final TransactionType UPDATED_TRANSACTION_TYPE = TransactionType.EXPENSE;

    private static final String DEFAULT_TITLE = "DEFAULT_TITLE";
    private static final String UPDATED_TITLE = "UPDATED_TITLE";

    private static final Instant DEFAULT_TRANSACTION_DATE = Instant.ofEpochMilli(4545);
    private static final Instant UPDATED_TRANSACTION_DATE = Instant.ofEpochMilli(8745);

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal SMALLER_AMOUNT = DEFAULT_AMOUNT.subtract(BigDecimal.ONE);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);

    private static final String DEFAULT_DESCRIPTION = "TRANSACTION_DETAILS";
    private static final String UPDATED_DETAILS = "TRANSACTION_UPDATED_DETAILS";

    private static final String DEFAULT_IMAGE_LINK = "DEFAULT_IMAGE_LINK";
    private static final String UPDATED_IMAGE_LINK = "UPDATED_IMAGE_LINK";

    private static final TransactionStatus DEFAULT_TRANSACTION_STATUS = TransactionStatus.PENDING;
    private static final TransactionStatus UPDATED_TRANSACTION_STATUS = TransactionStatus.COMPLETED;

    private static final String DEFAULT_CLOSED_BY = USERNAME;
    private static final String UPDATED_CLOSED_BY = "UPDATED_CLOSED_BY";

    private static final String DEFAULT_CREATED_BY = USERNAME;
    private static final String UPDATED_CREATED_BY = "UPDATED_CREATED_BY";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochSecond(53565);
    private static final Instant UPDATED_CREATED_DATE = Instant.ofEpochSecond(63565);

    private static final String DEFAULT_LAST_MODIFIED_BY = USERNAME;
    private static final String UPDATED_LAST_MODIFIED_BY = "UPDATED_LAST_MODIFIED_BY";

    private static final Instant DEFAULT_LAST_MODIFIED_DATE = Instant.ofEpochSecond(753565);
    private static final Instant UPDATED_LAST_MODIFIED_DATE = Instant.ofEpochSecond(953565);

    // Event Default data
    private static final String DEFAULT_EVENT_NAME = "DEFAULT_EVENT_NAME";
    private static final String DEFAULT_EVENT_DESCRIPTION = "DEFAULT_EVENT_DESCRIPTION";
    private static final String DEFAULT_EVENT_REMARKS = "DEFAULT_EVENT_REMARKS";
    private static final String DEFAULT_EVENT_VENUE = "DEFAULT_EVENT_VENUE";
    private static final Instant DEFAULT_EVENT_START_DATE = Instant.now().minus(5, ChronoUnit.DAYS);
    private static final Instant DEFAULT_EVENT_END_DATE = Instant.now().plus(5, ChronoUnit.DAYS);
    private static final BigDecimal DEFAULT_EVENT_FEE = new BigDecimal(2123);
    private static final EventStatus DEFAULT_EVENT_STATUS = EventStatus.OPEN;
    private static final String DEFAULT_RECEIPT_IMAGE_TYPE = "DEFAULT_RECEIPT_IMAGE_TYPE";
    private static final String DEFAULT_RECEIPT_IMAGE_FILENAME = "DEFAULT_RECEIPT_IMAGE_FILENAME";
    private static final String DEFAULT_RECEIPT_IMAGE_CONTENT = "DEFAULT_RECEIPT_IMAGE_CONTENT";

    // Event Crew Default data
    private static final Long DEFAULT_USER_ID = 1L;
    private static final EventCrewRole EVENT_CREW_ROLE_HEAD = EventCrewRole.HEAD;

    // Image File
    private static final MockMultipartFile MOCK_MULTIPART_FILE =  new MockMultipartFile("multipartFile", "Mocked Content".getBytes());


    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    private User currentUser;

    @Autowired
    private MockMvc restTransactionMockMvc;

    private Transaction transaction;

    @Autowired
    private EventRepository eventRepository;

    private Event event;

    @Autowired
    private ReceiptRepository receiptRepository;

    private ReceiptDTO receiptDTO;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private EventCrewRepository eventCrewRepository;

    private EventCrew eventCrew;

    private final MockMultipartHttpServletRequestBuilder putMultipartRequestBuilder = (MockMultipartHttpServletRequestBuilder) multipart(ENTITY_API_URL).with(request -> {
        request.setMethod("PUT");
        return request;
    });

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createTransactionEntity() {
        return new Transaction()
            .title(DEFAULT_TITLE)
            .transactionDate(DEFAULT_TRANSACTION_DATE)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .transactionStatus(DEFAULT_TRANSACTION_STATUS)
            .eventId(DEFAULT_EVENT_ID)
            .transactionAmount(DEFAULT_AMOUNT)
            .imageLink(DEFAULT_IMAGE_LINK)
            .closedBy(DEFAULT_CLOSED_BY)
            .description(DEFAULT_DESCRIPTION);
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

    public static ReceiptDTO createReceiptDTO() {
        ReceiptDTO receiptDTO = new ReceiptDTO();
        receiptDTO.setFileName(DEFAULT_RECEIPT_IMAGE_FILENAME);
        receiptDTO.setFileType(DEFAULT_RECEIPT_IMAGE_TYPE);
        receiptDTO.setReceiptContent(DEFAULT_RECEIPT_IMAGE_CONTENT);
        return receiptDTO;
    }

    public static EventCrew createEventCrew() {
        EventCrew eventCrew = new EventCrew();
        eventCrew.setEventId(DEFAULT_EVENT_ID);
        eventCrew.setUserId(DEFAULT_USER_ID);
        return eventCrew;
    }

    @BeforeEach
    public void initTest() {
        transaction = createTransactionEntity();
        event = createEventEntity();
        receiptDTO = createReceiptDTO();
        eventCrew = createEventCrew();
    }

    @BeforeEach
    public void initStub() throws IOException {
        ImageStorageDTO mockImageSavingResult = new ImageStorageDTO();
        mockImageSavingResult.setId(1L);
        mockImageSavingResult.setImageUrl(DEFAULT_IMAGE_LINK);
        mockImageSavingResult.setFileType("jpeg");
        mockImageSavingResult.setFileName("Receipt Image");

        when(imageStorageService.save(new ImageStorageDTO(), MOCK_MULTIPART_FILE)).thenReturn(mockImageSavingResult);
    }

    @AfterEach
    public void cleanUp() {
        transactionRepository.deleteAll();
        eventRepository.deleteAll();
        receiptRepository.deleteAll();
        eventCrewRepository.deleteAll();
    }

    @Test
    @Transactional
    void getTransactionsByIdFiltering() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        Long id = transaction.getId();

        defaultTransactionShouldBeFound("id.equals=" + id);
        defaultTransactionShouldNotBeFound("id.notEquals=" + id);

        defaultTransactionShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultTransactionShouldNotBeFound("id.greaterThan=" + id);

        defaultTransactionShouldBeFound("id.lessThanOrEqual=" + id);
        defaultTransactionShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllTransactionsByEventIdIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eventId equals to DEFAULT_EVENT_ID
        defaultTransactionShouldBeFound("eventId.equals=" + DEFAULT_EVENT_ID);

        // Get all the transactionList where eventId equals to UPDATED_EVENT_ID
        defaultTransactionShouldNotBeFound("eventId.equals=" + UPDATED_EVENT_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByEventIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);
        System.out.println("ALERT: " + transaction.getCreatedBy());

        // Get all the transactionList where eventId not equals to DEFAULT_EVENT_ID
        defaultTransactionShouldNotBeFound("eventId.notEquals=" + DEFAULT_EVENT_ID);

        // Get all the transactionList where eventId not equals to UPDATED_EVENT_ID
        defaultTransactionShouldBeFound("eventId.notEquals=" + UPDATED_EVENT_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByEventIdIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eventId in DEFAULT_EVENT_ID or UPDATED_EVENT_ID
        defaultTransactionShouldBeFound("eventId.in=" + DEFAULT_EVENT_ID + "," + UPDATED_EVENT_ID);

        // Get all the transactionList where eventId equals to UPDATED_EVENT_ID
        defaultTransactionShouldNotBeFound("eventId.in=" + UPDATED_EVENT_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByEventIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eventId is not null
        defaultTransactionShouldBeFound("eventId.specified=true");

        // Get all the transactionList where eventId is null
        defaultTransactionShouldNotBeFound("eventId.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByEventIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eventId is greater than or equal to DEFAULT_EVENT_ID
        defaultTransactionShouldBeFound("eventId.greaterThanOrEqual=" + DEFAULT_EVENT_ID);

        // Get all the transactionList where eventId is greater than or equal to UPDATED_EVENT_ID
        defaultTransactionShouldNotBeFound("eventId.greaterThanOrEqual=" + UPDATED_EVENT_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByEventIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eventId is less than or equal to DEFAULT_EVENT_ID
        defaultTransactionShouldBeFound("eventId.lessThanOrEqual=" + DEFAULT_EVENT_ID);

        // Get all the transactionList where eventId is less than or equal to SMALLER_EVENT_ID
        defaultTransactionShouldNotBeFound("eventId.lessThanOrEqual=" + SMALLER_EVENT_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByEventIdIsLessThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eventId is less than DEFAULT_EVENT_ID
        defaultTransactionShouldNotBeFound("eventId.lessThan=" + DEFAULT_EVENT_ID);

        // Get all the transactionList where eventId is less than UPDATED_EVENT_ID
        defaultTransactionShouldBeFound("eventId.lessThan=" + UPDATED_EVENT_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByEventIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where eventId is greater than DEFAULT_EVENT_ID
        defaultTransactionShouldNotBeFound("eventId.greaterThan=" + DEFAULT_EVENT_ID);

        // Get all the transactionList where eventId is greater than SMALLER_EVENT_ID
        defaultTransactionShouldBeFound("eventId.greaterThan=" + SMALLER_EVENT_ID);
    }

    @Test
    @Transactional
    void getAllTransactionsByCreatedByIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where createdBy not equals to DEFAULT_CREATED_BY
        defaultTransactionShouldNotBeFound("createdBy.notEquals=" + DEFAULT_CREATED_BY);

        // Get all the transactionList where createdBy not equals to UPDATED_CREATED_BY
        defaultTransactionShouldBeFound("createdBy.notEquals=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllTransactionsByCreatedByIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where createdBy in DEFAULT_CREATED_BY or UPDATED_CREATED_BY
        defaultTransactionShouldBeFound("createdBy.in=" + DEFAULT_CREATED_BY + "," + UPDATED_CREATED_BY);

        // Get all the transactionList where createdBy equals to UPDATED_CREATED_BY
        defaultTransactionShouldNotBeFound("createdBy.in=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllTransactionsByCreatedByIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where createdBy is not null
        defaultTransactionShouldBeFound("createdBy.specified=true");

        // Get all the transactionList where createdBy is null
        defaultTransactionShouldNotBeFound("createdBy.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByCreatedByContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where createdBy contains DEFAULT_CREATED_BY
        defaultTransactionShouldBeFound("createdBy.contains=" + DEFAULT_CREATED_BY);

        // Get all the transactionList where createdBy contains UPDATED_CREATED_BY
        defaultTransactionShouldNotBeFound("createdBy.contains=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllTransactionsByCreatedByNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where createdBy does not contain DEFAULT_CREATED_BY
        defaultTransactionShouldNotBeFound("createdBy.doesNotContain=" + DEFAULT_CREATED_BY);

        // Get all the transactionList where createdBy does not contain UPDATED_CREATED_BY
        defaultTransactionShouldBeFound("createdBy.doesNotContain=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllTransactionsByLastModifiedByIsEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where lastModifiedBy equals to DEFAULT_LAST_MODIFIED_BY
        defaultTransactionShouldBeFound("lastModifiedBy.equals=" + DEFAULT_LAST_MODIFIED_BY);

        // Get all the transactionList where lastModifiedBy equals to UPDATED_LAST_MODIFIED_BY
        defaultTransactionShouldNotBeFound("lastModifiedBy.equals=" + UPDATED_LAST_MODIFIED_BY);
    }

    @Test
    @Transactional
    void getAllTransactionsByLastModifiedByIsNotEqualToSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where lastModifiedBy not equals to DEFAULT_LAST_MODIFIED_BY
        defaultTransactionShouldNotBeFound("lastModifiedBy.notEquals=" + DEFAULT_LAST_MODIFIED_BY);

        // Get all the transactionList where lastModifiedBy not equals to UPDATED_LAST_MODIFIED_BY
        defaultTransactionShouldBeFound("lastModifiedBy.notEquals=" + UPDATED_LAST_MODIFIED_BY);
    }

    @Test
    @Transactional
    void getAllTransactionsByLastModifiedByIsInShouldWork() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where lastModifiedBy in DEFAULT_LAST_MODIFIED_BY or UPDATED_LAST_MODIFIED_BY
        defaultTransactionShouldBeFound("lastModifiedBy.in=" + DEFAULT_LAST_MODIFIED_BY + "," + UPDATED_LAST_MODIFIED_BY);

        // Get all the transactionList where lastModifiedBy equals to UPDATED_LAST_MODIFIED_BY
        defaultTransactionShouldNotBeFound("lastModifiedBy.in=" + UPDATED_LAST_MODIFIED_BY);
    }

    @Test
    @Transactional
    void getAllTransactionsByLastModifiedByIsNullOrNotNull() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where lastModifiedBy is not null
        defaultTransactionShouldBeFound("lastModifiedBy.specified=true");

        // Get all the transactionList where lastModifiedBy is null
        defaultTransactionShouldNotBeFound("lastModifiedBy.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByLastModifiedByContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where lastModifiedBy contains DEFAULT_LAST_MODIFIED_BY
        defaultTransactionShouldBeFound("lastModifiedBy.contains=" + DEFAULT_LAST_MODIFIED_BY);

        // Get all the transactionList where lastModifiedBy contains UPDATED_LAST_MODIFIED_BY
        defaultTransactionShouldNotBeFound("lastModifiedBy.contains=" + UPDATED_LAST_MODIFIED_BY);
    }

    @Test
    @Transactional
    void getAllTransactionsByLastModifiedByNotContainsSomething() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where lastModifiedBy does not contain DEFAULT_LAST_MODIFIED_BY
        defaultTransactionShouldNotBeFound("lastModifiedBy.doesNotContain=" + DEFAULT_LAST_MODIFIED_BY);

        // Get all the transactionList where lastModifiedBy does not contain UPDATED_LAST_MODIFIED_BY
        defaultTransactionShouldBeFound("lastModifiedBy.doesNotContain=" + UPDATED_LAST_MODIFIED_BY);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTransactionShouldBeFound(String filter) throws Exception {
        User currentUser = getCurrentUser();
        String currentUserName = currentUser.getFirstName() + " " + currentUser.getLastName();
        System.out.println(transaction);
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(DEFAULT_TRANSACTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].transactionType").value(hasItem(DEFAULT_TRANSACTION_TYPE.name())))
            .andExpect(jsonPath("$.[*].transactionStatus").value(hasItem(DEFAULT_TRANSACTION_STATUS.name())))
            .andExpect(jsonPath("$.[*].eventId").value(hasItem(DEFAULT_EVENT_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionAmount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].imageLink").value(hasItem(DEFAULT_IMAGE_LINK)))
            .andExpect(jsonPath("$.[*].closedBy").value(hasItem(DEFAULT_CLOSED_BY)))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(currentUserName)))
            .andExpect(jsonPath("$.[*].createdDate").value(Matchers.notNullValue()))
            .andExpect(jsonPath("$.[*].lastModifiedBy").value(hasItem(DEFAULT_LAST_MODIFIED_BY)))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(Matchers.notNullValue()));

        // Check, that the count call also returns 1
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTransactionShouldNotBeFound(String filter) throws Exception {
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(content().string("0"));
    }

    @Test
    public void createNullEventPendingIncome_UserIsAdmin_ShouldSuccess() throws Exception {
        // Initialize the database
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction
        restTransactionMockMvc.perform(multipart("/api/transactions")
            .file(MOCK_MULTIPART_FILE)
            .param("title", DEFAULT_TITLE)
            .param("eventId", "")
            .param("transactionType", DEFAULT_TRANSACTION_TYPE.name())
            .param("transactionAmount", DEFAULT_AMOUNT.toString())
            .param("description", DEFAULT_DESCRIPTION)
            .param("transactionStatus", DEFAULT_TRANSACTION_STATUS.name())
            .param("transactionDate", DEFAULT_TRANSACTION_DATE.toString())
        ).andExpect(status().isCreated());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);

        // Validate the saved Transaction
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testTransaction.getEventId()).isEqualTo(null);
        assertThat(testTransaction.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testTransaction.getTransactionAmount()).isEqualTo(DEFAULT_AMOUNT.setScale(2, RoundingMode.HALF_UP));
        assertThat(testTransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTransaction.getTransactionStatus()).isEqualTo(DEFAULT_TRANSACTION_STATUS);
        assertThat(testTransaction.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
        assertThat(testTransaction.getImageLink()).isEqualTo(DEFAULT_IMAGE_LINK);
    }

    @Test
    @WithNormalUser
    public void createNullEventTransactionPendingIncome_UserIsUser_ShouldThrow403() throws Exception {
        // Initialize the database
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction
        restTransactionMockMvc.perform(multipart("/api/transactions")
            .file(MOCK_MULTIPART_FILE)
            .param("title", DEFAULT_TITLE)
            .param("eventId", "")
            .param("transactionType", DEFAULT_TRANSACTION_TYPE.name())
            .param("transactionAmount", DEFAULT_AMOUNT.toString())
            .param("description", DEFAULT_DESCRIPTION)
            .param("transactionStatus", DEFAULT_TRANSACTION_STATUS.name())
            .param("transactionDate", DEFAULT_TRANSACTION_DATE.toString())
        ).andExpect(status().isForbidden());

        // Validate the Transaction not in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @WithEventCrew
    public void createNullEventTransactionPendingIncome_UserIsEventCrew_ShouldThrow403() throws Exception {
        // Initialize the database
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction
        restTransactionMockMvc.perform(multipart("/api/transactions")
            .file(MOCK_MULTIPART_FILE)
            .param("title", DEFAULT_TITLE)
            .param("eventId", "")
            .param("transactionType", DEFAULT_TRANSACTION_TYPE.name())
            .param("transactionAmount", DEFAULT_AMOUNT.toString())
            .param("description", DEFAULT_DESCRIPTION)
            .param("transactionStatus", DEFAULT_TRANSACTION_STATUS.name())
            .param("transactionDate", DEFAULT_TRANSACTION_DATE.toString())
        ).andExpect(status().isForbidden());

        // Validate the Transaction not in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @WithCurrentCCAdministrator
    public void createNullEventTransactionPendingIncome_UserIsCCAdmin_ShouldSuccess() throws Exception {
        // Initialize the database
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction
        restTransactionMockMvc.perform(multipart("/api/transactions")
            .file(MOCK_MULTIPART_FILE)
            .param("title", DEFAULT_TITLE)
            .param("eventId", "")
            .param("transactionType", DEFAULT_TRANSACTION_TYPE.name())
            .param("transactionAmount", DEFAULT_AMOUNT.toString())
            .param("description", DEFAULT_DESCRIPTION)
            .param("transactionStatus", DEFAULT_TRANSACTION_STATUS.name())
            .param("transactionDate", DEFAULT_TRANSACTION_DATE.toString())
        ).andExpect(status().isCreated());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);

        // Validate the saved Transaction
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testTransaction.getEventId()).isEqualTo(null);
        assertThat(testTransaction.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testTransaction.getTransactionAmount()).isEqualTo(DEFAULT_AMOUNT.setScale(2, RoundingMode.HALF_UP));
        assertThat(testTransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTransaction.getTransactionStatus()).isEqualTo(DEFAULT_TRANSACTION_STATUS);
        assertThat(testTransaction.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
        assertThat(testTransaction.getImageLink()).isEqualTo(DEFAULT_IMAGE_LINK);
    }

    @Test
    public void createEventTransactionPendingIncome_UserIsAdmin_ShouldSuccess() throws Exception {
        // Initialise event
        Event savedEvent = initEventDB();
        // Initialize the database
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction
        restTransactionMockMvc.perform(multipart("/api/transactions")
            .file(MOCK_MULTIPART_FILE)
            .param("title", DEFAULT_TITLE)
            .param("eventId", savedEvent.getId().toString())
            .param("transactionType", DEFAULT_TRANSACTION_TYPE.name())
            .param("transactionAmount", DEFAULT_AMOUNT.toString())
            .param("description", DEFAULT_DESCRIPTION)
            .param("transactionStatus", DEFAULT_TRANSACTION_STATUS.name())
            .param("transactionDate", DEFAULT_TRANSACTION_DATE.toString())
        ).andExpect(status().isCreated());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);

        // Validate the saved Transaction
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testTransaction.getEventId()).isEqualTo(savedEvent.getId());
        assertThat(testTransaction.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testTransaction.getTransactionAmount()).isEqualTo(DEFAULT_AMOUNT.setScale(2, RoundingMode.HALF_UP));
        assertThat(testTransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTransaction.getTransactionStatus()).isEqualTo(DEFAULT_TRANSACTION_STATUS);
        assertThat(testTransaction.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
        assertThat(testTransaction.getImageLink()).isEqualTo(DEFAULT_IMAGE_LINK);
    }

    @Test
    @WithNormalUser
    public void createEventTransactionPendingIncome_UserIsNormalUser_ShouldThrow403() throws Exception {
        // Initialise event
        Event savedEvent = initEventDB();
        // Initialize the database
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction
        restTransactionMockMvc.perform(multipart("/api/transactions")
            .file(MOCK_MULTIPART_FILE)
            .param("title", DEFAULT_TITLE)
            .param("eventId", savedEvent.getId().toString())
            .param("transactionType", DEFAULT_TRANSACTION_TYPE.name())
            .param("transactionAmount", DEFAULT_AMOUNT.toString())
            .param("description", DEFAULT_DESCRIPTION)
            .param("transactionStatus", DEFAULT_TRANSACTION_STATUS.name())
            .param("transactionDate", DEFAULT_TRANSACTION_DATE.toString())
        ).andExpect(status().isForbidden());

        // Validate the Transaction not in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @WithEventCrew
    public void createEventTransactionPendingIncome_UserIsEventCrew_ShouldThrow403() throws Exception {
        // Initialise event
        Event savedEvent = initEventDB();
        // Initialize the database
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction
        restTransactionMockMvc.perform(multipart("/api/transactions")
            .file(MOCK_MULTIPART_FILE)
            .param("title", DEFAULT_TITLE)
            .param("eventId", savedEvent.getId().toString())
            .param("transactionType", DEFAULT_TRANSACTION_TYPE.name())
            .param("transactionAmount", DEFAULT_AMOUNT.toString())
            .param("description", DEFAULT_DESCRIPTION)
            .param("transactionStatus", DEFAULT_TRANSACTION_STATUS.name())
            .param("transactionDate", DEFAULT_TRANSACTION_DATE.toString())
        ).andExpect(status().isForbidden());

        // Validate the Transaction not in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void updateNullEventTransactionPendingIncome_ToCompleted_UserIsAdmin_ShouldSuccess() throws Exception {
        // Create Transaction
        transactionRepository.saveAndFlush(transaction);
        // Get Database size after creating transaction
        int databaseSizeAfterCreate = transactionRepository.findAll().size();
        // find created transaction
        Transaction createdTransaction = transactionRepository.findById(transaction.getId()).get();

        // Update the transaction
        restTransactionMockMvc.perform(putMultipartRequestBuilder
            .file(MOCK_MULTIPART_FILE)
            .param("id", createdTransaction.getId().toString())
            .param("title", createdTransaction.getTitle())
            .param("eventId", "")
            .param("transactionType", createdTransaction.getTransactionType().name())
            .param("transactionAmount", createdTransaction.getTransactionAmount().toString())
            .param("description", createdTransaction.getDescription())
            .param("transactionStatus", UPDATED_TRANSACTION_STATUS.name())
            .param("transactionDate", createdTransaction.getTransactionDate().toString())
        ).andExpect(status().isOk());

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction updatedTransaction = transactionList.get(transactionList.size() - 1);

        // Validate the Transaction count stays the same after creating 1 transaction
        assertThat(transactionList).hasSize(databaseSizeAfterCreate);
        assertThat(updatedTransaction.getTransactionStatus()).isEqualTo(UPDATED_TRANSACTION_STATUS);
    }

    @Test
    @WithNormalUser
    public void updateNullEventTransactionPendingIncome_ToCompleted_UserIsNormalUser_ShouldThrow403() throws Exception {
        // Create Transaction
        transactionRepository.saveAndFlush(transaction);
        // Get Database size after creating transaction
        int databaseSizeAfterCreate = transactionRepository.findAll().size();
        // find created transaction
        Transaction createdTransaction = transactionRepository.findById(transaction.getId()).get();

        // Update the transaction
        restTransactionMockMvc.perform(putMultipartRequestBuilder
            .file(MOCK_MULTIPART_FILE)
            .param("id", createdTransaction.getId().toString())
            .param("title", createdTransaction.getTitle())
            .param("eventId", "")
            .param("transactionType", createdTransaction.getTransactionType().name())
            .param("transactionAmount", createdTransaction.getTransactionAmount().toString())
            .param("description", createdTransaction.getDescription())
            .param("transactionStatus", UPDATED_TRANSACTION_STATUS.name())
            .param("transactionDate", createdTransaction.getTransactionDate().toString())
        ).andExpect(status().isForbidden());

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction updatedTransaction = transactionList.get(transactionList.size() - 1);

        // Validate the Transaction count stays the same after creating 1 transaction
        assertThat(transactionList).hasSize(databaseSizeAfterCreate);
        assertThat(updatedTransaction.getTransactionStatus()).isEqualTo(DEFAULT_TRANSACTION_STATUS);
    }

    @Test
    @WithEventCrew
    public void updateNullEventTransactionPendingIncome_ToCompleted_UserIsEventCrew_ShouldThrow403() throws Exception {
        // Create Transaction
        transactionRepository.saveAndFlush(transaction);
        // Get Database size after creating transaction
        int databaseSizeAfterCreate = transactionRepository.findAll().size();
        // find created transaction
        Transaction createdTransaction = transactionRepository.findById(transaction.getId()).get();

        // Update the transaction
        restTransactionMockMvc.perform(putMultipartRequestBuilder
            .file(MOCK_MULTIPART_FILE)
            .param("id", createdTransaction.getId().toString())
            .param("title", createdTransaction.getTitle())
            .param("eventId", "")
            .param("transactionType", createdTransaction.getTransactionType().name())
            .param("transactionAmount", createdTransaction.getTransactionAmount().toString())
            .param("description", createdTransaction.getDescription())
            .param("transactionStatus", UPDATED_TRANSACTION_STATUS.name())
            .param("transactionDate", createdTransaction.getTransactionDate().toString())
        ).andExpect(status().isForbidden());

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction updatedTransaction = transactionList.get(transactionList.size() - 1);

        // Validate the Transaction count stays the same after creating 1 transaction
        assertThat(transactionList).hasSize(databaseSizeAfterCreate);
        assertThat(updatedTransaction.getTransactionStatus()).isEqualTo(DEFAULT_TRANSACTION_STATUS);
    }

    @Test
    @WithCurrentCCAdministrator
    public void updateNullEventTransactionPendingIncome_ToCompleted_UserIsCurrentCCAdmin_ShouldSuccess() throws Exception {
        // Create Transaction
        transactionRepository.saveAndFlush(transaction);
        // Get Database size after creating transaction
        int databaseSizeAfterCreate = transactionRepository.findAll().size();
        // find created transaction
        Transaction createdTransaction = transactionRepository.findById(transaction.getId()).get();

        // Update the transaction
        restTransactionMockMvc.perform(putMultipartRequestBuilder
            .file(MOCK_MULTIPART_FILE)
            .param("id", createdTransaction.getId().toString())
            .param("title", createdTransaction.getTitle())
            .param("eventId", "")
            .param("transactionType", createdTransaction.getTransactionType().name())
            .param("transactionAmount", createdTransaction.getTransactionAmount().toString())
            .param("description", createdTransaction.getDescription())
            .param("transactionStatus", UPDATED_TRANSACTION_STATUS.name())
            .param("transactionDate", createdTransaction.getTransactionDate().toString())
        ).andExpect(status().isOk());

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction updatedTransaction = transactionList.get(transactionList.size() - 1);

        // Validate the Transaction count stays the same after creating 1 transaction
        assertThat(transactionList).hasSize(databaseSizeAfterCreate);
        assertThat(updatedTransaction.getTransactionStatus()).isEqualTo(UPDATED_TRANSACTION_STATUS);
    }

    @Test
    public void updateEventTransactionPendingIncome_ToCompleted_UserIsAdmin_ShouldSuccess() throws Exception {
        // Create Event
        Event savedEvent = initEventDB();
        // Create Transaction
        transactionRepository.saveAndFlush(transaction.eventId(savedEvent.getId()));
        // Get Database size after creating transaction
        int databaseSizeAfterCreate = transactionRepository.findAll().size();
        // find created transaction
        Transaction createdTransaction = transactionRepository.findById(transaction.getId()).get();

        // Update the transaction
        restTransactionMockMvc.perform(putMultipartRequestBuilder
            .file(MOCK_MULTIPART_FILE)
            .param("id", createdTransaction.getId().toString())
            .param("title", createdTransaction.getTitle())
            .param("eventId", createdTransaction.getEventId().toString())
            .param("transactionType", createdTransaction.getTransactionType().name())
            .param("transactionAmount", createdTransaction.getTransactionAmount().toString())
            .param("description", createdTransaction.getDescription())
            .param("transactionStatus", UPDATED_TRANSACTION_STATUS.name())
            .param("transactionDate", createdTransaction.getTransactionDate().toString())
        ).andExpect(status().isOk());

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction updatedTransaction = transactionList.get(transactionList.size() - 1);

        // Validate the Transaction count stays the same after creating 1 transaction
        assertThat(transactionList).hasSize(databaseSizeAfterCreate);
        assertThat(updatedTransaction.getTransactionStatus()).isEqualTo(UPDATED_TRANSACTION_STATUS);
    }

    @Test
    @WithNormalUser
    public void updateEventTransactionPendingIncome_ToCompleted_UserIsUser_ShouldThrow403() throws Exception {
        // Create Event
        Event savedEvent = initEventDB();
        // Create Transaction
        transactionRepository.saveAndFlush(transaction.eventId(savedEvent.getId()));
        // Get Database size after creating transaction
        int databaseSizeAfterCreate = transactionRepository.findAll().size();
        // find created transaction
        Transaction createdTransaction = transactionRepository.findById(transaction.getId()).get();

        // Update the transaction
        restTransactionMockMvc.perform(putMultipartRequestBuilder
            .file(MOCK_MULTIPART_FILE)
            .param("id", createdTransaction.getId().toString())
            .param("title", createdTransaction.getTitle())
            .param("eventId", createdTransaction.getEventId().toString())
            .param("transactionType", createdTransaction.getTransactionType().name())
            .param("transactionAmount", createdTransaction.getTransactionAmount().toString())
            .param("description", createdTransaction.getDescription())
            .param("transactionStatus", UPDATED_TRANSACTION_STATUS.name())
            .param("transactionDate", createdTransaction.getTransactionDate().toString())
        ).andExpect(status().isForbidden());

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction updatedTransaction = transactionList.get(transactionList.size() - 1);

        // Validate the Transaction count stays the same after creating 1 transaction
        assertThat(transactionList).hasSize(databaseSizeAfterCreate);
        assertThat(updatedTransaction.getTransactionStatus()).isEqualTo(DEFAULT_TRANSACTION_STATUS);
    }

    @Test
    @WithEventCrew
    public void updateEventTransactionPendingIncome_ToCompleted_UserIsEventCrew_ShouldSuccess() throws Exception {
        // Get event id by current event crew
        EventCrew savedEventCrew = getEventCrewByCurrentLoginUser();
        // Create Transaction
        transactionRepository.saveAndFlush(transaction.eventId(savedEventCrew.getEventId()));
        // Get Database size after creating transaction
        int databaseSizeAfterCreate = transactionRepository.findAll().size();
        // find created transaction
        Transaction createdTransaction = transactionRepository.findById(transaction.getId()).get();

        // Update the transaction
        restTransactionMockMvc.perform(putMultipartRequestBuilder
            .file(MOCK_MULTIPART_FILE)
            .param("id", createdTransaction.getId().toString())
            .param("title", createdTransaction.getTitle())
            .param("eventId", createdTransaction.getEventId().toString())
            .param("transactionType", createdTransaction.getTransactionType().name())
            .param("transactionAmount", createdTransaction.getTransactionAmount().toString())
            .param("description", createdTransaction.getDescription())
            .param("transactionStatus", UPDATED_TRANSACTION_STATUS.name())
            .param("transactionDate", createdTransaction.getTransactionDate().toString())
        ).andExpect(status().isOk());

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction updatedTransaction = transactionList.get(transactionList.size() - 1);

        // Validate the Transaction count stays the same after creating 1 transaction
        assertThat(transactionList).hasSize(databaseSizeAfterCreate);
        assertThat(updatedTransaction.getTransactionStatus()).isEqualTo(UPDATED_TRANSACTION_STATUS);
    }

    @Test
    @WithCurrentCCAdministrator
    public void updateEventTransactionPendingIncome_ToCompleted_UserIsCurrentCCAdmin_ShouldSuccess() throws Exception {
        // Create Event
        Event savedEvent = initEventDB();
        // Create Transaction
        transactionRepository.saveAndFlush(transaction.eventId(savedEvent.getId()));
        // Get Database size after creating transaction
        int databaseSizeAfterCreate = transactionRepository.findAll().size();
        // find created transaction
        Transaction createdTransaction = transactionRepository.findById(transaction.getId()).get();

        // Update the transaction
        restTransactionMockMvc.perform(putMultipartRequestBuilder
            .file(MOCK_MULTIPART_FILE)
            .param("id", createdTransaction.getId().toString())
            .param("title", createdTransaction.getTitle())
            .param("eventId", createdTransaction.getEventId().toString())
            .param("transactionType", createdTransaction.getTransactionType().name())
            .param("transactionAmount", createdTransaction.getTransactionAmount().toString())
            .param("description", createdTransaction.getDescription())
            .param("transactionStatus", UPDATED_TRANSACTION_STATUS.name())
            .param("transactionDate", createdTransaction.getTransactionDate().toString())
        ).andExpect(status().isOk());

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction updatedTransaction = transactionList.get(transactionList.size() - 1);

        // Validate the Transaction count stays the same after creating 1 transaction
        assertThat(transactionList).hasSize(databaseSizeAfterCreate);
        assertThat(updatedTransaction.getTransactionStatus()).isEqualTo(UPDATED_TRANSACTION_STATUS);
    }

    @Test
    @WithCurrentCCAdministrator
    public void updateTransactionStatus_UserWithRoleAdmin() throws Exception {
        // Initialize the database
        initTransactionDB();

        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        restTransactionMockMvc.perform(put("/api/transactions/{id}/status/{transactionStatus}", transaction.getId(), UPDATED_TRANSACTION_STATUS)
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the Checklist in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
        // Validate the saved Transaction
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testTransaction.getEventId()).isEqualTo(DEFAULT_EVENT_ID);
        assertThat(testTransaction.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testTransaction.getTransactionAmount()).isEqualTo(DEFAULT_AMOUNT.setScale(2, RoundingMode.HALF_UP));
        assertThat(testTransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTransaction.getTransactionStatus()).isEqualTo(UPDATED_TRANSACTION_STATUS);
        assertThat(testTransaction.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
        assertThat(testTransaction.getImageLink()).isEqualTo(DEFAULT_IMAGE_LINK);
    }
    
    @Test
    @WithEventHead
    public void updateTransactionStatus_UserIsNotAdmin_ShouldThrow403IsForbidden() throws Exception {
        // Initialize the database
        initTransactionDB();

        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        restTransactionMockMvc.perform(put("/api/transactions/{id}/status/{transactionStatus}", transaction.getId(), UPDATED_TRANSACTION_STATUS)
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isForbidden());

        // Validate the Checklist in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
        // Validate the saved Transaction
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testTransaction.getEventId()).isEqualTo(DEFAULT_EVENT_ID);
        assertThat(testTransaction.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testTransaction.getTransactionAmount()).isEqualTo(DEFAULT_AMOUNT.setScale(2, RoundingMode.HALF_UP));
        assertThat(testTransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTransaction.getTransactionStatus()).isEqualTo(DEFAULT_TRANSACTION_STATUS);
        assertThat(testTransaction.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
        assertThat(testTransaction.getImageLink()).isEqualTo(DEFAULT_IMAGE_LINK);
    }
    
    @Test
    @WithCurrentCCAdministrator
    public void updateTransactionStatus_TransactionIsNotExists_ShouldThrow400BadRequest() throws Exception {
        // Initialize the database
        initTransactionDB();

        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        restTransactionMockMvc.perform(put("/api/transactions/{id}/status/{transactionStatus}", Long.MAX_VALUE, UPDATED_TRANSACTION_STATUS)
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isBadRequest());

        // Validate the Checklist in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
        // Validate the saved Transaction
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testTransaction.getEventId()).isEqualTo(DEFAULT_EVENT_ID);
        assertThat(testTransaction.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testTransaction.getTransactionAmount()).isEqualTo(DEFAULT_AMOUNT.setScale(2, RoundingMode.HALF_UP));
        assertThat(testTransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTransaction.getTransactionStatus()).isEqualTo(DEFAULT_TRANSACTION_STATUS);
        assertThat(testTransaction.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
        assertThat(testTransaction.getImageLink()).isEqualTo(DEFAULT_IMAGE_LINK);
    }
    
    @Test
    @WithCurrentCCAdministrator
    public void updateTransactionStatus_TransactionIsNotPending_ShouldThrow400BadRequest() throws Exception {
        // Initialize the database
        transaction.setTransactionStatus(UPDATED_TRANSACTION_STATUS);
        initTransactionDB();

        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        restTransactionMockMvc.perform(put("/api/transactions/{id}/status/{transactionStatus}", Long.MAX_VALUE, UPDATED_TRANSACTION_STATUS)
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isBadRequest());

        // Validate the Checklist in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
        // Validate the saved Transaction
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testTransaction.getEventId()).isEqualTo(DEFAULT_EVENT_ID);
        assertThat(testTransaction.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testTransaction.getTransactionAmount()).isEqualTo(DEFAULT_AMOUNT.setScale(2, RoundingMode.HALF_UP));
        assertThat(testTransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTransaction.getTransactionStatus()).isEqualTo(UPDATED_TRANSACTION_STATUS);
        assertThat(testTransaction.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
        assertThat(testTransaction.getImageLink()).isEqualTo(DEFAULT_IMAGE_LINK);
    }

    @Test
    @WithEventCrew
    public void getTotalBudgetByEventId_WithoutBudget() throws Exception {
        EventCrew savedEventCrew = getEventCrewByCurrentLoginUser();
        EventBudgetTotalDTO eventBudgetTotalDTO = new EventBudgetTotalDTO();

        restTransactionMockMvc.perform(get("/api/transactions/event/{eventId}/total", savedEventCrew.getEventId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.totalExpense").value(eventBudgetTotalDTO.getTotalExpense()))
            .andExpect(jsonPath("$.totalIncome").value(eventBudgetTotalDTO.getTotalIncome()));
    }

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

    private User getCurrentUser() {
        return userRepository.findOneByLogin(USERNAME)
            .orElseThrow(()-> new BadRequestException("User not found"));
    }

    private Transaction initTransactionDB() {
        return transactionRepository.saveAndFlush(transaction);
    }

    private Event initEventDB() {
        return eventRepository.saveAndFlush(event);
    }

    private EventCrew initEventCrewDB() { return eventCrewRepository.saveAndFlush(eventCrew); }

    private EventCrew getEventCrewByCurrentLoginUser() {
        User currentUser =  SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneWithAuthoritiesByLogin)
            .orElseThrow(() -> new BadRequestException("Cannot find user"));
        List<EventCrew> eventCrewList = eventCrewRepository
            .findAllByUserId(currentUser.getId());
        assertThat(eventCrewList).hasSize(1);
        return eventCrewList.get(0);
    }
}
