package com.example.oiso.favorite.controller;

import com.example.oiso.favorite.service.ProductFavoriteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class ProductFavoriteController {

    private final ProductFavoriteService productFavoriteService;

    public ProductFavoriteController(ProductFavoriteService productFavoriteService) {
        this.productFavoriteService = productFavoriteService;
    }


    // 상품 찜 추가
    @PostMapping("/add")
    public String addFavorite(@RequestParam Integer productId,
                              HttpSession session) {

        // 세션에서 현재 로그인한 사용자 이메일 가져오기
        Object loginUserEmail = session.getAttribute("loginUserEmail");

        // 로그인하지 않은 사용자는 찜 불가
        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        // Service로 사용자 이메일과 상품번호 전달
        return productFavoriteService.addFavorite(
                loginUserEmail.toString(),
                productId
        );
    }


    // 상품 찜 삭제
    @PostMapping("/delete")
    public String deleteFavorite(@RequestParam Integer productId,
                                 HttpSession session) {

        // 현재 로그인 사용자 확인
        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        // Service에서 찜 삭제
        return productFavoriteService.deleteFavorite(
                loginUserEmail.toString(),
                productId
        );
    }


    // 현재 로그인한 사용자가 해당 상품을 찜했는지 확인
    @GetMapping("/status")
    public boolean isFavorite(@RequestParam Integer productId,
                              HttpSession session) {

        // 현재 로그인 사용자 확인
        Object loginUserEmail = session.getAttribute("loginUserEmail");

        // 로그인하지 않았으면 찜 상태 false
        if (loginUserEmail == null) {
            return false;
        }

        return productFavoriteService.isFavorite(
                loginUserEmail.toString(),
                productId
        );
    }
    // 현재 로그인한 사용자가 찜한 상품번호 목록 조회
    @GetMapping("/my-product-ids")
    public List<Integer> getMyFavoriteProductIds(HttpSession session) {

        Object loginUserEmail = session.getAttribute("loginUserEmail");

        // 로그인하지 않았으면 빈 목록 반환
        if (loginUserEmail == null) {
            return List.of();
        }

        return productFavoriteService.getFavoriteProductIds(
                loginUserEmail.toString()
        );
    }
}