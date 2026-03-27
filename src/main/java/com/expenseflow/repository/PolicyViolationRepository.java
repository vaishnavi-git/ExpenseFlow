package com.expenseflow.repository;

import com.expenseflow.model.PolicyViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PolicyViolationRepository extends JpaRepository<PolicyViolation, Long> {
    List<PolicyViolation> findByEmployeeId(Long employeeId);
    List<PolicyViolation> findByResolutionStatus(PolicyViolation.ResolutionStatus status);
    List<PolicyViolation> findBySeverity(PolicyViolation.Severity severity);
    List<PolicyViolation> findByViolationType(PolicyViolation.ViolationType type);

    @Query("SELECT COUNT(v) FROM PolicyViolation v WHERE v.resolutionStatus = 'OPEN'")
    long countOpenViolations();

    @Query("SELECT v.violationType, COUNT(v) FROM PolicyViolation v GROUP BY v.violationType")
    List<Object[]> countByViolationType();
}
