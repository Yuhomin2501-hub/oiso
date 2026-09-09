package com.example.oiso.product.controller;

import com.example.oiso.product.dto.ProductResponseDto;
import com.example.oiso.product.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//프론트에서 들어온 상품관련요청을 받아서 Service로 전달
@Controller
@RequestMapping("/products") // 모든 요청 주소
public class ProductController {

    private final ProductService productService;


    //ProductService를 컨트롤러 내부 변수에 저장 (ProductController)
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/register") // 상품등록요첨
    @ResponseBody
    // 프론트에서 보낸 값 받는부분 (상품명,카테고리,설명)
    public String registerProduct(@RequestParam String productName, // 상품명
                                  @RequestParam String category,    // 카테고리
                                  @RequestParam(required = false) String productDesc,
                                  // 이미지 받는 부분 (대표사진 1장, 추가사진 여러장)
                                  @RequestParam MultipartFile mainProductImage,
                                  @RequestParam(required = false) List<MultipartFile> productImages,
                                  // 현재 로그인한 사용자 정보를 세션에서 가져옴
                                  HttpSession session) {

        Object loginUserEmail = session.getAttribute("loginUserEmail");

        // 로그인이 안되어있으면 리턴값 반환 (검증로직)
        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        // Service 호출 (controller에서 직접처리안하고 필요한 값을 모아서 service에 전달)
        return productService.registerProduct(
                loginUserEmail.toString(),
                productName,
                category,
                productDesc,
                mainProductImage,
                productImages
        );
    }

    @GetMapping("/list")  //상품 목록 조회
    @ResponseBody
    // 상품목록 관련결과를 문자열로 반환, productService의 getProductList()를 호출해서 결과를 받아서 반환함
    public String getProductList() {
        return productService.getProductList();
    }

    @GetMapping("/list-data")   // /products/list-data 주소로 GET요청이 들어오면 이 메서드가 실행됨
    @ResponseBody   // 반환값을 문자열 그대로 응답으로 보냄
    // ProductResponseDto 여러개를 리스트 형태로 반환함
    // 요청 주소에 keyword라는 검색어가 있으면 받고 없어도 에러내지 않음 (검색어가 null값일때, 검색어가 있을때, 검색어가 없을때)
    // 검색어 keyword를 ProductService로 넘기고 Service가 만든 상품 목록 데이터를 그대로 반환함
    // 상품 목록 페이지에서 사용할 상품 데이터를 JSON형태로 보내주는 API
    public List<ProductResponseDto> getProductListData(@RequestParam(required = false) String keyword) {
        return productService.getProductListData(keyword);
    }

    @GetMapping("/category-data")   // 특정 카테고리에 해당하는 상품목록조회
    @ResponseBody
    // 상품정보를 여러개 담아서 리스트로 반환함
    // category값이 반드시 있어야함 ( required = false 가 없기 때문에)
    public List<ProductResponseDto> getCategoryProductListData(@RequestParam String category) {
        // 받은 category값을 ProductService로 넘기고 Service가 카테고리에 맞는 상품 목록을 찾아서 반환함
        return productService.getCategoryProductListData(category);
    }

    @GetMapping("/latest-data") // 최신 상품 목록 조회
    @ResponseBody   // 화면 페이지를 반환하는게 아니라 상품데이터 자체를 반환함
    // 최신상품 여러개를 리스트로 반환, 최근 등록된 상품을 정해진 기준대로 가져오는 기능이기 때문에 파라미터 불필요
    public List<ProductResponseDto> getLatestProductListData() {
        // ProductController안에 있는 최신 상품 목록 조회 메서드를 실행함
        return productService.getLatestProductListData();
    }

    @GetMapping("/detail")  // 상품1개의 상세정보를 문자열 형태로 조회
    @ResponseBody   // 문자열 데이터 그대로 프론트에 응답함
    // 프론트에서 보낸 productId 값을 받아 결과를 String형태로 반환함
    public String getProductDetail(@RequestParam Integer productId) {
        // 받은 productId를 Service로 넘기고 Service 결과를 그대로 반환함
        return productService.getProductDetail(productId);
    }

    @GetMapping("/detail-data") // 상품1개의 상세정보를 DTO형태로 조회
    @ResponseBody
    // 상품 상세 정보를 담은 객체를 반환
    // 프론트에서 넘긴 상품 번호를 받고 현재 로그인한 사용자가 있는지 확인함
    public ProductResponseDto getProductDetailData(@RequestParam Integer productId,
                                                   HttpSession session) {
        //세션에서 현재 로그인한 사용자의 이메일을 꺼냄
        Object loginUserEmail = session.getAttribute("loginUserEmail");
        // 로그인 정보가 없으면 email에 null을 넣고 로그인 정보가 있으면 문자열로 바꿔서 email에 넣음
        String email = loginUserEmail == null ? null : loginUserEmail.toString();
        // productId와 email을 Service로 넘김
        return productService.getProductDetailData(productId, email);
    }

