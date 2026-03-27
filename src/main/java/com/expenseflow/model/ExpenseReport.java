package com.expenseflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_reports")
public class ExpenseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @DecimalMin("0.01")
    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    private String receiptUrl;

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    private String managerComment;

    private String reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(name = "is_flagged")
    private boolean flagged = false;

    @Column(name = "flag_reason")
    private String flagReason;

    public enum ExpenseCategory {
        TRAVEL, MEALS, ACCOMMODATION, EQUIPMENT, SOFTWARE, TRAINING, MARKETING, OTHER
    }

    public enum ApprovalStatus {
        PENDING, UNDER_REVIEW, APPROVED, REJECTED, ESCALATED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }
    public String getManagerComment() { return managerComment; }
    public void setManagerComment(String managerComment) { this.managerComment = managerComment; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }
}
