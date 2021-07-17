package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.ClubmanagementApp;
import com.thirdcc.webapp.annotations.authorization.WithNormalUser;
import com.thirdcc.webapp.annotations.init.InitYearSession;
import com.thirdcc.webapp.domain.User;
import com.thirdcc.webapp.domain.UserCCInfo;
import com.thirdcc.webapp.domain.UserUniInfo;
import com.thirdcc.webapp.domain.enumeration.ClubFamilyCode;
import com.thirdcc.webapp.domain.enumeration.ClubFamilyRole;
import com.thirdcc.webapp.exception.BadRequestException;
import com.thirdcc.webapp.repository.UserCCInfoRepository;
import com.thirdcc.webapp.repository.UserRepository;
import com.thirdcc.webapp.repository.UserUniInfoRepository;
import com.thirdcc.webapp.security.SecurityUtils;
import com.thirdcc.webapp.service.dto.UserUniInfoDTO;
import com.thirdcc.webapp.service.mapper.UserUniInfoMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.thirdcc.webapp.domain.enumeration.UserUniStatus;
/**
 * Integration tests for the {@Link UserUniInfoResource} REST controller.
 */
@SpringBootTest(classes = ClubmanagementApp.class)
@AutoConfigureMockMvc
@InitYearSession
@WithNormalUser
public class UserUniInfoResourceIT {

    private static final String ENTITY_API_URL = "/api/user-uni-infos";

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long SMALLER_USER_ID = DEFAULT_USER_ID - 1L;
    private static final Long UPDATED_USER_ID = 2L;

    private static final Long DEFAULT_COURSE_PROGRAM_ID = 1L;
    private static final Long SMALLER_COURSE_PROGRAM_ID = DEFAULT_COURSE_PROGRAM_ID - 1L;
    private static final Long UPDATED_COURSE_PROGRAM_ID = 1L;

    private static final String DEFAULT_PROGRAM = "DEFAULT_PROGRAM";
    private static final String UPDATED_PROGRAM = "UPDATED_PROGRAM";

    private static final String DEFAULT_YEAR_SESSION = "2020/2021";
    private static final String UPDATED_YEAR_SESSION = "2018/2019";

    private static final Integer DEFAULT_INTAKE_SEMESTER = 1;
    private static final Integer SMALLER_INTAKE_SEMESTER = DEFAULT_INTAKE_SEMESTER - 1;
    private static final Integer UPDATED_INTAKE_SEMESTER = 2;

    private static final BigDecimal DEFAULT_YEAR_OF_STUDY = new BigDecimal(1);
    private static final BigDecimal SMALLER_YEAR_OF_STUDY = DEFAULT_YEAR_OF_STUDY.subtract(BigDecimal.ONE);
    private static final BigDecimal UPDATED_YEAR_OF_STUDY = new BigDecimal(2);

    private static final String DEFAULT_STAY_IN = "DEFAULT_STAY_IN";
    private static final String UPDATED_STAY_IN = "UPDATED_STAY_IN";

    private static final UserUniStatus DEFAULT_STATUS = UserUniStatus.GRADUATED;
    private static final UserUniStatus UPDATED_STATUS = UserUniStatus.STUDYING;

    private static final ClubFamilyCode DEFAULT_USER_CC_CLUB_FAMILY_CODE = ClubFamilyCode.JIN_LONG;
    private static final ClubFamilyRole DEFAULT_USER_CC_FAMILY_ROLE = ClubFamilyRole.FATHER;
    private static final String DEFAULT_USER_CC_YEAR_SESSION = "2019/2020";

    @Autowired
    private UserUniInfoRepository userUniInfoRepository;

    @Autowired
    private UserUniInfoMapper userUniInfoMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCCInfoRepository userCCInfoRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserUniInfoMockMvc;

