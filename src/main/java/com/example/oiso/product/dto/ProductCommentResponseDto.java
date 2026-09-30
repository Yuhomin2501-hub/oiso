package com.example.oiso.product.dto;

import java.time.LocalDateTime;

public class ProductCommentResponseDto {

    private Integer commentId;
    private String userEmail;
    private String userName;
    private String commentContent;
    private LocalDateTime regDt;

    public ProductCommentResponseDto(Integer commentId,
                                     String userEmail,
                                     String userName,
                                     String commentContent,
                                     LocalDateTime regDt) {
        this.commentId = commentId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.commentContent = commentContent;
        this.regDt = regDt;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public LocalDateTime getRegDt() {
        return regDt;
    }
}