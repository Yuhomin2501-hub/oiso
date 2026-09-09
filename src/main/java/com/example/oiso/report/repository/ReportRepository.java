package com.example.oiso.report.repository;

import com.example.oiso.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    List<Report> findByReporterEmailOrderByReportIdDesc(String reporterEmail);

    List<Report> findAllByOrderByReportIdDesc();

    List<Report> findByProductIdOrderByReportIdDesc(Integer productId);

    List<Report> findByProductIdInOrderByReportIdDesc(List<Integer> productIdList);

    boolean existsByReporterEmailAndProductId(String reporterEmail, Integer productId);

    boolean existsByProductId(Integer productId);

    void deleteByProductId(Integer productId);
}