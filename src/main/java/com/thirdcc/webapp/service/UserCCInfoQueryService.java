package com.thirdcc.webapp.service;

import com.thirdcc.webapp.domain.*; // for static metamodels
import com.thirdcc.webapp.domain.UserCCInfo;
import com.thirdcc.webapp.repository.UserCCInfoRepository;
import com.thirdcc.webapp.service.criteria.UserCCInfoCriteria;
import com.thirdcc.webapp.service.dto.UserCCInfoDTO;
import com.thirdcc.webapp.service.mapper.UserCCInfoMapper;
import java.util.List;
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
 * Service for executing complex queries for {@link UserCCInfo} entities in the database.
 * The main input is a {@link UserCCInfoCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link UserCCInfoDTO} or a {@link Page} of {@link UserCCInfoDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class UserCCInfoQueryService extends QueryService<UserCCInfo> {

  private final Logger log = LoggerFactory.getLogger(UserCCInfoQueryService.class);

  private final UserCCInfoRepository userCCInfoRepository;

  private final UserCCInfoMapper userCCInfoMapper;

  public UserCCInfoQueryService(UserCCInfoRepository userCCInfoRepository, UserCCInfoMapper userCCInfoMapper) {
    this.userCCInfoRepository = userCCInfoRepository;
    this.userCCInfoMapper = userCCInfoMapper;
  }

  /**
   * Return a {@link List} of {@link UserCCInfoDTO} which matches the criteria from the database.
   * @param criteria The object which holds all the filters, which the entities should match.
   * @return the matching entities.
   */
  @Transactional(readOnly = true)
  public List<UserCCInfoDTO> findByCriteria(UserCCInfoCriteria criteria) {
    log.debug("find by criteria : {}", criteria);
    final Specification<UserCCInfo> specification = createSpecification(criteria);
    return userCCInfoMapper.toDto(userCCInfoRepository.findAll(specification));
  }

  /**
   * Return a {@link Page} of {@link UserCCInfoDTO} which matches the criteria from the database.
   * @param criteria The object which holds all the filters, which the entities should match.
   * @param page The page, which should be returned.
   * @return the matching entities.
   */
  @Transactional(readOnly = true)
  public Page<UserCCInfoDTO> findByCriteria(UserCCInfoCriteria criteria, Pageable page) {
    log.debug("find by criteria : {}, page: {}", criteria, page);
    final Specification<UserCCInfo> specification = createSpecification(criteria);
    return userCCInfoRepository.findAll(specification, page).map(userCCInfoMapper::toDto);
  }

  /**
   * Return the number of matching entities in the database.
   * @param criteria The object which holds all the filters, which the entities should match.
   * @return the number of matching entities.
   */
  @Transactional(readOnly = true)
  public long countByCriteria(UserCCInfoCriteria criteria) {
    log.debug("count by criteria : {}", criteria);
    final Specification<UserCCInfo> specification = createSpecification(criteria);
    return userCCInfoRepository.count(specification);
  }

  /**
   * Function to convert {@link UserCCInfoCriteria} to a {@link Specification}
   * @param criteria The object which holds all the filters, which the entities should match.
   * @return the matching {@link Specification} of the entity.
   */
  protected Specification<UserCCInfo> createSpecification(UserCCInfoCriteria criteria) {
    Specification<UserCCInfo> specification = Specification.where(null);
    if (criteria != null) {
      if (criteria.getId() != null) {
        specification = specification.and(buildRangeSpecification(criteria.getId(), UserCCInfo_.id));
      }
      if (criteria.getUserId() != null) {
        specification = specification.and(buildRangeSpecification(criteria.getUserId(), UserCCInfo_.userId));
      }
//      if (criteria.getClubFamilyId() != null) {
//        specification = specification.and(buildRangeSpecification(criteria.getClubFamilyId(), UserCCInfo_.clubFamilyId));
//      }
      if (criteria.getFamilyRole() != null) {
        specification = specification.and(buildSpecification(criteria.getFamilyRole(), UserCCInfo_.familyRole));
      }
      if (criteria.getYearSession() != null) {
        specification = specification.and(buildStringSpecification(criteria.getYearSession(), UserCCInfo_.yearSession));
      }
    }
    return specification;
  }
}
