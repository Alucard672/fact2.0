#!/bin/bash

# 测试环境部署脚本
set -e

echo "========================================="
echo "开始部署测试环境..."
echo "========================================="

# 检查 Docker 和 Docker Compose 是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装，请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "错误: Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

# 设置环境变量
export COMPOSE_PROJECT_NAME=garment-test
export ENVIRONMENT=test

# 创建必要的目录
echo "创建必要的目录..."
mkdir -p nginx/test/conf.d
mkdir -p database/init
mkdir -p logs/test

# 停止现有的测试环境容器
echo "停止现有的测试环境容器..."
docker-compose -f docker-compose.test.yml down

# 清理旧的镜像（可选）
echo "是否清理旧的 Docker 镜像? (y/N)"
read -r CLEAN_IMAGES
if [[ $CLEAN_IMAGES =~ ^[Yy]$ ]]; then
    echo "清理 Docker 镜像..."
    docker system prune -f
    docker image prune -f
fi

# 构建前端项目
echo "构建前端项目..."
cd garment-frontend
if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install --registry=https://registry.npmmirror.com
fi

echo "构建前端项目..."
npm run build

cd ..

# 构建后端项目
echo "构建后端项目..."
cd garment-backend

# 检查 Maven 是否安装
if ! command -v mvn &> /dev/null; then
    echo "错误: Maven 未安装，请先安装 Maven"
    exit 1
fi

echo "编译后端项目..."
mvn clean package -DskipTests -Ptest

cd ..

# 创建数据库初始化脚本
echo "创建数据库初始化脚本..."
cat > database/init/01-init-database.sql << 'EOF'
-- 创建测试数据库
CREATE DATABASE IF NOT EXISTS garment_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用测试数据库
USE garment_test;

-- 创建测试用户
CREATE USER IF NOT EXISTS 'garment'@'%' IDENTIFIED BY 'Garment@123';
GRANT ALL PRIVILEGES ON garment_test.* TO 'garment'@'%';
FLUSH PRIVILEGES;
EOF

# 启动测试环境
echo "启动测试环境..."
docker-compose -f docker-compose.test.yml up -d --build

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 检查服务健康状态
echo "检查服务健康状态..."
SERVICES=("garment-auth-test" "garment-admin-test" "garment-basic-test" "garment-production-test" "garment-print-test" "garment-stats-test" "garment-payroll-test")

for service in "${SERVICES[@]}"; do
    echo "检查 $service 服务状态..."
    max_attempts=30
    attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if docker exec $service curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
            echo "✅ $service 服务已启动"
            break
        else
            echo "⏳ 等待 $service 服务启动... (尝试 $attempt/$max_attempts)"
            sleep 10
            attempt=$((attempt + 1))
        fi
    done
    
    if [ $attempt -gt $max_attempts ]; then
        echo "❌ $service 服务启动失败"
        echo "查看服务日志:"
        docker logs $service --tail 50
        exit 1
    fi
done

# 检查 Nginx 状态
echo "检查 Nginx 服务状态..."
if curl -f http://localhost/health >/dev/null 2>&1; then
    echo "✅ Nginx 服务已启动"
else
    echo "❌ Nginx 服务启动失败"
    docker logs garment-nginx-test --tail 50
    exit 1
fi

# 显示部署结果
echo ""
echo "========================================="
echo "测试环境部署完成！"
echo "========================================="
echo ""
echo "服务访问地址:"
echo "前端地址: http://localhost"
echo "API 网关: http://localhost/api"
echo ""
echo "数据库连接信息:"
echo "主机: localhost:3307"
echo "数据库: garment_test"
echo "用户名: garment"
echo "密码: Garment@123"
echo ""
echo "Redis 连接信息:"
echo "主机: localhost:6380"
echo "密码: Redis@123"
echo ""
echo "后端服务端口:"
echo "认证服务: http://localhost:8081"
echo "管理服务: http://localhost:8082"
echo "基础数据: http://localhost:8083"
echo "生产管理: http://localhost:8084"
echo "打印服务: http://localhost:8085"
echo "统计分析: http://localhost:8086"
echo "计件工资: http://localhost:8087"
echo ""
echo "查看服务状态: docker-compose -f docker-compose.test.yml ps"
echo "查看服务日志: docker-compose -f docker-compose.test.yml logs [service-name]"
echo "停止测试环境: docker-compose -f docker-compose.test.yml down"
echo ""
echo "========================================="




