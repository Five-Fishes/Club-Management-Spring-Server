package com.thirdcc.webapp.service.impl;

import com.thirdcc.webapp.domain.CourseProgram;
import com.thirdcc.webapp.domain.User;
import com.thirdcc.webapp.domain.enumeration.UserUniStatus;
import com.thirdcc.webapp.exception.BadRequestException;
import com.thirdcc.webapp.exception.InternalServerErrorException;
import com.thirdcc.webapp.repository.CourseProgramRepository;
import com.thirdcc.webapp.repository.UserRepository;
import com.thirdcc.webapp.security.SecurityUtils;
import com.thirdcc.webapp.service.UserUniInfoService;
import com.thirdcc.webapp.domain.UserUniInfo;
import com.thirdcc.webapp.repository.UserUniInfoRepository;
import com.thirdcc.webapp.service.dto.UserUniInfoDTO;
import com.thirdcc.webapp.service.mapper.UserUniInfoMapper;
import com.thirdcc.webapp.utils.YearSessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link UserUniInfo}.
 */
@Service
@Transactional
public class UserUniInfoServiceImpl implements UserUniInfoService {

    private final Logger log = LoggerFactory.getLogger(UserUniInfoServiceImpl.class);

    private final UserUniInfoRepository userUniInfoRepository;

    private final UserUniInfoMapper userUniInfoMapper;

    private final UserRepository userRepository;

    private final CourseProgramRepository courseProgramRepository;

    private static final UserUniStatus DEFAULT_USER_UNI_STATUS = UserUniStatus.STUDYING;

    public UserUniInfoServiceImpl(UserUniInfoRepository userUniInfoRepository, UserUniInfoMapper userUniInfoMapper, UserRepository userRepository, CourseProgramRepository courseProgramRepository) {
        this.userUniInfoRepository = userUniInfoRepository;
        this.userUniInfoMapper = userUniInfoMapper;
        this.userRepository = userRepository;
        this.courseProgramRepository = courseProgramRepository;
    }

    /**
     * Save a userUniInfo.
     *
     * @param userUniInfoDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public UserUniInfoDTO save(UserUniInfoDTO userUniInfoDTO) {
        log.debug("Request to save UserUniInfo : {}", userUniInfoDTO);
        User currentUser = SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneWithAuthoritiesByLogin)
            .orElseThrow(() -> new BadRequestException("Cannot find user"));
        boolean isCourseProgramIdValid = courseProgramRepository.existsById(userUniInfoDTO.getCourseProgramId());
        if (!isCourseProgramIdValid) {
            throw new BadRequestException("Invalid Course Program Id");
        }
        UserUniInfo userUniInfo = userUniInfoRepository
            .findOneByUserId(currentUser.getId())
            .orElse(new UserUniInfo());
        UserUniStatus userUniStatus = userUniInfoDTO.getStatus();
        if (userUniStatus == null) {
            userUniStatus = DEFAULT_USER_UNI_STATUS;
        }
        userUniInfo.setUserId(currentUser.getId());
        userUniInfo.setCourseProgramId(userUniInfoDTO.getCourseProgramId());
        userUniInfo.setYearSession(userUniInfoDTO.getYearSession());
        userUniInfo.setIntakeSemester(userUniInfoDTO.getIntakeSemester());
        userUniInfo.setStayIn(userUniInfoDTO.getStayIn());
        userUniInfo.setStatus(userUniStatus);
        userUniInfo = userUniInfoRepository.save(userUniInfo);
        return userUniInfoMapper.toDto(userUniInfo);
    }

    /**
     * Get all the userUniInfos.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserUniInfoDTO> findAll() {
        log.debug("Request to get all UserUniInfos");
        return userUniInfoRepository.findAll().stream()
            .map(userUniInfoMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one userUniInfo by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserUniInfoDTO> findOne(Long id) {
        log.debug("Request to get UserUniInfo : {}", id);
        return userUniInfoRepository.findById(id)
            .map(userUniInfoMapper::toDto);
    }

    /**
     * Delete the userUniInfo by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete UserUniInfo : {}", id);
        userUniInfoRepository.deleteById(id);
    }

    @Override
    public boolean isUserUniInfoCompleted(Long userId) {
        UserUniInfo userUniInfo = userUniInfoRepository
            .findOneByUserId(userId)
            .orElse(new UserUniInfo());

        boolean hasCourseProgramId = userUniInfo.getCourseProgramId() != null;
        boolean hasYearSession = StringUtils.isNotBlank(userUniInfo.getYearSession());
        boolean hasIntakeSemester = userUniInfo.getIntakeSemester() != null;
        boolean hasStayIn = StringUtils.isNotBlank(userUniInfo.getStayIn());

        return hasCourseProgramId && hasYearSession && hasIntakeSemester && hasStayIn;
    }

    @Override
    public Optional<UserUniInfoDTO> getUserUniInfoByUserId(Long userId) {
        return userUniInfoRepository
            .findOneByUserId(userId)
            .map(userUniInfoMapper::toDto)
            .map(this::mapUserUniInfoDetails)
            .map(this::mapClubFamilyInfo);
    }

    private UserUniInfoDTO mapUserUniInfoDetails(UserUniInfoDTO userUniInfoDTO) {
        CourseProgram courseProgram = courseProgramRepository.findById(userUniInfoDTO.getCourseProgramId())
            .orElseThrow(() -> new InternalServerErrorException("Course Program not available for Id: " + userUniInfoDTO.getCourseProgramId()));
        userUniInfoDTO.setGraduateYearSession(
            YearSessionUtils.addYearSessionWithSemester(userUniInfoDTO.getYearSession(), courseProgram.getNumOfSem())
        );
        userUniInfoDTO.setTotalSemester(courseProgram.getNumOfSem());
        return userUniInfoDTO;
    }

    private UserUniInfoDTO mapClubFamilyInfo(UserUniInfoDTO userUniInfoDTO) {
        // TODO: [LU] Update implementation
//        Optional<ClubFamilyDTO> clubFamilyDTOOptional = clubFamilyService.findClubFamilyByUserId(userUniInfoDTO.getUserId());
//        clubFamilyDTOOptional.ifPresent(clubFamilyDTO -> {
//            userUniInfoDTO.setClubFamilyId(clubFamilyDTO.getId());
//            userUniInfoDTO.setClubFamilyName(clubFamilyDTO.getName());
//            userUniInfoDTO.setClubFamilySlogan(clubFamilyDTO.getSlogan());
//            userUniInfoDTO.setClubFamilyDescription(clubFamilyDTO.getDescription());
//        });
        return userUniInfoDTO;
    }

    @Override
    public UserUniInfoDTO mapUserUniInfoWithUser(UserUniInfoDTO userUniInfoDTO, User user) {
        userUniInfoDTO.setUserId(user.getId());
        userUniInfoDTO.setFirstName(user.getFirstName());
        userUniInfoDTO.setLastName(user.getLastName());
        userUniInfoDTO.setEmail(user.getEmail());
        userUniInfoDTO.setGender(user.getGender());
        userUniInfoDTO.setPhoneNumber(user.getPhoneNumber());
        userUniInfoDTO.setDateOfBirth(user.getDateOfBirth());
        userUniInfoDTO.setImageUrl(user.getImageUrl());
        return userUniInfoDTO;
    }
}
