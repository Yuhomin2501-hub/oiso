package com.example.oiso.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_comment")
@Getter
@NoArgsConstructor
public class ProductComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", nullable = false)
    private Integer commentId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "user_email", nullable = false, length = 300)
    private String userEmail;

    @Column(name = "comment_content", nullable = false, length = 500)
    private String commentContent;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    public ProductComment(Integer productId,
                          String userEmail,
                          String commentContent,
                          LocalDateTime regDt) {
        this.productId = productId;
        this.userEmail = userEmail;
        this.commentContent = commentContent;
        this.regDt = regDt;
    }
}