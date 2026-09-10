# OISO

판매보다 교환에 집중한 중고 물물교환 웹서비스입니다.

기존 융합프로젝트실습1에서 개발한 OISO 프로젝트를 기반으로
졸업프로젝트에서 백엔드 기능을 추가하여 서비스를 확장합니다.

## 주요 기술

- Java 17
- Spring Boot
- Spring Data JPA
- MariaDB
- MongoDB
- HTML / CSS / JavaScript
- AWS EC2
- AWS RDS
- AWS S3
- Nginx

## 기존 주요 기능

- 회원가입 및 이메일 인증
- 로그인 / 로그아웃
- 상품 등록 / 수정 / 삭제
- 상품 검색
- 상품 교환 신청
- 채팅
- 상품 신고
- SafetyKorea OpenAPI를 이용한 리콜 상품 확인
- AWS S3 상품 이미지 저장

## 졸업프로젝트 추가 기능

1. 상품 찜
2. 상품 조회수
3. 상품 댓글
4. 사용자 차단
5. 거래 후기

## 개발 진행 상황

- [ ] 상품 찜
- [ ] 상품 조회수
- [ ] 상품 댓글
- [ ] 사용자 차단
- [ ] 거래 후기

## 실행 환경변수

프로젝트 실행 시 다음 환경변수 설정이 필요합니다.

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- MONGODB_URI
- MAIL_USERNAME
- MAIL_PASSWORD
- AWS_REGION
- AWS_S3_BUCKET
- AWS_ACCESS_KEY_ID
- AWS_SECRET_ACCESS_KEY
- SAFETY_KOREA_AUTH_KEY

실제 비밀번호 및 API Key 등의 민감정보는 GitHub에 저장하지 않고 환경변수로 관리합니다.

## 실행 방법

1. 필요한 환경변수를 설정합니다.
2. 프로젝트를 실행합니다.
3. Spring Boot 서버가 8080 포트에서 실행됩니다.

## 배포 URL

https://oisomarket.co.kr/