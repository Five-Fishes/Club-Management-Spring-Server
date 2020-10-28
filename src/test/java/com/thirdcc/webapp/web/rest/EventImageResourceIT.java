package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.ClubmanagementApp;
import com.thirdcc.webapp.domain.EventImage;
import com.thirdcc.webapp.repository.EventImageRepository;
import com.thirdcc.webapp.service.EventImageService;
import com.thirdcc.webapp.service.dto.EventImageDTO;
import com.thirdcc.webapp.service.mapper.EventImageMapper;
import com.thirdcc.webapp.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;

import static com.thirdcc.webapp.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@Link EventImageResource} REST controller.
 */
@SpringBootTest(classes = ClubmanagementApp.class)
public class EventImageResourceIT {

    private static final Long DEFAULT_EVENT_ID = 1L;
    private static final Long UPDATED_EVENT_ID = 2L;

    private static final Long DEFAULT_IMAGE_STORAGE_ID = 1L;
    private static final Long UPDATED_IMAGE_STORAGE_ID = 2L;

    @Autowired
    private EventImageRepository eventImageRepository;

    @Autowired
    private EventImageMapper eventImageMapper;

    @Autowired
    private EventImageService eventImageService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restEventImageMockMvc;

    private EventImage eventImage;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final EventImageResource eventImageResource = new EventImageResource(eventImageService);
        this.restEventImageMockMvc = MockMvcBuilders.standaloneSetup(eventImageResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventImage createEntity(EntityManager em) {
        EventImage eventImage = new EventImage()
            .eventId(DEFAULT_EVENT_ID)
            .imageStorageId(DEFAULT_IMAGE_STORAGE_ID);
        return eventImage;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventImage createUpdatedEntity(EntityManager em) {
        EventImage eventImage = new EventImage()
            .eventId(UPDATED_EVENT_ID)
            .imageStorageId(UPDATED_IMAGE_STORAGE_ID);
        return eventImage;
    }

    @BeforeEach
    public void initTest() {
        eventImage = createEntity(em);
    }

    @Test
    @Transactional
    public void createEventImage() throws Exception {
        int databaseSizeBeforeCreate = eventImageRepository.findAll().size();

        // Create the EventImage
        EventImageDTO eventImageDTO = eventImageMapper.toDto(eventImage);
        restEventImageMockMvc.perform(post("/api/event-images")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventImageDTO)))
            .andExpect(status().isCreated());

        // Validate the EventImage in the database
        List<EventImage> eventImageList = eventImageRepository.findAll();
        assertThat(eventImageList).hasSize(databaseSizeBeforeCreate + 1);
        EventImage testEventImage = eventImageList.get(eventImageList.size() - 1);
        assertThat(testEventImage.getEventId()).isEqualTo(DEFAULT_EVENT_ID);
        assertThat(testEventImage.getImageStorageId()).isEqualTo(DEFAULT_IMAGE_STORAGE_ID);
    }

    @Test
    @Transactional
    public void createEventImageWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = eventImageRepository.findAll().size();

