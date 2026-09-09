package com.example.oiso.product.entity;

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
@Table(name = "product_image")
@Getter
@NoArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id", nullable = false)
    private Integer productImageId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "image_path", length = 500, nullable = false)
    private String imagePath;

    @Column(name = "image_order", nullable = false)
    private Integer imageOrder;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    public ProductImage(Integer productId,
                        String imagePath,
                        Integer imageOrder,
                        LocalDateTime regDt) {
        this.productId = productId;
        this.imagePath = imagePath;
        this.imageOrder = imageOrder;
        this.regDt = regDt;
    }

    public void updateImageOrder(Integer imageOrder) {
        this.imageOrder = imageOrder;
    }
}