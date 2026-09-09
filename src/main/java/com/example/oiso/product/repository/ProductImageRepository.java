package com.example.oiso.product.repository;

import com.example.oiso.product.entity.ProductImage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    List<ProductImage> findByProductIdOrderByImageOrderAsc(Integer productId);

    @Transactional
    void deleteByProductId(Integer productId);
}

// MariaDB의 product_image에 접근