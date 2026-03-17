# 하이브리드 챗봇 프로젝트

## 프로젝트 소개

이 프로젝트는 정형화된 질의에는 빠르게 응답하고, 복잡하거나 모호한 질의에는 대화 맥락을 유연하게 파악할 수 있는 Rule-based NLP + LLM 구조의 하이브리드 챗봇입니다.
Dialogflow를 기반으로 한 NLP 모듈과 OpenAI API를 활용한 LLM 모듈을 결합하여, 높은 정확도와 신뢰성, 응답 속도를 모두 만족시키는 고객 상담 챗봇을 구현합니다.


## 주요 기능

- 사용자 입력 기반 챗봇 대화
- 의도 기반 챗봇 응답 처리 (NLP 중심)
- LLM 기반 의도 분석 보조
- NLP 최종 응답 생성
- MongoDB 기반 대화 이력 저장
- 테스트 시나리오 기반 응답 흐름 구현



## 기술 스택

**프론트엔드:**
- React
- TypeScript
- Vite
- axios (API 통신)

**백엔드:**
- Spring Boot
- Google Cloud Dialogflow API

## 설치 및 실행 방법

프로젝트를 로컬 환경에서 실행하기 위해 다음 단계를 따르세요.

### 1. 리포지토리 클론

```bash
git clone [프로젝트 리포지토리 주소]
cd hybrid-chatbot
```

### 2. 백엔드 설정 및 실행

(백엔드 설정 및 실행 방법에 대한 설명 추가 - 예: Gradle/Maven 빌드 및 실행 명령어, Dialogflow 설정 방법 등)

### 3. 프론트엔드 설정 및 실행

`frontend` 디렉토리로 이동하여 필요한 패키지를 설치합니다.

```bash
cd frontend

# 개발 의존성 설치 (React, React-DOM, UUID 타입 정보 등)
npm install --save-dev @types/react @types/react-dom @types/uuid

# 프로젝트 의존성 설치 (package.json에 명시된 모든 패키지)
npm install

# API 통신 라이브러리 axios 설치
npm install axios
```
> **참고**: `npm install` 명령 실행 시 `package.json`과 `package-lock.json` 파일을 기반으로 필요한 모든 의존성이 설치되므로, `npm install axios`는 이미 `package.json`에 명시되어 있다면 `npm install`만으로 설치됩니다. 위에 제시된 명령어들은 사용자가 명시적으로 알리고 싶은 설치 과정입니다.

패키지 설치 후 프론트엔드 개발 서버를 실행합니다.

```bash
npm run dev
```

개발 서버가 시작되면 웹 브라우저에서 `http://localhost:8502` (또는 터미널에 표시된 주소)로 접속하여 챗봇을 사용할 수 있습니다.

## 🌿 브랜치 전략

- `main`: 배포 가능한 안정화 버전 유지
- `develop`: 최신 개발 기능 통합, 병합 전 테스트용 브랜치
- `feature/*`: 신규 기능 개발 브랜치 (ex: feature/intent-routing)
- `fix/*`: 버그 수정 브랜치
- `hotfix/*`: 긴급 수정사항 처리

모든 개발은 `feature/*` 브랜치에서 시작하며, `develop`에 Pull Request를 통해 병합합니다. `develop`은 최종 테스트 후 `main`으로 병합됩니다.

## 사용법


## 라이선스
