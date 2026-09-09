package com.example.oiso.exchange.dto;

import java.time.LocalDateTime;

public class ExchangeResponseDto {

    private Integer exchangeId;
    private String requesterEmail;
    private String requesterName;
    private Integer productId;
    private Integer requesterProductId;
    private String requestMsg;
    private String exchangeStatus;
    private LocalDateTime regDt;

    private String targetProductName;
    private String targetProductImagePath;
    private String requesterProductName;
    private String requesterProductImagePath;

    public ExchangeResponseDto(Integer exchangeId,
                               String requesterEmail,
                               String requesterName,
                               Integer productId,
                               Integer requesterProductId,
                               String requestMsg,
                               String exchangeStatus,
                               LocalDateTime regDt,
                               String targetProductName,
                               String targetProductImagePath,
                               String requesterProductName,
                               String requesterProductImagePath) {
        this.exchangeId = exchangeId;
        this.requesterEmail = requesterEmail;
        this.requesterName = requesterName;
        this.productId = productId;
        this.requesterProductId = requesterProductId;
        this.requestMsg = requestMsg;
        this.exchangeStatus = exchangeStatus;
        this.regDt = regDt;
        this.targetProductName = targetProductName;
        this.targetProductImagePath = targetProductImagePath;
        this.requesterProductName = requesterProductName;
        this.requesterProductImagePath = requesterProductImagePath;
    }

    public Integer getExchangeId() {
        return exchangeId;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getRequesterProductId() {
        return requesterProductId;
    }

    public String getRequestMsg() {
        return requestMsg;
    }

    public String getExchangeStatus() {
        return exchangeStatus;
    }

    public LocalDateTime getRegDt() {
        return regDt;
    }

    public String getTargetProductName() {
        return targetProductName;
    }

    public String getTargetProductImagePath() {
        return targetProductImagePath;
    }

    public String getRequesterProductName() {
        return requesterProductName;
    }

    public String getRequesterProductImagePath() {
        return requesterProductImagePath;
    }
}