    @GetMapping("/my-list") // 현재 로그인한 사용자가 등록한 내상품 목록을 문자열형태로 조회
    @ResponseBody
    // 현재 로그인한 사용자가 누구인지 확인하기 위해 HttpSession을 받음
    public String getMyProductList(HttpSession session) {
        //세션에서 로그인한 사용자의 이메일을 꺼냄 (로그인 되어있으면 이메일이 들어있고 안되어있으면 null이 나옴
        Object loginUserEmail = session.getAttribute("loginUserEmail");
        //로그인하지 않은 사용자의 내상품목록은 볼 수 없으므로 리턴값으로 로그인해야 볼수 있다고 말해줌
        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }
        // 로그인한 사용자의 이메일을 문자열로 바꿔서 Service에 넘김
        return productService.getMyProductList(loginUserEmail.toString());
    }

    @GetMapping("/my-list-data")    // 현재 로그인한 사용자가 등록한 내상품 목록을 DTO형태로 조회
    @ResponseBody
    // 반환값은 ProductResponseDto 여러개 이며 현재 로그인한 사용자를 확인하려고 세션을 받음
    public List<ProductResponseDto> getMyProductListData(HttpSession session) {
        // 세션에서 로그인한 사용자의 이메일을 꺼냄
        Object loginUserEmail = session.getAttribute("loginUserEmail");
        // 로그인이 안되어 있으면 상품 데이터를 못주니 null 반환
        if (loginUserEmail == null) {
            return null;
        }
        //로그인이 되어있으면 이메일을 Service로 넘김
        return productService.getMyProductListData(loginUserEmail.toString());
    }

    @PostMapping("/update") // 내가 등록한 상품 목록을 DTO 데이터로 조회
    @ResponseBody

    public String updateProduct(@RequestParam Integer productId,    // 수정할 상품 번호
                                @RequestParam String productName,   // 수정할 상품명
                                @RequestParam String category,      // 수정할 카테고리
                                @RequestParam(required = false) String productDesc,     //수정할 설명(없어도됨)
                                @RequestParam String productStatus, // 상품 상태
                                @RequestParam(required = false) MultipartFile mainProductImage, // 대표사진
                                @RequestParam(required = false) List<MultipartFile> productImages, // 새로 추가할 일반사진
                                @RequestParam(required = false) List<Integer> deleteProductImageIds,    // 삭제할 기존사진의 ID목록(없어도됨)
                                // 현재 로그인한 사용자 확인용
                                HttpSession session) {
        // 로그인하지 않은 사용자는 상품 수정이 불가하게 함
        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }
        // Service로 넘기는부분
        return productService.updateProduct(
                productId,
                loginUserEmail.toString(),
                productName,
                category,
                productDesc,
                productStatus,
                mainProductImage,
                productImages,
                deleteProductImageIds
        );
    }

    @GetMapping("/delete") // 상품 삭제 요청을 처리
    @ResponseBody
    // 삭제할 상품 번호를 받는 부분
    public String deleteProduct(@RequestParam Integer productId,
                                // 현재 로그인한 사용자를 확인함
                                HttpSession session) {
        // 세션에서 로그인한 사용자 이메일을 꺼냄
        Object loginUserEmail = session.getAttribute("loginUserEmail");
        // 로그인하니 않은 사용자는 상품 삭제를 못하게 막음
        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }
        // 삭제할 상품 번호와 로그인한 사용자 이메일을 Service로 넘김
        return productService.deleteProduct(productId, loginUserEmail.toString());
    }

    @GetMapping("/status") // 상품 상태를 변경함
    @ResponseBody
    public String updateProductStatus(@RequestParam Integer productId,         // 상태를 바꿀 상품번호
                                      @RequestParam String productStatus,       // 바꿀 상품 상태
                                      HttpSession session) {                // 현재 로그인한 사용자 확인
        // 세션에서 로그인한 사용자의 이메일을 가져옴
        Object loginUserEmail = session.getAttribute("loginUserEmail");
        // 로그인하지 않은 사용자는 상품 상태를 변경할수 없게 막음
        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }
        // 필요한 값 (상품번호, 로그인한 사용자 이메일, 변경할 상품상태)을 Service로 넘김
        return productService.updateProductStatus(
                productId,
                loginUserEmail.toString(),
                productStatus
        );
    }
}