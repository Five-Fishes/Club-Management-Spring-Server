package com.thirdcc.webapp.service.impl;

import com.thirdcc.webapp.domain.Budget;
import com.thirdcc.webapp.domain.Transaction;
import com.thirdcc.webapp.domain.enumeration.TransactionStatus;
import com.thirdcc.webapp.domain.enumeration.TransactionType;
import com.thirdcc.webapp.repository.BudgetRepository;
import com.thirdcc.webapp.repository.TransactionRepository;
import com.thirdcc.webapp.service.EventService;
import com.thirdcc.webapp.service.FinanceReportService;
import com.thirdcc.webapp.service.dto.EventDTO;
import com.thirdcc.webapp.service.dto.FinanceReportDTO;
import com.thirdcc.webapp.service.dto.FinanceReportStatisticDTO;
import com.thirdcc.webapp.utils.YearSessionUtils;
import io.github.jhipster.web.util.PageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinanceReportServiceImpl implements FinanceReportService {

    private final Logger log = LoggerFactory.getLogger(FinanceReportServiceImpl.class);

    private final EventService eventService;

    private final BudgetRepository budgetRepository;

    private final TransactionRepository transactionRepository;

    public FinanceReportServiceImpl(
        EventService eventService,
        BudgetRepository budgetRepository,
        TransactionRepository transactionRepository
    ) {
        this.eventService = eventService;
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinanceReportDTO> findAll(Pageable pageable) {
        List<FinanceReportDTO> financeReportDTOList = eventService
            .findAll(Pageable.unpaged())
            .stream()
            .map(this::toFinanceReportDTO)
            .map(this::mapTotalBudgetIncome)
            .map(this::mapTotalBudgetExpenses)
            .map(this::mapTotalIncome)
            .map(this::mapTotalExpenses)
            .collect(Collectors.toList());
        return PageUtil.createPageFromList(financeReportDTOList, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinanceReportDTO> findOneByEventId(Long eventId) {
        return eventService
            .findOne(eventId)
            .map(this::toFinanceReportDTO)
            .map(this::mapTotalBudgetIncome)
            .map(this::mapTotalBudgetExpenses)
            .map(this::mapTotalIncome)
            .map(this::mapTotalExpenses);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<TransactionType, Map<Month, BigDecimal>> getFinanceReportByYearSession(String yearSession) {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant inclusiveFrom = YearSessionUtils.getFirstInstantOfYearSession(yearSession);
        Instant exclusiveTo = inclusiveFrom.atZone(zoneId).toLocalDate().plusYears(1).atStartOfDay().atZone(zoneId).toInstant();
        log.info("getYearSessionFinanceReport inclusiveFrom {} exclusiveTo {}", inclusiveFrom, exclusiveTo);

        List<Transaction> transactionList = transactionRepository
            .findAllByCreatedDateGreaterThanEqualAndCreatedDateLessThan(inclusiveFrom, exclusiveTo);

        Map<TransactionType, Map<Month, BigDecimal>> result = new HashMap<>();
        result.put(TransactionType.INCOME, null);
        result.put(TransactionType.EXPENSE, null);

        for (Map.Entry<TransactionType, Map<Month, BigDecimal>> entry : result.entrySet()) {
            Map<Month, BigDecimal> defaultMap = new HashMap<>();
            YearSessionUtils.getAllMonthsOfYearSession()
                .forEach(month -> defaultMap.put(month, BigDecimal.ZERO));

            entry.setValue(defaultMap);
        }

        for (Transaction transaction : transactionList) {
            Month transactionMonth = transaction.getCreatedDate().atZone(zoneId).getMonth();
            TransactionType transactionType = transaction.getTransactionType();
            BigDecimal transactionAmount = transaction.getTransactionAmount();

            BigDecimal currentValue = result.get(transactionType).get(transactionMonth);
            result.get(transactionType).put(transactionMonth, currentValue.add(transactionAmount));
        }
        return result;
    }
    
    /**
     * get all transaction of current year session and calculate realiseIncome
     * pendingIncome, realiseExpense, pendingExpense, invalidExpense and badDebt
     * @return FinanceReportStatisticDTO 
     */
    @Override
    @Transactional(readOnly = true)
    public FinanceReportStatisticDTO getFinanceReportStatisticOfCurrentYearSession() {
        String currentYearSession = YearSessionUtils.getCurrentYearSession();
        String nextYearSession = YearSessionUtils.getNextYearSession();
        Instant inclusiveFrom = YearSessionUtils.getFirstInstantOfYearSession(currentYearSession);
        Instant exclusiveTo = YearSessionUtils.getFirstInstantOfYearSession(nextYearSession);
        log.info("getCurrentYearSessionFinanceReportStatistic inclusiveFrom {} exclusiveTo {}", inclusiveFrom, exclusiveTo);

        FinanceReportStatisticDTO financeReportStatisticDTO = new FinanceReportStatisticDTO();
        
        List<Transaction> transactionList = transactionRepository
            .findAllByCreatedDateGreaterThanEqualAndCreatedDateLessThan(inclusiveFrom, exclusiveTo);

        BigDecimal realiseExpense = BigDecimal.ZERO;
        BigDecimal pendingExpense = BigDecimal.ZERO;
        BigDecimal invalidExpense = BigDecimal.ZERO;
        BigDecimal realiseIncome = BigDecimal.ZERO;
        BigDecimal pendingIncome = BigDecimal.ZERO;
        BigDecimal badDebt = BigDecimal.ZERO;
        
        for (Transaction transaction : transactionList) {
            TransactionType transactionType = transaction.getTransactionType();
            TransactionStatus transactionStatus = transaction.getTransactionStatus();
            BigDecimal transactionAmount = transaction.getTransactionAmount();

            switch(transactionType){
                case INCOME:
                    switch(transactionStatus){
                        case COMPLETED: realiseIncome = realiseIncome.add(transactionAmount);
                        break;
                        case PENDING: pendingIncome = pendingIncome.add(transactionAmount);
                        break;
                        case INVALID: badDebt = badDebt.add(transactionAmount);
                        break;
                    }
                    break;
                case EXPENSE:
                    switch(transactionStatus){
                        case COMPLETED: realiseExpense = realiseExpense.add(transactionAmount);
                        break;
                        case PENDING: pendingExpense = pendingExpense.add(transactionAmount);
                        break;
                        case INVALID: invalidExpense = invalidExpense.add(transactionAmount);
                        break;
                    }
                    break;
            }
        }
        financeReportStatisticDTO.setBadDebt(badDebt);
        financeReportStatisticDTO.setInvalidExpense(invalidExpense);
        financeReportStatisticDTO.setPendingExpense(pendingExpense);
        financeReportStatisticDTO.setPendingIncome(pendingIncome);
        financeReportStatisticDTO.setRealiseExpense(realiseExpense);
        financeReportStatisticDTO.setRealiseIncome(realiseIncome);
        log.debug("getCurrentYearSessionFinanceReportStatistic returned: {}", financeReportStatisticDTO);
        
        return financeReportStatisticDTO;
    }

    private FinanceReportDTO toFinanceReportDTO(EventDTO eventDTO) {
        FinanceReportDTO financeReportDTO = new FinanceReportDTO();
        financeReportDTO.setEventDTO(eventDTO);
        return financeReportDTO;
    }

    private FinanceReportDTO mapTotalExpenses(FinanceReportDTO financeReportDTO) {
        BigDecimal totalExpenses = transactionRepository
            .findAllByEventIdAndTransactionType(financeReportDTO.getEventDTO().getId(), TransactionType.EXPENSE)
            .stream()
            .map(Transaction::getTransactionAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        financeReportDTO.setTotalExpenses(totalExpenses);
        return financeReportDTO;
    }

    private FinanceReportDTO mapTotalIncome(FinanceReportDTO financeReportDTO) {
        BigDecimal totalIncome = transactionRepository
            .findAllByEventIdAndTransactionType(financeReportDTO.getEventDTO().getId(), TransactionType.INCOME)
            .stream()
            .map(Transaction::getTransactionAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        financeReportDTO.setTotalIncome(totalIncome);
        return financeReportDTO;
    }

    private FinanceReportDTO mapTotalBudgetExpenses(FinanceReportDTO financeReportDTO) {
        BigDecimal totalBudgetExpenses = budgetRepository
            .findAllByEventIdAndType(financeReportDTO.getEventDTO().getId(), TransactionType.EXPENSE)
            .stream()
            .map(Budget::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        financeReportDTO.setTotalBudgetExpenses(totalBudgetExpenses);
        return financeReportDTO;
    }

    private FinanceReportDTO mapTotalBudgetIncome(FinanceReportDTO financeReportDTO) {
        BigDecimal totalBudgetIncome = budgetRepository
            .findAllByEventIdAndType(financeReportDTO.getEventDTO().getId(), TransactionType.INCOME)
            .stream()
            .map(Budget::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        financeReportDTO.setTotalBudgetIncome(totalBudgetIncome);
        return financeReportDTO;
    }
}
