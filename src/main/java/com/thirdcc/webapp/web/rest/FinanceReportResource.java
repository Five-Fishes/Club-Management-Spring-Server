package com.thirdcc.webapp.web.rest;

import com.thirdcc.webapp.domain.enumeration.TransactionType;
import com.thirdcc.webapp.service.FinanceReportService;
import com.thirdcc.webapp.service.YearSessionService;
import com.thirdcc.webapp.service.dto.FinanceReportDTO;
import com.thirdcc.webapp.utils.YearSessionUtils;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class FinanceReportResource {
    private final Logger log = LoggerFactory.getLogger(FinanceReportResource.class);

    private static final String ENTITY_NAME = "financeReport";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FinanceReportService financeReportService;

    private final YearSessionService yearSessionService;

    public FinanceReportResource(FinanceReportService financeReportService, YearSessionService yearSessionService) {
        this.financeReportService = financeReportService;
        this.yearSessionService = yearSessionService;
    }

    @GetMapping("/finance-report")
    public ResponseEntity<List<FinanceReportDTO>> getAllEventFinanceReport(
        Pageable pageable,
        @RequestParam MultiValueMap<String, String> queryParams,
        UriComponentsBuilder uriBuilder
    ) {
        log.debug("REST request to get a page of FinanceReport");
        Page<FinanceReportDTO> page = financeReportService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/finance-report/event/{eventId}")
    public ResponseEntity<FinanceReportDTO> getFinanceReportByEventId(@PathVariable Long eventId) {
        log.debug("REST request to get FinanceReport by eventId: {}", eventId);
        Optional<FinanceReportDTO> financeReportDTO = financeReportService.findOneByEventId(eventId);
        return ResponseUtil.wrapOrNotFound(financeReportDTO);
    }

    @GetMapping("/finance-report/year-session")
    public ResponseEntity<Map<TransactionType, Map<Month, BigDecimal>>> getFinanceReportByYearSession(
        @RequestParam(required = false) Long yearSessionId
    ) {
        log.debug("REST request to get FinanceReport by yearSessionId: {}", yearSessionId);
        String yearSession = null;
        if (yearSessionId == null) {
            yearSession = yearSessionService.getDefaultYearSessionString();
        } else {
            yearSession = yearSessionService.getYearSessionStringById(yearSessionId);
        }
        Map<TransactionType, Map<Month, BigDecimal>> result = financeReportService.getFinanceReportByYearSession(yearSession);
        return ResponseEntity.ok().headers((HttpHeaders) null).body(result);
    }
}
