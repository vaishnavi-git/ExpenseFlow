package com.expenseflow.service;

import com.expenseflow.exception.ResourceNotFoundException;
import com.expenseflow.model.*;
import com.expenseflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseReportRepository expenseReportRepository;
    private final EmployeeRepository employeeRepository;
    private final PolicyEngineService policyEngineService;

    public ExpenseService(ExpenseReportRepository expenseReportRepository,
                          EmployeeRepository employeeRepository,
                          PolicyEngineService policyEngineService) {
        this.expenseReportRepository = expenseReportRepository;
        this.employeeRepository = employeeRepository;
        this.policyEngineService = policyEngineService;
    }

    @Transactional
    public ExpenseReport submit(ExpenseReport report, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
        report.setEmployee(employee);
        report.setStatus(ExpenseReport.ApprovalStatus.PENDING);
        report.setSubmittedAt(LocalDateTime.now());
        expenseReportRepository.save(report);
        policyEngineService.evaluate(report);
        return expenseReportRepository.save(report);
    }

    @Transactional
    public ExpenseReport approve(Long reportId, String approvedBy, String comment) {
        ExpenseReport report = getById(reportId);
        report.setStatus(ExpenseReport.ApprovalStatus.APPROVED);
        report.setReviewedBy(approvedBy);
        report.setManagerComment(comment);
        report.setReviewedAt(LocalDateTime.now());
        return expenseReportRepository.save(report);
    }

    @Transactional
    public ExpenseReport reject(Long reportId, String rejectedBy, String comment) {
        ExpenseReport report = getById(reportId);
        report.setStatus(ExpenseReport.ApprovalStatus.REJECTED);
        report.setReviewedBy(rejectedBy);
        report.setManagerComment(comment);
        report.setReviewedAt(LocalDateTime.now());
        return expenseReportRepository.save(report);
    }

    @Transactional
    public ExpenseReport escalate(Long reportId, String escalatedBy) {
        ExpenseReport report = getById(reportId);
        report.setStatus(ExpenseReport.ApprovalStatus.ESCALATED);
        report.setReviewedBy(escalatedBy);
        report.setReviewedAt(LocalDateTime.now());
        return expenseReportRepository.save(report);
    }

    public ExpenseReport getById(Long id) {
        return expenseReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense report not found: " + id));
    }

    public List<ExpenseReport> getByEmployee(Long employeeId) {
        return expenseReportRepository.findByEmployeeId(employeeId);
    }

    public List<ExpenseReport> getPending() {
        return expenseReportRepository.findByStatus(ExpenseReport.ApprovalStatus.PENDING);
    }

    public List<ExpenseReport> getFlagged() {
        return expenseReportRepository.findByFlaggedTrue();
    }

    public List<ExpenseReport> getAll() {
        return expenseReportRepository.findAll();
    }
}
