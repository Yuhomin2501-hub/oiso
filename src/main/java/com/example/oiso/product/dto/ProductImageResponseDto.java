package com.example.oiso.product.dto;   //상품 추가 이미지 정보를 프론트에 보내는 DTO

public class ProductImageResponseDto {

    private Integer productImageId;
    private String imagePath;
    private Integer imageOrder;

    public ProductImageResponseDto(Integer productImageId,
                                   String imagePath,
                                   Integer imageOrder) {
        this.productImageId = productImageId;
        this.imagePath = imagePath;
        this.imageOrder = imageOrder;
    }

    public Integer getProductImageId() {
        return productImageId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Integer getImageOrder() {
        return imageOrder;
    }
}


// 상품의 추가사진 1개 정보를 프론트로 보내는 상자