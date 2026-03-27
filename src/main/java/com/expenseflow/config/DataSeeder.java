package com.expenseflow.config;

import com.expenseflow.model.*;
import com.expenseflow.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(UserRepository userRepo,
                               EmployeeRepository empRepo,
                               ExpenseReportRepository expRepo,
                               PasswordEncoder encoder) {
        return args -> {

            // Users
            if (!userRepo.existsByUsername("admin")) {
                User u = new User(); u.setUsername("admin");
                u.setPassword(encoder.encode("admin123"));
                u.setEmail("admin@expenseflow.com");
                u.setRoles(Set.of("ADMIN", "FINANCE_MANAGER")); userRepo.save(u);
            }
            if (!userRepo.existsByUsername("manager")) {
                User u = new User(); u.setUsername("manager");
                u.setPassword(encoder.encode("manager123"));
                u.setEmail("manager@expenseflow.com");
                u.setRoles(Set.of("MANAGER")); userRepo.save(u);
            }
            if (!userRepo.existsByUsername("employee")) {
                User u = new User(); u.setUsername("employee");
                u.setPassword(encoder.encode("employee123"));
                u.setEmail("employee@expenseflow.com");
                u.setRoles(Set.of("EMPLOYEE")); userRepo.save(u);
            }

            // Manager employee
            Employee mgr = new Employee();
            mgr.setFirstName("Diana"); mgr.setLastName("Prince");
            mgr.setEmail("d.prince@company.com");
            mgr.setDepartment("Engineering"); mgr.setRole("Engineering Manager");
            mgr.setMonthlyExpenseLimit(new BigDecimal("8000.00"));
            empRepo.save(mgr);

            // Regular employees
            Employee e1 = new Employee();
            e1.setFirstName("James"); e1.setLastName("Rhodes");
            e1.setEmail("j.rhodes@company.com");
            e1.setDepartment("Engineering"); e1.setRole("Software Engineer");
            e1.setMonthlyExpenseLimit(new BigDecimal("2000.00"));
            e1.setManager(mgr); empRepo.save(e1);

            Employee e2 = new Employee();
            e2.setFirstName("Maria"); e2.setLastName("Hill");
            e2.setEmail("m.hill@company.com");
            e2.setDepartment("Sales"); e2.setRole("Sales Executive");
            e2.setMonthlyExpenseLimit(new BigDecimal("3000.00"));
            e2.setManager(mgr); empRepo.save(e2);

            Employee e3 = new Employee();
            e3.setFirstName("Nick"); e3.setLastName("Fury");
            e3.setEmail("n.fury@company.com");
            e3.setDepartment("Operations"); e3.setRole("Operations Lead");
            e3.setMonthlyExpenseLimit(new BigDecimal("5000.00"));
            e3.setManager(mgr); empRepo.save(e3);

            // Normal approved expense
            ExpenseReport r1 = new ExpenseReport();
            r1.setEmployee(e1); r1.setTitle("AWS re:Invent Conference");
            r1.setDescription("Annual AWS conference registration fee");
            r1.setCategory(ExpenseReport.ExpenseCategory.TRAINING);
            r1.setAmount(new BigDecimal("1299.00"));
            r1.setExpenseDate(LocalDate.now().minusDays(5));
            r1.setReceiptUrl("https://receipts.company.com/aws-reinvent-2024.pdf");
            r1.setStatus(ExpenseReport.ApprovalStatus.APPROVED);
            r1.setReviewedBy("manager"); r1.setReviewedAt(LocalDateTime.now().minusDays(3));
            expRepo.save(r1);

            // Over single claim limit - triggers OVER_LIMIT CRITICAL flag
            ExpenseReport r2 = new ExpenseReport();
            r2.setEmployee(e2); r2.setTitle("Client Entertainment — Q4 Dinner");
            r2.setDescription("Executive client dinner for Q4 deal closure");
            r2.setCategory(ExpenseReport.ExpenseCategory.MEALS);
            r2.setAmount(new BigDecimal("6200.00"));
            r2.setExpenseDate(LocalDate.now().minusDays(2));
            r2.setReceiptUrl("https://receipts.company.com/q4-dinner.pdf");
            r2.setStatus(ExpenseReport.ApprovalStatus.PENDING);
            r2.setFlagged(true);
            r2.setFlagReason("Claim of $6200.00 exceeds the single-claim limit of $5000.00");
            expRepo.save(r2);

            // Missing receipt - triggers MISSING_RECEIPT HIGH flag
            ExpenseReport r3 = new ExpenseReport();
            r3.setEmployee(e3); r3.setTitle("Team Offsite Travel");
            r3.setDescription("Flight and accommodation for Q3 team offsite");
            r3.setCategory(ExpenseReport.ExpenseCategory.TRAVEL);
            r3.setAmount(new BigDecimal("850.00"));
            r3.setExpenseDate(LocalDate.now().minusDays(10));
            r3.setStatus(ExpenseReport.ApprovalStatus.PENDING);
            r3.setFlagged(true);
            r3.setFlagReason("Expense of $850.00 requires a receipt but none was attached");
            expRepo.save(r3);

            // Late submission - triggers LATE_SUBMISSION flag
            ExpenseReport r4 = new ExpenseReport();
            r4.setEmployee(e1); r4.setTitle("Software License — JetBrains");
            r4.setDescription("Annual IntelliJ IDEA license renewal");
            r4.setCategory(ExpenseReport.ExpenseCategory.SOFTWARE);
            r4.setAmount(new BigDecimal("249.00"));
            r4.setExpenseDate(LocalDate.now().minusDays(45));
            r4.setReceiptUrl("https://receipts.company.com/jetbrains-2024.pdf");
            r4.setStatus(ExpenseReport.ApprovalStatus.PENDING);
            r4.setFlagged(true);
            r4.setFlagReason("Submitted 45 days after the event, exceeding the 30-day policy window");
            expRepo.save(r4);

            System.out.println(">>> ExpenseFlow seeded. Login: admin/admin123 | manager/manager123 | employee/employee123");
        };
    }
}
