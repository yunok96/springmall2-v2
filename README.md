# Springmall2

[![codecov](https://codecov.io/gh/yunok96/springmall2-v2/branch/master/graph/badge.svg)](https://codecov.io/gh/yunok96/springmall2-v2)
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen)

https://springmall2-ghgvedckerfaaae3.koreacentral-01.azurewebsites.net

**간단한 쇼핑몰 프로젝트**

## 프로젝트 개요

로그인, 회원가입, 상품 등록, 상품 조회, 상품 구매, 판매/구매 이력 조회가 가능한 쇼핑몰 웹 애플리케이션입니다. 현재 개인 프로젝트로 개발 진행 중이며, 점진적으로 기능을 확장해 나갈 예정입니다.

## 주요 기능 (현재 개발/구상 중)

* **사용자 인증:**
    * [✅] 로그인
    * [✅] 회원가입
    * [ ] 소셜 로그인 (추후 고려)
* **상품 관리 (판매자):**
    * [✅] 상품 등록 (Amazon S3, Presigned URL 활용)
    * [ ] 상품 수정
    * [ ] 상품 삭제
* **상품 조회 (구매자/판매자):**
    * [ ] 상품 목록 조회
    * [ ] 상품 상세 정보 조회
    * [ ] 상품 검색
* **상품 구매 (구매자):**
    * [ ] 장바구니
    * [ ] 결제 (Paypal 연동 예정)
* **주문/판매 이력 조회:**
    * [ ] 구매 내역 조회 (구매자)
    * [ ] 판매 내역 조회 (판매자)
* **기타:**
    * [ ] 상품 댓글 기능

**✅: 완료, ⏳: 진행 중, [ ]: 예정**

## 기술 스택

* **Backend:** Java 17, Spring Boot 3.4.4, Gradle(Groovy), JPA, Mysql
* **Frontend:** Thymeleaf, Bootstrap 5
* **External Service:** Amazon S3, Paypal (예정)

## 개발 환경 설정

1.  **Java 17 설치**
    * [Java 다운로드 링크](https://www.oracle.com/java/technologies/javase-downloads.html)
2.  **Mysql 설치 및 실행**
    * 데이터베이스 스키마는 [Wiki](https://github.com/your-username/Springmall2/wiki)에서 확인하실 수 있습니다. (아직 Wiki를 안 만들었다면 나중에 추가 예정이라고 언급해도 좋아요.)
3.  **Gradle 설치**
    * [Gradle 다운로드 링크](https://gradle.org/install/)
4.  **프로젝트 복제**
    ```bash
    git clone [https://github.com/your-username/Springmall2.git](https://github.com/your-username/Springmall2.git)
    cd Springmall2
    ```
5.  **빌드 및 실행**
    ```bash
    ./gradlew bootRun
    ```
    또는 IDE에서 Spring Boot Application 실행

## 실행 방법

* 웹 브라우저에서 `http://localhost:8080` (또는 설정된 포트)으로 접속합니다.
* 초기 사용자 생성 및 로그인 기능은 현재 개발 중입니다. (혹은 완료되었다면 간단한 안내)

## 기여 방법

현재 개인 프로젝트로 진행 중입니다. 추후 협업 기회가 생긴다면 기여 방법에 대한 안내를 추가할 예정입니다.

## 라이선스

[MIT](https://opensource.org/licenses/MIT) (원하는 라이선스를 명시하거나 추후 결정이라고 작성)

## Contact
yunok96@naver.com

---

해당 readme 문서는 Google Gemini 의 도움을 받아 작성하였습니다. - 이 문구는 Gemini의 도움을 받지 않았습니다.

**Last Update:** 2025-05-01 (최종 업데이트 날짜)
