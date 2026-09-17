package com.example.oiso.favorite.repository;

import com.example.oiso.favorite.entity.ProductFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductFavoriteRepository
        extends JpaRepository<ProductFavorite, Integer> {

    // 특정 사용자가 특정 상품을 찜했는지 확인
    boolean existsByUserEmailAndProductId(String userEmail,
                                          Integer productId);

    // 특정 사용자의 특정 상품 찜 데이터 조회
    Optional<ProductFavorite> findByUserEmailAndProductId(
            String userEmail,
            Integer productId
    );

    // 사용자가 찜한 상품 목록 조회
    List<ProductFavorite> findByUserEmailOrderByFavoriteIdDesc(
            String userEmail
    );
}