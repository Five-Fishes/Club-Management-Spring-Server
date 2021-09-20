package com.thirdcc.webapp.repository;

import com.thirdcc.webapp.domain.CourseProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data  repository for the CourseProgram entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CourseProgramRepository extends JpaRepository<CourseProgram, Long>, JpaSpecificationExecutor<CourseProgram> {

    Page<CourseProgram> findAllByFacultyId(Long facultyId, Pageable pageable);
}
