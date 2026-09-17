package com.example.oiso.favorite.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_favorite",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_favorite_user_product",
                        columnNames = {"user_email", "product_id"}
                )
        }
)
@Getter
@NoArgsConstructor
public class ProductFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id", nullable = false)
    private Integer favoriteId;

    @Column(name = "user_email", length = 300, nullable = false)
    private String userEmail;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    public ProductFavorite(String userEmail,
                           Integer productId,
                           LocalDateTime regDt) {

        this.userEmail = userEmail;
        this.productId = productId;
        this.regDt = regDt;
    }
}