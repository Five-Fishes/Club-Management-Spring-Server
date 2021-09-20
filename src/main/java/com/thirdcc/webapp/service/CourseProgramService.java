package com.thirdcc.webapp.service;

import com.thirdcc.webapp.domain.CourseProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseProgramService {
    Page<CourseProgram> findAllByFacultyId(Long facultyId, Pageable pageable);

    List<CourseProgram> findAll();
}
