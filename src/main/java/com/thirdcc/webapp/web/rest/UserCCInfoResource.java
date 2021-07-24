package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.domain.User;
import com.thirdcc.webapp.domain.UserCCInfo;
import com.thirdcc.webapp.exception.BadRequestException;
import com.thirdcc.webapp.security.SecurityUtils;
import com.thirdcc.webapp.service.UserCCInfoQueryService;
import com.thirdcc.webapp.service.UserCCInfoService;
import com.thirdcc.webapp.service.UserService;
import com.thirdcc.webapp.service.criteria.UserCCInfoCriteria;
import com.thirdcc.webapp.service.dto.UserCCRoleDTO;
import com.thirdcc.webapp.web.rest.errors.BadRequestAlertException;
import com.thirdcc.webapp.service.dto.UserCCInfoDTO;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.thirdcc.webapp.domain.UserCCInfo}.
 */
@RestController
@RequestMapping("/api")
public class UserCCInfoResource {

    private final Logger log = LoggerFactory.getLogger(UserCCInfoResource.class);

    private static final String ENTITY_NAME = "userCCInfo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserCCInfoService userCCInfoService;

    private final UserService userService;

    private final UserCCInfoQueryService userCCInfoQueryService;

    public UserCCInfoResource(UserCCInfoService userCCInfoService, UserService userService, UserCCInfoQueryService userCCInfoQueryService) {
        this.userCCInfoService = userCCInfoService;
        this.userService = userService;
        this.userCCInfoQueryService = userCCInfoQueryService;
    }

    /**
     * {@code POST  /user-cc-infos} : Create a new userCCInfo.
     *
     * @param userCCInfoDTO the userCCInfoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new userCCInfoDTO, or with status {@code 400 (Bad Request)} if the userCCInfo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/user-cc-infos")
    public ResponseEntity<UserCCInfoDTO> createUserCCInfo(@RequestBody UserCCInfoDTO userCCInfoDTO) throws URISyntaxException {
        log.debug("REST request to save UserCCInfo : {}", userCCInfoDTO);
        if (userCCInfoDTO.getId() != null) {
            throw new BadRequestAlertException("A new userCCInfo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        UserCCInfoDTO result = userCCInfoService.save(userCCInfoDTO);
        return ResponseEntity.created(new URI("/api/user-cc-infos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /user-cc-infos} : Updates an existing userCCInfo.
     *
     * @param userCCInfoDTO the userCCInfoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userCCInfoDTO,
     * or with status {@code 400 (Bad Request)} if the userCCInfoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the userCCInfoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/user-cc-infos")
    public ResponseEntity<UserCCInfoDTO> updateUserCCInfo(@RequestBody UserCCInfoDTO userCCInfoDTO) throws URISyntaxException {
        log.debug("REST request to update UserCCInfo : {}", userCCInfoDTO);
        if (userCCInfoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        UserCCInfoDTO result = userCCInfoService.save(userCCInfoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userCCInfoDTO.getId().toString()))
            .body(result);
    }

    @GetMapping("/user-cc-infos")
    public ResponseEntity<List<UserCCInfoDTO>> getAllUserCCInfos(UserCCInfoCriteria criteria) {
        log.debug("REST request to get UserCCInfos by criteria: {}", criteria);
        List<UserCCInfoDTO> entityList = userCCInfoQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    @GetMapping("/user-cc-infos/count")
    public ResponseEntity<Long> countUserCCInfos(UserCCInfoCriteria criteria) {
        log.debug("REST request to count UserCCInfos by criteria: {}", criteria);
        return ResponseEntity.ok().body(userCCInfoQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /user-cc-infos/:id} : get the "id" userCCInfo.
     *
     * @param id the id of the userCCInfoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the userCCInfoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/user-cc-infos/{id}")
    public ResponseEntity<UserCCInfoDTO> getUserCCInfo(@PathVariable Long id) {
        log.debug("REST request to get UserCCInfo : {}", id);
        Optional<UserCCInfoDTO> userCCInfoDTO = userCCInfoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(userCCInfoDTO);
    }

    /**
     * {@code GET  /user-cc-infos/current} : get the current User userCCInfo Profile.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the List of userCCInfoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/user-cc-infos/current")
    public ResponseEntity<List<UserCCInfoDTO>> getCurrentUserCCInfoProfile() {
        log.debug("REST request to get Current User UserCCInfo Profile");
        String userLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestException("User not Login"));
        User user = userService.getUserByLogin(userLogin)
            .orElseThrow(() -> new BadRequestException("User not found"));
        List<UserCCInfoDTO> result = userCCInfoService.getUserCCInfoByUserId(user.getId());
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code GET  /user-cc-infos/roles/current} : get the current User CC Roles Profile.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the List of UserCCRoleDTO.
     */
    @GetMapping("/user-cc-infos/roles/current")
    public ResponseEntity<List<UserCCRoleDTO>> getCurrentUserCCRolesProfile() {
        log.debug("REST request to get Current User CC Roles Profile List");
        String userLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestException("User not Login"));
        User user = userService.getUserByLogin(userLogin)
            .orElseThrow(() -> new BadRequestException("User not found"));
        List<UserCCRoleDTO> result = userCCInfoService.getUserCCRolesByUserId(user.getId());
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code DELETE  /user-cc-infos/:id} : delete the "id" userCCInfo.
     *
     * @param id the id of the userCCInfoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/user-cc-infos/{id}")
    public ResponseEntity<Void> deleteUserCCInfo(@PathVariable Long id) {
        log.debug("REST request to delete UserCCInfo : {}", id);
        userCCInfoService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    /**
     * use {@link #getCcMembers(UserCCInfoCriteria, Pageable)}
     * @param criteria Search Criteria of CC Members List
     * @param pageable Pagination Info
     * @return Page of User Full Info: UserCCInfo, User, UserUniInfo
     */
    @GetMapping("/cc-members")
    public ResponseEntity<List<UserCCInfo>> getCcMembers(UserCCInfoCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Members by criteria: {}", criteria);
        Page<UserCCInfo> page = userCCInfoQueryService.findMembersByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok()
            .headers(headers)
            .body(page.getContent());
    }
}
