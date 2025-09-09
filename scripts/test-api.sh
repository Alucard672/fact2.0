#!/bin/bash

# API 联调测试脚本
set -e

echo "========================================="
echo "开始执行 API 联调测试..."
echo "========================================="

# 设置基础变量
BASE_URL="http://localhost"
API_BASE="$BASE_URL/api"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 测试函数
test_api() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_status="$5"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -n "测试: $name ... "
    
    if [ "$method" == "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$url")
    elif [ "$method" == "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" == "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" == "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE "$url")
    fi
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [ "$status_code" == "$expected_status" ]; then
        echo -e "${GREEN}✅ PASSED${NC} (HTTP $status_code)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ FAILED${NC} (Expected: $expected_status, Got: $status_code)"
        echo "Response: $response_body"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# 等待服务启动
echo "等待服务启动..."
sleep 10

# 检查服务健康状态
echo "检查服务健康状态..."
curl -f "$BASE_URL/health" || {
    echo -e "${RED}错误: Nginx 服务未启动${NC}"
    exit 1
}

echo -e "${GREEN}✅ 服务已启动，开始测试...${NC}"
echo ""

# ==========================================
# 1. 认证服务测试
# ==========================================
echo -e "${YELLOW}=== 认证服务测试 ===${NC}"

# 用户注册测试
test_api "用户注册" "POST" "$API_BASE/auth/register" '{
    "username": "testuser",
    "password": "Test@123456",
    "email": "test@example.com",
    "phone": "13800138000"
}' "200"

# 用户登录测试
test_api "用户登录" "POST" "$API_BASE/auth/login" '{
    "username": "testuser",
    "password": "Test@123456"
}' "200"

# 获取用户信息测试（需要token，这里测试无token情况）
test_api "获取用户信息(无token)" "GET" "$API_BASE/auth/user/profile" "" "401"

echo ""

# ==========================================
# 2. 基础数据服务测试
# ==========================================
echo -e "${YELLOW}=== 基础数据服务测试 ===${NC}"

# 获取客户列表
test_api "获取客户列表" "POST" "$API_BASE/basic/customers/query" '{
    "page": 1,
    "size": 10
}' "200"

# 创建客户
test_api "创建客户" "POST" "$API_BASE/basic/customers" '{
    "name": "测试客户",
    "contactPerson": "张三",
    "phone": "13800138001",
    "address": "测试地址"
}' "200"

# 获取车间列表
test_api "获取车间列表" "POST" "$API_BASE/basic/workshops/query" '{
    "page": 1,
    "size": 10
}' "200"

# 创建车间
test_api "创建车间" "POST" "$API_BASE/basic/workshops" '{
    "name": "测试车间",
    "description": "测试车间描述",
    "capacity": 50
}' "200"

echo ""

# ==========================================
# 3. 生产管理服务测试
# ==========================================
echo -e "${YELLOW}=== 生产管理服务测试 ===${NC}"

# 获取裁床单列表
test_api "获取裁床单列表" "POST" "$API_BASE/production/cut-orders/query" '{
    "page": 1,
    "size": 10
}' "200"

# 创建裁床单
test_api "创建裁床单" "POST" "$API_BASE/production/cut-orders" '{
    "orderNo": "CO20240101001",
    "customerId": 1,
    "styleId": 1,
    "totalQuantity": 1000,
    "urgentLevel": "normal"
}' "200"

# 获取扎包列表
test_api "获取扎包列表" "POST" "$API_BASE/production/bundles/query" '{
    "page": 1,
    "size": 10
}' "200"

echo ""

# ==========================================
# 4. 打印服务测试
# ==========================================
echo -e "${YELLOW}=== 打印服务测试 ===${NC}"

# 获取打印任务列表
test_api "获取打印任务列表" "POST" "$API_BASE/print/tasks/query" '{
    "page": 1,
    "size": 10
}' "200"

# 创建打印任务
test_api "创建打印任务" "POST" "$API_BASE/print/tasks" '{
    "taskName": "测试打印任务",
    "templateId": 1,
    "bundleIds": [1, 2, 3]
}' "200"

echo ""

# ==========================================
# 5. 统计分析服务测试
# ==========================================
echo -e "${YELLOW}=== 统计分析服务测试 ===${NC}"

# 获取生产统计
test_api "获取生产统计" "POST" "$API_BASE/stats/production" '{
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "workshopIds": []
}' "200"

# 获取车间排名
test_api "获取车间排名" "POST" "$API_BASE/stats/workshop-ranking" '{
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "rankType": "efficiency"
}' "200"

echo ""

# ==========================================
# 6. 计件工资服务测试
# ==========================================
echo -e "${YELLOW}=== 计件工资服务测试 ===${NC}"

# 获取计件记录列表
test_api "获取计件记录列表" "POST" "$API_BASE/payroll/piecework-records/query" '{
    "page": 1,
    "size": 10
}' "200"

# 获取工资期间列表
test_api "获取工资期间列表" "POST" "$API_BASE/payroll/payroll-periods/query" '{
    "page": 1,
    "size": 10
}' "200"

echo ""

# ==========================================
# 7. 管理服务测试
# ==========================================
echo -e "${YELLOW}=== 管理服务测试 ===${NC}"

# 获取租户列表
test_api "获取租户列表" "POST" "$API_BASE/admin/tenants/query" '{
    "page": 1,
    "size": 10
}' "200"

# 获取订阅套餐列表
test_api "获取订阅套餐列表" "GET" "$API_BASE/admin/subscription-plans/active" "" "200"

echo ""

# ==========================================
# 测试结果统计
# ==========================================
echo "========================================="
echo -e "${YELLOW}测试结果统计${NC}"
echo "========================================="
echo "总测试数: $TOTAL_TESTS"
echo -e "通过数: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败数: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}🎉 所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}❌ 有测试失败，请检查服务状态${NC}"
    exit 1
fi




