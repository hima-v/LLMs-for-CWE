-- Create database
CREATE DATABASE myapp_db;

-- Use the database
USE myapp_db;

-- Create a configuration table to store database credentials
CREATE TABLE system_config (
    config_id INT PRIMARY KEY AUTO_INCREMENT,
    server_name VARCHAR(255) NOT NULL,
    database_name VARCHAR(255) NOT NULL,
    database_user VARCHAR(255) NOT NULL,
    database_password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert configuration values
INSERT INTO system_config (server_name, database_name, database_user, database_password)
VALUES ('localhost', 'myapp_db', 'db_user', 'secure_password_123');

-- Function to get server name
DELIMITER //
CREATE FUNCTION get_server_name()
RETURNS VARCHAR(255)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE server VARCHAR(255);
    SELECT server_name INTO server FROM system_config LIMIT 1;
    RETURN server;
END //
DELIMITER ;

-- Function to get database name
DELIMITER //
CREATE FUNCTION get_database_name()
RETURNS VARCHAR(255)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE db_name VARCHAR(255);
    SELECT database_name INTO db_name FROM system_config LIMIT 1;
    RETURN db_name;
END //
DELIMITER ;

-- Function to get database user
DELIMITER //
CREATE FUNCTION get_database_user()
RETURNS VARCHAR(255)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE db_user VARCHAR(255);
    SELECT database_user INTO db_user FROM system_config LIMIT 1;
    RETURN db_user;
END //
DELIMITER ;

-- Function to get database password
DELIMITER //
CREATE FUNCTION get_database_password()
RETURNS VARCHAR(255)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE db_password VARCHAR(255);
    SELECT database_password INTO db_password FROM system_config LIMIT 1;
    RETURN db_password;
END //
DELIMITER ;

-- Query to retrieve all configuration
SELECT 
    get_server_name() AS server_name,
    get_database_name() AS database_name,
    get_database_user() AS database_user,
    get_database_password() AS database_password;