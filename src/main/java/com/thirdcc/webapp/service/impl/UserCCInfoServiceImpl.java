package com.thirdcc.webapp.service.impl;

import com.thirdcc.webapp.domain.enumeration.CCRoleType;
import com.thirdcc.webapp.service.AdministratorService;
import com.thirdcc.webapp.service.EventCrewService;
import com.thirdcc.webapp.service.UserCCInfoService;
import com.thirdcc.webapp.domain.UserCCInfo;
import com.thirdcc.webapp.repository.UserCCInfoRepository;
import com.thirdcc.webapp.service.UserUniInfoService;
import com.thirdcc.webapp.service.dto.UserCCInfoDTO;
import com.thirdcc.webapp.service.dto.UserUniInfoDTO;
import com.thirdcc.webapp.service.dto.UserCCRoleDTO;
import com.thirdcc.webapp.service.mapper.UserCCInfoMapper;
import com.thirdcc.webapp.utils.FishLevelUtils;
import com.thirdcc.webapp.utils.YearSessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service Implementation for managing {@link UserCCInfo}.
 */
@Service
@Transactional
public class UserCCInfoServiceImpl implements UserCCInfoService {

    private final Logger log = LoggerFactory.getLogger(UserCCInfoServiceImpl.class);

    private final UserCCInfoRepository userCCInfoRepository;

    private final UserCCInfoMapper userCCInfoMapper;

    private final UserUniInfoService userUniInfoService;

    private final EventCrewService eventCrewService;

    private final AdministratorService administratorService;

    public UserCCInfoServiceImpl(UserCCInfoRepository userCCInfoRepository,
                                 UserCCInfoMapper userCCInfoMapper,
                                 UserUniInfoService userUniInfoService,
                                 EventCrewService eventCrewService,
                                 AdministratorService administratorService
    ) {
        this.userCCInfoRepository = userCCInfoRepository;
        this.userCCInfoMapper = userCCInfoMapper;
        this.userUniInfoService = userUniInfoService;
        this.eventCrewService = eventCrewService;
        this.administratorService = administratorService;
    }

    /**
     * Save a userCCInfo.
     *
     * @param userCCInfoDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public UserCCInfoDTO save(UserCCInfoDTO userCCInfoDTO) {
        log.debug("Request to save UserCCInfo : {}", userCCInfoDTO);
        UserCCInfo userCCInfo = userCCInfoMapper.toEntity(userCCInfoDTO);
        userCCInfo = userCCInfoRepository.save(userCCInfo);
        return userCCInfoMapper.toDto(userCCInfo);
    }

    /**
     * Get all the userCCInfos.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserCCInfoDTO> findAll() {
        log.debug("Request to get all UserCCInfos");
        return userCCInfoRepository.findAll().stream()
            .map(userCCInfoMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one userCCInfo by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserCCInfoDTO> findOne(Long id) {
        log.debug("Request to get UserCCInfo : {}", id);
        return userCCInfoRepository.findById(id)
            .map(userCCInfoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCCInfoDTO> findOneByUserId(Long userId) {
        log.debug("Request to get UserCCInfo by userId : {}", userId);
        return userCCInfoRepository.findByUserId(userId)
            .map(userCCInfoMapper::toDto)
            .map(this::clubFamilyDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCCInfoDTO> getUserCCInfoByUserId(Long userId) {
        log.debug("Request to get UserCCInfo of User : {}", userId);
        List<UserCCInfoDTO> userCCInfoDTOList = userCCInfoRepository
            .findAllByUserId(userId, Pageable.unpaged())
            .map(userCCInfoMapper::toDto)
            .getContent();
        return getFullUserCCInfoList(userCCInfoDTOList, userId);
    }

    /**
     *
     * @param userCCInfoDTOList
     * @param userId
     * @return List of user CC Info Sorted by Year Session
     */
    private List<UserCCInfoDTO> getFullUserCCInfoList(List<UserCCInfoDTO> userCCInfoDTOList, Long userId) {
        List<UserCCInfoDTO> fullUserCCInfoDTOList = new ArrayList<>();
        Optional<UserUniInfoDTO> userUniInfoDTOOptional = userUniInfoService.getUserUniInfoByUserId(userId);
        if (!userUniInfoDTOOptional.isPresent()) {
            return fullUserCCInfoDTOList;
        }
        String currentYearSession = YearSessionUtils.getCurrentYearSession();
        String intakeYearSession = userUniInfoDTOOptional.get().getYearSession();
        Integer totalSemester = userUniInfoDTOOptional.get().getTotalSemester();
        String yearSessionCounter = intakeYearSession;
        for (int i = 1; i <= totalSemester; i = i + 2) {
            String currentYearSessionCounter = yearSessionCounter;
            UserCCInfoDTO userCCInfoDTO = userCCInfoDTOList.stream()
                .filter(userCCInfoDTO1 -> userCCInfoDTO1.getYearSession().equals(currentYearSessionCounter))
                .findFirst()
                .orElse(
                    UserCCInfoDTO.builder()
                        .yearSession(yearSessionCounter)
                        .userId(userId)
                        .build()
                );
            userCCInfoDTO.setFishLevel(FishLevelUtils.getFishLevelBySemesterStudied(i));
            fullUserCCInfoDTOList.add(userCCInfoDTO);
            if (YearSessionUtils.isBefore(currentYearSession, yearSessionCounter)) {
                break; // yearSession is in future, don't include
            }
            yearSessionCounter = YearSessionUtils.addYearSessionWithSemester(yearSessionCounter, 2);
        }
        return fullUserCCInfoDTOList.stream()
            .sorted(Comparator.comparing(UserCCInfoDTO::getYearSession).reversed())
            .collect(Collectors.toList());
    }

