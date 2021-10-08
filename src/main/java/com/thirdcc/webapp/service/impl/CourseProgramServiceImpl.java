package com.thirdcc.webapp.service.impl;

import com.thirdcc.webapp.domain.CourseProgram;
import com.thirdcc.webapp.repository.CourseProgramRepository;
import com.thirdcc.webapp.service.CourseProgramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing {@link CourseProgram}.
 */
@Service
@Transactional
public class CourseProgramServiceImpl implements CourseProgramService {

    private final Logger log = LoggerFactory.getLogger(EventActivityServiceImpl.class);

    private final CourseProgramRepository courseProgramRepository;

    public CourseProgramServiceImpl(CourseProgramRepository courseProgramRepository) {
        this.courseProgramRepository = courseProgramRepository;
    }

    /**
     * Get all the courseProgram by Faculty.
     * @param facultyId
     * @param pageable
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CourseProgram> findAllByFacultyId(Long facultyId, Pageable pageable) {
        log.debug("Request to get all courseProgram by Faculty: {}", facultyId);
        return courseProgramRepository.findAllByFacultyId(facultyId, pageable);
    }

    /**
     * Get all the courseProgram.
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public List<CourseProgram> findAll() {
        log.debug("Request to get all courseProgram");
        return courseProgramRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
}
