package com.example.oiso.report.controller;

import com.example.oiso.report.dto.ReportResponseDto;
import com.example.oiso.report.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/register")
    @ResponseBody
    public String registerReport(@RequestParam Integer productId,
                                 @RequestParam String reportReason,
                                 HttpSession session) {

        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        return reportService.registerReport(
                loginUserEmail.toString(),
                productId,
                reportReason
        );
    }

    @GetMapping("/my-list")
    @ResponseBody
    public String getMyReportList(HttpSession session) {

        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        return reportService.getMyReportList(loginUserEmail.toString());
    }

    @GetMapping("/my-list-data")
    @ResponseBody
    public Object getMyReportListData(HttpSession session) {
        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        List<ReportResponseDto> reportList = reportService.getMyReportListData(loginUserEmail.toString());
        return reportList;
    }

    @GetMapping("/status")
    @ResponseBody
    public String updateReportStatus(@RequestParam Integer reportId,
                                     @RequestParam String reportStatus) {
        return reportService.updateReportStatus(reportId, reportStatus);
    }

    @GetMapping("/all-list")
    @ResponseBody
    public String getAllReportList(HttpSession session) {

        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        return reportService.getAllReportList();
    }

    @GetMapping("/all-list-data")
    @ResponseBody
    public Object getAllReportListData(HttpSession session) {
        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        List<ReportResponseDto> reportList = reportService.getAllReportListData();
        return reportList;
    }

    @GetMapping("/product-list")
    @ResponseBody
    public String getReportListByProductId(@RequestParam Integer productId,
                                           HttpSession session) {

        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        return reportService.getReportListByProductId(productId);
    }
}