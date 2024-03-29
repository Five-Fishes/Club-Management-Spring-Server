package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.ClubmanagementApp;
import com.thirdcc.webapp.annotations.authorization.WithCurrentCCAdministrator;
import com.thirdcc.webapp.annotations.authorization.WithCurrentCCHead;
import com.thirdcc.webapp.annotations.init.InitYearSession;
import com.thirdcc.webapp.domain.User;
import com.thirdcc.webapp.domain.UserCCInfo;
import com.thirdcc.webapp.domain.UserUniInfo;
import com.thirdcc.webapp.domain.enumeration.UserUniStatus;
import com.thirdcc.webapp.exception.BadRequestException;
import com.thirdcc.webapp.repository.UserCCInfoRepository;
import com.thirdcc.webapp.repository.UserRepository;
import com.thirdcc.webapp.repository.UserUniInfoRepository;
import com.thirdcc.webapp.security.SecurityUtils;
import com.thirdcc.webapp.domain.*;
import com.thirdcc.webapp.domain.enumeration.*;
import com.thirdcc.webapp.repository.*;
import com.thirdcc.webapp.service.UserCCInfoService;
import com.thirdcc.webapp.service.dto.UserCCInfoDTO;
import com.thirdcc.webapp.service.mapper.UserCCInfoMapper;

import com.thirdcc.webapp.utils.YearSessionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.thirdcc.webapp.utils.FishLevelUtils.*;
import static com.thirdcc.webapp.web.rest.UserResourceIT.createUserEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@Link UserCCInfoResource} REST controller.
 */
@SpringBootTest(classes = ClubmanagementApp.class)
@AutoConfigureMockMvc
@WithMockUser
@InitYearSession
public class UserCCInfoResourceIT {

    private static final String ENTITY_API_URL = "/api/user-cc-infos";
    private static final String CC_MEMBER_API_URL = "/api/cc-members";

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long SMALLER_USER_ID = DEFAULT_USER_ID - 1L;
    private static final Long UPDATED_USER_ID = 2L;

    private static final ClubFamilyCode DEFAULT_CLUB_FAMILY_CODE = ClubFamilyCode.JIN_LONG;
    private static final ClubFamilyCode SMALLER_CLUB_FAMILY_CODE = ClubFamilyCode.QI_CAI;
    private static final ClubFamilyCode UPDATED_CLUB_FAMILY_CODE = ClubFamilyCode.BI_MU;

    private static final ClubFamilyRole DEFAULT_FAMILY_ROLE = ClubFamilyRole.FATHER;
    private static final ClubFamilyRole UPDATED_FAMILY_ROLE = ClubFamilyRole.MOTHER;

    private static final String DEFAULT_YEAR_SESSION = "2020/2021";
    private static final String UPDATED_YEAR_SESSION = "2019/2020";

    private static final String DEFAULT_UNI_INTAKE_YEAR_SESSION = "2020/2021";
    private static final String UPDATED_UNI_INTAKE_YEAR_SESSION = "2019/2020";
    private static final Long DEFAULT_COURSE_PROGRAM_ID = 1L;
    private static final Long UPDATED_COURSE_PROGRAM_ID = 2L;
    private static final int DEFAULT_INTAKE_SEMESTER = 1;
    private static final String DEFAULT_STAY_IN = "KK3 UM";
    private static final UserUniStatus DEFAULT_UNI_INFO_STATUS = UserUniStatus.STUDYING;

    // Event DEFAULT value
    private static final String DEFAULT_EVENT_NAME = "CC_NIGHT";
    private static final String DEFAULT_EVENT_DESCRIPTION = "CC Party Night";
    private static final String DEFAULT_EVENT_REMARKS = "Annual Gathering for CC Member";
    private static final String DEFAULT_EVENT_VENUE = "KLCC";
    private static final Instant DEFAULT_EVENT_START_DATE = Instant.parse("2020-05-01T20:00:00Z");
    private static final Instant DEFAULT_EVENT_END_DATE = Instant.parse("2020-05-01T23:00:00Z");
    private static final BigDecimal DEFAULT_EVENT_FEE = new BigDecimal(1);
    private static final Boolean DEFAULT_EVENT_REQUIRED_TRANSPORT = false;
    private static final EventStatus DEFAULT_EVENT_STATUS = EventStatus.OPEN;

    // Event Crew DEFAULT value
    private static final Long DEFAULT_EVENT_CREW_EVENT_ID = 1L;
    private static final Long DEFAULT_EVENT_CREW_USER_ID = 1L;
    private static final EventCrewRole DEFAULT_EVENT_CREW_ROLE = EventCrewRole.HEAD;

    // Administrator DEFAULT value
    private static final Long DEFAULT_ADMINISTRATOR_USER_ID = 1L;
    private static final String DEFAULT_ADMINISTRATOR_YEAR_SESSION = "2020/2021";
    private static final AdministratorStatus DEFAULT_ADMINISTRATOR_STATUS = AdministratorStatus.ACTIVE;
    private static final AdministratorRole DEFAULT_ADMINISTRATOR_ROLE = AdministratorRole.CC_HEAD;

    // User DEFAULT value
    private static final String DEFAULT_USER_FIRST_NAME = "Default First Name";
    private static final String UPDATED_USER_FIRST_NAME = "Updated First Name";
    private static final String DEFAULT_USER_LAST_NAME = "Default Last Name";
    private static final String UPDATED_USER_LAST_NAME = "Updated Last Name";

    @Autowired
    private UserCCInfoRepository userCCInfoRepository;

    @Autowired
    private UserCCInfoMapper userCCInfoMapper;

    @Autowired
    private UserCCInfoService userCCInfoService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserUniInfoRepository userUniInfoRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventCrewRepository eventCrewRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserCCInfoMockMvc;

