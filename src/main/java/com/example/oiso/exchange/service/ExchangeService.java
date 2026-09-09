package com.example.oiso.exchange.service;

import com.example.oiso.chat.service.ChatRoomService;
import com.example.oiso.exchange.dto.ExchangeResponseDto;
import com.example.oiso.exchange.entity.Exchange;
import com.example.oiso.exchange.repository.ExchangeRepository;
import com.example.oiso.product.entity.Product;
import com.example.oiso.product.repository.ProductRepository;
import com.example.oiso.user.entity.User;
import com.example.oiso.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor //final 필드들을 생성자 주입 방식으로 자동 주입
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;  // 교환 신청 저장, 조회, 상태 변경에 사용
    private final ProductRepository productRepository;  // 교환 대상 상품과 신청자 상품 조회/상태 변경에 사용
    private final ChatRoomService chatRoomService; // 교환 수락 시 채팅방 생성, 취소 시 채팅방 삭제에 사용
    private final UserRepository userRepository;  // 신청자 이름 조회에 사용

    // 교환 신청을 등록하는 메서드
    // loginUserEmail: 현재 로그인한 사용자 이메일
    // productId: 교환을 신청할 상대방 상품 ID
    // requesterProductId: 신청자가 교환 대상으로 제시하는 본인 상품 ID
    // requestMsg: 교환 신청 메시지
    public void requestExchange(String loginUserEmail, Integer productId, Integer requesterProductId, String requestMsg) {
        Product targetProduct = productRepository.findById(productId) // 교환 대상 상품을 DB에서 조회
                .orElseThrow(() -> new IllegalArgumentException("대상 상품이 존재하지 않습니다.")); // 없으면 예외 발생

        Product requesterProduct = productRepository.findById(requesterProductId)  // 신청자가 제시한 본인 상품을 DB에서 조회
                .orElseThrow(() -> new IllegalArgumentException("내 상품이 존재하지 않습니다."));

        if (targetProduct.getUserEmail().equals(loginUserEmail)) {  // 자기 자신의 상품에는 교환 신청을 할 수 없도록 막음
            throw new IllegalArgumentException("본인 상품에는 교환 신청할 수 없습니다.");
        }

        if (!requesterProduct.getUserEmail().equals(loginUserEmail)) {  // 신청자가 제시한 상품이 실제 로그인한 사용자의 상품인지 검증
            throw new IllegalArgumentException("본인 소유 상품으로만 교환 신청할 수 있습니다.");
        }

        if (!"판매중".equals(targetProduct.getProductStatus())) {  // 상대방 상품이 판매중 상태일 때만 교환 신청 가능
            throw new IllegalArgumentException("대상 상품이 판매중 상태가 아니어서 신청할 수 없습니다.");
        }

        if (!"판매중".equals(requesterProduct.getProductStatus())) {  // 내가 제시하는 상품도 판매중 상태일 때만 교환 신청 가능
            throw new IllegalArgumentException("내 상품이 판매중 상태가 아니어서 신청할 수 없습니다.");
        }

        List<Exchange> existingExchangeList =   // 같은 사용자가 같은 대상 상품에 이미 교환 신청한 내역이 있는지 조회
                exchangeRepository.findByRequesterEmailAndProductIdOrderByExchangeIdDesc(loginUserEmail, productId);

        if (!existingExchangeList.isEmpty()) {   // 같은 상품에 중복 교환 신청을 하지 못하도록 막음
            throw new IllegalArgumentException("같은 상품에는 교환 신청을 한 번만 보낼 수 있습니다.");
        }

        Exchange exchange = new Exchange(  // 새로운 교환 신청 Entity 생성, 처음 상태는 "대기중"으로 저장
                loginUserEmail,
                productId,
                requesterProductId,
                requestMsg,
                "대기중",
                LocalDateTime.now()
        );

        exchangeRepository.save(exchange);  // 생성한 교환 신청 정보를 DB에 저장
    }

    public List<Exchange> getReceivedExchangeList(String loginUserEmail) { // 내가 받은 교환 신청 목록을 Entity 형태로 조회하는 메서드
        List<Product> myProductList = productRepository.findByUserEmailOrderByProductIdDesc(loginUserEmail);
        // 현재 로그인한 사용자가 등록한 상품 목록 조회

        List<Integer> myProductIdList = myProductList.stream()  // 내 상품 목록에서 productId만 뽑아서 리스트로 변환
                .map(Product::getProductId)
                .toList();

        if (myProductIdList.isEmpty()) {  // 내가 등록한 상품이 없으면 받은 교환 신청도 없으므로 빈 리스트 반환
            return List.of();
        }

                                    // 내 상품들에 들어온 교환 신청 목록 조회
        return exchangeRepository.findByProductIdInOrderByExchangeIdDesc(myProductIdList).stream()
                .filter(exchange -> !"취소됨".equals(exchange.getExchangeStatus()))
                .filter(exchange -> !"거절".equals(exchange.getExchangeStatus()))
                .toList();      // 취소됨, 거절 상태는 목록에서 제외
    }

    // 내가 받은 교환 신청 목록을 화면 출력용 DTO로 조회하는 메서드
    public List<ExchangeResponseDto> getReceivedExchangeListData(String loginUserEmail) {
        List<Product> myProductList = productRepository.findByUserEmailOrderByProductIdDesc(loginUserEmail);
        // 현재 로그인한 사용자가 등록한 상품 목록 조회

        List<Integer> myProductIdList = myProductList.stream()  // 내 상품 ID만 리스트로 추출
                .map(Product::getProductId)
                .toList();

        if (myProductIdList.isEmpty()) {  // 내 상품이 없으면 빈 리스트 반환
            return List.of();
        }

        List<Exchange> exchangeList = exchangeRepository.findByProductIdInOrderByExchangeIdDesc(myProductIdList);
        // 내 상품들에 들어온 교환 신청 목록 조회

        return exchangeList.stream()  // 취소됨, 거절 상태의 교환 신청은 화면 목록에서 제외
                .filter(exchange -> !"취소됨".equals(exchange.getExchangeStatus()))
                .filter(exchange -> !"거절".equals(exchange.getExchangeStatus()))
                .map(exchange -> {  // Exchange Entity를 화면에 필요한 ExchangeResponseDto로 변환

                    // 교환 대상 상품 조회, 삭제된 상품일 수도 있으므로 없으면 null 처리
                    Product targetProduct = productRepository.findById(exchange.getProductId()).orElse(null);

                    // 신청자가 제시한 상품 조회, 삭제된 상품일 수도 있으므로 없으면 null 처리
                    Product requesterProduct = productRepository.findById(exchange.getRequesterProductId()).orElse(null);

                    // 신청자 이메일로 사용자 이름 조회, 사용자가 없으면 이메일을 대신 사용
                    String requesterName = getUserNameOrEmail(exchange.getRequesterEmail());

                    // 대상 상품이 없으면 안내 문구 사용, 있으면 상품명 사용
                    String targetProductName = targetProduct == null ? "삭제되었거나 없는 상품" : targetProduct.getProductName();

                    // 대상 상품 이미지 경로 설정
                    String targetProductImagePath = targetProduct == null ? "" : targetProduct.getProductImagePath();

                    // 신청자 상품이 없으면 안내 문구 사용, 있으면 상품명 사용
                    String requesterProductName = requesterProduct == null ? "삭제되었거나 없는 상품" : requesterProduct.getProductName();

                    // 신청자 상품 이미지 경로 설정
                    String requesterProductImagePath = requesterProduct == null ? "" : requesterProduct.getProductImagePath();

                    return new ExchangeResponseDto(
                            exchange.getExchangeId(),
                            exchange.getRequesterEmail(),
                            requesterName,
                            exchange.getProductId(),
                            exchange.getRequesterProductId(),
                            exchange.getRequestMsg(),
                            exchange.getExchangeStatus(),
                            exchange.getRegDt(),
                            targetProductName,
                            targetProductImagePath,
                            requesterProductName,
                            requesterProductImagePath
                    );
                })      // 화면에 필요한 교환 신청 정보만 DTO에 담아서 반환
                .toList();
    }

    // 내가 보낸 교환 신청 목록을 Entity 형태로 조회하는 메서드
    public List<Exchange> getRequestedExchangeList(String loginUserEmail) {

        // 현재 로그인한 사용자가 보낸 교환 신청 목록 조회
        return exchangeRepository.findByRequesterEmailOrderByExchangeIdDesc(loginUserEmail).stream()
                .filter(exchange -> !"취소됨".equals(exchange.getExchangeStatus()))
                .filter(exchange -> !"거절".equals(exchange.getExchangeStatus()))
                .toList();              // 취소됨, 거절 상태는 제외
    }

    // 내가 보낸 교환 신청 목록을 화면 출력용 DTO로 조회하는 메서드
    public List<ExchangeResponseDto> getRequestedExchangeListData(String loginUserEmail) {
        List<Exchange> exchangeList = exchangeRepository.findByRequesterEmailOrderByExchangeIdDesc(loginUserEmail);
        // 현재 로그인한 사용자가 보낸 교환 신청 목록 조회

        return exchangeList.stream()
                .filter(exchange -> !"취소됨".equals(exchange.getExchangeStatus()))
                .filter(exchange -> !"거절".equals(exchange.getExchangeStatus()))
                // 취소됨, 거절 상태는 화면 목록에서 제외

                .map(exchange -> {  // Exchange Entity를 ExchangeResponseDto로 변환
                    Product targetProduct = productRepository.findById(exchange.getProductId()).orElse(null); // 교환 대상 상품 조회
                    Product requesterProduct = productRepository.findById(exchange.getRequesterProductId()).orElse(null); // 내가 제시한 상품 조회

                    String requesterName = getUserNameOrEmail(exchange.getRequesterEmail()); // 신청자 이름 조회

                    String targetProductName = targetProduct == null ? "삭제되었거나 없는 상품" : targetProduct.getProductName(); // 대상 상품명 설정
                    String targetProductImagePath = targetProduct == null ? "" : targetProduct.getProductImagePath(); // 대상 상품 이미지 경로 설정
                    String requesterProductName = requesterProduct == null ? "삭제되었거나 없는 상품" : requesterProduct.getProductName(); // 신청자 상품명 설정
                    String requesterProductImagePath = requesterProduct == null ? "" : requesterProduct.getProductImagePath(); // 신청자 상품 이미지 경로 설정

                    return new ExchangeResponseDto(
                            exchange.getExchangeId(),
                            exchange.getRequesterEmail(),
                            requesterName,
                            exchange.getProductId(),
                            exchange.getRequesterProductId(),
                            exchange.getRequestMsg(),
                            exchange.getExchangeStatus(),
                            exchange.getRegDt(),
                            targetProductName,
                            targetProductImagePath,
                            requesterProductName,
                            requesterProductImagePath
                    );
                })   // 화면에 필요한 값만 담은 DTO 생성
                .toList();
    }

    // 받은 교환 신청을 수락 또는 거절하는 메서드
    public void updateExchangeStatus(Integer exchangeId, String loginUserEmail, String exchangeStatus) {

        // 교환 신청 ID로 교환 신청 조회, 없으면 예외 발생
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new IllegalArgumentException("교환 신청이 존재하지 않습니다."));

        Product targetProduct = productRepository.findById(exchange.getProductId())     // 교환 대상 상품 조회
                .orElseThrow(() -> new IllegalArgumentException("대상 상품이 존재하지 않습니다."));

        Product requesterProduct = productRepository.findById(exchange.getRequesterProductId()) // 교환 신청자가 제시한 상품 조회
                .orElseThrow(() -> new IllegalArgumentException("신청자 상품이 존재하지 않습니다."));

        if (!targetProduct.getUserEmail().equals(loginUserEmail)) {     // 교환 대상 상품의 주인만 수락/거절할 수 있도록 권한 검증
            throw new IllegalArgumentException("내 상품에 들어온 신청만 처리할 수 있습니다.");
        }

        if (!"수락".equals(exchangeStatus) && !"거절".equals(exchangeStatus)) {     // 상태값은 수락 또는 거절만 허용
            throw new IllegalArgumentException("교환 상태는 수락 또는 거절만 가능합니다.");
        }

        if (!"대기중".equals(exchange.getExchangeStatus())) {      // 이미 처리된 교환 신청은 다시 처리하지 못하게 막음
            throw new IllegalArgumentException("대기중 상태인 신청만 처리할 수 있습니다.");
        }

        if ("수락".equals(exchangeStatus)) {  // 교환 신청을 수락하는 경우
            if (!"판매중".equals(targetProduct.getProductStatus())) {      // 대상 상품이 판매중일 때만 수락 가능
                throw new IllegalArgumentException("대상 상품이 판매중 상태가 아니어서 수락할 수 없습니다.");
            }

            if (!"판매중".equals(requesterProduct.getProductStatus())) {       // 신청자 상품도 판매중일 때만 수락 가능
                throw new IllegalArgumentException("신청자 상품이 판매중 상태가 아니어서 수락할 수 없습니다.");
            }

            targetProduct.updateProductStatus("예약중");
            requesterProduct.updateProductStatus("예약중");
            // 교환이 수락되면 두 상품 모두 예약중 상태로 변경

            productRepository.save(targetProduct);
            productRepository.save(requesterProduct);
            // 변경된 상품 상태를 DB에 저장

            chatRoomService.createChatRoomIfNotExists( // 교환이 수락되면 두 사용자 간 채팅방 생성, 이미 채팅방이 있으면 생성 X
                    targetProduct.getProductId(),
                    targetProduct.getUserEmail(),
                    exchange.getRequesterEmail()
            );
        }

        exchange.updateExchangeStatus(exchangeStatus);  // 교환 신청 상태를 수락 또는 거절로 변경
        exchangeRepository.save(exchange);      // 변경된 교환 신청 상태를 DB에 저장
    }

    public void completeExchange(Integer exchangeId, String loginUserEmail) {   // 수락된 교환을 거래완료 상태로 변경하는 메서드
        Exchange exchange = exchangeRepository.findById(exchangeId) // 교환 신청 조회
                .orElseThrow(() -> new IllegalArgumentException("교환 신청이 존재하지 않습니다."));

        Product targetProduct = productRepository.findById(exchange.getProductId())     // 대상 상품 조회
                .orElseThrow(() -> new IllegalArgumentException("대상 상품이 존재하지 않습니다."));

        Product requesterProduct = productRepository.findById(exchange.getRequesterProductId()) // 상대방이 제시한 상품 조회
                .orElseThrow(() -> new IllegalArgumentException("상대 상품이 존재하지 않습니다."));

        if (!targetProduct.getUserEmail().equals(loginUserEmail)) {     // 받은 교환의 상품 등록자만 거래완료 처리 가능
            throw new IllegalArgumentException("받은 교환의 상품 등록자만 거래완료 처리할 수 있습니다.");
        }

        if (!"수락".equals(exchange.getExchangeStatus())) {       // 수락된 교환만 거래완료 처리 가능
            throw new IllegalArgumentException("수락된 교환만 거래완료 처리할 수 있습니다.");
        }

        if (!"예약중".equals(targetProduct.getProductStatus()) || !"예약중".equals(requesterProduct.getProductStatus())) {
            throw new IllegalArgumentException("두 상품이 모두 예약중 상태여야 거래완료 처리할 수 있습니다.");
        }       // 두 상품 모두 예약중 상태일 때만 거래완료 가능

        targetProduct.updateProductStatus("거래완료");
        requesterProduct.updateProductStatus("거래완료");
        exchange.updateExchangeStatus("거래완료");
        // 대상 상품, 신청자 상품, 교환 신청 상태를 모두 거래완료로 변경

        // 변경된 상태를 DB에 저장
        productRepository.save(targetProduct);
        productRepository.save(requesterProduct);
        exchangeRepository.save(exchange);
    }

    public void cancelExchange(Integer exchangeId, String loginUserEmail) {    // 수락된 교환을 취소하는 메서드
        Exchange exchange = exchangeRepository.findById(exchangeId)      // 교환 신청 조회
                .orElseThrow(() -> new IllegalArgumentException("교환 신청이 존재하지 않습니다."));

        Product targetProduct = productRepository.findById(exchange.getProductId())     // 대상 상품 조회
                .orElseThrow(() -> new IllegalArgumentException("대상 상품이 존재하지 않습니다."));

        Product requesterProduct = productRepository.findById(exchange.getRequesterProductId())     // 상대방 상품 조회
                .orElseThrow(() -> new IllegalArgumentException("상대 상품이 존재하지 않습니다."));

        boolean isTargetOwner = targetProduct.getUserEmail().equals(loginUserEmail);  // 로그인 사용자가 대상 상품의 주인인지 확인
        boolean isRequesterOwner = requesterProduct.getUserEmail().equals(loginUserEmail); // 로그인 사용자가 신청자 상품의 주인인지 확인


        if (!isTargetOwner && !isRequesterOwner) {      // 교환 당사자가 아니면 거래 취소 불가
            throw new IllegalArgumentException("거래 당사자만 거래취소 할 수 있습니다.");
        }

        if (!"수락".equals(exchange.getExchangeStatus())) {       // 수락된 교환만 취소 가능
            throw new IllegalArgumentException("수락된 교환만 거래취소 할 수 있습니다.");
        }

        targetProduct.updateProductStatus("판매중");
        requesterProduct.updateProductStatus("판매중");
        exchange.updateExchangeStatus("취소됨");
        // 거래 취소 시 두 상품은 다시 판매중으로 변경하고, 교환 신청은 취소됨으로 변경

        productRepository.save(targetProduct);
        productRepository.save(requesterProduct);
        exchangeRepository.save(exchange);
        // 변경된 상품 상태와 교환 상태를 DB에 저장

        chatRoomService.deleteChatRoomIfExists(     // 거래가 취소되면 해당 교환으로 생성된 채팅방 삭제
                targetProduct.getProductId(),
                targetProduct.getUserEmail(),
                exchange.getRequesterEmail()
        );
    }

    // 이메일을 기준으로 사용자 이름을 조회하는 보조 메서드, 사용자가 없으면 이메일을 그대로 반환
    private String getUserNameOrEmail(String userEmail) {
        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail);  // 이메일로 사용자 조회

        if (optionalUser.isEmpty()) {   // 사용자 정보가 없으면 이메일 반환
            return userEmail;
        }

        return optionalUser.get().getUserName();  // 사용자 정보가 있으면 사용자 이름 반환
    }
}