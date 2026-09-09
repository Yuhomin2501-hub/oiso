package com.example.oiso.product.entity;  //상품 엔티티 클래스

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
@Table(name = "product") // MariaDB의 product 테이블과 연결
@Getter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB의 AUTO_INCREMENT 방식으로 productId 자동 증가
    @Column(name = "product_id", nullable = false)       // product_id 컬럼과 매핑, null 불가
    private Integer productId;  // 상품 고유 번호

    @Column(name = "user_email", length = 300, nullable = false)  // user_email 컬럼과 매핑, 상품 등록자 이메일 저장
    private String userEmail;  // 상품을 등록한 사용자 이메일

    @Column(name = "product_name", length = 100, nullable = false)  // 상품을 등록한 사용자 이메일
    private String productName;   // 상품명

    @Column(name = "category", length = 50, nullable = false)  // category 컬럼과 매핑, null 불가
    private String category;  // 상품 카테고리

    @Column(name = "product_desc", length = 1000)  // product_desc 컬럼과 매핑, 상품 설명은 null 가능
    private String productDesc;  // 상품 설명

    @Column(name = "product_image_path", length = 500, nullable = false) // product_image_path 컬럼과 매핑
    private String productImagePath;  // 대표 이미지 경로, 실제 이미지는 S3에 저장되고 DB에는 경로만 저장

    @Column(name = "product_status", length = 30, nullable = false)  // product_status 컬럼과 매핑
    private String productStatus;  // 상품 상태, 예: 판매중, 예약중, 거래완료

    @Column(name = "reg_dt", nullable = false)  // reg_dt 컬럼과 매핑, null 불가
    private LocalDateTime regDt;  // 상품 등록일시

    public Product(String userEmail,
                   String productName,
                   String category,
                   String productDesc,
                   String productImagePath,
                   String productStatus,
                   LocalDateTime regDt) {   // 상품 등록 시 새로운 Product 객체를 만들기 위한 생성자
        this.userEmail = userEmail;      // 상품 등록자 이메일 저장
        this.productName = productName;     // 상품명 저장
        this.category = category;           // 카테고리 저장
        this.productDesc = productDesc;     // 상품 설명 저장
        this.productImagePath = productImagePath;   // 대표 이미지 경로 저장
        this.productStatus = productStatus;      // 상품 상태 저장
        this.regDt = regDt;             // 등록일시 저장
    }

    public void updateProduct(String productName,
                              String category,
                              String productDesc,
                              String productImagePath) {  // 상품 수정 시 상품명, 카테고리, 설명, 대표 이미지 경로를 변경하는 메서드
        this.productName = productName;     // 수정된 상품명 반영
        this.category = category;           // 수정된 카테고리 반영
        this.productDesc = productDesc;     // 수정된 상품 설명 반영
        this.productImagePath = productImagePath;    // 수정된 대표 이미지 경로 반영
    }

    public void updateProductStatus(String productStatus) {
        this.productStatus = productStatus;
    } // 판매중, 예약중, 거래완료 같은 상태값으로 변경
}   // 상품 상태만 변경하는 메서드
