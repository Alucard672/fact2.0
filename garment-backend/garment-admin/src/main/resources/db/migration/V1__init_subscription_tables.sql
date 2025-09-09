-- Initialize subscription-related tables

-- subscription_plans
CREATE TABLE IF NOT EXISTS subscription_plans (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plan_code VARCHAR(50) NOT NULL,
  plan_name VARCHAR(100) NOT NULL,
  description VARCHAR(500) NULL,
  plan_type VARCHAR(50) NOT NULL,
  monthly_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  yearly_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  trial_days INT NULL,
  user_limit INT NULL,
  workshop_limit INT NULL,
  storage_limit BIGINT NULL,
  features TEXT NULL,
  is_recommended TINYINT(1) DEFAULT 0,
  sort_order INT DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  UNIQUE KEY uk_plan_code (plan_code),
  KEY idx_status (status),
  KEY idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- subscriptions
CREATE TABLE IF NOT EXISTS subscriptions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  plan_id BIGINT NOT NULL,
  plan_name VARCHAR(100) NOT NULL,
  plan_price DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  auto_renew TINYINT(1) DEFAULT 0,
  user_limit INT NULL,
  workshop_limit INT NULL,
  storage_limit BIGINT NULL,
  features TEXT NULL,
  remark VARCHAR(255) NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  INDEX idx_tenant (tenant_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;