    private UserUniInfo userUniInfo;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void cleanUp() {
        userUniInfoRepository.deleteAll();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserUniInfo createEntity(EntityManager em) {
        UserUniInfo userUniInfo = new UserUniInfo();
        userUniInfo.setUserId(DEFAULT_USER_ID);
        userUniInfo.setCourseProgramId(DEFAULT_COURSE_PROGRAM_ID);
        userUniInfo.setYearSession(DEFAULT_YEAR_SESSION);
        userUniInfo.setIntakeSemester(DEFAULT_INTAKE_SEMESTER);
        userUniInfo.setStayIn(DEFAULT_STAY_IN);
        userUniInfo.setStatus(DEFAULT_STATUS);
        return userUniInfo;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserUniInfo createUpdatedEntity(EntityManager em) {
        UserUniInfo userUniInfo = new UserUniInfo();
        userUniInfo.setUserId(UPDATED_USER_ID);
        userUniInfo.setCourseProgramId(UPDATED_COURSE_PROGRAM_ID);
        userUniInfo.setYearSession(UPDATED_YEAR_SESSION);
        userUniInfo.setIntakeSemester(UPDATED_INTAKE_SEMESTER);
        userUniInfo.setStayIn(UPDATED_STAY_IN);
        userUniInfo.setStatus(UPDATED_STATUS);
        return userUniInfo;
    }

    public static UserCCInfo createUserCCInfoEntity() {
        return new UserCCInfo()
            .userId(DEFAULT_USER_ID)
            .clubFamilyCode(DEFAULT_USER_CC_CLUB_FAMILY_CODE)
            .familyRole(DEFAULT_USER_CC_FAMILY_ROLE)
            .yearSession(DEFAULT_USER_CC_YEAR_SESSION);
    }

    @BeforeEach
    public void initTest() {
        userUniInfo = createEntity(em);
    }

    @Test
    @WithNormalUser
    public void createUserUniInfo() throws Exception {
        User currentUser = getCurrentUser();
        int databaseSizeBeforeCreate = userUniInfoRepository.findAll().size();

        // Create the UserUniInfo
        UserUniInfoDTO userUniInfoDTO = userUniInfoMapper.toDto(userUniInfo);
        restUserUniInfoMockMvc.perform(post("/api/user-uni-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userUniInfoDTO)))
            .andExpect(status().isCreated());

        // Validate the UserUniInfo in the database
        List<UserUniInfo> userUniInfoList = userUniInfoRepository.findAll();
        assertThat(userUniInfoList).hasSize(databaseSizeBeforeCreate + 1);
        UserUniInfo testUserUniInfo = userUniInfoList.get(userUniInfoList.size() - 1);
        assertThat(testUserUniInfo.getUserId()).isEqualTo(currentUser.getId());
        assertThat(testUserUniInfo.getCourseProgramId()).isEqualTo(DEFAULT_COURSE_PROGRAM_ID);
        assertThat(testUserUniInfo.getYearSession()).isEqualTo(DEFAULT_YEAR_SESSION);
        assertThat(testUserUniInfo.getIntakeSemester()).isEqualTo(DEFAULT_INTAKE_SEMESTER);
        assertThat(testUserUniInfo.getStayIn()).isEqualTo(DEFAULT_STAY_IN);
        assertThat(testUserUniInfo.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @WithNormalUser
    public void createUserUniInfoWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userUniInfoRepository.findAll().size();

        // Create the UserUniInfo with an existing ID
        userUniInfo.setId(1L);
        UserUniInfoDTO userUniInfoDTO = userUniInfoMapper.toDto(userUniInfo);

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserUniInfoMockMvc.perform(post("/api/user-uni-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userUniInfoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the UserUniInfo in the database
        List<UserUniInfo> userUniInfoList = userUniInfoRepository.findAll();
        assertThat(userUniInfoList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @WithNormalUser
    public void getAllUserUniInfos() throws Exception {
        // Initialize the database
        UserUniInfo savedUserUniInfo = initUserUniInfoDB();
        User currentUser = getCurrentUser();
        // Get all the userUniInfoList
        restUserUniInfoMockMvc.perform(get("/api/user-uni-infos?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(savedUserUniInfo.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(currentUser.getId().intValue())))
            .andExpect(jsonPath("$.[*].courseProgramId").value(hasItem(DEFAULT_COURSE_PROGRAM_ID.intValue())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(DEFAULT_YEAR_SESSION.toString())))
            .andExpect(jsonPath("$.[*].intakeSemester").value(hasItem(DEFAULT_INTAKE_SEMESTER)))
            .andExpect(jsonPath("$.[*].stayIn").value(hasItem(DEFAULT_STAY_IN.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }


    @Test
    void getUserUniInfosByIdFiltering() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        Long id = userUniInfo.getId();

        defaultUserUniInfoShouldBeFound("id.equals=" + id);
        defaultUserUniInfoShouldNotBeFound("id.notEquals=" + id);

        defaultUserUniInfoShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultUserUniInfoShouldNotBeFound("id.greaterThan=" + id);

        defaultUserUniInfoShouldBeFound("id.lessThanOrEqual=" + id);
        defaultUserUniInfoShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    void getAllUserUniInfosByUserIdIsEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where userId equals to DEFAULT_USER_ID
        defaultUserUniInfoShouldBeFound("userId.equals=" + DEFAULT_USER_ID);

        // Get all the userUniInfoList where userId equals to UPDATED_USER_ID
        defaultUserUniInfoShouldNotBeFound("userId.equals=" + UPDATED_USER_ID);
    }

    @Test
    void getAllUserUniInfosByUserIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where userId not equals to DEFAULT_USER_ID
        defaultUserUniInfoShouldNotBeFound("userId.notEquals=" + DEFAULT_USER_ID);

        // Get all the userUniInfoList where userId not equals to UPDATED_USER_ID
        defaultUserUniInfoShouldBeFound("userId.notEquals=" + UPDATED_USER_ID);
    }

    @Test
    void getAllUserUniInfosByUserIdIsInShouldWork() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where userId in DEFAULT_USER_ID or UPDATED_USER_ID
        defaultUserUniInfoShouldBeFound("userId.in=" + DEFAULT_USER_ID + "," + UPDATED_USER_ID);

        // Get all the userUniInfoList where userId equals to UPDATED_USER_ID
        defaultUserUniInfoShouldNotBeFound("userId.in=" + UPDATED_USER_ID);
    }

    @Test
    void getAllUserUniInfosByUserIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where userId is not null
        defaultUserUniInfoShouldBeFound("userId.specified=true");

        // Get all the userUniInfoList where userId is null
        defaultUserUniInfoShouldNotBeFound("userId.specified=false");
    }

    @Test
    void getAllUserUniInfosByUserIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where userId is greater than or equal to DEFAULT_USER_ID
        defaultUserUniInfoShouldBeFound("userId.greaterThanOrEqual=" + DEFAULT_USER_ID);

        // Get all the userUniInfoList where userId is greater than or equal to UPDATED_USER_ID
        defaultUserUniInfoShouldNotBeFound("userId.greaterThanOrEqual=" + UPDATED_USER_ID);
    }

    @Test
    void getAllUserUniInfosByUserIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where userId is less than or equal to DEFAULT_USER_ID
        defaultUserUniInfoShouldBeFound("userId.lessThanOrEqual=" + DEFAULT_USER_ID);

        // Get all the userUniInfoList where userId is less than or equal to SMALLER_USER_ID
        defaultUserUniInfoShouldNotBeFound("userId.lessThanOrEqual=" + SMALLER_USER_ID);
    }

    @Test
    void getAllUserUniInfosByUserIdIsLessThanSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where userId is less than DEFAULT_USER_ID
        defaultUserUniInfoShouldNotBeFound("userId.lessThan=" + DEFAULT_USER_ID);

        // Get all the userUniInfoList where userId is less than UPDATED_USER_ID
        defaultUserUniInfoShouldBeFound("userId.lessThan=" + UPDATED_USER_ID);
    }

    @Test
    void getAllUserUniInfosByUserIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where userId is greater than DEFAULT_USER_ID
        defaultUserUniInfoShouldNotBeFound("userId.greaterThan=" + DEFAULT_USER_ID);

        // Get all the userUniInfoList where userId is greater than SMALLER_USER_ID
        defaultUserUniInfoShouldBeFound("userId.greaterThan=" + SMALLER_USER_ID);
    }

    @Test
    void getAllUserUniInfosByYearSessionIsEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where yearSession equals to DEFAULT_YEAR_SESSION
        defaultUserUniInfoShouldBeFound("yearSession.equals=" + DEFAULT_YEAR_SESSION);

        // Get all the userUniInfoList where yearSession equals to UPDATED_YEAR_SESSION
        defaultUserUniInfoShouldNotBeFound("yearSession.equals=" + UPDATED_YEAR_SESSION);
    }

    @Test
    void getAllUserUniInfosByYearSessionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where yearSession not equals to DEFAULT_YEAR_SESSION
        defaultUserUniInfoShouldNotBeFound("yearSession.notEquals=" + DEFAULT_YEAR_SESSION);

        // Get all the userUniInfoList where yearSession not equals to UPDATED_YEAR_SESSION
        defaultUserUniInfoShouldBeFound("yearSession.notEquals=" + UPDATED_YEAR_SESSION);
    }

    @Test
    void getAllUserUniInfosByYearSessionIsInShouldWork() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where yearSession in DEFAULT_YEAR_SESSION or UPDATED_YEAR_SESSION
        defaultUserUniInfoShouldBeFound("yearSession.in=" + DEFAULT_YEAR_SESSION + "," + UPDATED_YEAR_SESSION);

        // Get all the userUniInfoList where yearSession equals to UPDATED_YEAR_SESSION
        defaultUserUniInfoShouldNotBeFound("yearSession.in=" + UPDATED_YEAR_SESSION);
    }

    @Test
    void getAllUserUniInfosByYearSessionIsNullOrNotNull() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where yearSession is not null
        defaultUserUniInfoShouldBeFound("yearSession.specified=true");

        // Get all the userUniInfoList where yearSession is null
        defaultUserUniInfoShouldNotBeFound("yearSession.specified=false");
    }

    @Test
    void getAllUserUniInfosByYearSessionContainsSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where yearSession contains DEFAULT_YEAR_SESSION
        defaultUserUniInfoShouldBeFound("yearSession.contains=" + DEFAULT_YEAR_SESSION);

        // Get all the userUniInfoList where yearSession contains UPDATED_YEAR_SESSION
        defaultUserUniInfoShouldNotBeFound("yearSession.contains=" + UPDATED_YEAR_SESSION);
    }

    @Test
    void getAllUserUniInfosByYearSessionNotContainsSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where yearSession does not contain DEFAULT_YEAR_SESSION
        defaultUserUniInfoShouldNotBeFound("yearSession.doesNotContain=" + DEFAULT_YEAR_SESSION);

        // Get all the userUniInfoList where yearSession does not contain UPDATED_YEAR_SESSION
        defaultUserUniInfoShouldBeFound("yearSession.doesNotContain=" + UPDATED_YEAR_SESSION);
    }

