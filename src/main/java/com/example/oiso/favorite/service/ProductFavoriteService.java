package com.example.oiso.favorite.service;

import com.example.oiso.favorite.entity.ProductFavorite;
import com.example.oiso.favorite.repository.ProductFavoriteRepository;
import com.example.oiso.product.entity.Product;
import com.example.oiso.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ProductFavoriteService {

    private final ProductFavoriteRepository productFavoriteRepository;
    private final ProductRepository productRepository;

    public ProductFavoriteService(ProductFavoriteRepository productFavoriteRepository,
                                  ProductRepository productRepository) {

        this.productFavoriteRepository = productFavoriteRepository;
        this.productRepository = productRepository;
    }


    // 상품 찜 추가
    @Transactional
    public String addFavorite(String userEmail, Integer productId) {

        // 상품번호로 상품 조회
        Optional<Product> optionalProduct = productRepository.findById(productId);

        // 존재하지 않는 상품이면 찜 불가
        if (optionalProduct.isEmpty()) {
            return "해당 상품이 없습니다.";
        }

        Product product = optionalProduct.get();

        // 삭제된 상품이면 찜 불가
        if ("삭제됨".equals(product.getProductStatus())) {
            return "해당 상품이 없습니다.";
        }

        // 이미 찜한 상품인지 확인
        boolean alreadyFavorite =
                productFavoriteRepository.existsByUserEmailAndProductId(
                        userEmail,
                        productId
                );

        if (alreadyFavorite) {
            return "이미 찜한 상품입니다.";
        }

        // 새로운 찜 데이터 생성
        ProductFavorite productFavorite = new ProductFavorite(
                userEmail,
                productId,
                LocalDateTime.now()
        );

        // DB에 찜 정보 저장
        productFavoriteRepository.save(productFavorite);

        return "찜 추가 완료";
    }


    // 상품 찜 삭제
    @Transactional
    public String deleteFavorite(String userEmail, Integer productId) {

        // 현재 사용자의 해당 상품 찜 데이터 조회
        Optional<ProductFavorite> optionalFavorite =
                productFavoriteRepository.findByUserEmailAndProductId(
                        userEmail,
                        productId
                );

        // 찜하지 않은 상품이면
        if (optionalFavorite.isEmpty()) {
            return "찜하지 않은 상품입니다.";
        }

        // 찜 데이터 삭제
        productFavoriteRepository.delete(optionalFavorite.get());

        return "찜 삭제 완료";
    }


    // 현재 사용자가 해당 상품을 찜했는지 확인
    public boolean isFavorite(String userEmail, Integer productId) {

        return productFavoriteRepository.existsByUserEmailAndProductId(
                userEmail,
                productId
        );
    }
}