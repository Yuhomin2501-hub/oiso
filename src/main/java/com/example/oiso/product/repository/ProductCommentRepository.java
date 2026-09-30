package com.example.oiso.product.repository;

import com.example.oiso.product.entity.ProductComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCommentRepository extends JpaRepository<ProductComment, Integer> {

    List<ProductComment> findByProductIdOrderByCommentIdAsc(Integer productId);
}