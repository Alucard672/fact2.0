-- Create application user and grant privileges for garment_production
CREATE USER IF NOT EXISTS 'garment_user'@'%' IDENTIFIED BY 'garment_pass_2024';
CREATE USER IF NOT EXISTS 'garment_user'@'localhost' IDENTIFIED BY 'garment_pass_2024';

-- Ensure MySQL 8 compatibility for common drivers
ALTER USER 'garment_user'@'%' IDENTIFIED WITH mysql_native_password BY 'garment_pass_2024';
ALTER USER 'garment_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'garment_pass_2024';

GRANT ALL PRIVILEGES ON garment_production.* TO 'garment_user'@'%';
GRANT ALL PRIVILEGES ON garment_production.* TO 'garment_user'@'localhost';
FLUSH PRIVILEGES;

-- Show result
SELECT Host, User, plugin FROM mysql.user WHERE User='garment_user';
SHOW GRANTS FOR 'garment_user'@'%';