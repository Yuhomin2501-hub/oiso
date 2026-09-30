package com.example.oiso.product.service;

import com.example.oiso.product.dto.ProductCommentResponseDto;
import com.example.oiso.product.entity.Product;
import com.example.oiso.product.entity.ProductComment;
import com.example.oiso.product.repository.ProductCommentRepository;
import com.example.oiso.product.repository.ProductRepository;
import com.example.oiso.user.entity.User;
import com.example.oiso.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductCommentService {

    private final ProductCommentRepository productCommentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductCommentService(ProductCommentRepository productCommentRepository,
                                 ProductRepository productRepository,
                                 UserRepository userRepository) {
        this.productCommentRepository = productCommentRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // 댓글 등록
    public String registerComment(Integer productId,
                                  String userEmail,
                                  String commentContent) {

        Optional<Product> optionalProduct = productRepository.findById(productId);

        if (optionalProduct.isEmpty()) {
            return "해당 상품이 없습니다.";
        }

        Product product = optionalProduct.get();

        if ("삭제됨".equals(product.getProductStatus())) {
            return "해당 상품이 없습니다.";
        }

        if (commentContent == null || commentContent.trim().isEmpty()) {
            return "댓글 내용을 입력하세요.";
        }

        ProductComment comment = new ProductComment(
                productId,
                userEmail,
                commentContent.trim(),
                LocalDateTime.now()
        );

        productCommentRepository.save(comment);

        return "댓글 등록 완료";
    }

    // 댓글 목록 조회
    public List<ProductCommentResponseDto> getCommentList(Integer productId) {

        List<ProductComment> comments =
                productCommentRepository.findByProductIdOrderByCommentIdAsc(productId);

        List<ProductCommentResponseDto> result = new ArrayList<>();

        for (ProductComment comment : comments) {

            Optional<User> user =
                    userRepository.findByUserEmail(comment.getUserEmail());

            String userName = user
                    .map(User::getUserName)
                    .orElse(comment.getUserEmail());

            result.add(new ProductCommentResponseDto(
                    comment.getCommentId(),
                    comment.getUserEmail(),
                    userName,
                    comment.getCommentContent(),
                    comment.getRegDt()
            ));
        }

        return result;
    }
}