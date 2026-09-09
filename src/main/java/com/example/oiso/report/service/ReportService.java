package com.example.oiso.report.service;

import com.example.oiso.product.entity.Product;
import com.example.oiso.product.repository.ProductRepository;
import com.example.oiso.report.dto.ReportResponseDto;
import com.example.oiso.report.entity.Report;
import com.example.oiso.report.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;

    public ReportService(ReportRepository reportRepository, ProductRepository productRepository) {
        this.reportRepository = reportRepository;
        this.productRepository = productRepository;
    }

    public String registerReport(String loginUserEmail, Integer productId, String reportReason) {
        if (productId == null) {
            return "상품번호는 필수입니다.";
        }

        if (reportReason == null || reportReason.trim().isEmpty()) {
            return "신고사유는 필수입니다.";
        }

        Optional<Product> optionalProduct = productRepository.findById(productId);

        if (optionalProduct.isEmpty()) {
            return "신고할 상품이 없습니다.";
        }

        Product product = optionalProduct.get();

        if ("삭제됨".equals(product.getProductStatus())) {
            return "삭제된 상품은 신고할 수 없습니다.";
        }

        if (product.getUserEmail().equals(loginUserEmail)) {
            return "본인 상품은 신고할 수 없습니다.";
        }

        boolean alreadyReported = reportRepository.existsByReporterEmailAndProductId(loginUserEmail, productId);
        if (alreadyReported) {
            return "같은 상품은 한 번만 신고할 수 있습니다.";
        }

        Report report = new Report(
                loginUserEmail,
                productId,
                reportReason.trim(),
                "접수",
                LocalDateTime.now()
        );

        reportRepository.save(report);

        return "신고 등록 완료";
    }

    public String getMyReportList(String loginUserEmail) {
        List<Report> reportList = reportRepository.findByReporterEmailOrderByReportIdDesc(loginUserEmail);

        StringBuilder result = new StringBuilder();
        int count = 0;

        for (Report report : reportList) {
            Optional<Product> optionalProduct = productRepository.findById(report.getProductId());

            if (optionalProduct.isEmpty()) {
                continue;
            }

            Product product = optionalProduct.get();

            if ("삭제됨".equals(product.getProductStatus())) {
                continue;
            }

            result.append("신고번호: ").append(report.getReportId()).append("\n");
            result.append("신고자이메일: ").append(report.getReporterEmail()).append("\n");
            result.append("상품번호: ").append(report.getProductId()).append("\n");
            result.append("신고사유: ").append(report.getReportReason()).append("\n");
            result.append("신고상태: ").append(report.getReportStatus()).append("\n");
            result.append("신고일: ").append(report.getRegDt()).append("\n");
            result.append("--------------------").append("\n");
            count++;
        }

        if (count == 0) {
            return "내가 신고한 내역이 없습니다.";
        }

        return result.toString();
    }

    public List<ReportResponseDto> getMyReportListData(String loginUserEmail) {
        List<Report> reportList = reportRepository.findByReporterEmailOrderByReportIdDesc(loginUserEmail);
        List<ReportResponseDto> result = new ArrayList<>();

        for (Report report : reportList) {
            Optional<Product> optionalProduct = productRepository.findById(report.getProductId());

            if (optionalProduct.isEmpty()) {
                continue;
            }

            Product product = optionalProduct.get();

            if ("삭제됨".equals(product.getProductStatus())) {
                continue;
            }

            result.add(new ReportResponseDto(
                    report.getReportId(),
                    report.getReporterEmail(),
                    report.getProductId(),
                    product.getProductName(),
                    product.getProductImagePath(),
                    report.getReportReason(),
                    report.getReportStatus(),
                    report.getRegDt()
            ));
        }

        return result;
    }

    public String updateReportStatus(Integer reportId, String reportStatus) {
        if (reportId == null) {
            return "신고번호는 필수입니다.";
        }

        if (reportStatus == null || reportStatus.trim().isEmpty()) {
            return "변경할 신고상태는 필수입니다.";
        }

        Optional<Report> optionalReport = reportRepository.findById(reportId);

        if (optionalReport.isEmpty()) {
            return "해당 신고가 없습니다.";
        }

        if (!reportStatus.equals("접수")
                && !reportStatus.equals("처리중")
                && !reportStatus.equals("처리완료")
                && !reportStatus.equals("반려")) {
            return "변경 가능한 신고상태는 접수, 처리중, 처리완료, 반려만 가능합니다.";
        }

        Report report = optionalReport.get();
        report.updateReportStatus(reportStatus);
        reportRepository.save(report);

        return "신고 상태 변경 완료";
    }

    public String getAllReportList() {
        List<Report> reportList = reportRepository.findAllByOrderByReportIdDesc();

        StringBuilder result = new StringBuilder();
        int count = 0;

        for (Report report : reportList) {
            Optional<Product> optionalProduct = productRepository.findById(report.getProductId());

            if (optionalProduct.isEmpty()) {
                continue;
            }

            Product product = optionalProduct.get();

            if ("삭제됨".equals(product.getProductStatus())) {
                continue;
            }

            result.append("신고번호: ").append(report.getReportId()).append("\n");
            result.append("신고자이메일: ").append(report.getReporterEmail()).append("\n");
            result.append("상품번호: ").append(report.getProductId()).append("\n");
            result.append("신고사유: ").append(report.getReportReason()).append("\n");
            result.append("신고상태: ").append(report.getReportStatus()).append("\n");
            result.append("신고일: ").append(report.getRegDt()).append("\n");
            result.append("--------------------").append("\n");
            count++;
        }

        if (count == 0) {
            return "등록된 신고가 없습니다.";
        }

        return result.toString();
    }

    public List<ReportResponseDto> getAllReportListData() {
        List<Report> reportList = reportRepository.findAllByOrderByReportIdDesc();
        List<ReportResponseDto> result = new ArrayList<>();

        for (Report report : reportList) {
            Optional<Product> optionalProduct = productRepository.findById(report.getProductId());

            if (optionalProduct.isEmpty()) {
                continue;
            }

            Product product = optionalProduct.get();

            if ("삭제됨".equals(product.getProductStatus())) {
                continue;
            }

            result.add(new ReportResponseDto(
                    report.getReportId(),
                    report.getReporterEmail(),
                    report.getProductId(),
                    product.getProductName(),
                    product.getProductImagePath(),
                    report.getReportReason(),
                    report.getReportStatus(),
                    report.getRegDt()
            ));
        }

        return result;
    }

    public String getReportListByProductId(Integer productId) {
        if (productId == null) {
            return "상품번호는 필수입니다.";
        }

        Optional<Product> optionalProduct = productRepository.findById(productId);

        if (optionalProduct.isEmpty()) {
            return "해당 상품에 대한 신고가 없습니다.";
        }

        Product product = optionalProduct.get();

        if ("삭제됨".equals(product.getProductStatus())) {
            return "해당 상품에 대한 신고가 없습니다.";
        }

        List<Report> reportList = reportRepository.findByProductIdOrderByReportIdDesc(productId);

        if (reportList.isEmpty()) {
            return "해당 상품에 대한 신고가 없습니다.";
        }

        StringBuilder result = new StringBuilder();

        for (Report report : reportList) {
            result.append("신고번호: ").append(report.getReportId()).append("\n");
            result.append("신고자이메일: ").append(report.getReporterEmail()).append("\n");
            result.append("상품번호: ").append(report.getProductId()).append("\n");
            result.append("신고사유: ").append(report.getReportReason()).append("\n");
            result.append("신고상태: ").append(report.getReportStatus()).append("\n");
            result.append("신고일: ").append(report.getRegDt()).append("\n");
            result.append("--------------------").append("\n");
        }

        return result.toString();
    }
}