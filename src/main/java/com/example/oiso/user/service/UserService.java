package com.example.oiso.user.service;

import com.example.oiso.chat.document.ChatRoom;
import com.example.oiso.chat.repository.ChatMessageRepository;
import com.example.oiso.chat.repository.ChatRoomRepository;
import com.example.oiso.exchange.entity.Exchange;
import com.example.oiso.exchange.repository.ExchangeRepository;
import com.example.oiso.product.entity.Product;
import com.example.oiso.product.repository.ProductRepository;
import com.example.oiso.report.entity.Report;
import com.example.oiso.report.repository.ReportRepository;
import com.example.oiso.user.dto.SignupRequestDto;
import com.example.oiso.user.entity.User;
import com.example.oiso.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ExchangeRepository exchangeRepository;
    private final ReportRepository reportRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String mailUsername; // 네이버 SMTP 발신자 이메일 주소

    private final Map<String, EmailVerificationInfo> emailVerificationMap = new HashMap<>(); // 이메일별 인증번호/만료시간/인증여부를 임시 저장
    private final SecureRandom secureRandom = new SecureRandom(); // 인증번호를 예측하기 어렵게 생성하기 위한 랜덤 객체

    public UserService(UserRepository userRepository, // 생성자 주입 시작
                       ProductRepository productRepository, // 상품 Repository 주입
                       ExchangeRepository exchangeRepository, // 교환 Repository 주입
                       ReportRepository reportRepository,
                       ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository,
                       PasswordEncoder passwordEncoder,
                       JavaMailSender javaMailSender) {
        this.userRepository = userRepository; // 주입받은 UserRepository 저장
        this.productRepository = productRepository; // 주입받은 ProductRepository 저장
        this.exchangeRepository = exchangeRepository;
        this.reportRepository = reportRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.passwordEncoder = passwordEncoder;
        this.javaMailSender = javaMailSender;
    }

    public String checkEmail(String userEmail) { // 이메일 중복확인 기능
        userEmail = clean(userEmail);

        if (userEmail.isEmpty()) {
            return "이메일을 입력하세요.";
        }

        if (!isValidEmail(userEmail)) {
            return "올바른 이메일 형식으로 입력하세요.";
        }

        if (userRepository.existsByUserEmail(userEmail)) {
            return "이미 사용 중인 이메일입니다.";
        }

        return "사용 가능한 이메일입니다.";
    }

    public String sendEmailCode(String userEmail) { // 이메일 인증번호 발송 기능
        userEmail = clean(userEmail);

        if (userEmail.isEmpty()) { // 이메일을 입력하지 않은 경우
            return "이메일을 입력하세요.";
        }

        if (!isValidEmail(userEmail)) { // 이메일 형식 오류
            return "올바른 이메일 형식으로 입력하세요.";
        }

        if (userRepository.existsByUserEmail(userEmail)) { // 이메일 중복 확인
            return "이미 사용 중인 이메일입니다.";
        }

        String code = createEmailCode(); // 6자리 인증번호 생성

        EmailVerificationInfo verificationInfo = new EmailVerificationInfo( // 인증번호 정보를 담는 객체 생성
                code,
                LocalDateTime.now().plusMinutes(5), // 현재 시간 기준 5분 뒤를 만료시간으로 설정
                false
        );

        emailVerificationMap.put(userEmail, verificationInfo); // 이메일을 key로 인증정보를 메모리에 저장

        SimpleMailMessage message = new SimpleMailMessage(); // 간단한 텍스트 메일 객체 생성
        message.setFrom(mailUsername); // 발신자 이메일
        message.setTo(userEmail); // 수신자 이메일
        message.setSubject("[oiso] 이메일 인증번호 안내"); // 메일 제목
        message.setText( // 메일 본문 설정
                "안녕하세요. oiso 회원가입 이메일 인증번호입니다.\n\n" +
                        "인증번호: " + code + "\n\n" + // 인증번호 내용
                        "인증번호는 5분 동안만 유효합니다." // 유효시간 안내
        );

        javaMailSender.send(message); // 이메일 발송

        return "인증번호가 이메일로 발송되었습니다.";
    }

    public String verifyEmailCode(String userEmail, String code) { // 사용자가 입력한 인증번호 확인 기능
        userEmail = clean(userEmail); // 공백 제거
        code = clean(code);

        if (userEmail.isEmpty()) { // 이메일이 비어 있는 경우
            return "이메일을 입력하세요.";
        }

        if (code.isEmpty()) { // 인증번호가 비어 있는 경우
            return "인증번호를 입력하세요.";
        }

        EmailVerificationInfo verificationInfo = emailVerificationMap.get(userEmail); // 해당 이메일의 인증정보 조회

        if (verificationInfo == null) { // 인증번호 발송 이력이 없는 경우
            return "발송된 인증번호가 없습니다. 인증번호를 먼저 발송해주세요.";
        }

        if (LocalDateTime.now().isAfter(verificationInfo.expiredAt())) { // 현재 시간이 만료시간을 지난 경우
            emailVerificationMap.remove(userEmail); // 만료된 인증정보 삭제
            return "인증번호가 만료되었습니다. 다시 발송해주세요.";
        }

        if (!verificationInfo.code().equals(code)) { // 저장된 인증번호와 입력한 인증번호가 다른 경우
            return "인증번호가 일치하지 않습니다.";
        }

        emailVerificationMap.put( // 인증 성공 상태로 다시 저장
                userEmail,
                new EmailVerificationInfo( // 기존 인증번호 정보에서 인증 여부만 true로 변경
                        verificationInfo.code(), // 기존 인증번호
                        verificationInfo.expiredAt(), // 기존 만료시간
                        true // 인증 완료 처리
                )
        );

        return "이메일 인증이 완료되었습니다.";
    }

    public String signup(SignupRequestDto requestDto) { // 회원가입 처리  , Dto에서 값을 꺼내고 공백처리
        String userEmail = clean(requestDto.getUserEmail()); // 이메일 값 정리
        String userPwd = clean(requestDto.getUserPwd()); // 비밀번호 값 정리
        String userPwdCheck = clean(requestDto.getUserPwdCheck()); // 비밀번호 확인 값 정리
        String userName = clean(requestDto.getUserName()); // 이름 값 정리
        String userPnum = clean(requestDto.getUserPnum()); // 전화번호 값 정리
        // clean : 앞뒤 공백 제거 , null문제 해결


        if (userEmail.isEmpty()) { // 이메일 미입력 검사
            return "이메일을 입력하세요.";
        }

        if (!isValidEmail(userEmail)) { // 이메일 형식 검사
            return "올바른 이메일 형식으로 입력하세요.";
        }

        if (userPwd.isEmpty()) { // 비밀번호 미입력 검사
            return "비밀번호를 입력하세요.";
        }

        if (userPwdCheck.isEmpty()) { // 비밀번호 확인 미입력 검사
            return "비밀번호 확인을 입력하세요.";
        }

        if (!userPwd.equals(userPwdCheck)) { // 비밀번호와 비밀번호 확인이 같은지 검사
            return "비밀번호와 비밀번호 확인이 일치하지 않습니다.";
        }

        if (userName.isEmpty()) { // 이름 미입력 검사
            return "이름을 입력하세요.";
        }

        if (userPnum.isEmpty()) { // 전화번호 미입력 검사
            return "전화번호를 입력하세요.";
        }

        if (userRepository.existsByUserEmail(userEmail)) { // DB에 이미 같은 이메일로 가입된 회원이 있는지 검사
            return "이미 사용 중인 이메일입니다.";
        }

        EmailVerificationInfo verificationInfo = emailVerificationMap.get(userEmail); // 해당 이메일의 인증정보 조회
        // 이메일 인증번호 발송,인증의 성공정보가 emailVerificationMap에 저장되어 있는데 여기서 이메일의 인증 정보를 꺼냄

        if (verificationInfo == null || !verificationInfo.verified()) { // 인증정보가 없거나 인증 완료가 아닌 경우
            return "이메일 인증을 완료해주세요.";
        }

        if (LocalDateTime.now().isAfter(verificationInfo.expiredAt())) { // 이메일 인증 시간이 만료된 경우
            emailVerificationMap.remove(userEmail); // 만료된 인증정보 삭제
            return "이메일 인증 시간이 만료되었습니다. 다시 인증해주세요.";
        }

        String encodedPassword = passwordEncoder.encode(userPwd); // 사용자가 입력한 평문 비밀번호인 pwd를 암호화해서 저장 준비
        //암호화된 값을 user 생성자에 넣음, user 엔티티에서 userpwd 필드는 DB의 user_pwd컬럼과 연결되어 있음

        User user = new User( // 새 회원 Entity 생성
                userEmail, // 회원 이메일
                encodedPassword, // 암호화된 비밀번호
                userName, // 회원 이름
                userPnum, // 회원 전화번호
                LocalDateTime.now() // 가입일시
        );

        userRepository.save(user); // 회원 정보를 MariaDB의 user_info 테이블에 저장
        emailVerificationMap.remove(userEmail); // 회원가입 완료 후 임시로 저장해둔 이메일 인증정보 제거

        return "회원가입 완료"; // 회원가입 성공 메시지 반환
    }

    public boolean login(String userEmail, String userPwd) { // 로그인 기능
        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail); // 이메일로 회원 조회

        if (optionalUser.isEmpty()) { // 해당 이메일의 회원이 없는 경우
            return false; // 로그인 실패
        }

        User user = optionalUser.get(); // Optional안에 들어있던 User 객체를 꺼냄

        return passwordEncoder.matches(userPwd, user.getUserPwd()); // 입력 비밀번호와 암호화된 DB 비밀번호 비교
    }                       // userPwd : 사용자가 로그인창에 입력한 비밀번호, user.getUserPwd(): DB에 저장된 암호화된 비밀번호
        // 결과가 맞다면 true , 틀리면 false가 userController로 돌아감

    public Optional<User> findByUserEmail(String userEmail) { // 이메일로 회원 조회
        return userRepository.findByUserEmail(userEmail); // Repository를 통해 회원 조회 결과 반환
    }

    public Map<String, Object> getMyPageData(String userEmail) { // 마이페이지에 필요한 데이터 조회
        Map<String, Object> result = new HashMap<>(); // 결과 데이터를 담을 Map 생성

        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail); // 이메일로 회원 조회

        if (optionalUser.isEmpty()) { // 회원 정보가 없는 경우
            result.put("loggedIn", false); // 로그인되지 않은 상태로 표시
            return result; // 결과 반환
        }

        User user = optionalUser.get(); // 회원 객체 꺼내기

        List<Product> myProductList = productRepository.findByUserEmailOrderByProductIdDesc(userEmail); // 내가 등록한 상품 목록 조회

        long myProductCount = myProductList.stream() // 내 상품 개수 계산 시작
                .filter(product -> !"삭제됨".equals(product.getProductStatus())) // 삭제됨 상태는 제외
                .count(); // 개수 계산

        List<Integer> myProductIdList = myProductList.stream() // 내가 등록한 상품 ID 목록 생성
                .map(Product::getProductId) // Product에서 productId만 추출
                .toList(); // 리스트로 변환

        long receivedExchangeCount = 0; // 받은 교환 신청 개수 기본값

        if (!myProductIdList.isEmpty()) { // 내가 등록한 상품이 있을 때만 받은 교환 신청 조회
            receivedExchangeCount = exchangeRepository.findByProductIdInOrderByExchangeIdDesc(myProductIdList).stream() // 내 상품에 들어온 교환 신청 조회
                    .filter(exchange -> !"취소됨".equals(exchange.getExchangeStatus())) // 취소된 교환은 제외
                    .count(); // 개수 계산
        }

        long requestedExchangeCount = exchangeRepository.findByRequesterEmailOrderByExchangeIdDesc(userEmail).stream() // 내가 신청한 교환 목록 조회
                .filter(exchange -> !"취소됨".equals(exchange.getExchangeStatus())) // 취소된 교환은 제외
                .count(); // 개수 계산

        long myReportCount = reportRepository.findByReporterEmailOrderByReportIdDesc(userEmail).size(); // 내가 신고한 내역 개수 계산

        result.put("loggedIn", true); // 로그인 상태 true 저장
        result.put("userEmail", user.getUserEmail()); // 사용자 이메일 저장
        result.put("userName", user.getUserName()); // 사용자 이름 저장
        result.put("userPnum", user.getUserPnum()); // 사용자 전화번호 저장
        result.put("regDt", user.getRegDt()); // 가입일 저장
        result.put("myProductCount", myProductCount); // 내 상품 개수 저장
        result.put("receivedExchangeCount", receivedExchangeCount); // 받은 교환 신청 개수 저장
        result.put("requestedExchangeCount", requestedExchangeCount); // 내가 신청한 교환 개수 저장
        result.put("myReportCount", myReportCount); // 내 신고 개수 저장

        return result; // 마이페이지 데이터 반환
    }

    public String findEmail(String userName, String userPnum) { // 아이디 찾기 기능
        Optional<User> optionalUser = userRepository.findByUserNameAndUserPnum(userName, userPnum); // 이름과 전화번호로 회원 조회

        if (optionalUser.isEmpty()) { // 일치하는 회원이 없는 경우
            return "일치하는 회원정보가 없습니다.";
        }

        return "가입된 이메일: " + optionalUser.get().getUserEmail(); // 찾은 이메일 반환
    }


    public String resetPassword(String userEmail, String userName, String userPnum, String newUserPwd) { // 비밀번호 재설정 기능
        Optional<User> optionalUser = userRepository.findByUserEmailAndUserNameAndUserPnum(userEmail, userName, userPnum); // 이메일/이름/전화번호가 모두 일치하는 회원 조회

        if (optionalUser.isEmpty()) { // 일치하는 회원이 없는 경우
            return "입력한 정보와 일치하는 회원이 없습니다.";
        }

        User user = optionalUser.get(); // 회원 객체 꺼내기
        user.changePassword(passwordEncoder.encode(newUserPwd)); // 새 비밀번호를 암호화해서 변경
        userRepository.save(user); // 변경된 회원 정보 저장

        return "비밀번호가 재설정되었습니다.";
    }

    public String updateMyInfo(String userEmail, String userName, String userPnum, String newUserPwd) { // 내 정보 수정 기능
        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail); // 현재 로그인한 사용자 조회

        if (optionalUser.isEmpty()) { // 회원 정보가 없는 경우
            return "회원 정보를 찾을 수 없습니다.";
        }

        User user = optionalUser.get(); // 회원 객체 꺼내기

        boolean updated = false; // 실제 수정된 값이 있는지 확인하는 변수

        if (userName != null && !userName.trim().isEmpty()) { // 이름 값이 입력된 경우
            user.changeUserName(userName.trim()); // 이름 변경
            updated = true; // 수정 발생 표시
        }

        if (userPnum != null && !userPnum.trim().isEmpty()) { // 전화번호 값이 입력된 경우
            user.changeUserPnum(userPnum.trim()); // 전화번호 변경
            updated = true; // 수정 발생 표시
        }

        if (newUserPwd != null && !newUserPwd.trim().isEmpty()) { // 새 비밀번호가 입력된 경우
            user.changePassword(passwordEncoder.encode(newUserPwd.trim())); // 새 비밀번호 암호화 후 변경
            updated = true; // 수정 발생 표시
        }

        if (!updated) { // 수정된 값이 하나도 없는 경우
            return "수정할 정보를 하나 이상 입력하세요."; // 입력 요청 메시지 반환
        }

        userRepository.save(user); // 변경된 회원 정보 저장
        return "내 정보 수정 완료"; // 성공 메시지 반환
    }

    @Transactional
    public String withdrawUser(String userEmail) { // 회원 탈퇴 기능
        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail); // 탈퇴할 회원 조회

        if (optionalUser.isEmpty()) { // 회원 정보가 없는 경우
            return "회원 정보를 찾을 수 없습니다.";
        }

        List<Product> myProductList = productRepository.findByUserEmailOrderByProductIdDesc(userEmail); // 탈퇴 회원이 등록한 상품 목록 조회
        List<Integer> myProductIdList = myProductList.stream() // 탈퇴 회원의 상품 ID 목록 생성
                .map(Product::getProductId) // 상품에서 productId만 추출
                .toList();

        List<ChatRoom> myChatRoomList = chatRoomRepository.findBySellerEmailOrBuyerEmailOrderByCreatedAtDesc(userEmail, userEmail); // 회원이 판매자/구매자로 참여한 채팅방 조회
        List<String> chatRoomIdList = myChatRoomList.stream() // 채팅방 ID 목록 생성
                .map(ChatRoom::getId) // ChatRoom에서 id만 추출
                .toList();

        if (!chatRoomIdList.isEmpty()) { // 삭제할 채팅방이 있는 경우
            chatMessageRepository.deleteByChatRoomIdIn(chatRoomIdList); // 해당 채팅방들의 메시지를 먼저 삭제
            chatRoomRepository.deleteAll(myChatRoomList); // 그 다음 채팅방 삭제
        }

        List<Exchange> exchangeDeleteList = new ArrayList<>(); // 삭제할 교환 신청들을 담을 리스트

        exchangeDeleteList.addAll(exchangeRepository.findByRequesterEmailOrderByExchangeIdDesc(userEmail)); // 탈퇴 회원이 신청자로 들어간 교환 내역 추가

        if (!myProductIdList.isEmpty()) { // 탈퇴 회원이 등록한 상품이 있는 경우
            exchangeDeleteList.addAll(exchangeRepository.findByProductIdInOrderByExchangeIdDesc(myProductIdList)); // 내 상품에 들어온 교환 신청 추가
            exchangeDeleteList.addAll(exchangeRepository.findByRequesterProductIdInOrderByExchangeIdDesc(myProductIdList)); // 내 상품이 교환 제안 상품으로 쓰인 내역 추가
        }

        if (!exchangeDeleteList.isEmpty()) { // 삭제할 교환 내역이 있는 경우
            exchangeRepository.deleteAll(exchangeDeleteList); // 교환 내역 전체 삭제
        }

        List<Report> reportDeleteList = new ArrayList<>(); // 삭제할 신고 내역들을 담을 리스트

        reportDeleteList.addAll(reportRepository.findByReporterEmailOrderByReportIdDesc(userEmail)); // 탈퇴 회원이 신고한 내역 추가

        if (!myProductIdList.isEmpty()) { // 탈퇴 회원의 상품이 있는 경우
            reportDeleteList.addAll(reportRepository.findByProductIdInOrderByReportIdDesc(myProductIdList)); // 내 상품이 신고된 내역 추가
        }

        if (!reportDeleteList.isEmpty()) { // 삭제할 신고 내역이 있는 경우
            reportRepository.deleteAll(reportDeleteList); // 신고 내역 전체 삭제
        }

        if (!myProductList.isEmpty()) { // 탈퇴 회원이 등록한 상품이 있는 경우
            productRepository.deleteAll(myProductList); // 상품 전체 삭제
        }

        userRepository.delete(optionalUser.get()); // 마지막으로 회원 정보 삭제

        return "회원 탈퇴 완료"; // 탈퇴 성공 메시지 반환
    }

    private String createEmailCode() { // 6자리 이메일 인증번호 생성 메서드
        int number = secureRandom.nextInt(900000) + 100000; // 100000부터 999999까지의 숫자 생성
        return String.valueOf(number); // 숫자를 문자열로 변환해서 반환
    }

    private String clean(String value) { // null 방지 및 앞뒤 공백 제거 메서드
        if (value == null) { // 값이 null인 경우
            return ""; // 빈 문자열 반환
        }

        return value.trim(); // 앞뒤 공백 제거 후 반환
    }

    private boolean isValidEmail(String userEmail) { // 이메일 형식 검사 메서드
        return userEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"); // 이메일 정규식 검사 결과 반환
    }

    private record EmailVerificationInfo( // 이메일 인증 정보를 담는 record
                                          String code, // 인증번호
                                          LocalDateTime expiredAt, // 인증번호 만료시간
                                          boolean verified // 인증 완료 여부
    ) {
    }
}