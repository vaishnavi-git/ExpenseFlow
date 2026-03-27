package com.expenseflow.controller;

import com.expenseflow.model.PolicyViolation;
import com.expenseflow.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(auditService.getSummary());
    }

    @GetMapping("/violations")
    public ResponseEntity<List<PolicyViolation>> getOpenViolations() {
        return ResponseEntity.ok(auditService.getOpenViolations());
    }

    @GetMapping("/violations/employee/{employeeId}")
    public ResponseEntity<List<PolicyViolation>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(auditService.getViolationsByEmployee(employeeId));
    }

    @PutMapping("/violations/{id}/resolve")
    public ResponseEntity<PolicyViolation> resolve(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(auditService.resolveViolation(id, user.getUsername()));
    }
}
