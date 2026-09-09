package com.example.oiso.product.repository;

import com.example.oiso.product.document.BlockedProductLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BlockedProductLogRepository extends MongoRepository<BlockedProductLog, String> {
}

// mongoDB의 차단 상품 로그 컬렉션에 접근