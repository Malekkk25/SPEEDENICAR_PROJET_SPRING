package tn.enicarthage.speedenicar_projet.module_psychologue.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PsychologistDashboardResponse {
    private String psychologistName;
    private String specialization;
    private String officeLocation;

    private Long todayAppointments;
    private Long pendingRequests;
    private Long totalPatients;
    private Long criticalAlerts;
    private Long pendingFollowUps;

    private Long weekSessions;
    private Long monthSessions;
}
