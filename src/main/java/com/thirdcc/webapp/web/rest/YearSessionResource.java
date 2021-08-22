package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.domain.YearSession;
import com.thirdcc.webapp.service.YearSessionQueryService;
import com.thirdcc.webapp.service.YearSessionService;
import com.thirdcc.webapp.service.criteria.YearSessionCriteria;
import com.thirdcc.webapp.web.rest.errors.BadRequestAlertException;

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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing {@link com.thirdcc.webapp.domain.YearSession}.
 */
@RestController
@RequestMapping("/api")
public class YearSessionResource {

    private final Logger log = LoggerFactory.getLogger(YearSessionResource.class);

    private static final String ENTITY_NAME = "yearSession";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final YearSessionService yearSessionService;

    private final YearSessionQueryService yearSessionQueryService;

    public YearSessionResource(YearSessionService yearSessionService, YearSessionQueryService yearSessionQueryService) {
        this.yearSessionService = yearSessionService;
        this.yearSessionQueryService = yearSessionQueryService;
    }

    /**
     * {@code POST  /year-sessions} : Create a new yearSession.
     *
     * @param yearSession the yearSession to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new yearSession, or with status {@code 400 (Bad Request)} if the yearSession has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/year-sessions")
    public ResponseEntity<YearSession> createYearSession(@RequestBody YearSession yearSession) throws URISyntaxException {
        log.debug("REST request to save YearSession : {}", yearSession);
        if (yearSession.getId() != null) {
            throw new BadRequestAlertException("A new yearSession cannot already have an ID", ENTITY_NAME, "idexists");
        }
        YearSession result = yearSessionService.save(yearSession);
        return ResponseEntity.created(new URI("/api/year-sessions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /year-sessions} : Updates an existing yearSession.
     *
     * @param yearSession the yearSession to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated yearSession,
     * or with status {@code 400 (Bad Request)} if the yearSession is not valid,
     * or with status {@code 500 (Internal Server Error)} if the yearSession couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/year-sessions")
    public ResponseEntity<YearSession> updateYearSession(@RequestBody YearSession yearSession) throws URISyntaxException {
        log.debug("REST request to update YearSession : {}", yearSession);
        if (yearSession.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        YearSession result = yearSessionService.save(yearSession);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, yearSession.getId().toString()))
            .body(result);
    }

    @GetMapping("/year-sessions")
    public ResponseEntity<List<YearSession>> getAllYearSessions(YearSessionCriteria criteria) {
        log.debug("REST request to get YearSessions by criteria: {}", criteria);
        List<YearSession> entityList = yearSessionQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    @GetMapping("/year-sessions/values")
    public ResponseEntity<List<String>> getAllYearSessionsValueOnly(YearSessionCriteria criteria, Pageable pageable) {
        log.debug("REST request to get YearSessions value only by criteria: {}", criteria);
        Page<YearSession> page = yearSessionQueryService.findByCriteria(criteria, pageable);
        List<String> entityList = page.stream()
            .map(YearSession::getValue)
            .collect(Collectors.toList());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(entityList);
    }

    @GetMapping("/year-sessions/count")
    public ResponseEntity<Long> countYearSessions(YearSessionCriteria criteria) {
        log.debug("REST request to count YearSessions by criteria: {}", criteria);
        return ResponseEntity.ok().body(yearSessionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /year-sessions/:id} : get the "id" yearSession.
     *
     * @param id the id of the yearSession to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the yearSession, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/year-sessions/{id}")
    public ResponseEntity<YearSession> getYearSession(@PathVariable Long id) {
        log.debug("REST request to get YearSession : {}", id);
        Optional<YearSession> yearSession = yearSessionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(yearSession);
    }

    /**
     * {@code DELETE  /year-sessions/:id} : delete the "id" yearSession.
     *
     * @param id the id of the yearSession to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/year-sessions/{id}")
    public ResponseEntity<Void> deleteYearSession(@PathVariable Long id) {
        log.debug("REST request to delete YearSession : {}", id);
        yearSessionService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
