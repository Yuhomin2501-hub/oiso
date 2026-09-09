package com.example.oiso.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Integer reportId;

    @Column(name = "reporter_email", length = 300, nullable = false)
    private String reporterEmail;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "report_reason", length = 1000, nullable = false)
    private String reportReason;

    @Column(name = "report_status", length = 30, nullable = false)
    private String reportStatus;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    public Report(String reporterEmail, Integer productId, String reportReason, String reportStatus, LocalDateTime regDt) {
        this.reporterEmail = reporterEmail;
        this.productId = productId;
        this.reportReason = reportReason;
        this.reportStatus = reportStatus;
        this.regDt = regDt;
    }

    public void updateReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }
}