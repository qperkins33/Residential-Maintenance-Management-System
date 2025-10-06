-- Placeholder schema for the Residential Maintenance Management System local database.
-- Iteration 1 focuses on project scaffolding, so this schema captures only the minimum
-- structure needed to persist example users for upcoming authentication work.

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
