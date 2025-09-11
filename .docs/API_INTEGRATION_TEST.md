# API集成测试文档

## 测试环境配置

### 基础URL
- 本地开发: `http://localhost:8080`
- 测试环境: `http://test.garment.com`
- 生产环境: `http://api.garment.com`

### 认证方式
所有API请求需要在Header中携带JWT Token：
```
Authorization: Bearer {token}
```

## 核心业务流程测试

### 1. 完整生产流程测试

#### 1.1 创建裁床订单
```http
POST /api/production/cut-orders
Content-Type: application/json

{
  "styleId": 1,
  "color": "白色",
  "bedNo": "B001",
  "totalLayers": 100,
  "cuttingType": "average",
  "sizeRatio": {
    "S": 20,
    "M": 30,
    "L": 30,
    "XL": 20
  },
  "cuttingDate": "2024-01-20",
  "deliveryDate": "2024-01-25",
  "priority": "medium",
  "remark": "测试订单"
}
```

**预期结果**: 
- 状态码: 200
- 返回创建的订单信息，状态为"draft"

#### 1.2 确认裁床订单
```http
POST /api/production/cut-orders/{id}/confirm
```

**预期结果**: 
- 状态码: 200
- 订单状态变为"confirmed"

#### 1.3 生成包
```http
POST /api/production/bundles/generate
Content-Type: application/json

{
  "cutOrderId": 1,
  "bundleSize": 50,
  "regenerate": false
}
```

**预期结果**: 
- 状态码: 200
- 返回生成的包列表
- 每个包都有唯一的二维码

#### 1.4 打印包菲票
```http
POST /api/print/bundle-tickets/batch
Content-Type: application/json

{
  "cutOrderId": 1,
  "printerIp": "192.168.1.100"
}
```

**预期结果**: 
- 状态码: 200
- 返回打印任务信息

#### 1.5 扫码领工
```http
POST /api/production/flows/scan-take
Content-Type: application/json

{
  "qrCode": "BDL-1-1-1-check-A1B2",
  "processId": 1,
  "workerId": 1,
  "remark": "开始缝合工序"
}
```

**预期结果**: 
- 状态码: 200
- 返回生产流转记录
- 包状态变为"in_work"

#### 1.6 扫码交工
```http
POST /api/production/flows/scan-submit
Content-Type: application/json

{
  "qrCode": "BDL-1-1-1-check-A1B2",
  "quantityOk": 48,
  "quantityNg": 2,
  "qualityScore": 4,
  "needRepair": false,
  "workerId": 1,
  "remark": "缝合完成"
}
```

**预期结果**: 
- 状态码: 200
- 返回生产流转记录
- 包状态变为"completed"
- 自动生成计件记录

### 2. 计件工资流程测试

#### 2.1 查询计件记录
```http
GET /api/payroll/piecework-records?workerId=1&startDate=2024-01-20&endDate=2024-01-20
```

**预期结果**: 
- 状态码: 200
- 返回工人当日的计件记录

#### 2.2 创建工资周期
```http
POST /api/payroll/payroll-periods
Content-Type: application/json

{
  "periodType": "weekly",
  "startDate": "2024-01-15",
  "endDate": "2024-01-21"
}
```

**预期结果**: 
- 状态码: 200
- 返回创建的工资周期

#### 2.3 批量结算
```http
POST /api/payroll/piecework-records/batch-settle
Content-Type: application/json

{
  "recordIds": [1, 2, 3],
  "payrollPeriodId": 1
}
```

**预期结果**: 
- 状态码: 200
- 返回结算数量
- 记录状态变为"settled"

### 3. 统计分析测试

#### 3.1 获取生产统计
```http
POST /api/stats/production
Content-Type: application/json

{
  "dimension": "day",
  "startDate": "2024-01-20",
  "endDate": "2024-01-20",
  "workshopIds": [1],
  "includeRepair": false
}
```

**预期结果**: 
- 状态码: 200
- 返回生产统计数据

#### 3.2 获取车间排名
```http
GET /api/stats/workshop-ranking?period=today&sortBy=overall
```

**预期结果**: 
- 状态码: 200
- 返回车间排名数据

### 4. 小程序API测试

#### 4.1 工人端 - 获取今日统计
```http
GET /api/stats/today?workerId=1
```

**预期结果**: 
- 状态码: 200
- 返回工人今日统计数据

#### 4.2 管理端 - 获取今日概览
```http
GET /api/production/today-overview
```

**预期结果**: 
- 状态码: 200
- 返回今日生产概览数据

## 异常情况测试

### 1. 业务异常测试

#### 1.1 重复床次号
```http
POST /api/production/cut-orders
Content-Type: application/json

{
  "styleId": 1,
  "bedNo": "B001",  // 重复的床次号
  "totalLayers": 50,
  "cuttingType": "average",
  "sizeRatio": {"M": 100},
  "cuttingDate": "2024-01-20",
  "deliveryDate": "2024-01-25",
  "priority": "medium"
}
```

**预期结果**: 
- 状态码: 400
- 错误信息: "床次号已存在"

#### 1.2 无效二维码扫码
```http
POST /api/production/flows/scan-take
Content-Type: application/json

{
  "qrCode": "INVALID_QR_CODE",
  "processId": 1,
  "workerId": 1
}
```

**预期结果**: 
- 状态码: 400
- 错误信息: "无效的二维码"

