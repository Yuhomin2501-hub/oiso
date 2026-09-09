package com.example.oiso.report.dto;

import java.time.LocalDateTime;

public class ReportResponseDto {

    private Integer reportId;
    private String reporterEmail;
    private Integer productId;
    private String productName;
    private String productImagePath;
    private String reportReason;
    private String reportStatus;
    private LocalDateTime regDt;

    public ReportResponseDto(Integer reportId,
                             String reporterEmail,
                             Integer productId,
                             String productName,
                             String productImagePath,
                             String reportReason,
                             String reportStatus,
                             LocalDateTime regDt) {
        this.reportId = reportId;
        this.reporterEmail = reporterEmail;
        this.productId = productId;
        this.productName = productName;
        this.productImagePath = productImagePath;
        this.reportReason = reportReason;
        this.reportStatus = reportStatus;
        this.regDt = regDt;
    }

    public Integer getReportId() {
        return reportId;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductImagePath() {
        return productImagePath;
    }

    public String getReportReason() {
        return reportReason;
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public LocalDateTime getRegDt() {
        return regDt;
    }
}