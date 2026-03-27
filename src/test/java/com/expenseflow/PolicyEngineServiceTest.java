package com.expenseflow;

import com.expenseflow.model.*;
import com.expenseflow.repository.*;
import com.expenseflow.service.PolicyEngineService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyEngineServiceTest {

    @Mock
    private PolicyViolationRepository violationRepository;

    @Mock
    private ExpenseReportRepository expenseReportRepository;

    @InjectMocks
    private PolicyEngineService policyEngineService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Test");
        employee.setLastName("User");
        employee.setEmail("test@company.com");
        employee.setDepartment("Engineering");
        employee.setRole("Engineer");
        employee.setMonthlyExpenseLimit(new BigDecimal("2000.00"));
    }

    @Test
    @DisplayName("Normal expense under all limits should not generate any violations")
    void testNormalExpense_NoViolations() {
        ExpenseReport report = buildReport(new BigDecimal("50.00"),
                ExpenseReport.ExpenseCategory.MEALS, true);

        when(expenseReportRepository.sumApprovedAmountByEmployeeAndMonth(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(expenseReportRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        policyEngineService.evaluate(report);

        verify(violationRepository, never()).save(any());
        assertFalse(report.isFlagged());
    }

    @Test
    @DisplayName("Expense over $5000 should generate OVER_LIMIT CRITICAL violation")
    void testOverSingleClaimLimit_ShouldFlagCritical() {
        ExpenseReport report = buildReport(new BigDecimal("5500.00"),
                ExpenseReport.ExpenseCategory.TRAVEL, true);

        when(expenseReportRepository.sumApprovedAmountByEmployeeAndMonth(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(expenseReportRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        policyEngineService.evaluate(report);

        assertTrue(report.isFlagged());
        verify(violationRepository, atLeastOnce()).save(argThat(v ->
                v.getViolationType() == PolicyViolation.ViolationType.OVER_LIMIT &&
                v.getSeverity() == PolicyViolation.Severity.CRITICAL
        ));
    }

    @Test
    @DisplayName("Expense over $25 without receipt should generate MISSING_RECEIPT HIGH violation")
    void testMissingReceipt_ShouldFlagHigh() {
        ExpenseReport report = buildReport(new BigDecimal("120.00"),
                ExpenseReport.ExpenseCategory.TRAVEL, false);

        when(expenseReportRepository.sumApprovedAmountByEmployeeAndMonth(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(expenseReportRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        policyEngineService.evaluate(report);

        assertTrue(report.isFlagged());
        verify(violationRepository, atLeastOnce()).save(argThat(v ->
                v.getViolationType() == PolicyViolation.ViolationType.MISSING_RECEIPT
        ));
    }

    @Test
    @DisplayName("Meal expense over $150 should generate INVALID_CATEGORY MEDIUM violation")
    void testMealOverLimit_ShouldFlagMedium() {
        ExpenseReport report = buildReport(new BigDecimal("200.00"),
                ExpenseReport.ExpenseCategory.MEALS, true);

        when(expenseReportRepository.sumApprovedAmountByEmployeeAndMonth(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(expenseReportRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        policyEngineService.evaluate(report);

        assertTrue(report.isFlagged());
        verify(violationRepository, atLeastOnce()).save(argThat(v ->
                v.getViolationType() == PolicyViolation.ViolationType.INVALID_CATEGORY &&
                v.getSeverity() == PolicyViolation.Severity.MEDIUM
        ));
    }

    @Test
    @DisplayName("Monthly budget exceeded should generate MONTHLY_BUDGET_EXCEEDED violation")
    void testMonthlyBudgetExceeded_ShouldFlag() {
        ExpenseReport report = buildReport(new BigDecimal("500.00"),
                ExpenseReport.ExpenseCategory.EQUIPMENT, true);

        when(expenseReportRepository.sumApprovedAmountByEmployeeAndMonth(any(), any(), any()))
                .thenReturn(new BigDecimal("1800.00"));
        when(expenseReportRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        policyEngineService.evaluate(report);

        assertTrue(report.isFlagged());
        verify(violationRepository, atLeastOnce()).save(argThat(v ->
                v.getViolationType() == PolicyViolation.ViolationType.MONTHLY_BUDGET_EXCEEDED
        ));
    }

    @Test
    @DisplayName("Duplicate claim same amount and category should flag DUPLICATE_CLAIM CRITICAL")
    void testDuplicateClaim_ShouldFlagCritical() {
        ExpenseReport existing = buildReport(new BigDecimal("300.00"),
                ExpenseReport.ExpenseCategory.TRAVEL, true);
        existing.setId(99L);

        ExpenseReport newReport = buildReport(new BigDecimal("300.00"),
                ExpenseReport.ExpenseCategory.TRAVEL, true);

        when(expenseReportRepository.sumApprovedAmountByEmployeeAndMonth(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(expenseReportRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(java.util.List.of(existing));

        policyEngineService.evaluate(newReport);

        verify(violationRepository, atLeastOnce()).save(argThat(v ->
                v.getViolationType() == PolicyViolation.ViolationType.DUPLICATE_CLAIM &&
                v.getSeverity() == PolicyViolation.Severity.CRITICAL
        ));
    }

    private ExpenseReport buildReport(BigDecimal amount,
                                       ExpenseReport.ExpenseCategory category,
                                       boolean hasReceipt) {
        ExpenseReport report = new ExpenseReport();
        report.setEmployee(employee);
        report.setTitle("Test Expense");
        report.setDescription("Test");
        report.setAmount(amount);
        report.setCategory(category);
        report.setExpenseDate(LocalDate.now());
        if (hasReceipt) report.setReceiptUrl("https://receipts.company.com/test.pdf");
        report.setStatus(ExpenseReport.ApprovalStatus.PENDING);
        return report;
    }
}
