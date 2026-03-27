package com.expenseflow.service;

import com.expenseflow.model.*;
import com.expenseflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PolicyEngineService {

    private static final BigDecimal SINGLE_CLAIM_LIMIT = new BigDecimal("5000.00");
    private static final BigDecimal MEAL_LIMIT = new BigDecimal("150.00");
    private static final BigDecimal SUSPICIOUS_ROUND_AMOUNT = new BigDecimal("1000.00");
    private static final int LATE_SUBMISSION_DAYS = 30;

    private final PolicyViolationRepository violationRepository;
    private final ExpenseReportRepository expenseReportRepository;

    public PolicyEngineService(PolicyViolationRepository violationRepository,
                               ExpenseReportRepository expenseReportRepository) {
        this.violationRepository = violationRepository;
        this.expenseReportRepository = expenseReportRepository;
    }

    @Transactional
    public void evaluate(ExpenseReport report) {
        checkReceiptRequired(report);
        checkSingleClaimLimit(report);
        checkMealLimit(report);
        checkMonthlyBudget(report);
        checkDuplicateClaim(report);
        checkLateSubmission(report);
        checkSuspiciousAmount(report);
    }

    private void checkReceiptRequired(ExpenseReport report) {
        if (report.getAmount().compareTo(new BigDecimal("25.00")) > 0
                && (report.getReceiptUrl() == null || report.getReceiptUrl().isBlank())) {
            flag(report, PolicyViolation.ViolationType.MISSING_RECEIPT,
                    PolicyViolation.Severity.HIGH,
                    String.format("Expense of $%.2f requires a receipt but none was attached", report.getAmount()));
        }
    }

    private void checkSingleClaimLimit(ExpenseReport report) {
        if (report.getAmount().compareTo(SINGLE_CLAIM_LIMIT) > 0) {
            flag(report, PolicyViolation.ViolationType.OVER_LIMIT,
                    PolicyViolation.Severity.CRITICAL,
                    String.format("Claim of $%.2f exceeds the single-claim limit of $%.2f — requires finance approval",
                            report.getAmount(), SINGLE_CLAIM_LIMIT));
        }
    }

    private void checkMealLimit(ExpenseReport report) {
        if (report.getCategory() == ExpenseReport.ExpenseCategory.MEALS
                && report.getAmount().compareTo(MEAL_LIMIT) > 0) {
            flag(report, PolicyViolation.ViolationType.INVALID_CATEGORY,
                    PolicyViolation.Severity.MEDIUM,
                    String.format("Meal expense of $%.2f exceeds the per-meal policy limit of $%.2f",
                            report.getAmount(), MEAL_LIMIT));
        }
    }

    private void checkMonthlyBudget(ExpenseReport report) {
        LocalDate start = report.getExpenseDate().withDayOfMonth(1);
        LocalDate end = report.getExpenseDate().withDayOfMonth(report.getExpenseDate().lengthOfMonth());

        BigDecimal monthlyTotal = expenseReportRepository
                .sumApprovedAmountByEmployeeAndMonth(report.getEmployee().getId(), start, end);

        BigDecimal limit = report.getEmployee().getMonthlyExpenseLimit();
        if (limit != null && monthlyTotal.add(report.getAmount()).compareTo(limit) > 0) {
            flag(report, PolicyViolation.ViolationType.MONTHLY_BUDGET_EXCEEDED,
                    PolicyViolation.Severity.HIGH,
                    String.format("This claim would bring monthly total to $%.2f, exceeding the $%.2f monthly limit",
                            monthlyTotal.add(report.getAmount()), limit));
        }
    }

    private void checkDuplicateClaim(ExpenseReport report) {
        LocalDate dayBefore = report.getExpenseDate().minusDays(1);
        LocalDate dayAfter = report.getExpenseDate().plusDays(1);

        expenseReportRepository
                .findByEmployeeAndDateRange(report.getEmployee().getId(), dayBefore, dayAfter)
                .stream()
                .filter(r -> !r.getId().equals(report.getId()))
                .filter(r -> r.getAmount().compareTo(report.getAmount()) == 0)
                .filter(r -> r.getCategory() == report.getCategory())
                .findAny()
                .ifPresent(duplicate -> flag(report, PolicyViolation.ViolationType.DUPLICATE_CLAIM,
                        PolicyViolation.Severity.CRITICAL,
                        String.format("Possible duplicate of expense report #%d submitted on %s for the same amount and category",
                                duplicate.getId(), duplicate.getExpenseDate())));
    }

    private void checkLateSubmission(ExpenseReport report) {
        long daysSinceExpense = java.time.temporal.ChronoUnit.DAYS
                .between(report.getExpenseDate(), LocalDate.now());
        if (daysSinceExpense > LATE_SUBMISSION_DAYS) {
            flag(report, PolicyViolation.ViolationType.LATE_SUBMISSION,
                    PolicyViolation.Severity.MEDIUM,
                    String.format("Expense dated %s was submitted %d days after the event, exceeding the %d-day policy window",
                            report.getExpenseDate(), daysSinceExpense, LATE_SUBMISSION_DAYS));
        }
    }

    private void checkSuspiciousAmount(ExpenseReport report) {
        BigDecimal remainder = report.getAmount()
                .remainder(SUSPICIOUS_ROUND_AMOUNT);
        if (remainder.compareTo(BigDecimal.ZERO) == 0
                && report.getAmount().compareTo(SUSPICIOUS_ROUND_AMOUNT) >= 0) {
            flag(report, PolicyViolation.ViolationType.SUSPICIOUS_AMOUNT,
                    PolicyViolation.Severity.LOW,
                    String.format("Claim of $%.2f is a round number — please verify accuracy with receipt", report.getAmount()));
        }
    }

    private void flag(ExpenseReport report, PolicyViolation.ViolationType type,
                      PolicyViolation.Severity severity, String description) {
        report.setFlagged(true);
        report.setFlagReason(description);

        PolicyViolation violation = new PolicyViolation();
        violation.setExpenseReport(report);
        violation.setEmployee(report.getEmployee());
        violation.setViolationType(type);
        violation.setSeverity(severity);
        violation.setDescription(description);
        violationRepository.save(violation);
    }
}
