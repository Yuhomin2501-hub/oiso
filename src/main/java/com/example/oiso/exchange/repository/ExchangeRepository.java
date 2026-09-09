package com.example.oiso.exchange.repository;

import com.example.oiso.exchange.entity.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeRepository extends JpaRepository<Exchange, Integer> {

    List<Exchange> findByProductIdInOrderByExchangeIdDesc(List<Integer> productIdList);

    List<Exchange> findByRequesterEmailOrderByExchangeIdDesc(String requesterEmail);

    List<Exchange> findByRequesterProductIdInOrderByExchangeIdDesc(List<Integer> requesterProductIdList);

    List<Exchange> findByRequesterEmailAndProductIdOrderByExchangeIdDesc(String requesterEmail, Integer productId);
}