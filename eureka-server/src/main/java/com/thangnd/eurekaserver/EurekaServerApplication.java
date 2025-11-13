package com.thangnd.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 1. Khi các microservice khởi động, chúng sẽ tự động đăng ký với Eureka Server
 * (gửi thông tin: tên service, địa chỉ IP, port, health status)
 *
 * 2. Eureka Server lưu trữ danh sách tất cả các service đang chạy trong hệ thống
 *
 * 3. Khi một service cần gọi service khác, nó sẽ hỏi Eureka Server:
 * "Service X đang chạy ở đâu?" và Eureka sẽ trả về danh sách các instance
 *
 * 4. Service có thể tự động load balance giữa các instance của service đích
 *
 * VÍ DỤ THỰC TẾ:
 * - Gateway muốn gọi Identity Service
 * - Gateway hỏi Eureka: "identity-svc ở đâu?"
 * - Eureka trả về: "identity-svc đang chạy ở localhost:8081"
 * - Gateway gọi đến localhost:8081
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
