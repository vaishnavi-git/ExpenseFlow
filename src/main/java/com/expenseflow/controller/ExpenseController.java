package com.expenseflow.controller;

import com.expenseflow.model.ExpenseReport;
import com.expenseflow.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseReport>> getAll() {
        return ResponseEntity.ok(expenseService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseReport> getById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(id));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ExpenseReport>> getPending() {
        return ResponseEntity.ok(expenseService.getPending());
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<ExpenseReport>> getFlagged() {
        return ResponseEntity.ok(expenseService.getFlagged());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ExpenseReport>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(expenseService.getByEmployee(employeeId));
    }

    @PostMapping("/submit/{employeeId}")
    public ResponseEntity<ExpenseReport> submit(@PathVariable Long employeeId,
                                                @RequestBody ExpenseReport report) {
        return ResponseEntity.ok(expenseService.submit(report, employeeId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ExpenseReport> approve(@PathVariable Long id,
                                                  @RequestParam(required = false) String comment,
                                                  @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(expenseService.approve(id, user.getUsername(), comment));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ExpenseReport> reject(@PathVariable Long id,
                                                 @RequestParam String comment,
                                                 @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(expenseService.reject(id, user.getUsername(), comment));
    }

    @PutMapping("/{id}/escalate")
    public ResponseEntity<ExpenseReport> escalate(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(expenseService.escalate(id, user.getUsername()));
    }
}
