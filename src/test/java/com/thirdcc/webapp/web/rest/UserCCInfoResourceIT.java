package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.ClubmanagementApp;
import com.thirdcc.webapp.annotations.init.InitYearSession;
import com.thirdcc.webapp.domain.*;
import com.thirdcc.webapp.domain.enumeration.*;
import com.thirdcc.webapp.exception.BadRequestException;
import com.thirdcc.webapp.repository.*;
import com.thirdcc.webapp.security.SecurityUtils;
import com.thirdcc.webapp.service.UserCCInfoService;
import com.thirdcc.webapp.service.dto.UserCCInfoDTO;
import com.thirdcc.webapp.service.mapper.UserCCInfoMapper;

import com.thirdcc.webapp.utils.YearSessionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@Link UserCCInfoResource} REST controller.
 */
@SpringBootTest(classes = ClubmanagementApp.class)
@AutoConfigureMockMvc
@WithMockUser(value = "user")
@InitYearSession
public class UserCCInfoResourceIT {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long UPDATED_USER_ID = 2L;

    private static final Long DEFAULT_CLUB_FAMILY_ID = 1L;
    private static final Long UPDATED_CLUB_FAMILY_ID = 2L;

    private static final ClubFamilyRole DEFAULT_FAMILY_ROLE = ClubFamilyRole.FATHER;
    private static final ClubFamilyRole UPDATED_FAMILY_ROLE = ClubFamilyRole.MOTHER;

    private static final String DEFAULT_YEAR_SESSION = "2020/2021";
    private static final String UPDATED_YEAR_SESSION = "BBBBBBBBBB";

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

    @Autowired
    private UserCCInfoRepository userCCInfoRepository;

    @Autowired
    private UserCCInfoMapper userCCInfoMapper;

    @Autowired
    private UserCCInfoService userCCInfoService;

    @Autowired
    private UserRepository userRepository;

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
        UserCCInfo userCCInfo = new UserCCInfo()
            .userId(DEFAULT_USER_ID)
            .clubFamilyId(DEFAULT_CLUB_FAMILY_ID)
            .familyRole(DEFAULT_FAMILY_ROLE)
            .yearSession(DEFAULT_YEAR_SESSION);
        return userCCInfo;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserCCInfo createUpdatedEntity(EntityManager em) {
        UserCCInfo userCCInfo = new UserCCInfo()
            .userId(UPDATED_USER_ID)
            .clubFamilyId(UPDATED_CLUB_FAMILY_ID)
            .familyRole(UPDATED_FAMILY_ROLE)
            .yearSession(UPDATED_YEAR_SESSION);
        return userCCInfo;
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

    @Test
    @Transactional
    public void createUserCCInfo() throws Exception {
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
        assertThat(testUserCCInfo.getClubFamilyId()).isEqualTo(DEFAULT_CLUB_FAMILY_ID);
        assertThat(testUserCCInfo.getFamilyRole()).isEqualTo(DEFAULT_FAMILY_ROLE);
        assertThat(testUserCCInfo.getYearSession()).isEqualTo(DEFAULT_YEAR_SESSION);
    }

    @Test
    @Transactional
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
    public void getAllUserCCInfos() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        // Get all the userCCInfoList
        restUserCCInfoMockMvc.perform(get("/api/user-cc-infos?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userCCInfo.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].clubFamilyId").value(hasItem(DEFAULT_CLUB_FAMILY_ID.intValue())))
            .andExpect(jsonPath("$.[*].familyRole").value(hasItem(DEFAULT_FAMILY_ROLE.toString())))
            .andExpect(jsonPath("$.[*].yearSession").value(hasItem(DEFAULT_YEAR_SESSION.toString())));
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
            .andExpect(jsonPath("$.clubFamilyId").value(DEFAULT_CLUB_FAMILY_ID.intValue()))
            .andExpect(jsonPath("$.familyRole").value(DEFAULT_FAMILY_ROLE.toString()))
            .andExpect(jsonPath("$.yearSession").value(DEFAULT_YEAR_SESSION.toString()));
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
    public void updateUserCCInfo() throws Exception {
        // Initialize the database
        userCCInfoRepository.saveAndFlush(userCCInfo);

        int databaseSizeBeforeUpdate = userCCInfoRepository.findAll().size();

        // Update the userCCInfo
        UserCCInfo updatedUserCCInfo = userCCInfoRepository.findById(userCCInfo.getId()).get();
        // Disconnect from session so that the updates on updatedUserCCInfo are not directly saved in db
        em.detach(updatedUserCCInfo);
        updatedUserCCInfo
            .userId(UPDATED_USER_ID)
            .clubFamilyId(UPDATED_CLUB_FAMILY_ID)
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
        assertThat(testUserCCInfo.getClubFamilyId()).isEqualTo(UPDATED_CLUB_FAMILY_ID);
        assertThat(testUserCCInfo.getFamilyRole()).isEqualTo(UPDATED_FAMILY_ROLE);
        assertThat(testUserCCInfo.getYearSession()).isEqualTo(UPDATED_YEAR_SESSION);
    }

    @Test
    @Transactional
    public void updateNonExistingUserCCInfo() throws Exception {
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
    public void deleteUserCCInfo() throws Exception {
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
}
