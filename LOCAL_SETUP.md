# 로컬 개발 환경 세팅 가이드

## 사전 요구사항

| 도구 | 버전 | 비고 |
|------|------|------|
| Java | 17+ | OpenJDK Temurin 권장 |
| Node.js | 16+ | 현재 확인: v24.14.0 |
| Python | 3.12 | AI 서버용 |
| Docker Desktop | 최신 | MongoDB 실행용 |
| OpenAI API Key | - | 의도 분류 및 임베딩에 필요 |
| (선택) Google Dialogflow 인증 JSON | - | 없어도 기본 동작 가능 |
| (선택) Naver Shopping API Key | - | 쇼핑 검색 기능에 필요 |

## 1. 외부 서비스 실행

### MongoDB (필수)
```bash
# Docker로 MongoDB 실행
docker run -d --name mongodb -p 27017:27017 mongo:latest

# 실행 확인
docker ps | grep mongodb
```

### Redis (선택 - 캐싱용)
```bash
# Docker로 Redis 실행
docker run -d --name redis -p 6379:6379 redis:latest

# 실행 확인
docker ps | grep redis
```
> Redis가 없어도 서버가 실행됩니다. 캐시 에러가 로그에 나올 수 있지만 기능에 영향 없습니다.

### Kafka (선택 - local 프로필에서 비활성화됨)
```bash
# 로컬 개발 시에는 Kafka가 필요 없습니다.
# local 프로필이 Kafka를 비활성화하고, 메시지를 직접 처리합니다.

# Kafka가 필요한 경우 (dev 프로필):
docker compose up -d
```

## 2. AI 서버 실행 (Port: 8000)

```bash
cd hybrid-chatbot-ai

# 가상환경 생성 및 활성화
python -m venv .venv
# Windows
.venv\Scripts\activate
# Mac/Linux
source .venv/bin/activate

# 의존성 설치
pip install -r requirements.txt

# 환경변수 설정
# .env 파일에 OPENAI_API_KEY 설정 필요

# 서버 실행
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

확인: `http://localhost:8000/docs` 에서 API 문서 확인 가능

## 3. Backend 실행 (Port: 8080)

```bash
cd hybrid-chatbot-backend

# 환경변수 설정 (.env.example 참고)
# 방법 1: .env.example을 복사하여 .env 생성 후 IDE에서 로드
# 방법 2: 직접 환경변수 설정
export OPENAI_API_KEY=your-key-here
export SPRING_PROFILES_ACTIVE=local

# 빌드
./mvnw package -DskipTests

# 서버 실행
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

확인: `http://localhost:8080/actuator` 에서 Health 확인 가능

### local 프로필 특징
- **Kafka 비활성화**: 메시지를 Kafka 없이 직접 처리
- **Dialogflow 비활성화**: AI 서버(FastAPI)로 직접 의도 분류 위임
- MongoDB, Redis는 로컬 기본값(localhost) 사용

## 4. Frontend 실행 (Port: 8501)

```bash
cd hybrid-chatbot-frontend

# 의존성 설치
npm install

# 환경변수 설정 (선택사항 - 기본값이 localhost:8080)
# .env.example을 복사하여 .env.local 생성
cp .env.example .env.local

# 개발 서버 실행
npm run dev
```

확인: `http://localhost:8501` 에서 채팅 UI 확인

## 5. 동작 확인

### 서버 상태 확인
```bash
# Backend 상태 확인
curl http://localhost:8080/actuator

# AI 서버 상태 확인
curl http://localhost:8000/docs
```

### 채팅 테스트
1. 브라우저에서 `http://localhost:8501` 접속
2. 채팅창에 메시지 입력 (예: "환불 절차를 알려주세요")
3. 응답이 돌아오는지 확인

### API 직접 테스트
```bash
# CS 상담 메시지 전송
curl -X POST http://localhost:8080/api/messages/receive \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "test-session-1",
    "userId": "testUser",
    "message": "환불 절차를 알려주세요",
    "languageCode": "ko"
  }'

# 결과 조회 (몇 초 후)
curl http://localhost:8080/api/messages/result/test-session-1
```

## 트러블슈팅

### MongoDB 연결 실패
```
MongoSocketOpenException: Exception opening socket
```
- Docker Desktop이 실행 중인지 확인
- `docker ps`로 MongoDB 컨테이너가 동작 중인지 확인
- `docker start mongodb`로 재시작

### OpenAI API Key 오류
```
Could not resolve placeholder 'spring.ai.openai.api-key'
```
- `OPENAI_API_KEY` 환경변수를 설정했는지 확인
- IDE에서 환경변수가 올바르게 주입되는지 확인

### AI 서버 연결 실패
```
ResourceAccessException: I/O error on POST request
```
- AI 서버(`http://localhost:8000`)가 실행 중인지 확인
- 재시도 로직이 내장되어 있으므로 (최대 3회) AI 서버 시작 후 자동 복구됨

### Frontend CORS 오류
```
Access to XMLHttpRequest has been blocked by CORS policy
```
- Backend의 `@CrossOrigin` 설정에 `http://localhost:8501`이 포함되어 있는지 확인
- Backend가 8080 포트에서 정상 동작 중인지 확인

### Kafka 관련 오류 (dev 프로필 사용 시)
```
KafkaException: Failed to construct kafka producer
```
- `local` 프로필을 사용하면 Kafka 불필요
- `SPRING_PROFILES_ACTIVE=local`로 설정

### Port 충돌
- AI 서버: 8000
- Backend: 8080
- Frontend: 8501
- MongoDB: 27017
- Redis: 6379
- 해당 포트가 이미 사용 중이면 기존 프로세스를 종료하거나 포트를 변경
