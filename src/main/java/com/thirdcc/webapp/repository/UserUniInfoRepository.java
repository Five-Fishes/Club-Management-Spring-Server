package com.thirdcc.webapp.repository;

import com.thirdcc.webapp.domain.UserUniInfo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the UserUniInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserUniInfoRepository extends JpaRepository<UserUniInfo, Long> {

}
