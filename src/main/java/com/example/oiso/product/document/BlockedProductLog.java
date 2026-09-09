package com.example.oiso.product.document;      //mongoDB에 저장되는 리콜 상품 차단 로그

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "blocked_product_log")
@Getter
@NoArgsConstructor
public class BlockedProductLog {

    @Id
    private String id;

    private String userEmail;
    private String productName;
    private String category;
    private String productDesc;
    private String blockedKeyword;
    private String blockedReason;
    private LocalDateTime createdAt;

    public BlockedProductLog(String userEmail, String productName, String category, String productDesc,
                             String blockedKeyword, String blockedReason, LocalDateTime createdAt) {
        this.userEmail = userEmail;
        this.productName = productName;
        this.category = category;
        this.productDesc = productDesc;
        this.blockedKeyword = blockedKeyword;
        this.blockedReason = blockedReason;
        this.createdAt = createdAt;
    }
}

// mongodb에 저장되는 차단 상품 로그 클래스

// 상품등록,수정시 금지키워드나 safety korea 리콜 검사