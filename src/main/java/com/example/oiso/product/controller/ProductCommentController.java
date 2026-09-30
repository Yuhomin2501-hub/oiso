package com.example.oiso.product.controller;

import com.example.oiso.product.dto.ProductCommentResponseDto;
import com.example.oiso.product.service.ProductCommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class ProductCommentController {

    private final ProductCommentService productCommentService;

    public ProductCommentController(ProductCommentService productCommentService) {
        this.productCommentService = productCommentService;
    }

    // 댓글 등록
    @PostMapping("/register")
    public String registerComment(@RequestParam Integer productId,
                                  @RequestParam String commentContent,
                                  HttpSession session) {

        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        return productCommentService.registerComment(
                productId,
                loginUserEmail.toString(),
                commentContent
        );
    }

    // 댓글 목록 조회
    @GetMapping("/list")
    public List<ProductCommentResponseDto> getCommentList(@RequestParam Integer productId) {
        return productCommentService.getCommentList(productId);
    }
}