        // Create the EventImage with an existing ID
        eventImage.setId(1L);
        EventImageDTO eventImageDTO = eventImageMapper.toDto(eventImage);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventImageMockMvc.perform(post("/api/event-images")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventImageDTO)))
            .andExpect(status().isBadRequest());

        // Validate the EventImage in the database
        List<EventImage> eventImageList = eventImageRepository.findAll();
        assertThat(eventImageList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllEventImages() throws Exception {
        // Initialize the database
        eventImageRepository.saveAndFlush(eventImage);

        // Get all the eventImageList
        restEventImageMockMvc.perform(get("/api/event-images?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventImage.getId().intValue())))
            .andExpect(jsonPath("$.[*].eventId").value(hasItem(DEFAULT_EVENT_ID.intValue())))
            .andExpect(jsonPath("$.[*].imageStorageId").value(hasItem(DEFAULT_IMAGE_STORAGE_ID.intValue())));
    }
    
    @Test
    @Transactional
    public void getEventImage() throws Exception {
        // Initialize the database
        eventImageRepository.saveAndFlush(eventImage);

        // Get the eventImage
        restEventImageMockMvc.perform(get("/api/event-images/{id}", eventImage.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(eventImage.getId().intValue()))
            .andExpect(jsonPath("$.eventId").value(DEFAULT_EVENT_ID.intValue()))
            .andExpect(jsonPath("$.imageStorageId").value(DEFAULT_IMAGE_STORAGE_ID.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingEventImage() throws Exception {
        // Get the eventImage
        restEventImageMockMvc.perform(get("/api/event-images/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEventImage() throws Exception {
        // Initialize the database
        eventImageRepository.saveAndFlush(eventImage);

        int databaseSizeBeforeUpdate = eventImageRepository.findAll().size();

        // Update the eventImage
        EventImage updatedEventImage = eventImageRepository.findById(eventImage.getId()).get();
        // Disconnect from session so that the updates on updatedEventImage are not directly saved in db
        em.detach(updatedEventImage);
        updatedEventImage
            .eventId(UPDATED_EVENT_ID)
            .imageStorageId(UPDATED_IMAGE_STORAGE_ID);
        EventImageDTO eventImageDTO = eventImageMapper.toDto(updatedEventImage);

        restEventImageMockMvc.perform(put("/api/event-images")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventImageDTO)))
            .andExpect(status().isOk());

        // Validate the EventImage in the database
        List<EventImage> eventImageList = eventImageRepository.findAll();
        assertThat(eventImageList).hasSize(databaseSizeBeforeUpdate);
        EventImage testEventImage = eventImageList.get(eventImageList.size() - 1);
        assertThat(testEventImage.getEventId()).isEqualTo(UPDATED_EVENT_ID);
        assertThat(testEventImage.getImageStorageId()).isEqualTo(UPDATED_IMAGE_STORAGE_ID);
    }

    @Test
    @Transactional
    public void updateNonExistingEventImage() throws Exception {
        int databaseSizeBeforeUpdate = eventImageRepository.findAll().size();

        // Create the EventImage
        EventImageDTO eventImageDTO = eventImageMapper.toDto(eventImage);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventImageMockMvc.perform(put("/api/event-images")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventImageDTO)))
            .andExpect(status().isBadRequest());

        // Validate the EventImage in the database
        List<EventImage> eventImageList = eventImageRepository.findAll();
        assertThat(eventImageList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteEventImage() throws Exception {
        // Initialize the database
        eventImageRepository.saveAndFlush(eventImage);

        int databaseSizeBeforeDelete = eventImageRepository.findAll().size();

        // Delete the eventImage
        restEventImageMockMvc.perform(delete("/api/event-images/{id}", eventImage.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<EventImage> eventImageList = eventImageRepository.findAll();
        assertThat(eventImageList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventImage.class);
        EventImage eventImage1 = new EventImage();
        eventImage1.setId(1L);
        EventImage eventImage2 = new EventImage();
        eventImage2.setId(eventImage1.getId());
        assertThat(eventImage1).isEqualTo(eventImage2);
        eventImage2.setId(2L);
        assertThat(eventImage1).isNotEqualTo(eventImage2);
        eventImage1.setId(null);
        assertThat(eventImage1).isNotEqualTo(eventImage2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventImageDTO.class);
        EventImageDTO eventImageDTO1 = new EventImageDTO();
        eventImageDTO1.setId(1L);
        EventImageDTO eventImageDTO2 = new EventImageDTO();
        assertThat(eventImageDTO1).isNotEqualTo(eventImageDTO2);
        eventImageDTO2.setId(eventImageDTO1.getId());
        assertThat(eventImageDTO1).isEqualTo(eventImageDTO2);
        eventImageDTO2.setId(2L);
        assertThat(eventImageDTO1).isNotEqualTo(eventImageDTO2);
        eventImageDTO1.setId(null);
        assertThat(eventImageDTO1).isNotEqualTo(eventImageDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(eventImageMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(eventImageMapper.fromId(null)).isNull();
    }
}