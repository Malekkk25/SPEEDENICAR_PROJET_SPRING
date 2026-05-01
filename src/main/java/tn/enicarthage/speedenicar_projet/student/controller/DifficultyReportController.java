package tn.enicarthage.speedenicar_projet.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.student.dto.request.DifficultyReportRequest;
import tn.enicarthage.speedenicar_projet.student.service.DifficultyReportService;

import java.security.Principal;



import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.speedenicar_projet.student.dto.request.DifficultyReportRequest;
import tn.enicarthage.speedenicar_projet.student.service.DifficultyReportService;

import java.security.Principal;

@RestController // 👈 INDISPENSABLE
@RequestMapping("/api/student/reports") // 👈 C'est ça que Spring cherche !
@RequiredArgsConstructor
public class DifficultyReportController {

    private final DifficultyReportService reportService;

    @PostMapping
    public ResponseEntity<?> submitReport(@RequestBody DifficultyReportRequest request, Principal principal) {
        return ResponseEntity.ok(reportService.createReport(principal.getName(), request));
    }

    @GetMapping
    public ResponseEntity<?> getMyReports(Principal principal) {
        return ResponseEntity.ok(reportService.getStudentReports(principal.getName()));
    }
}
