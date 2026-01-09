-- Database initialization script

CREATE TABLE IF NOT EXISTS venues (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL,
    location VARCHAR(500) NOT NULL,
    sport_id VARCHAR(50) NOT NULL,
    sport_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sport_id (sport_id),
    INDEX idx_name (name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS time_slots (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          venue_id BIGINT NOT NULL,
                                          start_time DATETIME NOT NULL,
                                          end_time DATETIME NOT NULL,
                                          is_available BOOLEAN DEFAULT TRUE,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE,
    INDEX idx_venue_time (venue_id, start_time, end_time),
    INDEX idx_availability (venue_id, is_available),
    CONSTRAINT chk_time_order CHECK (end_time > start_time),
    UNIQUE KEY unique_venue_time (venue_id, start_time, end_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bookings (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        slot_id BIGINT NOT NULL,
                                        customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20),
    status ENUM('CONFIRMED', 'CANCELLED') DEFAULT 'CONFIRMED',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (slot_id) REFERENCES time_slots(id) ON DELETE CASCADE,
    INDEX idx_customer_email (customer_email),
    INDEX idx_status (status),
    INDEX idx_slot_id (slot_id),
    INDEX idx_slot_status (slot_id, status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;