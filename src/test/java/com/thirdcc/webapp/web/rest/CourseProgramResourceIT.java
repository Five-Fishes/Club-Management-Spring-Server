package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.ClubmanagementApp;
import com.thirdcc.webapp.annotations.authorization.WithNormalUser;
import com.thirdcc.webapp.domain.CourseProgram;
import com.thirdcc.webapp.domain.Faculty;
import com.thirdcc.webapp.repository.CourseProgramRepository;
import com.thirdcc.webapp.repository.FacultyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManager;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Integration tests for the {@Link CourseProgramResource} REST controller.
 */
@SpringBootTest(classes = ClubmanagementApp.class)
@AutoConfigureMockMvc
public class CourseProgramResourceIT {

    private static final String DEFAULT_FACULTY_NAME = "DEFAULT_FACULTY_NAME";
    private static final String DEFAULT_FACULTY_SHORT_NAME = "DEFAULT_FACULTY_SHORT_NAME";
    private static final String DEFAULT_COURSE_PROGRAM_NAME = "DEFAULT_COURSE_PROGRAM_NAME";
    private static final Integer DEFAULT_COURSE_PROGRAM_NUM_OF_SEM = 7;
    private static final Long DEFAULT_COURSE_PROGRAM_FACULTY_ID = 1L;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private CourseProgramRepository courseProgramRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEventActivityMockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void cleanUp() {
        facultyRepository.deleteAll();
    }

    private Faculty createFacultyEntity() {
        Faculty faculty = new Faculty();
        faculty.setName(DEFAULT_FACULTY_NAME);
        faculty.setShortName(DEFAULT_FACULTY_SHORT_NAME);
        return faculty;
    }

    private CourseProgram createCourseProgramEntity() {
        CourseProgram courseProgram = new CourseProgram();
        courseProgram.setName(DEFAULT_COURSE_PROGRAM_NAME);
        courseProgram.setNumOfSem(DEFAULT_COURSE_PROGRAM_NUM_OF_SEM);
        courseProgram.setFacultyId(DEFAULT_COURSE_PROGRAM_FACULTY_ID);
        return courseProgram;
    }

    @Test
    @WithNormalUser
    public void getAllEventActivitiesByEventId() throws Exception {
        // Initialize the database
        Faculty savedFaculty = initFaculty(createFacultyEntity());
        CourseProgram courseProgram = createCourseProgramEntity();
        courseProgram.setFacultyId(savedFaculty.getId());
        CourseProgram savedCourseProgram = initCourseProgram(courseProgram);

        // Get all the eventActivityList
        restEventActivityMockMvc.perform(get("/api/course-programs/faculty/{facultyId}?sort=id,desc", savedFaculty.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(savedCourseProgram.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_COURSE_PROGRAM_NAME)))
            .andExpect(jsonPath("$.[*].numOfSem").value(hasItem(DEFAULT_COURSE_PROGRAM_NUM_OF_SEM)))
            .andExpect(jsonPath("$.[*].facultyId").value(hasItem(savedFaculty.getId().intValue())));
    }

    @Test
    @WithNormalUser
    public void getAllCourseProgram() throws Exception {
        // Initialize the database
        Faculty savedFaculty = initFaculty(createFacultyEntity());
        CourseProgram courseProgram = createCourseProgramEntity();
        courseProgram.setFacultyId(savedFaculty.getId());
        CourseProgram savedCourseProgram = initCourseProgram(courseProgram);

        restEventActivityMockMvc.perform(get("/api/course-programs/faculty", savedFaculty.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(savedCourseProgram.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_COURSE_PROGRAM_NAME)))
            .andExpect(jsonPath("$.[*].numOfSem").value(hasItem(DEFAULT_COURSE_PROGRAM_NUM_OF_SEM)))
            .andExpect(jsonPath("$.[*].facultyId").value(hasItem(savedFaculty.getId().intValue())));
    }

    private CourseProgram initCourseProgram(CourseProgram courseProgram) {
        return courseProgramRepository.save(courseProgram);
    }

    private Faculty initFaculty(Faculty facultyEntity) {
        return facultyRepository.save(facultyEntity);
    }
}
