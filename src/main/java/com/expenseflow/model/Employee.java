package com.expenseflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    private String department;

    @NotBlank
    private String role;

    @DecimalMin("0.0")
    private BigDecimal monthlyExpenseLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    public enum EmploymentStatus { ACTIVE, INACTIVE }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public BigDecimal getMonthlyExpenseLimit() { return monthlyExpenseLimit; }
    public void setMonthlyExpenseLimit(BigDecimal monthlyExpenseLimit) { this.monthlyExpenseLimit = monthlyExpenseLimit; }
    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }
    public EmploymentStatus getStatus() { return status; }
    public void setStatus(EmploymentStatus status) { this.status = status; }
}