    @Test
    void getAllUserUniInfosByIntakeSemesterIsEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where intakeSemester equals to DEFAULT_INTAKE_SEMESTER
        defaultUserUniInfoShouldBeFound("intakeSemester.equals=" + DEFAULT_INTAKE_SEMESTER);

        // Get all the userUniInfoList where intakeSemester equals to UPDATED_INTAKE_SEMESTER
        defaultUserUniInfoShouldNotBeFound("intakeSemester.equals=" + UPDATED_INTAKE_SEMESTER);
    }

    @Test
    void getAllUserUniInfosByIntakeSemesterIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where intakeSemester not equals to DEFAULT_INTAKE_SEMESTER
        defaultUserUniInfoShouldNotBeFound("intakeSemester.notEquals=" + DEFAULT_INTAKE_SEMESTER);

        // Get all the userUniInfoList where intakeSemester not equals to UPDATED_INTAKE_SEMESTER
        defaultUserUniInfoShouldBeFound("intakeSemester.notEquals=" + UPDATED_INTAKE_SEMESTER);
    }

    @Test
    void getAllUserUniInfosByIntakeSemesterIsInShouldWork() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where intakeSemester in DEFAULT_INTAKE_SEMESTER or UPDATED_INTAKE_SEMESTER
        defaultUserUniInfoShouldBeFound("intakeSemester.in=" + DEFAULT_INTAKE_SEMESTER + "," + UPDATED_INTAKE_SEMESTER);

        // Get all the userUniInfoList where intakeSemester equals to UPDATED_INTAKE_SEMESTER
        defaultUserUniInfoShouldNotBeFound("intakeSemester.in=" + UPDATED_INTAKE_SEMESTER);
    }

    @Test
    void getAllUserUniInfosByIntakeSemesterIsNullOrNotNull() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where intakeSemester is not null
        defaultUserUniInfoShouldBeFound("intakeSemester.specified=true");

        // Get all the userUniInfoList where intakeSemester is null
        defaultUserUniInfoShouldNotBeFound("intakeSemester.specified=false");
    }

    @Test
    void getAllUserUniInfosByIntakeSemesterIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where intakeSemester is greater than or equal to DEFAULT_INTAKE_SEMESTER
        defaultUserUniInfoShouldBeFound("intakeSemester.greaterThanOrEqual=" + DEFAULT_INTAKE_SEMESTER);

        // Get all the userUniInfoList where intakeSemester is greater than or equal to UPDATED_INTAKE_SEMESTER
        defaultUserUniInfoShouldNotBeFound("intakeSemester.greaterThanOrEqual=" + UPDATED_INTAKE_SEMESTER);
    }

    @Test
    void getAllUserUniInfosByIntakeSemesterIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where intakeSemester is less than or equal to DEFAULT_INTAKE_SEMESTER
        defaultUserUniInfoShouldBeFound("intakeSemester.lessThanOrEqual=" + DEFAULT_INTAKE_SEMESTER);

        // Get all the userUniInfoList where intakeSemester is less than or equal to SMALLER_INTAKE_SEMESTER
        defaultUserUniInfoShouldNotBeFound("intakeSemester.lessThanOrEqual=" + SMALLER_INTAKE_SEMESTER);
    }

    @Test
    void getAllUserUniInfosByIntakeSemesterIsLessThanSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where intakeSemester is less than DEFAULT_INTAKE_SEMESTER
        defaultUserUniInfoShouldNotBeFound("intakeSemester.lessThan=" + DEFAULT_INTAKE_SEMESTER);

        // Get all the userUniInfoList where intakeSemester is less than UPDATED_INTAKE_SEMESTER
        defaultUserUniInfoShouldBeFound("intakeSemester.lessThan=" + UPDATED_INTAKE_SEMESTER);
    }

    @Test
    void getAllUserUniInfosByIntakeSemesterIsGreaterThanSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where intakeSemester is greater than DEFAULT_INTAKE_SEMESTER
        defaultUserUniInfoShouldNotBeFound("intakeSemester.greaterThan=" + DEFAULT_INTAKE_SEMESTER);

        // Get all the userUniInfoList where intakeSemester is greater than SMALLER_INTAKE_SEMESTER
        defaultUserUniInfoShouldBeFound("intakeSemester.greaterThan=" + SMALLER_INTAKE_SEMESTER);
    }

    @Test
    void getAllUserUniInfosByStayInIsEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where stayIn equals to DEFAULT_STAY_IN
        defaultUserUniInfoShouldBeFound("stayIn.equals=" + DEFAULT_STAY_IN);

        // Get all the userUniInfoList where stayIn equals to UPDATED_STAY_IN
        defaultUserUniInfoShouldNotBeFound("stayIn.equals=" + UPDATED_STAY_IN);
    }

    @Test
    void getAllUserUniInfosByStayInIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where stayIn not equals to DEFAULT_STAY_IN
        defaultUserUniInfoShouldNotBeFound("stayIn.notEquals=" + DEFAULT_STAY_IN);

        // Get all the userUniInfoList where stayIn not equals to UPDATED_STAY_IN
        defaultUserUniInfoShouldBeFound("stayIn.notEquals=" + UPDATED_STAY_IN);
    }

    @Test
    void getAllUserUniInfosByStayInIsInShouldWork() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where stayIn in DEFAULT_STAY_IN or UPDATED_STAY_IN
        defaultUserUniInfoShouldBeFound("stayIn.in=" + DEFAULT_STAY_IN + "," + UPDATED_STAY_IN);

        // Get all the userUniInfoList where stayIn equals to UPDATED_STAY_IN
        defaultUserUniInfoShouldNotBeFound("stayIn.in=" + UPDATED_STAY_IN);
    }

    @Test
    void getAllUserUniInfosByStayInIsNullOrNotNull() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where stayIn is not null
        defaultUserUniInfoShouldBeFound("stayIn.specified=true");

        // Get all the userUniInfoList where stayIn is null
        defaultUserUniInfoShouldNotBeFound("stayIn.specified=false");
    }

    @Test
    void getAllUserUniInfosByStayInContainsSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where stayIn contains DEFAULT_STAY_IN
        defaultUserUniInfoShouldBeFound("stayIn.contains=" + DEFAULT_STAY_IN);

        // Get all the userUniInfoList where stayIn contains UPDATED_STAY_IN
        defaultUserUniInfoShouldNotBeFound("stayIn.contains=" + UPDATED_STAY_IN);
    }

    @Test
    void getAllUserUniInfosByStayInNotContainsSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where stayIn does not contain DEFAULT_STAY_IN
        defaultUserUniInfoShouldNotBeFound("stayIn.doesNotContain=" + DEFAULT_STAY_IN);

        // Get all the userUniInfoList where stayIn does not contain UPDATED_STAY_IN
        defaultUserUniInfoShouldBeFound("stayIn.doesNotContain=" + UPDATED_STAY_IN);
    }

    @Test
    void getAllUserUniInfosByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where status equals to DEFAULT_STATUS
        defaultUserUniInfoShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the userUniInfoList where status equals to UPDATED_STATUS
        defaultUserUniInfoShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    void getAllUserUniInfosByStatusIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where status not equals to DEFAULT_STATUS
        defaultUserUniInfoShouldNotBeFound("status.notEquals=" + DEFAULT_STATUS);

        // Get all the userUniInfoList where status not equals to UPDATED_STATUS
        defaultUserUniInfoShouldBeFound("status.notEquals=" + UPDATED_STATUS);
    }

    @Test
    void getAllUserUniInfosByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultUserUniInfoShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the userUniInfoList where status equals to UPDATED_STATUS
        defaultUserUniInfoShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    void getAllUserUniInfosByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        userUniInfoRepository.saveAndFlush(userUniInfo);

        // Get all the userUniInfoList where status is not null
        defaultUserUniInfoShouldBeFound("status.specified=true");

        // Get all the userUniInfoList where status is null
        defaultUserUniInfoShouldNotBeFound("status.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultUserUniInfoShouldBeFound(String filter) throws Exception {
        restUserUniInfoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userUniInfo.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].courseProgramId").value(hasItem(DEFAULT_COURSE_PROGRAM_ID.intValue())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(DEFAULT_YEAR_SESSION)))
            .andExpect(jsonPath("$.[*].intakeSemester").value(hasItem(DEFAULT_INTAKE_SEMESTER)))
            .andExpect(jsonPath("$.[*].stayIn").value(hasItem(DEFAULT_STAY_IN)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));

        // Check, that the count call also returns 1
        restUserUniInfoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultUserUniInfoShouldNotBeFound(String filter) throws Exception {
        restUserUniInfoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restUserUniInfoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(content().string("0"));
    }


    @Test
    @WithNormalUser
    public void getUserUniInfo() throws Exception {
        // Initialize the database
        User currentUser = getCurrentUser();
        UserUniInfo savedUserUniInfo = initUserUniInfoDB();
        // Get the userUniInfo
        restUserUniInfoMockMvc.perform(get("/api/user-uni-infos/{id}", savedUserUniInfo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(savedUserUniInfo.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(currentUser.getId().intValue()))
            .andExpect(jsonPath("$.courseProgramId").value(DEFAULT_COURSE_PROGRAM_ID.intValue()))
            .andExpect(jsonPath("$.yearSession").value(DEFAULT_YEAR_SESSION.toString()))
            .andExpect(jsonPath("$.intakeSemester").value(DEFAULT_INTAKE_SEMESTER))
            .andExpect(jsonPath("$.stayIn").value(DEFAULT_STAY_IN.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @WithNormalUser
    public void getNonExistingUserUniInfo() throws Exception {
        // Get the userUniInfo
        restUserUniInfoMockMvc.perform(get("/api/user-uni-infos/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithNormalUser
    public void getCurrentUserDetailsWithUniInfo() throws Exception {
        User user = getCurrentUser();
        UserUniInfo userUniInfo = createEntity(em);
        userUniInfo.setUserId(user.getId());
        userUniInfoRepository.save(userUniInfo);

        UserCCInfo userCCInfo = createUserCCInfoEntity();
        userCCInfo.setUserId(user.getId());
        UserCCInfo savedUserCCInfo = userCCInfoRepository.saveAndFlush(userCCInfo);

        restUserUniInfoMockMvc.perform(get("/api/user-uni-infos/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(userUniInfo.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(user.getId().intValue()))
            .andExpect(jsonPath("$.courseProgramId").value(DEFAULT_COURSE_PROGRAM_ID.intValue()))
            .andExpect(jsonPath("$.yearSession").value(DEFAULT_YEAR_SESSION))
            .andExpect(jsonPath("$.intakeSemester").value(DEFAULT_INTAKE_SEMESTER))
            .andExpect(jsonPath("$.stayIn").value(DEFAULT_STAY_IN))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
            .andExpect(jsonPath("$.lastName").value(user.getLastName()))
            .andExpect(jsonPath("$.email").value(user.getEmail()))
            .andExpect(jsonPath("$.gender").value(user.getGender()))
            .andExpect(jsonPath("$.dateOfBirth").value(user.getDateOfBirth()))
            .andExpect(jsonPath("$.phoneNumber").value(user.getPhoneNumber()))
            .andExpect(jsonPath("$.imageUrl").value(user.getImageUrl()));
    }

    @Test
    @WithNormalUser
    public void getCurrentUserDetailsWithUniInfo_UserUniInfoNotExist() throws Exception {
        User user = getCurrentUser();

        restUserUniInfoMockMvc.perform(get("/api/user-uni-infos/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.userId").value(user.getId().intValue()))
            .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
            .andExpect(jsonPath("$.lastName").value(user.getLastName()))
            .andExpect(jsonPath("$.email").value(user.getEmail()))
            .andExpect(jsonPath("$.gender").value(user.getGender()))
            .andExpect(jsonPath("$.dateOfBirth").value(user.getDateOfBirth()))
            .andExpect(jsonPath("$.phoneNumber").value(user.getPhoneNumber()))
            .andExpect(jsonPath("$.imageUrl").value(user.getImageUrl()));
    }

    @Test
    @WithNormalUser
    public void updateUserUniInfo() throws Exception {
        User currentUser = getCurrentUser();
        // Initialize the database
        UserUniInfo savedUserUniInfo = initUserUniInfoDB();

        int databaseSizeBeforeUpdate = userUniInfoRepository.findAll().size();

        // Update the userUniInfo
        UserUniInfo updatedUserUniInfo = new UserUniInfo();
        updatedUserUniInfo.setId(savedUserUniInfo.getId());
        updatedUserUniInfo.setUserId(UPDATED_USER_ID);
        updatedUserUniInfo.setCourseProgramId(UPDATED_COURSE_PROGRAM_ID);
        updatedUserUniInfo.setYearSession(UPDATED_YEAR_SESSION);
        updatedUserUniInfo.setIntakeSemester(UPDATED_INTAKE_SEMESTER);
        updatedUserUniInfo.setStayIn(UPDATED_STAY_IN);
        updatedUserUniInfo.setStatus(UPDATED_STATUS);
        UserUniInfoDTO userUniInfoDTO = userUniInfoMapper.toDto(updatedUserUniInfo);

        restUserUniInfoMockMvc.perform(put("/api/user-uni-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userUniInfoDTO)))
            .andExpect(status().isOk());

        // Validate the UserUniInfo in the database
        List<UserUniInfo> userUniInfoList = userUniInfoRepository.findAll();
        assertThat(userUniInfoList).hasSize(databaseSizeBeforeUpdate);
        UserUniInfo testUserUniInfo = userUniInfoList.get(userUniInfoList.size() - 1);
        assertThat(testUserUniInfo.getUserId()).isEqualTo(currentUser.getId());
        assertThat(testUserUniInfo.getCourseProgramId()).isEqualTo(UPDATED_COURSE_PROGRAM_ID);
        assertThat(testUserUniInfo.getYearSession()).isEqualTo(UPDATED_YEAR_SESSION);
        assertThat(testUserUniInfo.getIntakeSemester()).isEqualTo(UPDATED_INTAKE_SEMESTER);
        assertThat(testUserUniInfo.getStayIn()).isEqualTo(UPDATED_STAY_IN);
        assertThat(testUserUniInfo.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @WithNormalUser
    public void updateNonExistingUserUniInfo() throws Exception {
        int databaseSizeBeforeUpdate = userUniInfoRepository.findAll().size();

        // Create the UserUniInfo
        UserUniInfoDTO userUniInfoDTO = userUniInfoMapper.toDto(userUniInfo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserUniInfoMockMvc.perform(put("/api/user-uni-infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userUniInfoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the UserUniInfo in the database
        List<UserUniInfo> userUniInfoList = userUniInfoRepository.findAll();
        assertThat(userUniInfoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @WithNormalUser
    public void deleteUserUniInfo() throws Exception {
        // Initialize the database
        UserUniInfo savedUserUniInfo = initUserUniInfoDB();

        int databaseSizeBeforeDelete = userUniInfoRepository.findAll().size();

        // Delete the userUniInfo
        restUserUniInfoMockMvc.perform(delete("/api/user-uni-infos/{id}", savedUserUniInfo.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<UserUniInfo> userUniInfoList = userUniInfoRepository.findAll();
        assertThat(userUniInfoList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserUniInfo.class);
        UserUniInfo userUniInfo1 = new UserUniInfo();
        userUniInfo1.setId(1L);
        UserUniInfo userUniInfo2 = new UserUniInfo();
        userUniInfo2.setId(userUniInfo1.getId());
        assertThat(userUniInfo1).isEqualTo(userUniInfo2);
        userUniInfo2.setId(2L);
        assertThat(userUniInfo1).isNotEqualTo(userUniInfo2);
        userUniInfo1.setId(null);
        assertThat(userUniInfo1).isNotEqualTo(userUniInfo2);
    }

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserUniInfoDTO.class);
        UserUniInfoDTO userUniInfoDTO1 = new UserUniInfoDTO();
        userUniInfoDTO1.setId(1L);
        UserUniInfoDTO userUniInfoDTO2 = new UserUniInfoDTO();
        assertThat(userUniInfoDTO1).isNotEqualTo(userUniInfoDTO2);
        userUniInfoDTO2.setId(userUniInfoDTO1.getId());
        assertThat(userUniInfoDTO1).isEqualTo(userUniInfoDTO2);
        userUniInfoDTO2.setId(2L);
        assertThat(userUniInfoDTO1).isNotEqualTo(userUniInfoDTO2);
        userUniInfoDTO1.setId(null);
        assertThat(userUniInfoDTO1).isNotEqualTo(userUniInfoDTO2);
    }

    @Test
    public void testEntityFromId() {
        assertThat(userUniInfoMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(userUniInfoMapper.fromId(null)).isNull();
    }

    private User getCurrentUser() {
        return SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneWithAuthoritiesByLogin)
            .orElseThrow(() -> new BadRequestException("Cannot find user"));
    }

    private UserUniInfo initUserUniInfoDB() {
        User currentUser = getCurrentUser();
        UserUniInfo userUniInfo = createEntity(em);
        userUniInfo.setUserId(currentUser.getId());
        return userUniInfoRepository.saveAndFlush(userUniInfo);
    }
}
