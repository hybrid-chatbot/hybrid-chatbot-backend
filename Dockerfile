# 1. 베이스 이미지 선택
# 리눅스 완경의 자바 17버전을 베이스로 
FROM openjdk:18-jdk-slim

# 2. .jar 파일을 이미지 안으로 복사하고 이름 변경
# COPY [원본 경로] [이미지 안의 새로운 경로/이름]
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# 3. 실행 지시
CMD ["java", "-jar", "app.jar"]