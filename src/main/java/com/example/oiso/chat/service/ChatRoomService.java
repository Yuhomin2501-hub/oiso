package com.example.oiso.chat.service; // 채팅방 관련 비즈니스 로직 처리

import com.example.oiso.chat.document.ChatRoom;
import com.example.oiso.chat.dto.ChatRoomResponseDto;
import com.example.oiso.chat.repository.ChatMessageRepository;
import com.example.oiso.chat.repository.ChatRoomRepository;
import com.example.oiso.exchange.entity.Exchange;
import com.example.oiso.exchange.repository.ExchangeRepository;
import com.example.oiso.product.entity.Product;
import com.example.oiso.product.repository.ProductRepository;
import com.example.oiso.user.entity.User;
import com.example.oiso.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository; //채팅방생성,조회,삭제를 담당
    private final ChatMessageRepository chatMessageRepository; // 채팅방삭제시 메시지도 함께 삭제
    private final ExchangeRepository exchangeRepository; // 교환신청자의 상품정보를 찾기위해 사용하는 JPA
    private final ProductRepository productRepository; // 채팅방목록에 상품명,이미지,상태를 보여줌
    private final UserRepository userRepository; // 상대방 이메일대신 이름을 보여줌

    public ChatRoomService(ChatRoomRepository chatRoomRepository,
                           ChatMessageRepository chatMessageRepository,
                           ExchangeRepository exchangeRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository) { //생성자주입방식
        this.chatRoomRepository = chatRoomRepository; // 채팅방repository 저장
        this.chatMessageRepository = chatMessageRepository; //채팅메시지 repository 저장
        this.exchangeRepository = exchangeRepository; //교환 repository 저장
        this.productRepository = productRepository; // 상품 repository 저장
        this.userRepository = userRepository; // 회원 repository 저장
    }

    //교환수락시 채팅방이 없으면 새로 생성하는 메서드
    public void createChatRoomIfNotExists(Integer productId, String sellerEmail, String buyerEmail) {
        Optional<ChatRoom> optionalChatRoom =
                chatRoomRepository.findByProductIdAndSellerEmailAndBuyerEmail(productId, sellerEmail, buyerEmail);
                // 같은상품,판매자,신청자 조합의 채팅방이 이미 있는지 조회함

        if (optionalChatRoom.isPresent()) { // 채팅방이 이미 존재하는 경우
            return; // 메서드 종료(채팅방 중복생성 안함)
        }

        ChatRoom chatRoom = new ChatRoom(
                productId,  //교환 대상 상품 ID
                sellerEmail,//상품 등록자 이메일
                buyerEmail, //교환 신청자 이메일
                LocalDateTime.now() //현재 시간을 채팅방 생성시간으로 저장
        );

        chatRoomRepository.save(chatRoom); // 새 채팅방을 mongodb chat_room 컬렉션에 저장
    }

    //교환이 취소되거나 삭제될때 채팅방이 있으면 같이 삭제하는 메서드
    public void deleteChatRoomIfExists(Integer productId, String sellerEmail, String buyerEmail) {
        Optional<ChatRoom> optionalChatRoom =
                chatRoomRepository.findByProductIdAndSellerEmailAndBuyerEmail(productId, sellerEmail, buyerEmail);
                //삭제할 대상 채팅방이 존재하는지 조회

        if (optionalChatRoom.isEmpty()) { // 채팅방이 존재하지 않는 경우
            return; //삭제할 것이 없으므로 메서드 종료
        }

        ChatRoom chatRoom = optionalChatRoom.get(); // optional 안에있는 채팅방 객체를 꺼냄

        //해당 채팅방에 속한 채팅 메시지를 먼저 삭제
        chatMessageRepository.deleteByChatRoomIdIn(List.of(chatRoom.getId()));
        chatRoomRepository.delete(chatRoom); //채팅방 자체를 mongodb에서 삭제
    }

    public String getMyChatRoomList(String loginUserEmail) { //내 채팅방 목록을 문자열 형태로 반환
        List<ChatRoom> chatRoomList =
                chatRoomRepository.findBySellerEmailOrBuyerEmailOrderByCreatedAtDesc(loginUserEmail, loginUserEmail);
                // 내가 판매자이거나 구매자인 채팅방을 최신순으로 조회

        if (chatRoomList.isEmpty()) { // 참여중인 채팅방이 없는 경우
            return "참여중인 채팅방이 없습니다.";
        }

        StringBuilder result = new StringBuilder(); //여러 채팅방정보를 문자열로 합치기위해 사용

        for (ChatRoom chatRoom : chatRoomList) { // 조회된 채팅방 목록을 하나씩 반복
            result.append("채팅방ID: ").append(chatRoom.getId()).append("\n"); // 채팅방ID추가
            result.append("상품번호: ").append(chatRoom.getProductId()).append("\n");//상품ID추가
            result.append("판매자이메일: ").append(chatRoom.getSellerEmail()).append("\n");//판매자이메일 추가
            result.append("구매자이메일: ").append(chatRoom.getBuyerEmail()).append("\n");//구매자이메일 추가
            result.append("생성일: ").append(chatRoom.getCreatedAt()).append("\n");//채팅방 생성일 추가
            result.append("--------------------").append("\n");// 채팅방 구분선 추가
        }

        return result.toString(); // 완성된 문자열 반환
    }

    //프론트 채팅방 목록 화면에 필요한 데이터를 DTO리스트로 반환하는 메서드
    public List<ChatRoomResponseDto> getMyChatRoomListData(String loginUserEmail) {
        List<ChatRoom> chatRoomList =
                chatRoomRepository.findBySellerEmailOrBuyerEmailOrderByCreatedAtDesc(loginUserEmail, loginUserEmail);
                // 현재 사용자가 참여한 채팅방을 최신순으로 조회

        List<ChatRoomResponseDto> result = new ArrayList<>();// 프론트로 반환할 DTO목록 생성

        for (ChatRoom chatRoom : chatRoomList) { // 채팅방을 하나씩 DTO로 변환
            // 현재 로그인한 사용자를 기준으로 상대방 이메일 계산
            String otherUserEmail = getOtherUserEmail(loginUserEmail, chatRoom);
            //상대방 이메일로 회원 이름 조회한뒤 없으면 이메일 그대로 사용
            String otherUserName = getUserNameOrEmail(otherUserEmail);

            //채팅방의 상품 ID로 상대방 상품정보를 mariadb에서 조회
            Product product = productRepository.findById(chatRoom.getProductId()).orElse(null);

            String productName = ""; // 상품명이 없을때 기본값
            String productImagePath = ""; // 상품 이미지가 없을 때 기본값
            String productStatus = ""; // 상품 상태가 없을 때 기본값

            if (product != null) { //상품이 실제로 존재하는 경우
                productName = product.getProductName(); // 상품명 저장
                productImagePath = product.getProductImagePath(); // 상품 대표 이미지 경로 저장
                productStatus = product.getProductStatus();//상품 상태 저장
            }

            Integer requesterProductId = null; // 교환 신청자가 선택한 상품 ID 기본값
            String requesterProductName = ""; // 교환 신청자 상품명 기본값
            String requesterProductImagePath = ""; //교환 신청자 상품 이미지 기본값

            List<Exchange> exchangeList =
                    exchangeRepository.findByRequesterEmailAndProductIdOrderByExchangeIdDesc(
                            chatRoom.getBuyerEmail(), //교환 신청자 이메일
                            chatRoom.getProductId() // 교환 대상 상품 ID
                    );

            if (!exchangeList.isEmpty()) { //연결된 교환 신청 정보가 있는 경우
                Exchange exchange = exchangeList.get(0); //가장 최신 교환신청 정보 사용
                requesterProductId = exchange.getRequesterProductId();
                // 교환 신청자가 선택한 자기 상품 ID를 가져옴

                // 신청자 상품 ID로 상품 저옵를 조회
                Product requesterProduct = productRepository.findById(requesterProductId).orElse(null);

                if (requesterProduct != null) { // 신청자 상품이 존재하는 경우
                    requesterProductName = requesterProduct.getProductName(); //신청자 상품명 저장
                    requesterProductImagePath = requesterProduct.getProductImagePath();
                    // 신청자 상품 이미지 경로 저장
                }
            }

            result.add(new ChatRoomResponseDto(
                    chatRoom.getId(), // 채팅방 ID
                    chatRoom.getProductId(), // 상대방 상품 ID
                    productName, // 상대방 상품명
                    productImagePath, // 상대방 상품 이미지 경로
                    productStatus,// 상대방 상품 상태
                    requesterProductId, // 교환 신청자가 선택한 상품 ID
                    requesterProductName, // 교환 신청자가 선택한 상품명
                    requesterProductImagePath, // 교환 신청자가 선택한 상품 이미지 경로
                    chatRoom.getSellerEmail(), // 판매자 이메일
                    chatRoom.getBuyerEmail(), // 교환 신청자 이메일
                    otherUserEmail, // 현재 로그인 사용자 기준 상대방 이메일
                    otherUserName, // 현재 로그인 사용자 기준 상대방 이름
                    chatRoom.getCreatedAt() // 채팅방 생성일
            ));
        }

        return result; // 완성된 채팅방 목록 DTO 리스트 반환
    }

    // 현재 로그인한 사용자를 기준으로 상대방 이메일을 구하는 메서드
    private String getOtherUserEmail(String loginUserEmail, ChatRoom chatRoom) {
        if (loginUserEmail.equals(chatRoom.getSellerEmail())) { // 로그인한 사용자가 판매자일 경우
            return chatRoom.getBuyerEmail(); // 상대방은 구매자 또는 교환 신청자
        }

        return chatRoom.getSellerEmail(); // 로그인한 사용자가 구매자라면 상대방은 판매자
    }

    // 이메일로 사용자이름을 찾고 없으면 이메일을 반환하는 메서드
    private String getUserNameOrEmail(String userEmail) {
        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail);
        // 회원 테이블에서 이메일로 사용자 조회

        if (optionalUser.isEmpty()) { // 해당 이메일의 회원 정보를 찾지 못한 경우
            return userEmail; //이름 대신 이메일 반환
        }

        return optionalUser.get().getUserName(); // 회원정보가 있으면 사용자 이름 반환
    }
}