#### 1.3 数量不匹配交工
```http
POST /api/production/flows/scan-submit
Content-Type: application/json

{
  "qrCode": "BDL-1-1-1-check-A1B2",
  "quantityOk": 60,  // 超过包总数量
  "quantityNg": 0,
  "qualityScore": 5,
  "workerId": 1
}
```

**预期结果**: 
- 状态码: 400
- 错误信息: "交工数量不能超过包总数量"

### 2. 权限异常测试

#### 2.1 未登录访问
```http
GET /api/production/cut-orders
```

**预期结果**: 
- 状态码: 401
- 错误信息: "未授权访问"

#### 2.2 跨租户访问
```http
GET /api/production/cut-orders/999
Authorization: Bearer {other_tenant_token}
```

**预期结果**: 
- 状态码: 403
- 错误信息: "无权访问该资源"

## 性能测试

### 1. 批量操作测试

#### 1.1 批量生成包（大订单）
```http
POST /api/production/bundles/generate
Content-Type: application/json

{
  "cutOrderId": 1,
  "bundleSize": 50
}
```

**性能要求**: 
- 1000件订单生成20个包 < 5秒
- 5000件订单生成100个包 < 15秒

#### 1.2 批量结算计件记录
```http
POST /api/payroll/piecework-records/batch-settle
Content-Type: application/json

{
  "recordIds": [1,2,3,...,1000],  // 1000条记录
  "payrollPeriodId": 1
}
```

**性能要求**: 
- 1000条记录批量结算 < 10秒
- 数据库事务完整性保证

### 2. 并发测试

#### 2.1 同时扫码测试
模拟10个工人同时扫码领工不同的包

**性能要求**: 
- 无数据冲突
- 响应时间 < 2秒
- 数据一致性保证

#### 2.2 统计查询并发
模拟20个管理员同时查询生产统计

**性能要求**: 
- 缓存命中率 > 80%
- 响应时间 < 3秒

## 数据一致性测试

### 1. 事务一致性
- 扫码交工时，生产流转记录和计件记录必须同时创建成功或失败
- 批量结算时，所有记录状态必须同时更新

### 2. 数据完整性
- 包的二维码必须全局唯一
- 计件记录的金额计算必须准确
- 统计数据与明细数据必须一致

## 测试数据准备

### 1. 基础数据
```sql
-- 租户数据
INSERT INTO tenants (name, contact_person, contact_phone, status) 
VALUES ('测试服装厂', '张经理', '13800138000', 'active');

-- 用户数据
INSERT INTO users (username, password, name, phone, email, status) 
VALUES ('test_admin', '$2a$10$...', '测试管理员', '13800138001', 'admin@test.com', 'active');

-- 车间数据
INSERT INTO workshops (tenant_id, name, description, manager_name, status) 
VALUES (1, '测试车间', '用于测试的车间', '李主管', 'active');

-- 款式数据
INSERT INTO styles (tenant_id, style_code, style_name, description, status) 
VALUES (1, 'TEST001', '测试款式', '用于测试的款式', 'active');

-- 工序数据
INSERT INTO processes (tenant_id, process_name, description, category, difficulty_level, status) 
VALUES (1, '测试缝合', '测试用缝合工序', '缝制', 3, 'active');
```

### 2. 测试脚本
```bash
#!/bin/bash
# 完整流程测试脚本

BASE_URL="http://localhost:8080"
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

echo "开始完整流程测试..."

# 1. 创建裁床订单
echo "1. 创建裁床订单"
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/production/cut-orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "styleId": 1,
    "color": "测试色",
    "bedNo": "TEST001",
    "totalLayers": 100,
    "cuttingType": "average",
    "sizeRatio": {"S": 25, "M": 25, "L": 25, "XL": 25},
    "cuttingDate": "2024-01-20",
    "deliveryDate": "2024-01-25",
    "priority": "medium"
  }')

ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.data.id')
echo "订单ID: $ORDER_ID"

# 2. 确认订单
echo "2. 确认订单"
curl -s -X POST "$BASE_URL/api/production/cut-orders/$ORDER_ID/confirm" \
  -H "Authorization: Bearer $TOKEN"

# 3. 生成包
echo "3. 生成包"
BUNDLE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/production/bundles/generate" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"cutOrderId\": $ORDER_ID, \"bundleSize\": 25}")

echo "包生成完成"

echo "测试完成！"
```

## 测试报告模板

### 测试执行记录
| 测试用例 | 执行时间 | 状态 | 响应时间 | 备注 |
|---------|---------|------|----------|------|
| 创建裁床订单 | 2024-01-20 10:00 | PASS | 150ms | 正常 |
| 生成包 | 2024-01-20 10:01 | PASS | 2.3s | 性能良好 |
| 扫码领工 | 2024-01-20 10:02 | PASS | 200ms | 正常 |
| 扫码交工 | 2024-01-20 10:03 | PASS | 300ms | 正常 |

### 问题记录
| 问题ID | 问题描述 | 严重程度 | 状态 | 修复时间 |
|--------|----------|----------|------|----------|
| BUG001 | 二维码解析偶现失败 | 中 | 已修复 | 2024-01-20 |

---

**注意事项**:
1. 所有测试都应该在独立的测试数据库中执行
2. 测试完成后及时清理测试数据
3. 关键业务流程必须有自动化测试覆盖
4. 性能测试应该在接近生产环境的配置下执行