    @Override
    public List<UserCCRoleDTO> getUserCCRolesByUserId(Long userId) {
        log.debug("Request to get UserCC Roles of User : {}", userId);
        List<UserCCRoleDTO> userCCFamilyRoleDTOList = getUserCCFamilyRole(userId);
        List<UserCCRoleDTO> userCCEventRolesList = getUserCCEventRole(userId);
        List<UserCCRoleDTO> userCCAdministratorRolesList = getUserCCAdministratorRole(userId);
        return Stream.of(userCCFamilyRoleDTOList, userCCEventRolesList, userCCAdministratorRolesList)
            .flatMap(Collection::stream)
            .sorted(Comparator.comparing(UserCCRoleDTO::getYearSession).reversed())
            .collect(Collectors.toList());
    }

    private List<UserCCRoleDTO> getUserCCFamilyRole(Long userId) {
        return userCCInfoRepository
            .findAllByUserId(userId, Pageable.unpaged())
            .map(userCCInfo -> {
                UserCCRoleDTO userCCFamilyRoleDTO = new UserCCRoleDTO();
                userCCFamilyRoleDTO.setUserId(userCCInfo.getUserId());
                userCCFamilyRoleDTO.setType(CCRoleType.FAMILY_ROLE);
                userCCFamilyRoleDTO.setRole(userCCInfo.getFamilyRole().name());
                userCCFamilyRoleDTO.setYearSession(userCCInfo.getYearSession());
                return userCCFamilyRoleDTO;
            })
            .getContent();
    }

    private List<UserCCRoleDTO> getUserCCEventRole(Long userId) {
        return eventCrewService.findAllByUserId(userId, Pageable.unpaged())
            .map(eventCrewDTO -> {
                UserCCRoleDTO userCCEventRoleDTO = new UserCCRoleDTO();
                userCCEventRoleDTO.setUserId(eventCrewDTO.getUserId());
                userCCEventRoleDTO.setType(CCRoleType.EVENT_CREW);
                userCCEventRoleDTO.setRole(eventCrewDTO.getRole().name());
                userCCEventRoleDTO.setYearSession(eventCrewDTO.getYearSession());
                userCCEventRoleDTO.setEventId(eventCrewDTO.getEventId());
                userCCEventRoleDTO.setEventName(eventCrewDTO.getEventName());
                return userCCEventRoleDTO;
            })
            .getContent();
    }

    private List<UserCCRoleDTO> getUserCCAdministratorRole(Long userId) {
        return administratorService.findAllByUserId(userId)
            .stream()
            .map(administratorDTO -> {
                UserCCRoleDTO userCCAdministratorRoleDTO = new UserCCRoleDTO();
                userCCAdministratorRoleDTO.setUserId(administratorDTO.getUserId());
                userCCAdministratorRoleDTO.setType(CCRoleType.CC_ADMINISTRATOR);
                userCCAdministratorRoleDTO.setRole(administratorDTO.getRole().name());
                userCCAdministratorRoleDTO.setYearSession(administratorDTO.getYearSession());
                return userCCAdministratorRoleDTO;
            })
            .collect(Collectors.toList());
    }

    /**
     * Delete the userCCInfo by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete UserCCInfo : {}", id);
        userCCInfoRepository.deleteById(id);
    }

    private UserCCInfoDTO clubFamilyDetails(UserCCInfoDTO userCCInfoDTO) {
        // TODO: [LU] update implementation
//        clubFamilyService.findOne(userCCInfoDTO.getClubFamilyId())
//            .ifPresent(clubFamilyDTO -> {
//                userCCInfoDTO.setClubFamilyName(clubFamilyDTO.getName());
//                userCCInfoDTO.setClubFamilySlogan(clubFamilyDTO.getSlogan());
//            });
        return userCCInfoDTO;
    }
}
