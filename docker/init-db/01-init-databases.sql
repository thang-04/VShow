-- init-db/01-init-databases.sql

-- Tạo database cho Identity Service
CREATE DATABASE IF NOT EXISTS vshow_identity
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Tạo database cho Event Catalog Service
CREATE DATABASE IF NOT EXISTS vshow_events
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Cấp quyền cho root user (optional - đã có sẵn)
GRANT ALL PRIVILEGES ON vshow_identity.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON vshow_events.* TO 'root'@'%';

FLUSH PRIVILEGES;
