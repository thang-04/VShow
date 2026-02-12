
# Multi-stage build Dockerfile dùng chung cho tất cả các services
# Sử dụng: docker build --build-arg SERVICE_NAME=<tên-service> -t <image-name> .

# =============================================================================
# Stage 1: Build stage - Build tất cả modules và tạo JAR file
# =============================================================================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Argument để xác định service cần build
ARG SERVICE_NAME

# Copy toàn bộ project vào container
WORKDIR /app
COPY pom.xml .
COPY common-libs ./common-libs
COPY eureka-server ./eureka-server
COPY gateway ./gateway
COPY identity-svc ./identity-svc
COPY event-catalog-svc ./event-catalog-svc
COPY notify-svc ./notify-svc

# Build common-libs trước (dependency dùng chung)
RUN mvn clean install -pl common-libs -am -DskipTests

# Build service được chỉ định
RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests

# =============================================================================
# Stage 2: Runtime stage - Chạy ứng dụng với image nhẹ hơn
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

# Tạo user non-root để tăng bảo mật
RUN addgroup -S vticket && adduser -S vticket -G vticket

RUN mkdir -p /tmp && chmod 1777 /tmp
RUN mkdir -p /app/tmp && chown -R vticket:vticket /app/tmp
# Tạo thư mục làm việc
WORKDIR /app

# Argument để xác định service
ARG SERVICE_NAME

# Copy JAR file từ build stage
COPY --from=builder /app/${SERVICE_NAME}/target/*.jar /tmp/
RUN set -eux; \
    JAR="$(ls -1 /tmp/*.jar | grep -v '\.original$' | head -n 1)"; \
    cp "$JAR" /app/app.jar; \
    rm -rf /tmp/*

# Đổi ownership cho user vticket
RUN chown -R vticket:vticket /app

# Chuyển sang user non-root
USER vticket

# Expose port mặc định (sẽ được override trong docker-compose)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

# Chạy ứng dụng với các JVM options tối ưu cho container
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-XX:InitialRAMPercentage=50.0","-Djava.security.egd=file:/dev/./urandom","-Djava.io.tmpdir=/app/tmp","-jar","app.jar"]

