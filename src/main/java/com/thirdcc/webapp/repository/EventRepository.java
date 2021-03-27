package com.thirdcc.webapp.repository;

import com.thirdcc.webapp.domain.Event;
import com.thirdcc.webapp.domain.enumeration.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;


/**
 * Spring Data  repository for the Event entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findOneByIdAndStatusIn(Long id, Set<EventStatus> eventStatus);

    Page<Event> findEventsByStartDateBetween(Instant fromDate, Instant toDate, Pageable pageable);
}
