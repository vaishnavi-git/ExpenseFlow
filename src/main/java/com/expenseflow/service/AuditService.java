package com.expenseflow.service;

import com.expenseflow.model.PolicyViolation;
import com.expenseflow.repository.ExpenseReportRepository;
import com.expenseflow.repository.PolicyViolationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

    private final PolicyViolationRepository violationRepository;
    private final ExpenseReportRepository expenseReportRepository;

    public AuditService(PolicyViolationRepository violationRepository,
                        ExpenseReportRepository expenseReportRepository) {
        this.violationRepository = violationRepository;
        this.expenseReportRepository = expenseReportRepository;
    }

    public Map<String, Object> getSummary() {
        long totalReports = expenseReportRepository.count();
        long flaggedReports = expenseReportRepository.findByFlaggedTrue().size();
        long openViolations = violationRepository.countOpenViolations();
        long criticalViolations = violationRepository.findBySeverity(PolicyViolation.Severity.CRITICAL).size();

        List<Object[]> typeCounts = violationRepository.countByViolationType();
        Map<String, Long> byType = new HashMap<>();
        for (Object[] row : typeCounts) {
            byType.put(row[0].toString(), (Long) row[1]);
        }

        List<Object[]> categoryData = expenseReportRepository.summarizeByCategory();
        Map<String, Object> byCategory = new HashMap<>();
        for (Object[] row : categoryData) {
            byCategory.put(row[0].toString(), Map.of("count", row[1], "total", row[2]));
        }

        return Map.of(
            "totalReports", totalReports,
            "flaggedReports", flaggedReports,
            "openViolations", openViolations,
            "criticalViolations", criticalViolations,
            "violationsByType", byType,
            "expensesByCategory", byCategory,
            "flagRate", totalReports > 0 ? (double) flaggedReports / totalReports * 100 : 0
        );
    }

    public List<PolicyViolation> getOpenViolations() {
        return violationRepository.findByResolutionStatus(PolicyViolation.ResolutionStatus.OPEN);
    }

    public List<PolicyViolation> getViolationsByEmployee(Long employeeId) {
        return violationRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public PolicyViolation resolveViolation(Long id, String resolvedBy) {
        PolicyViolation v = violationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Violation not found: " + id));
        v.setResolutionStatus(PolicyViolation.ResolutionStatus.RESOLVED);
        v.setResolvedBy(resolvedBy);
        v.setResolvedAt(LocalDateTime.now());
        return violationRepository.save(v);
    }
}
