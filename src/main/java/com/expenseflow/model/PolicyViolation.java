package com.expenseflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy_violations")
public class PolicyViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_report_id", nullable = false)
    private ExpenseReport expenseReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ViolationType violationType;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ResolutionStatus resolutionStatus = ResolutionStatus.OPEN;

    private String resolvedBy;
    private LocalDateTime resolvedAt;

    public enum ViolationType {
        OVER_LIMIT, MISSING_RECEIPT, DUPLICATE_CLAIM, INVALID_CATEGORY,
        MONTHLY_BUDGET_EXCEEDED, SUSPICIOUS_AMOUNT, LATE_SUBMISSION
    }

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    public enum ResolutionStatus { OPEN, IN_REVIEW, RESOLVED, DISMISSED }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ExpenseReport getExpenseReport() { return expenseReport; }
    public void setExpenseReport(ExpenseReport expenseReport) { this.expenseReport = expenseReport; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public ViolationType getViolationType() { return violationType; }
    public void setViolationType(ViolationType violationType) { this.violationType = violationType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    public ResolutionStatus getResolutionStatus() { return resolutionStatus; }
    public void setResolutionStatus(ResolutionStatus resolutionStatus) { this.resolutionStatus = resolutionStatus; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
