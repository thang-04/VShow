package com.vticket.eventcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * VAI TRÒ CỦA EVENT CATALOG SERVICE:
 * Service này chịu trách nhiệm quản lý thông tin về các sự kiện (events) trong hệ thống:
 * - Tạo, cập nhật, xóa sự kiện
 * - Xem danh sách sự kiện
 * - Quản lý loại vé (ticket types)
 * - Quản lý thông tin chỗ ngồi (seat metadata)
 * 
 * CÁC CHỨC NĂNG CHÍNH:
 * 1. Event Management:
 *    - CRUD operations cho events
 *    - Tìm kiếm và lọc events
 *    - Quản lý thông tin chi tiết event (tên, mô tả, ngày giờ, địa điểm...)
 * 
 * 2. Ticket Type Management:
 *    - Định nghĩa các loại vé (VIP, Standard, Economy...)
 *    - Quản lý giá vé cho từng loại
 * 
 * 3. Seat Metadata:
 *    - Lưu trữ thông tin về sơ đồ chỗ ngồi
 *    - Metadata về các khu vực, hàng, ghế
 * 
 * KIẾN TRÚC:
 * - Domain Layer: Business entities (Event, TicketType, SeatMeta)
 * - Application Layer: Use cases (CreateEventUseCase, GetEventUseCase...)
 * - Infrastructure Layer: JPA repositories để tương tác với database
 * - Web Layer: REST Controllers để expose APIs
 * 
 * DATABASE:
 * - MySQL: Lưu trữ thông tin events, ticket types, seat metadata
 * - Database name: vshow_events
 * 
 * BẢO MẬT:
 * - Service này được bảo vệ bởi Gateway (JWT authentication)
 * - Chỉ user đã đăng nhập mới có thể truy cập
 * - Có thể phân quyền: ADMIN mới được tạo/sửa/xóa events
 * 
 * TƯƠNG TÁC VỚI CÁC SERVICE KHÁC:
 * - Gateway: Nhận request từ client qua Gateway
 * - Identity Service: Có thể gọi để verify user permissions
 * - Booking Service (tương lai): Cung cấp thông tin events để đặt vé
 * - Seatmap Service (tương lai): Cung cấp metadata về chỗ ngồi
 * 
 * ANNOTATIONS:
 * - @SpringBootApplication: Đánh dấu đây là Spring Boot application
 * - scanBasePackages: Chỉ scan các package trong "com.vticket.eventcatalog"
 * - @EnableJpaRepositories: Kích hoạt JPA repositories
 * - @EntityScan: Chỉ định package chứa JPA entities
 */
@SpringBootApplication(scanBasePackages = "com.vticket.eventcatalog")
@EnableJpaRepositories(basePackages = "com.vticket.eventcatalog.infra.jpa")
@EntityScan(basePackages = "com.vticket.eventcatalog.infra.jpa")
@EnableDiscoveryClient
public class EventCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventCatalogApplication.class, args);
    }

}
