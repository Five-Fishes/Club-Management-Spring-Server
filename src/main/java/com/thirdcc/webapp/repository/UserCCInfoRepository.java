package com.thirdcc.webapp.repository;

import com.thirdcc.webapp.domain.UserCCInfo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the UserCCInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserCCInfoRepository extends JpaRepository<UserCCInfo, Long> {

}