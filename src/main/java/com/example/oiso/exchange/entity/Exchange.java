package com.example.oiso.exchange.entity;

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
@Table(name = "exchange_request")
@Getter
@NoArgsConstructor
public class Exchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchange_id", nullable = false)
    private Integer exchangeId;

    @Column(name = "requester_email", length = 300, nullable = false)
    private String requesterEmail;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "requester_product_id", nullable = false)
    private Integer requesterProductId;

    @Column(name = "request_msg", length = 1000)
    private String requestMsg;

    @Column(name = "exchange_status", length = 30, nullable = false)
    private String exchangeStatus;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    public Exchange(String requesterEmail,
                    Integer productId,
                    Integer requesterProductId,
                    String requestMsg,
                    String exchangeStatus,
                    LocalDateTime regDt) {
        this.requesterEmail = requesterEmail;
        this.productId = productId;
        this.requesterProductId = requesterProductId;
        this.requestMsg = requestMsg;
        this.exchangeStatus = exchangeStatus;
        this.regDt = regDt;
    }

    public void updateExchangeStatus(String exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
    }
}