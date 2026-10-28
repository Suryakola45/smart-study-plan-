-- ========================================================
-- AI Smart Study Planner Database Schema
-- Compatible with MySQL 8.0+ and Tomcat 10.1
-- ========================================================

CREATE DATABASE IF NOT EXISTS smart_study_planner
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE smart_study_planner;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    preferred_strategy VARCHAR(30) DEFAULT 'PRIORITY_BASED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Subjects Table
CREATE TABLE IF NOT EXISTS subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20),
    exam_date DATE NOT NULL,
    color_hex VARCHAR(10) DEFAULT '#4361ee',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_subjects_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_subjects_user (user_id),
    INDEX idx_subjects_exam (exam_date)
) ENGINE=InnoDB;

-- 3. Topics Table
CREATE TABLE IF NOT EXISTS topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    difficulty INT NOT NULL,
    confidence_level INT NOT NULL,
    estimated_minutes INT NOT NULL,
    completed_minutes INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    calculated_priority DOUBLE DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_topics_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    INDEX idx_topics_subject (subject_id),
    INDEX idx_topics_status (status),
    INDEX idx_topics_priority (calculated_priority)
) ENGINE=InnoDB;

-- 4. Availability Table
CREATE TABLE IF NOT EXISTS availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    available_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT DEFAULT 45,
    break_duration_minutes INT DEFAULT 15,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_availability_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_avail_user_date (user_id, available_date)
) ENGINE=InnoDB;

-- 5. Study Sessions Table
CREATE TABLE IF NOT EXISTS study_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_minutes INT NOT NULL,
    actual_minutes_studied INT DEFAULT 0,
    status VARCHAR(25) DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sessions_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE,
    INDEX idx_sessions_user_date (user_id, session_date),
    INDEX idx_sessions_topic (topic_id),
    INDEX idx_sessions_status (status)
) ENGINE=InnoDB;
