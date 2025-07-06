# 1단계: 빌드용 이미지 (Gradle 빌드)
FROM gradle:8.2.1-jdk17 AS build

WORKDIR /app

# Gradle wrapper 복사 및 실행 권한 부여
COPY gradlew* ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# 설정 파일 복사
COPY build.gradle settings.gradle ./

# 의존성 다운로드 (캐시 최적화)
RUN ./gradlew --no-daemon dependencies

# 소스 코드 복사
COPY src ./src

# 빌드 실행
RUN ./gradlew --no-daemon clean bootJar -x test

# 2단계: 실행용 이미지
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]