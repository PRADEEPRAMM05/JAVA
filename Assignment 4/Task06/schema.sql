-- =========================================================================
-- SIMATS Engineering - Cultural Event Registration System Schema
-- Dialect: MySQL 8.0+ / ANSI SQL Standard
-- =========================================================================

CREATE DATABASE IF NOT EXISTS simats_events;
USE simats_events;

DROP TABLE IF EXISTS event_registrations;

CREATE TABLE event_registrations (
    registration_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(32) NOT NULL,
    student_name VARCHAR(128) NOT NULL,
    department VARCHAR(64) NOT NULL,
    event_name VARCHAR(128) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Business Rule: A student can register for an event only once
    CONSTRAINT uq_student_event UNIQUE (student_id, event_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Performance Index for event filtering and pagination
CREATE INDEX idx_event_name ON event_registrations (event_name);
CREATE INDEX idx_student_id ON event_registrations (student_id);

-- Sample Initial Data
INSERT INTO event_registrations (student_id, student_name, department, event_name, contact_phone)
VALUES 
('2024CS01', 'Aarav Patel', 'Computer Science', 'Code Relay Hackathon', '9876543210'),
('2024EC14', 'Bhavna Sharma', 'Electronics', 'RoboWars Combat', '9876543211'),
('2024ME09', 'Chetan Rao', 'Mechanical', 'CAD 3D Modeling Challenge', '9876543212'),
('2024CS22', 'Divya Nair', 'Computer Science', 'Battle of the Bands', '9876543213');
