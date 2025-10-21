# CI/CD Pipeline 가이드

이 프로젝트는 GitHub Actions를 사용한 자동화된 CI/CD 파이프라인을 구축했습니다.

## 🚀 파이프라인 구성

### 1. 메인 CI/CD 파이프라인 (`.github/workflows/ci-cd.yml`)

**트리거 조건:**
- `main`, `develop` 브랜치에 push
- `main`, `develop` 브랜치로의 Pull Request

**작업 단계:**
1. **테스트 (test)**
   - Java 17 환경 설정
   - Maven 의존성 캐싱
   - MongoDB, Kafka 서비스 실행
   - 단위 테스트 실행
   - 테스트 리포트 생성

2. **빌드 (build)**
   - Maven으로 애플리케이션 빌드
   - Docker 이미지 빌드 및 GitHub Container Registry에 푸시
   - 멀티 플랫폼 빌드 지원

3. **보안 스캔 (security-scan)**
   - Trivy를 사용한 취약점 스캔
   - GitHub Security 탭에 결과 업로드

4. **배포 (deploy)**
   - `main` 브랜치에만 실행
   - 프로덕션 환경 배포 (사용자 정의 필요)

### 2. Docker Compose 통합 테스트 (`.github/workflows/docker-compose-test.yml`)

**목적:** 실제 Docker Compose 환경에서의 통합 테스트

**작업 단계:**
- 애플리케이션 빌드
- Docker Compose로 서비스 실행
- 통합 테스트 실행
- 실패 시 로그 수집

### 3. 릴리즈 파이프라인 (`.github/workflows/release.yml`)

**트리거 조건:** `v*` 태그 푸시

**작업 단계:**
- 테스트 실행
- Docker 이미지 빌드 및 푸시
- GitHub Release 생성
- 시맨틱 버전 태그 지원

## 🔧 설정 방법

### 1. GitHub Secrets 설정

다음 시크릿을 GitHub 저장소 설정에서 추가하세요:

```
OPENAI_API_KEY=your_openai_api_key
DIALOGFLOW_PROJECT_ID=hybrid-chatbot
DIALOGFLOW_CREDENTIALS=your_dialogflow_service_account_json
MONGODB_URI=mongodb://localhost:27017/test
DOCKER_USERNAME=your_dockerhub_username
DOCKER_PASSWORD=your_dockerhub_password
```

### 2. 환경별 설정

- **개발 환경**: `application.yml` (기본)
- **테스트 환경**: `application-test.yml`
- **프로덕션**: 환경 변수 또는 별도 설정 파일

### 3. Docker 이미지 사용

```bash
# 최신 이미지 실행
docker run -p 8080:8080 your-dockerhub-username/hybrid-chatbot-backend:latest

# 특정 태그 실행
docker run -p 8080:8080 your-dockerhub-username/hybrid-chatbot-backend:v1.0.0

# develop 브랜치 이미지 실행
docker run -p 8080:8080 your-dockerhub-username/hybrid-chatbot-backend:develop
```

## 📊 모니터링

### 1. GitHub Actions 탭
- 파이프라인 실행 상태 확인
- 실패한 작업 디버깅
- 로그 및 아티팩트 다운로드

### 2. Security 탭
- 취약점 스캔 결과 확인
- 의존성 보안 이슈 추적

### 3. Packages 탭
- 빌드된 Docker 이미지 관리
- 이미지 다운로드 및 사용

## 🛠️ 커스터마이징

### 1. 배포 환경 설정

`ci-cd.yml`의 `deploy` 작업에서 실제 배포 명령을 추가하세요:

```yaml
- name: Deploy to production
  run: |
    # Kubernetes 배포 예시
    kubectl apply -f k8s/
    
    # Docker Swarm 배포 예시
    docker service update --image ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }} your-service
```

### 2. 추가 테스트 단계

필요에 따라 다음 테스트를 추가할 수 있습니다:
- 정적 코드 분석 (SonarQube)
- 성능 테스트
- E2E 테스트

### 3. 알림 설정

Slack, Discord 등으로 알림을 추가할 수 있습니다:

```yaml
- name: Notify on failure
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: failure
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## 🔄 워크플로우

### 개발 워크플로우
1. `develop` 브랜치에서 개발
2. Pull Request 생성
3. 자동 테스트 실행
4. 코드 리뷰 후 머지

### 릴리즈 워크플로우
1. `main` 브랜치에 머지
2. 자동 배포 실행
3. 태그 생성으로 릴리즈
4. GitHub Release 자동 생성

## 📝 주의사항

1. **의존성 관리**: Dependabot이 자동으로 의존성 업데이트 PR을 생성합니다.
2. **보안**: 민감한 정보는 GitHub Secrets에 저장하세요.
3. **리소스**: GitHub Actions 무료 플랜 제한을 고려하세요.
4. **캐싱**: Maven 의존성과 Docker 레이어가 자동으로 캐싱됩니다.

## 🆘 문제 해결

### 일반적인 문제들

1. **테스트 실패**
   - 로컬에서 테스트 실행 확인
   - 환경 변수 설정 확인

2. **Docker 빌드 실패**
   - Dockerfile 문법 확인
   - 의존성 경로 확인

3. **배포 실패**
   - 배포 환경 접근 권한 확인
   - 환경 변수 및 시크릿 확인

자세한 로그는 GitHub Actions 탭에서 확인할 수 있습니다.
