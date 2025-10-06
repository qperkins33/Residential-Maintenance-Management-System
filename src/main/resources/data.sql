-- Seed data providing a single placeholder account for local development.
-- Credentials are intentionally simple and will be replaced once authentication is implemented.

INSERT INTO users (username, display_name, password_hash, role)
VALUES ('manager.demo', 'Quin Perkins', 'changeme', 'PROPERTY_MANAGER');