    private UserCCInfo userCCInfo;

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
    public static UserCCInfo createEntity() {
        return new UserCCInfo()
            .userId(DEFAULT_USER_ID)
            .clubFamilyCode(DEFAULT_CLUB_FAMILY_CODE)
            .familyRole(DEFAULT_FAMILY_ROLE)
            .yearSession(DEFAULT_YEAR_SESSION);
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserCCInfo createUpdatedEntity() {
        return new UserCCInfo()
            .userId(UPDATED_USER_ID)
            .clubFamilyCode(UPDATED_CLUB_FAMILY_CODE)
            .familyRole(UPDATED_FAMILY_ROLE)
            .yearSession(UPDATED_YEAR_SESSION);
    }

    public static UserUniInfo createUserUniInfoEntity() {
        UserUniInfo userUniInfo = new UserUniInfo();
        userUniInfo.setUserId(DEFAULT_USER_ID);
        userUniInfo.setYearSession(DEFAULT_UNI_INTAKE_YEAR_SESSION);
        userUniInfo.setCourseProgramId(DEFAULT_COURSE_PROGRAM_ID);
        userUniInfo.setIntakeSemester(DEFAULT_INTAKE_SEMESTER);
        userUniInfo.setStayIn(DEFAULT_STAY_IN);
        userUniInfo.setStatus(DEFAULT_UNI_INFO_STATUS);
        return userUniInfo;
    }

    public static Event createEventEntity() {
        Event event = new Event();
        event.setName(DEFAULT_EVENT_NAME);
        event.setVenue(DEFAULT_EVENT_VENUE);
        event.setFee(DEFAULT_EVENT_FEE);
        event.setStatus(DEFAULT_EVENT_STATUS);
        event.setStartDate(DEFAULT_EVENT_START_DATE);
        event.setEndDate(DEFAULT_EVENT_END_DATE);
        event.setRequiredTransport(DEFAULT_EVENT_REQUIRED_TRANSPORT);
        event.setDescription(DEFAULT_EVENT_DESCRIPTION);
        event.setRemarks(DEFAULT_EVENT_REMARKS);
        return event;
    }

    public static EventCrew createEventCrewEntity() {
        EventCrew eventCrew = new EventCrew();
        eventCrew.setEventId(DEFAULT_EVENT_CREW_EVENT_ID);
        eventCrew.setRole(DEFAULT_EVENT_CREW_ROLE);
        eventCrew.setUserId(DEFAULT_EVENT_CREW_USER_ID);
        return eventCrew;
    }

    public static Administrator createAdministratorEntity() {
        Administrator administrator = new Administrator();
        administrator.setYearSession(DEFAULT_ADMINISTRATOR_YEAR_SESSION);
        administrator.setStatus(DEFAULT_ADMINISTRATOR_STATUS);
        administrator.setRole(DEFAULT_ADMINISTRATOR_ROLE);
        administrator.setUserId(DEFAULT_ADMINISTRATOR_USER_ID);
        return administrator;
    }

    @BeforeEach
    public void initTest() {
        userCCInfo = createEntity();
    }

    @AfterEach
    public void cleanUp() {
        userRepository.deleteAll();
        userUniInfoRepository.deleteAll();
        userCCInfoRepository.deleteAll();
    }

    @Test
    @Transactional
    @WithCurrentCCAdministrator
    public void createUserCCInfo_withAdmin_should201() throws Exception {
        int databaseSizeBeforeCreate = userCCInfoRepository.findAll().size();

        // Create the UserCCInfo
        UserCCInfoDTO userCCInfoDTO = userCCInfoMapper.toDto(userCCInfo);
        restUserCCInfoMockMvc.perform(post("/api/user-cc-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userCCInfoDTO)))
            .andExpect(status().isCreated());

        // Validate the UserCCInfo in the database
        List<UserCCInfo> userCCInfoList = userCCInfoRepository.findAll();
        assertThat(userCCInfoList).hasSize(databaseSizeBeforeCreate + 1);
        UserCCInfo testUserCCInfo = userCCInfoList.get(userCCInfoList.size() - 1);
        assertThat(testUserCCInfo.getUserId()).isEqualTo(DEFAULT_USER_ID);
        assertThat(testUserCCInfo.getClubFamilyCode()).isEqualTo(DEFAULT_CLUB_FAMILY_CODE);
        assertThat(testUserCCInfo.getFamilyRole()).isEqualTo(DEFAULT_FAMILY_ROLE);
        assertThat(testUserCCInfo.getYearSession()).isEqualTo(DEFAULT_YEAR_SESSION);
    }

    @Test
    @Transactional
    public void createUserCCInfo_withUser_should403() throws Exception {
        // Create the UserCCInfo
        UserCCInfoDTO userCCInfoDTO = userCCInfoMapper.toDto(userCCInfo);
        restUserCCInfoMockMvc.perform(post("/api/user-cc-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userCCInfoDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithCurrentCCAdministrator
    public void createUserCCInfoWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userCCInfoRepository.findAll().size();

        // Create the UserCCInfo with an existing ID
        userCCInfo.setId(1L);
        UserCCInfoDTO userCCInfoDTO = userCCInfoMapper.toDto(userCCInfo);

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserCCInfoMockMvc.perform(post("/api/user-cc-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userCCInfoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the UserCCInfo in the database
        List<UserCCInfo> userCCInfoList = userCCInfoRepository.findAll();
        assertThat(userCCInfoList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    void getUserCCInfosByIdFiltering() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        Long id = userCCInfo.getId();

        defaultUserCCInfoShouldBeFound("id.equals=" + id);
        defaultUserCCInfoShouldNotBeFound("id.notEquals=" + id);

        defaultUserCCInfoShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultUserCCInfoShouldNotBeFound("id.greaterThan=" + id);

        defaultUserCCInfoShouldBeFound("id.lessThanOrEqual=" + id);
        defaultUserCCInfoShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByUserIdIsEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where userId equals to DEFAULT_USER_ID
        defaultUserCCInfoShouldBeFound("userId.equals=" + DEFAULT_USER_ID);

        // Get all the userCCInfoList where userId equals to UPDATED_USER_ID
        defaultUserCCInfoShouldNotBeFound("userId.equals=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByUserIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where userId not equals to DEFAULT_USER_ID
        defaultUserCCInfoShouldNotBeFound("userId.notEquals=" + DEFAULT_USER_ID);

        // Get all the userCCInfoList where userId not equals to UPDATED_USER_ID
        defaultUserCCInfoShouldBeFound("userId.notEquals=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByUserIdIsInShouldWork() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where userId in DEFAULT_USER_ID or UPDATED_USER_ID
        defaultUserCCInfoShouldBeFound("userId.in=" + DEFAULT_USER_ID + "," + UPDATED_USER_ID);

        // Get all the userCCInfoList where userId equals to UPDATED_USER_ID
        defaultUserCCInfoShouldNotBeFound("userId.in=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByUserIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where userId is not null
        defaultUserCCInfoShouldBeFound("userId.specified=true");

        // Get all the userCCInfoList where userId is null
        defaultUserCCInfoShouldNotBeFound("userId.specified=false");
    }

    @Test
    @Transactional
    void getAllUserCCInfosByUserIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where userId is greater than or equal to DEFAULT_USER_ID
        defaultUserCCInfoShouldBeFound("userId.greaterThanOrEqual=" + DEFAULT_USER_ID);

        // Get all the userCCInfoList where userId is greater than or equal to UPDATED_USER_ID
        defaultUserCCInfoShouldNotBeFound("userId.greaterThanOrEqual=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByUserIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where userId is less than or equal to DEFAULT_USER_ID
        defaultUserCCInfoShouldBeFound("userId.lessThanOrEqual=" + DEFAULT_USER_ID);

        // Get all the userCCInfoList where userId is less than or equal to SMALLER_USER_ID
        defaultUserCCInfoShouldNotBeFound("userId.lessThanOrEqual=" + SMALLER_USER_ID);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByUserIdIsLessThanSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where userId is less than DEFAULT_USER_ID
        defaultUserCCInfoShouldNotBeFound("userId.lessThan=" + DEFAULT_USER_ID);

        // Get all the userCCInfoList where userId is less than UPDATED_USER_ID
        defaultUserCCInfoShouldBeFound("userId.lessThan=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByUserIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where userId is greater than DEFAULT_USER_ID
        defaultUserCCInfoShouldNotBeFound("userId.greaterThan=" + DEFAULT_USER_ID);

        // Get all the userCCInfoList where userId is greater than SMALLER_USER_ID
        defaultUserCCInfoShouldBeFound("userId.greaterThan=" + SMALLER_USER_ID);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByclubFamilyCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where clubFamilyCode equals to DEFAULT_CLUB_FAMILY_ID
        defaultUserCCInfoShouldBeFound("clubFamilyCode.equals=" + DEFAULT_CLUB_FAMILY_CODE);

        // Get all the userCCInfoList where clubFamilyCode equals to UPDATED_CLUB_FAMILY_ID
        defaultUserCCInfoShouldNotBeFound("clubFamilyCode.equals=" + UPDATED_CLUB_FAMILY_CODE);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByclubFamilyCodeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where clubFamilyCode not equals to DEFAULT_CLUB_FAMILY_ID
        defaultUserCCInfoShouldNotBeFound("clubFamilyCode.notEquals=" + DEFAULT_CLUB_FAMILY_CODE);

        // Get all the userCCInfoList where clubFamilyCode not equals to UPDATED_CLUB_FAMILY_ID
        defaultUserCCInfoShouldBeFound("clubFamilyCode.notEquals=" + UPDATED_CLUB_FAMILY_CODE);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByclubFamilyCodeIsInShouldWork() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where clubFamilyCode in DEFAULT_CLUB_FAMILY_ID or UPDATED_CLUB_FAMILY_ID
        defaultUserCCInfoShouldBeFound("clubFamilyCode.in=" + DEFAULT_CLUB_FAMILY_CODE + "," + UPDATED_CLUB_FAMILY_CODE);

        // Get all the userCCInfoList where clubFamilyCode equals to UPDATED_CLUB_FAMILY_ID
        defaultUserCCInfoShouldNotBeFound("clubFamilyCode.in=" + UPDATED_CLUB_FAMILY_CODE);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByclubFamilyCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where clubFamilyCode is not null
        defaultUserCCInfoShouldBeFound("clubFamilyCode.specified=true");

        // Get all the userCCInfoList where clubFamilyCode is null
        defaultUserCCInfoShouldNotBeFound("clubFamilyCode.specified=false");
    }

    @Test
    @Transactional
    void getAllUserCCInfosByFamilyRoleIsEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where familyRole equals to DEFAULT_FAMILY_ROLE
        defaultUserCCInfoShouldBeFound("familyRole.equals=" + DEFAULT_FAMILY_ROLE);

        // Get all the userCCInfoList where familyRole equals to UPDATED_FAMILY_ROLE
        defaultUserCCInfoShouldNotBeFound("familyRole.equals=" + UPDATED_FAMILY_ROLE);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByFamilyRoleIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where familyRole not equals to DEFAULT_FAMILY_ROLE
        defaultUserCCInfoShouldNotBeFound("familyRole.notEquals=" + DEFAULT_FAMILY_ROLE);

        // Get all the userCCInfoList where familyRole not equals to UPDATED_FAMILY_ROLE
        defaultUserCCInfoShouldBeFound("familyRole.notEquals=" + UPDATED_FAMILY_ROLE);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByFamilyRoleIsInShouldWork() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where familyRole in DEFAULT_FAMILY_ROLE or UPDATED_FAMILY_ROLE
        defaultUserCCInfoShouldBeFound("familyRole.in=" + DEFAULT_FAMILY_ROLE + "," + UPDATED_FAMILY_ROLE);

        // Get all the userCCInfoList where familyRole equals to UPDATED_FAMILY_ROLE
        defaultUserCCInfoShouldNotBeFound("familyRole.in=" + UPDATED_FAMILY_ROLE);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByFamilyRoleIsNullOrNotNull() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where familyRole is not null
        defaultUserCCInfoShouldBeFound("familyRole.specified=true");

        // Get all the userCCInfoList where familyRole is null
        defaultUserCCInfoShouldNotBeFound("familyRole.specified=false");
    }

    @Test
    @Transactional
    void getAllUserCCInfosByYearSessionIsEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where yearSession equals to DEFAULT_YEAR_SESSION
        defaultUserCCInfoShouldBeFound("yearSession.equals=" + DEFAULT_YEAR_SESSION);

        // Get all the userCCInfoList where yearSession equals to UPDATED_YEAR_SESSION
        defaultUserCCInfoShouldNotBeFound("yearSession.equals=" + UPDATED_YEAR_SESSION);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByYearSessionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where yearSession not equals to DEFAULT_YEAR_SESSION
        defaultUserCCInfoShouldNotBeFound("yearSession.notEquals=" + DEFAULT_YEAR_SESSION);

        // Get all the userCCInfoList where yearSession not equals to UPDATED_YEAR_SESSION
        defaultUserCCInfoShouldBeFound("yearSession.notEquals=" + UPDATED_YEAR_SESSION);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByYearSessionIsInShouldWork() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where yearSession in DEFAULT_YEAR_SESSION or UPDATED_YEAR_SESSION
        defaultUserCCInfoShouldBeFound("yearSession.in=" + DEFAULT_YEAR_SESSION + "," + UPDATED_YEAR_SESSION);

        // Get all the userCCInfoList where yearSession equals to UPDATED_YEAR_SESSION
        defaultUserCCInfoShouldNotBeFound("yearSession.in=" + UPDATED_YEAR_SESSION);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByYearSessionIsNullOrNotNull() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where yearSession is not null
        defaultUserCCInfoShouldBeFound("yearSession.specified=true");

        // Get all the userCCInfoList where yearSession is null
        defaultUserCCInfoShouldNotBeFound("yearSession.specified=false");
    }

    @Test
    @Transactional
    void getAllUserCCInfosByYearSessionContainsSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where yearSession contains DEFAULT_YEAR_SESSION
        defaultUserCCInfoShouldBeFound("yearSession.contains=" + DEFAULT_YEAR_SESSION);

        // Get all the userCCInfoList where yearSession contains UPDATED_YEAR_SESSION
        defaultUserCCInfoShouldNotBeFound("yearSession.contains=" + UPDATED_YEAR_SESSION);
    }

    @Test
    @Transactional
    void getAllUserCCInfosByYearSessionNotContainsSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList where yearSession does not contain DEFAULT_YEAR_SESSION
        defaultUserCCInfoShouldNotBeFound("yearSession.doesNotContain=" + DEFAULT_YEAR_SESSION);

        // Get all the userCCInfoList where yearSession does not contain UPDATED_YEAR_SESSION
        defaultUserCCInfoShouldBeFound("yearSession.doesNotContain=" + UPDATED_YEAR_SESSION);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultUserCCInfoShouldBeFound(String filter) throws Exception {
        restUserCCInfoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userCCInfo.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].familyRole").value(hasItem(DEFAULT_FAMILY_ROLE.toString())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(DEFAULT_YEAR_SESSION)));

        // Check, that the count call also returns 1
        restUserCCInfoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultUserCCInfoShouldNotBeFound(String filter) throws Exception {
        restUserCCInfoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restUserCCInfoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getUserCCInfo() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get the userCCInfo
        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/{id}", userCCInfo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(userCCInfo.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID.intValue()))
            .andExpect(jsonPath("$.familyRole").value(DEFAULT_FAMILY_ROLE.toString()))
            .andExpect(jsonPath("$.yearSession").value(DEFAULT_YEAR_SESSION));
    }

    @Test
    @Transactional
    public void getNonExistingUserCCInfo() throws Exception {
        // Get the userCCInfo
        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithCurrentCCAdministrator
    public void updateUserCCInfo_withCurrentAdmin_should200() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        int databaseSizeBeforeUpdate = userCCInfoRepository.findAll().size();

        // Update the userCCInfo
        UserCCInfo updatedUserCCInfo = userCCInfoRepository.findById(userCCInfo.getId()).get();
        // Disconnect from session so that the updates on updatedUserCCInfo are not directly saved in db
        em.detach(updatedUserCCInfo);
        updatedUserCCInfo
            .userId(UPDATED_USER_ID)
            .clubFamilyCode(UPDATED_CLUB_FAMILY_CODE)
            .familyRole(UPDATED_FAMILY_ROLE)
            .yearSession(UPDATED_YEAR_SESSION);
        UserCCInfoDTO userCCInfoDTO = userCCInfoMapper.toDto(updatedUserCCInfo);

        restUserCCInfoMockMvc.perform(put("/api/user-cc-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userCCInfoDTO)))
            .andExpect(status().isOk());

        // Validate the UserCCInfo in the database
        List<UserCCInfo> userCCInfoList = userCCInfoRepository.findAll();
        assertThat(userCCInfoList).hasSize(databaseSizeBeforeUpdate);
        UserCCInfo testUserCCInfo = userCCInfoList.get(userCCInfoList.size() - 1);
        assertThat(testUserCCInfo.getUserId()).isEqualTo(UPDATED_USER_ID);
        assertThat(testUserCCInfo.getClubFamilyCode()).isEqualTo(UPDATED_CLUB_FAMILY_CODE);
        assertThat(testUserCCInfo.getFamilyRole()).isEqualTo(UPDATED_FAMILY_ROLE);
        assertThat(testUserCCInfo.getYearSession()).isEqualTo(UPDATED_YEAR_SESSION);
    }

    @Test
    @Transactional
    public void updateUserCCInfo_withUser_should403() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        // Update the userCCInfo
        UserCCInfo updatedUserCCInfo = userCCInfoRepository.findById(userCCInfo.getId()).get();
        // Disconnect from session so that the updates on updatedUserCCInfo are not directly saved in db
        em.detach(updatedUserCCInfo);
        updatedUserCCInfo
            .userId(UPDATED_USER_ID)
            .clubFamilyCode(UPDATED_CLUB_FAMILY_CODE)
            .familyRole(UPDATED_FAMILY_ROLE)
            .yearSession(UPDATED_YEAR_SESSION);
        UserCCInfoDTO userCCInfoDTO = userCCInfoMapper.toDto(updatedUserCCInfo);

        restUserCCInfoMockMvc.perform(put("/api/user-cc-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userCCInfoDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithCurrentCCAdministrator
    public void updateNonExistingUserCCInfo_withCurrentAdmin_should400() throws Exception {
        int databaseSizeBeforeUpdate = userCCInfoRepository.findAll().size();

        // Create the UserCCInfo
        UserCCInfoDTO userCCInfoDTO = userCCInfoMapper.toDto(userCCInfo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserCCInfoMockMvc.perform(put("/api/user-cc-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userCCInfoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the UserCCInfo in the database
        List<UserCCInfo> userCCInfoList = userCCInfoRepository.findAll();
        assertThat(userCCInfoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void updateNonExistingUserCCInfo_withUser_should403() throws Exception {
        int databaseSizeBeforeUpdate = userCCInfoRepository.findAll().size();

        // Create the UserCCInfo
        UserCCInfoDTO userCCInfoDTO = userCCInfoMapper.toDto(userCCInfo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserCCInfoMockMvc.perform(put("/api/user-cc-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userCCInfoDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithCurrentCCHead
    public void deleteUserCCInfo_withCurrentCCHead_should204() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        int databaseSizeBeforeDelete = userCCInfoRepository.findAll().size();

        // Delete the userCCInfo
        restUserCCInfoMockMvc.perform(delete("/api/user-cc-infos/{id}", userCCInfo.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<UserCCInfo> userCCInfoList = userCCInfoRepository.findAll();
        assertThat(userCCInfoList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    @WithCurrentCCAdministrator
    public void deleteUserCCInfo_withCurrentAdmin_should403() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Delete the userCCInfo
        restUserCCInfoMockMvc.perform(delete("/api/user-cc-infos/{id}", userCCInfo.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void deleteUserCCInfo_withUser_should403() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        int databaseSizeBeforeDelete = userCCInfoRepository.findAll().size();

        // Delete the userCCInfo
        restUserCCInfoMockMvc.perform(delete("/api/user-cc-infos/{id}", userCCInfo.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void getCurrentUserCCInfoProfile_FirstYearWithoutFamilyRole() throws Exception {
        String currentYearSession = YearSessionUtils.getCurrentYearSession();
        User currentUser = getCurrentUser();

        UserUniInfo userUniInfo = createUserUniInfoEntity();
        userUniInfo.setUserId(currentUser.getId());
        userUniInfo.setYearSession(currentYearSession);
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(currentUser.getId().intValue())))
            .andExpect(jsonPath("$.[0].fishLevel").value(FIRST_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[0].yearSession").value(currentYearSession))
            .andExpect(jsonPath("$.[0].familyRole").value(nullValue()));
    }

    @Test
    @Transactional
    public void getCurrentUserCCInfoProfile_SecondYearWithoutFamilyRole() throws Exception {
        String currentYearSession = YearSessionUtils.getCurrentYearSession();
        String intakeYearSession = YearSessionUtils.addYearSessionWithSemester(currentYearSession, -2);
        User currentUser = getCurrentUser();

        UserUniInfo userUniInfo = createUserUniInfoEntity();
        userUniInfo.setUserId(currentUser.getId());
        userUniInfo.setYearSession(intakeYearSession);
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(currentUser.getId().intValue())))
            .andExpect(jsonPath("$.[0].fishLevel").value(SECOND_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[0].yearSession").value(currentYearSession))
            .andExpect(jsonPath("$.[0].familyRole").value(nullValue()))
            .andExpect(jsonPath("$.[1].fishLevel").value(FIRST_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[1].yearSession").value(intakeYearSession))
            .andExpect(jsonPath("$.[1].familyRole").value(nullValue()));
    }

    @Test
    @Transactional
    public void getCurrentUserCCInfoProfile_SecondYearWithFamilyRole() throws Exception {
        String currentYearSession = YearSessionUtils.getCurrentYearSession();
        String intakeYearSession = YearSessionUtils.addYearSessionWithSemester(currentYearSession, -2);
        User currentUser = getCurrentUser();

        UserUniInfo userUniInfo = createUserUniInfoEntity();
        userUniInfo.setUserId(currentUser.getId());
        userUniInfo.setYearSession(intakeYearSession);
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);

        UserCCInfo userCCInfo = createEntity();
        userCCInfo.setUserId(currentUser.getId());
        userCCInfo.setYearSession(currentYearSession);
        UserCCInfo savedUserCCInfo = userCCInfoRepository.saveAndFlush(userCCInfo);

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(currentUser.getId().intValue())))
            .andExpect(jsonPath("$.[0].fishLevel").value(SECOND_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[0].yearSession").value(currentYearSession))
            .andExpect(jsonPath("$.[0].familyRole").value(DEFAULT_FAMILY_ROLE.name()))
            .andExpect(jsonPath("$.[1].fishLevel").value(FIRST_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[1].yearSession").value(intakeYearSession))
            .andExpect(jsonPath("$.[1].familyRole").value(nullValue()));
    }

    @Test
    @Transactional
    public void getCurrentUserCCInfoProfile_ThirdYearWithoutFamilyRole() throws Exception {
        String currentYearSession = YearSessionUtils.getCurrentYearSession();
        String intakeYearSession = YearSessionUtils.addYearSessionWithSemester(currentYearSession, -4);
        String secondYearSession = YearSessionUtils.addYearSessionWithSemester(currentYearSession, -2);
        User currentUser = getCurrentUser();

        UserUniInfo userUniInfo = createUserUniInfoEntity();
        userUniInfo.setUserId(currentUser.getId());
        userUniInfo.setYearSession(intakeYearSession);
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$.[0].fishLevel").value(OLDER_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[0].familyRole").value(nullValue()))
            .andExpect(jsonPath("$.[1].fishLevel").value(SECOND_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[1].yearSession").value(secondYearSession))
            .andExpect(jsonPath("$.[1].familyRole").value(nullValue()))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(currentUser.getId().intValue())))
            .andExpect(jsonPath("$.[2].fishLevel").value(FIRST_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[2].yearSession").value(intakeYearSession))
            .andExpect(jsonPath("$.[2].familyRole").value(nullValue()));
    }

    @Test
    @Transactional
    public void getCurrentUserCCInfoProfile_ThirdYearWithFamilyRole() throws Exception {
        String currentYearSession = YearSessionUtils.getCurrentYearSession();
        String intakeYearSession = YearSessionUtils.addYearSessionWithSemester(currentYearSession, -4);
        String secondYearSession = YearSessionUtils.addYearSessionWithSemester(currentYearSession, -2);
        User currentUser = getCurrentUser();

        UserUniInfo userUniInfo = createUserUniInfoEntity();
        userUniInfo.setUserId(currentUser.getId());
        userUniInfo.setYearSession(intakeYearSession);
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);

        UserCCInfo userCCInfo = createEntity();
        userCCInfo.setUserId(currentUser.getId());
        userCCInfo.setYearSession(secondYearSession);
        UserCCInfo savedUserCCInfo = userCCInfoRepository.saveAndFlush(userCCInfo);

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(currentUser.getId().intValue())))
            .andExpect(jsonPath("$.[0].fishLevel").value(OLDER_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[0].familyRole").value(nullValue()))
            .andExpect(jsonPath("$.[1].fishLevel").value(SECOND_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[1].yearSession").value(secondYearSession))
            .andExpect(jsonPath("$.[1].familyRole").value(DEFAULT_FAMILY_ROLE.name()))
            .andExpect(jsonPath("$.[2].fishLevel").value(FIRST_YEAR_FISH_LEVEL))
            .andExpect(jsonPath("$.[2].yearSession").value(intakeYearSession))
            .andExpect(jsonPath("$.[2].familyRole").value(nullValue()));
    }

    @Test
    @Transactional
    public void getCurrentUserCCInfoProfile_WithoutUserUniInfo_ShouldReturnEmptyList() throws Exception {
        User currentUser = getCurrentUser();

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Transactional
    public void getCurrentUserCCRolesProfile_WithCCFamilyRole() throws Exception {
        User currentUser = getCurrentUser();

        UserCCInfo userCCInfo = createEntity();
        userCCInfo.setUserId(currentUser.getId());
        UserCCInfo savedUserCCInfo = userCCInfoRepository.saveAndFlush(userCCInfo);

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/roles/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").value(hasSize(1)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(currentUser.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(CCRoleType.FAMILY_ROLE.name())))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_FAMILY_ROLE.name())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(DEFAULT_YEAR_SESSION)));
    }

    @Test
    @Transactional
    public void getCurrentUserCCRolesProfile_WithCCEventRole() throws Exception {
        User currentUser = getCurrentUser();

        Event event = createEventEntity();
        Event savedEvent = eventRepository.saveAndFlush(event);
        EventCrew eventCrew = createEventCrewEntity();
        eventCrew.setEventId(savedEvent.getId());
        eventCrew.setUserId(currentUser.getId());
        EventCrew savedEventCrew = eventCrewRepository.saveAndFlush(eventCrew);
        String eventCrewYearSession = YearSessionUtils.toYearSession(savedEvent.getStartDate());

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/roles/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").value(hasSize(1)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(currentUser.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(CCRoleType.EVENT_CREW.name())))
            .andExpect(jsonPath("$.[*].eventId").value(hasItem(savedEvent.getId().intValue())))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_EVENT_CREW_ROLE.name())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(eventCrewYearSession)));
    }

    @Test
    @Transactional
    public void getCurrentUserCCRolesProfile_WithCCAdministratorRole() throws Exception {
        User currentUser = getCurrentUser();

        Administrator administrator = createAdministratorEntity();
        administrator.setUserId(currentUser.getId());
        Administrator savedAdministrator = administratorRepository.saveAndFlush(administrator);

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/roles/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").value(hasSize(1)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(currentUser.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(CCRoleType.CC_ADMINISTRATOR.name())))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ADMINISTRATOR_ROLE.name())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(DEFAULT_ADMINISTRATOR_YEAR_SESSION)));
    }

    @Test
    @Transactional
    public void getCurrentUserCCRolesProfile_WithAllCCRole() throws Exception {
        User currentUser = getCurrentUser();

        Event event = createEventEntity();
        Event savedEvent = eventRepository.saveAndFlush(event);
        EventCrew eventCrew = createEventCrewEntity();
        eventCrew.setEventId(savedEvent.getId());
        eventCrew.setUserId(currentUser.getId());
        EventCrew savedEventCrew = eventCrewRepository.saveAndFlush(eventCrew);
        String eventCrewYearSession = YearSessionUtils.toYearSession(savedEvent.getStartDate());

        UserCCInfo userCCInfo = createEntity();
        userCCInfo.setUserId(currentUser.getId());
        UserCCInfo savedUserCCInfo = userCCInfoRepository.saveAndFlush(userCCInfo);

        Administrator administrator = createAdministratorEntity();
        administrator.setUserId(currentUser.getId());
        Administrator savedAdministrator = administratorRepository.saveAndFlush(administrator);

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/roles/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").value(hasSize(3)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(currentUser.getId().intValue())))

            .andExpect(jsonPath("$.[*].type").value(hasItem(CCRoleType.FAMILY_ROLE.name())))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_FAMILY_ROLE.name())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(DEFAULT_YEAR_SESSION)))

            .andExpect(jsonPath("$.[*].type").value(hasItem(CCRoleType.EVENT_CREW.name())))
            .andExpect(jsonPath("$.[*].eventId").value(hasItem(savedEvent.getId().intValue())))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_EVENT_CREW_ROLE.name())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(eventCrewYearSession)))

            .andExpect(jsonPath("$.[*].type").value(hasItem(CCRoleType.CC_ADMINISTRATOR.name())))
            .andExpect(jsonPath("$.[*].role").value(hasItem(DEFAULT_ADMINISTRATOR_ROLE.name())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(DEFAULT_ADMINISTRATOR_YEAR_SESSION)));
    }

    @Test
    @Transactional
    public void getCurrentUserCCRolesProfile_WithoutCCRole_ShouldReturnEmptyList() throws Exception {
        User currentUser = getCurrentUser();

        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos/roles/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").value(hasSize(0)));
    }

    /**
     * Executes the search of cc members, and checks that the default entity is returned.
     */
    private void getCcMembersShouldBeFound(String filter) throws Exception {
        MvcResult res = restUserCCInfoMockMvc
            .perform(get(CC_MEMBER_API_URL + "?" + filter))
            .andReturn();
        System.out.println(userUniInfoRepository.findAll());
        System.out.println(userRepository.findAll());
        System.out.println(res.getResponse().getContentAsString());
        restUserCCInfoMockMvc
            .perform(get(CC_MEMBER_API_URL + "?" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userCCInfo.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].familyRole").value(hasItem(DEFAULT_FAMILY_ROLE.toString())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(DEFAULT_YEAR_SESSION)));
            // TODO: Solve userUniInfo is null
//            .andExpect(jsonPath("$.[*].userUniInfo.courseProgramId").value(hasItem(DEFAULT_COURSE_PROGRAM_ID)))
//            .andExpect(jsonPath("$.[*].userUniInfo.yearSession").value(hasItem(DEFAULT_UNI_INTAKE_YEAR_SESSION)))
            // TODO: Solve user is null
//            .andExpect(jsonPath("$.[*].user.firstName").value(hasItem(DEFAULT_USER_FIRST_NAME)))
//            .andExpect(jsonPath("$.[*].user.lastName").value(hasItem(DEFAULT_USER_LAST_NAME)));
    }

    /**
     * Executes the search Cc Members, and checks that the default entity is not returned.
     */
    private void getCcMembersShouldNotBeFound(String filter) throws Exception {
        restUserCCInfoMockMvc
            .perform(get(CC_MEMBER_API_URL + "?" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    void getAllCCMemberssByCourseProgramIdIsEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where courseProgramId equals to DEFAULT_COURSE_PROGRAM_ID
        getCcMembersShouldBeFound("courseProgramId.equals=" + DEFAULT_COURSE_PROGRAM_ID);

        // Get all the userCCInfoList where courseProgramId equals to UPDATED_COURSE_PROGRAM_ID
        getCcMembersShouldNotBeFound("courseProgramId.equals=" + UPDATED_COURSE_PROGRAM_ID);
    }

    @Test
    @Transactional
    void getAllCCMembersByCourseProgramIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where courseProgramId not equals to DEFAULT_COURSE_PROGRAM_ID
        getCcMembersShouldNotBeFound("courseProgramId.notEquals=" + DEFAULT_COURSE_PROGRAM_ID);

        // Get all the userCCInfoList where courseProgramId not equals to UPDATED_COURSE_PROGRAM_ID
        getCcMembersShouldBeFound("courseProgramId.notEquals=" + UPDATED_COURSE_PROGRAM_ID);
    }

    @Test
    @Transactional
    void getAllCCMembersByCourseProgramIdIsInShouldWork() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where courseProgramId in DEFAULT_COURSE_PROGRAM_ID or UPDATED_COURSE_PROGRAM_ID
        getCcMembersShouldBeFound("courseProgramId.in=" + DEFAULT_COURSE_PROGRAM_ID + "," + UPDATED_COURSE_PROGRAM_ID);

        // Get all the userCCInfoList where courseProgramId equals to UPDATED_USER_ID
        getCcMembersShouldNotBeFound("courseProgramId.in=" + UPDATED_COURSE_PROGRAM_ID);
    }

    @Test
    @Transactional
    void getAllCCMembersByCourseProgramIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where courseProgramId is not null
        getCcMembersShouldBeFound("courseProgramId.specified=true");

        // Get all the userCCInfoList where courseProgramId is null
        getCcMembersShouldNotBeFound("courseProgramId.specified=false");
    }

    @Test
    @Transactional
    void getAllCCMembersByIntakeYearSessionIsEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where intakeYearSession equals to DEFAULT_UNI_INTAKE_YEAR_SESSION
        getCcMembersShouldBeFound("intakeYearSession.equals=" + DEFAULT_UNI_INTAKE_YEAR_SESSION);

        // Get all the userCCInfoList where intakeYearSession equals to UPDATED_UNI_INTAKE_YEAR_SESSION
        getCcMembersShouldNotBeFound("intakeYearSession.equals=" + UPDATED_UNI_INTAKE_YEAR_SESSION);
    }

    @Test
    @Transactional
    void getAllCCMembersByIntakeYearSessionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where intakeYearSession not equals to DEFAULT_UNI_INTAKE_YEAR_SESSION
        getCcMembersShouldNotBeFound("intakeYearSession.notEquals=" + DEFAULT_UNI_INTAKE_YEAR_SESSION);

        // Get all the userCCInfoList where intakeYearSession not equals to UPDATE_UNI_INTAKE_YEAR_SESSION
        getCcMembersShouldBeFound("intakeYearSession.notEquals=" + UPDATED_UNI_INTAKE_YEAR_SESSION);
    }

    @Test
    @Transactional
    void getAllCCMembersByIntakeYearSessionIsInShouldWork() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where intakeYearSession in DEFAULT_UNI_INTAKE_YEAR_SESSION or UPDATED_UNI_INTAKE_YEAR_SESSION
        getCcMembersShouldBeFound("intakeYearSession.in=" + DEFAULT_UNI_INTAKE_YEAR_SESSION + "," + UPDATED_UNI_INTAKE_YEAR_SESSION);

        // Get all the userCCInfoList where intakeYearSession equals to UPDATED_UNI_INTAKE_YEAR_SESSION
        getCcMembersShouldNotBeFound("intakeYearSession.in=" + UPDATED_UNI_INTAKE_YEAR_SESSION);
    }

    @Test
    @Transactional
    void getAllCCMembersByIntakeYearSessionIsNullOrNotNull() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where intakeYearSession is not null
        getCcMembersShouldBeFound("intakeYearSession.specified=true");

        // Get all the userCCInfoList where intakeYearSession is null
        getCcMembersShouldNotBeFound("intakeYearSession.specified=false");
    }

    @Test
    @Transactional
    void getAllCCMembersByUserFirstNameIsEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where userFirstName equals to DEFAULT_USER_FIRST_NAME
        getCcMembersShouldBeFound("userFirstName.equals=" + DEFAULT_USER_FIRST_NAME);

        // Get all the userCCInfoList where userFirstName equals to UPDATED_USER_FIRST_NAME
        getCcMembersShouldNotBeFound("userFirstName.equals=" + UPDATED_USER_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllCCMembersByUserFirstNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where userFirstName not equals to DEFAULT_USER_FIRST_NAME
        getCcMembersShouldNotBeFound("userFirstName.notEquals=" + DEFAULT_USER_FIRST_NAME);

        // Get all the userCCInfoList where userFirstName not equals to UPDATED_USER_FIRST_NAME
        getCcMembersShouldBeFound("userFirstName.notEquals=" + UPDATED_USER_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllCCMembersByUserFirstNameIsInShouldWork() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where userFirstName in DEFAULT_USER_FIRST_NAME or UPDATED_USER_FIRST_NAME
        getCcMembersShouldBeFound("userFirstName.in=" + DEFAULT_USER_FIRST_NAME + "," + UPDATED_USER_FIRST_NAME);

        // Get all the userCCInfoList where userFirstName equals to UPDATE_USER_FIRST_NAME
        getCcMembersShouldNotBeFound("userFirstName.in=" + UPDATED_USER_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllCCMembersByUserFirstNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where userFirstName is not null
        getCcMembersShouldBeFound("userFirstName.specified=true");

        // Get all the userCCInfoList where userFirstName is null
        getCcMembersShouldNotBeFound("userFirstName.specified=false");
    }

    @Test
    @Transactional
    void getAllCCMembersByUserLastNameIsEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where userLastName equals to DEFAULT_USER_LAST_NAME
        getCcMembersShouldBeFound("userLastName.equals=" + DEFAULT_USER_LAST_NAME);

        // Get all the userCCInfoList where userLastName equals to UPDATED_USER_LAST_NAME
        getCcMembersShouldNotBeFound("userLastName.equals=" + UPDATED_USER_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllCCMembersByUserLastNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where userLastName not equals to DEFAULT_USER_LAST_NAME
        getCcMembersShouldNotBeFound("userLastName.notEquals=" + DEFAULT_USER_LAST_NAME);

        // Get all the userCCInfoList where userLastName not equals to UPDATED_USER_LAST_NAME
        getCcMembersShouldBeFound("userLastName.notEquals=" + UPDATED_USER_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllCCMembersByUserLastNameIsInShouldWork() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where userLastName in DEFAULT_USER_LAST_NAME or UPDATED_USER_LAST_NAME
        getCcMembersShouldBeFound("userLastName.in=" + DEFAULT_USER_LAST_NAME + "," + UPDATED_USER_LAST_NAME);

        // Get all the userCCInfoList where userLastName equals to UPDATED_USER_LAST_NAME
        getCcMembersShouldNotBeFound("userLastName.in=" + UPDATED_USER_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllCCMembersByUserLastNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);
        UserUniInfo userUniInfo = createUserUniInfoEntity();
        UserUniInfo savedUserUniInfo = userUniInfoRepository.saveAndFlush(userUniInfo);
        saveUserIfNotExist(DEFAULT_USER_ID);

        // Get all the userCCInfoList where userLastName is not null
        getCcMembersShouldBeFound("userLastName.specified=true");

        // Get all the userCCInfoList where userLastName is null
        getCcMembersShouldNotBeFound("userLastName.specified=false");
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserCCInfo.class);
        UserCCInfo userCCInfo1 = new UserCCInfo();
        userCCInfo1.setId(1L);
        UserCCInfo userCCInfo2 = new UserCCInfo();
        userCCInfo2.setId(userCCInfo1.getId());
        assertThat(userCCInfo1).isEqualTo(userCCInfo2);
        userCCInfo2.setId(2L);
        assertThat(userCCInfo1).isNotEqualTo(userCCInfo2);
        userCCInfo1.setId(null);
        assertThat(userCCInfo1).isNotEqualTo(userCCInfo2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserCCInfoDTO.class);
        UserCCInfoDTO userCCInfoDTO1 = new UserCCInfoDTO();
        userCCInfoDTO1.setId(1L);
        UserCCInfoDTO userCCInfoDTO2 = new UserCCInfoDTO();
        assertThat(userCCInfoDTO1).isNotEqualTo(userCCInfoDTO2);
        userCCInfoDTO2.setId(userCCInfoDTO1.getId());
        assertThat(userCCInfoDTO1).isEqualTo(userCCInfoDTO2);
        userCCInfoDTO2.setId(2L);
        assertThat(userCCInfoDTO1).isNotEqualTo(userCCInfoDTO2);
        userCCInfoDTO1.setId(null);
        assertThat(userCCInfoDTO1).isNotEqualTo(userCCInfoDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(userCCInfoMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(userCCInfoMapper.fromId(null)).isNull();
    }

    private User getCurrentUser() {
        return SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneWithAuthoritiesByLogin)
            .orElseThrow(() -> new BadRequestException("Cannot find user"));
    }

    private User saveUserIfNotExist(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            user = createUserEntity();
        }
        user.setFirstName(DEFAULT_USER_FIRST_NAME);
        user.setLastName(DEFAULT_USER_LAST_NAME);
        return userRepository.saveAndFlush(user);
    }
}
