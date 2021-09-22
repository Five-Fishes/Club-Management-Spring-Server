package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.domain.CourseProgram;
import com.thirdcc.webapp.service.CourseProgramService;
import io.github.jhipster.web.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * REST controller for managing {@link com.thirdcc.webapp.domain.CourseProgram}.
 */
@RestController
@RequestMapping("/api")
public class CourseProgramResource {
    private final Logger log = LoggerFactory.getLogger(FacultyResource.class);

    private static final String ENTITY_NAME = "courseProgram";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CourseProgramService courseProgramService;

    public CourseProgramResource(CourseProgramService courseProgramService) {
        this.courseProgramService = courseProgramService;
    }

    /**
     * {@code GET  /course-programs/faculty/{facultyId}} : get all the course program by faculty.
     *
     * @param facultyId
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of faculties in body.
     */
    @GetMapping("/course-programs/faculty/{facultyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseProgram>> getAllCourseProgramsByFacultyId(@PathVariable Long facultyId, Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get all Course Programs by Faculty: {}", facultyId);
        Page<CourseProgram> page = courseProgramService.findAllByFacultyId(facultyId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /course-programs/faculty} : get all the course program.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of faculties in body.
     */
    @GetMapping("/course-programs/faculty")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseProgram>> getAllCoursePrograms() {
        log.debug("REST request to get all Course Programs");
        List<CourseProgram> entityList = courseProgramService.findAll();
        return ResponseEntity.ok().body(entityList);
    }

}
