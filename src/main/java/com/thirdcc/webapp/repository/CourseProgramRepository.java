package com.thirdcc.webapp.repository;

import com.thirdcc.webapp.domain.CourseProgram;
import com.thirdcc.webapp.domain.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the CourseProgram entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CourseProgramRepository extends JpaRepository<CourseProgram, Long> {

    Page<CourseProgram> findAllByFacultyId(Long facultyId, Pageable pageable);
}
