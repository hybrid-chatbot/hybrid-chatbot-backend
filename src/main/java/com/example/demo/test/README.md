# 🎯 가격순 정렬 기능 테스트 모음

이 폴더에는 가격순 정렬 기능과 관련된 모든 테스트 코드가 포함되어 있습니다.

## 📁 테스트 파일 목록

### 1. **PriceSortTestConsole.java** - 완전한 Spring Boot 테스트
- **설명**: Spring Boot 애플리케이션과 연동된 완전한 테스트
- **기능**: 
  - 더미 데이터 생성
  - 실제 데이터베이스 연동
  - 사용자 입력을 통한 인터랙티브 테스트
- **실행 방법**:
  ```bash
  # 터미널에서 실행
  mvn spring-boot:run -Dspring-boot.run.main-class=com.example.demo.test.PriceSortTestConsole
  
  # IDE에서 실행
  PriceSortTestConsole.java의 main 메서드 실행
  ```

### 2. **SimplePriceSortTest.java** - 간단한 독립 테스트
- **설명**: Spring Boot 없이도 실행 가능한 독립적인 테스트
- **기능**:
  - 가격순 정렬 로직 테스트
  - 키워드 감지 기능 테스트
  - 정렬 검증 기능
- **실행 방법**:
  ```bash
  # 터미널에서 실행
  javac -cp "src/main/java" src/main/java/com/example/demo/test/SimplePriceSortTest.java
  java -cp "src/main/java" com.example.demo.test.SimplePriceSortTest
  
  # IDE에서 실행
  SimplePriceSortTest.java의 main 메서드 실행
  ```

### 3. **ProductVisualizationTest.java** - 상품 시각화 테스트
- **설명**: 네이버 쇼핑 API를 호출하여 상품 데이터를 시각화하는 테스트
- **기능**:
  - 네이버 쇼핑 API 호출
  - 상품 데이터 파싱 및 표시
  - 콘솔 시각화
- **실행 방법**:
  ```bash
  # 터미널에서 실행
  javac -cp "src/main/java" src/main/java/com/example/demo/test/ProductVisualizationTest.java
  java -cp "src/main/java" com.example.demo.test.ProductVisualizationTest
  
  # IDE에서 실행
  ProductVisualizationTest.java의 main 메서드 실행
  ```

### 4. **NaverShoppingTest.java** - 네이버 API 테스트
- **설명**: 네이버 쇼핑 API의 기본 기능을 테스트
- **기능**:
  - API 호출 테스트
  - 응답 시간 측정
  - JSON 파싱 테스트
- **실행 방법**:
  ```bash
  # 터미널에서 실행
  javac -cp "src/main/java" src/main/java/com/example/demo/test/NaverShoppingTest.java
  java -cp "src/main/java" com.example.demo.test.NaverShoppingTest
  
  # IDE에서 실행
  NaverShoppingTest.java의 main 메서드 실행
  ```

## 🚀 빠른 시작 가이드

### 1단계: 간단한 테스트부터 시작
```bash
# 가장 간단한 테스트 실행
java -cp "src/main/java" com.example.demo.test.SimplePriceSortTest
```

### 2단계: 네이버 API 테스트
```bash
# 네이버 API 연결 테스트
java -cp "src/main/java" com.example.demo.test.NaverShoppingTest
```

### 3단계: 완전한 통합 테스트
```bash
# Spring Boot와 함께 완전한 테스트
mvn spring-boot:run -Dspring-boot.run.main-class=com.example.demo.test.PriceSortTestConsole
```

## 🎮 테스트 사용법

### 자동 테스트 모드
- 프로그램이 미리 정의된 테스트 케이스를 자동으로 실행
- 빠른 기능 검증에 적합

### 수동 테스트 모드
- 사용자가 직접 검색어나 명령을 입력
- 다양한 시나리오 테스트에 적합

## 📋 테스트 시나리오

### 가격순 정렬 테스트
- **최저가순**: "나이키 운동화 최저가순으로 검색해줘"
- **최고가순**: "나이키 운동화 최고가순으로 검색해줘"
- **낮은가격순**: "나이키 운동화 낮은가격순으로 검색해줘"
- **높은가격순**: "나이키 운동화 높은가격순으로 검색해줘"

### 일반 검색 테스트
- **기본 검색**: "나이키 운동화 검색해줘"
- **브랜드 검색**: "아디다스 신발 검색해줘"
- **카테고리 검색**: "컨버스 스니커즈 검색해줘"

## 🔧 환경 설정

### 필수 환경변수
```bash
# Naver API 키 설정 (PowerShell)
$env:NAVER_CLIENT_ID="MX1_wyfeo9eBuPfVTCSA"
$env:NAVER_CLIENT_SECRET="MdiPTZAHE0"

# Naver API 키 설정 (CMD)
set NAVER_CLIENT_ID=MX1_wyfeo9eBuPfVTCSA
set NAVER_CLIENT_SECRET=MdiPTZAHE0
```

### Java 버전
- Java 11 이상 권장
- Java 8에서도 실행 가능

## 🐛 문제 해결

### 1. 컴파일 오류
```bash
# 클래스패스 확인
javac -cp "src/main/java" src/main/java/com/example/demo/test/SimplePriceSortTest.java
```

### 2. 실행 오류
```bash
# 클래스패스에 src/main/java 포함 확인
java -cp "src/main/java" com.example.demo.test.SimplePriceSortTest
```

### 3. Spring Boot 실행 오류
```bash
# Maven 프로젝트 루트에서 실행
cd hybrid-chatbot-backend
mvn spring-boot:run -Dspring-boot.run.main-class=com.example.demo.test.PriceSortTestConsole
```

## 📊 예상 출력 결과

### 성공적인 테스트 출력 예시
```
==========================================
🎯 가격순 정렬 기능 테스트 시작
==========================================

📦 1단계: 더미 데이터 생성
------------------------------------------
✅ 더미 데이터 10개 생성 완료

🔍 2단계: 사용자 입력 테스트
------------------------------------------
🎮 사용자 입력 테스트 모드
💡 사용법:
   - 일반 검색: '나이키 운동화 검색해줘'
   - 최저가순: '나이키 운동화 최저가순으로 검색해줘'
   - 최고가순: '나이키 운동화 최고가순으로 검색해줘'
   - 종료: 'exit' 또는 'quit' 입력

🔍 검색어를 입력하세요: 나이키 운동화 최저가순으로 검색해줘

==================================================
🔍 검색 실행: '나이키 운동화 최저가순으로 검색해줘'
==================================================
📊 검색 결과:
   - 상품 개수: 20개
   - 정렬 정보: asc
   - 정렬 타입: price
   - 응답 메시지: '나이키 운동화 최저가순으로 검색해줘' 검색 결과 20개의 상품을 찾았습니다. (낮은 가격순으로 정렬)
   - 정렬 검증: ✅ 올바른 오름차순 정렬
   - 상품 목록 (상위 10개):
     1. 주니어 티엠포 레전드 10 클럽 TF... - 76,050원
     2. 코트 슛 COURT SHOT - 나이키... - 89,900원
     3. 나이키 뱀의 해 에어 모나크 4... - 122,500원
     ...
```

## 🎉 완료!

모든 테스트 코드가 `test` 폴더에 정리되었습니다. 원하는 테스트를 선택하여 실행해보세요!
