package com.example.oiso.product.service;       //차단 상품 로그 저장 로직을 담당하는 Service

import com.example.oiso.product.document.BlockedProductLog;
import com.example.oiso.product.repository.BlockedProductLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BlockedProductLogService {

    private final BlockedProductLogRepository blockedProductLogRepository;

    public BlockedProductLogService(BlockedProductLogRepository blockedProductLogRepository) {
        this.blockedProductLogRepository = blockedProductLogRepository;
    }

    public void saveBlockedProductLog(String userEmail, String productName, String category, String productDesc,
                                      String blockedKeyword, String blockedReason) {
        BlockedProductLog blockedProductLog = new BlockedProductLog(
                userEmail,
                productName,
                category,
                productDesc,
                blockedKeyword,
                blockedReason,
                LocalDateTime.now()
        );

        blockedProductLogRepository.save(blockedProductLog);
    }
}



// 차단된 상품 로그를 MongdoDB에 저장하는 역할

// 상품 등록시 금지키워드에 해당하거나 safety korea api에서 리콜 상품으로 분류된 경우