package com.example.oiso.product.dto;   //상품 정보를 프론트화면에 보내가위한 응답 DTO

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponseDto {           // 상품 1개의 전체 정보를 프론트로 보내는 상자

    private Integer productId;
    private String userEmail;
    private String userName;
    private String productName;
    private String category;
    private String productDesc;
    private String productImagePath;
    private List<String> productImagePathList;
    private List<ProductImageResponseDto> additionalImages;
    private String productStatus;
    private LocalDateTime regDt;
    private boolean myProduct;
    private boolean reported;

    public ProductResponseDto(Integer productId,
                              String userEmail,
                              String userName,
                              String productName,
                              String category,
                              String productDesc,
                              String productImagePath,
                              List<String> productImagePathList,
                              List<ProductImageResponseDto> additionalImages,
                              String productStatus,
                              LocalDateTime regDt,
                              boolean myProduct,
                              boolean reported) {
        this.productId = productId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.productName = productName;
        this.category = category;
        this.productDesc = productDesc;
        this.productImagePath = productImagePath;
        this.productImagePathList = productImagePathList;
        this.additionalImages = additionalImages;
        this.productStatus = productStatus;
        this.regDt = regDt;
        this.myProduct = myProduct;
        this.reported = reported;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public String getProductImagePath() {
        return productImagePath;
    }

    public List<String> getProductImagePathList() {
        return productImagePathList;
    }

    public List<ProductImageResponseDto> getAdditionalImages() {
        return additionalImages;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public LocalDateTime getRegDt() {
        return regDt;
    }

    public boolean isMyProduct() {
        return myProduct;
    }

    public boolean isReported() {
        return reported;
    }
}