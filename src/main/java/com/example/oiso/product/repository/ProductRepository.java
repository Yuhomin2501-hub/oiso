package com.example.oiso.product.repository;

import com.example.oiso.product.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Product Entity를 DB에서 조회/저장/삭제하는 Repository, 기본키 타입은 Integer

    List<Product> findAllByOrderByProductIdDesc();
    // 모든 상품을 productId 기준 내림차순으로 조회, 최신 상품이 먼저 나옴

    List<Product> findByUserEmailOrderByProductIdDesc(String userEmail);
    // 특정 사용자가 등록한 상품들을 최신순으로 조회

    List<Product> findByProductNameContainingOrderByProductIdDesc(String keyword);
    // 상품명에 keyword가 포함된 상품들을 최신순으로 검색

    List<Product> findByCategoryOrderByProductIdDesc(String category);
    // 특정 카테고리에 해당하는 상품들을 최신순으로 조회

    List<Product> findTop10ByProductStatusInOrderByProductIdDesc(List<String> productStatusList);
    // 상품 상태가 목록에 포함되는 상품 중 최신 10개 조회

    // 메서드 이름만으로 만들기 어려운 조회 조건을 JPQL로 직접 작성
    @Query("""
            SELECT p
            FROM Product p
            WHERE p.productStatus IN :productStatusList
              AND NOT EXISTS (
                  SELECT r
                  FROM Report r
                  WHERE r.productId = p.productId
              )
            ORDER BY p.productId DESC
            """)
    List<Product> findLatestNotReportedProducts(        // 신고되지 않은 최신 상품 목록을 조회하는 메서드
            @Param("productStatusList") List<String> productStatusList, // 조회할 상품 상태 목록을 JPQL의 :productStatusList에 연결
            Pageable pageable   // 조회할 개수 제한 또는 페이징 조건을 전달
    );
}

// 상품 테이블에 접근
// Product Entity를 기준으로 상품데이터를 조회/저장/삭제함
