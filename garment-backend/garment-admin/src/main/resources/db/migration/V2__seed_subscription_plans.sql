-- Seed demo data for subscription_plans (idempotent)

INSERT INTO subscription_plans (
  plan_code, plan_name, description, plan_type,
  monthly_price, yearly_price, trial_days,
  user_limit, workshop_limit, storage_limit,
  features, is_recommended, sort_order, status
)
SELECT 'trial', '试用版', '新租户试用套餐，含基础功能', 'trial',
       0.00, 0.00, 14,
       5, 1, 2048,
       'basic_reporting,barcode_print', 0, 10, 'active'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE plan_code = 'trial');

INSERT INTO subscription_plans (
  plan_code, plan_name, description, plan_type,
  monthly_price, yearly_price, trial_days,
  user_limit, workshop_limit, storage_limit,
  features, is_recommended, sort_order, status
)
SELECT 'basic', '基础版', '适合小型车间的核心功能套餐', 'basic',
       99.00, 999.00, NULL,
       20, 2, 10240,
       'basic_reporting,plan_scheduling,barcode_print', 1, 20, 'active'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE plan_code = 'basic');

INSERT INTO subscription_plans (
  plan_code, plan_name, description, plan_type,
  monthly_price, yearly_price, trial_days,
  user_limit, workshop_limit, storage_limit,
  features, is_recommended, sort_order, status
)
SELECT 'standard', '标准版', '成长型团队的进阶功能套餐', 'standard',
       199.00, 1999.00, NULL,
       50, 5, 51200,
       'basic_reporting,plan_scheduling,barcode_print,quality_control,workload_analytics', 1, 30, 'active'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE plan_code = 'standard');

INSERT INTO subscription_plans (
  plan_code, plan_name, description, plan_type,
  monthly_price, yearly_price, trial_days,
  user_limit, workshop_limit, storage_limit,
  features, is_recommended, sort_order, status
)
SELECT 'premium', '高级版', '大中型工厂的全面生产管理', 'premium',
       399.00, 3999.00, NULL,
       200, 20, 204800,
       'basic_reporting,plan_scheduling,barcode_print,quality_control,workload_analytics,advanced_printing,role_based_access', 0, 40, 'active'
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE plan_code = 'premium');