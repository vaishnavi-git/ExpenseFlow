package com.expenseflow.repository;

import com.expenseflow.model.ExpenseReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseReportRepository extends JpaRepository<ExpenseReport, Long> {

    List<ExpenseReport> findByEmployeeId(Long employeeId);

    List<ExpenseReport> findByStatus(ExpenseReport.ApprovalStatus status);

    List<ExpenseReport> findByFlaggedTrue();

    @Query("SELECT e FROM ExpenseReport e WHERE e.employee.id = :empId AND e.expenseDate BETWEEN :start AND :end")
    List<ExpenseReport> findByEmployeeAndDateRange(
        @Param("empId") Long employeeId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseReport e WHERE e.employee.id = :empId AND e.expenseDate BETWEEN :start AND :end AND e.status != 'REJECTED'")
    BigDecimal sumApprovedAmountByEmployeeAndMonth(
        @Param("empId") Long employeeId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    @Query("SELECT e FROM ExpenseReport e WHERE e.amount > :threshold")
    List<ExpenseReport> findHighValueReports(@Param("threshold") BigDecimal threshold);

    @Query("SELECT e.category, COUNT(e), SUM(e.amount) FROM ExpenseReport e GROUP BY e.category")
    List<Object[]> summarizeByCategory();
}
