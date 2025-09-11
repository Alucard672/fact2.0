# 部署指南

## 系统架构概览

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端管理系统    │    │  微信小程序工人端  │    │  微信小程序管理端  │
│   (Vue 3)       │    │   (原生小程序)    │    │   (原生小程序)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌─────────────────────────────────────────────────┐
         │                   Nginx                         │
         │            (反向代理 + 静态资源)                 │
         └─────────────────────────────────────────────────┘
                                 │
         ┌─────────────────────────────────────────────────┐
         │              Spring Boot 后端                   │
         │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │
         │  │ 认证模块 │ │ 生产管理 │ │ 统计分析 │ │ 计件工资 │ │
         │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ │
         │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │
         │  │ 基础资料 │ │ 打印服务 │ │ 系统管理 │ │ 公共模块 │ │
         │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ │
         └─────────────────────────────────────────────────┘
                                 │
         ┌─────────────────────────────────────────────────┐
         │                数据存储层                        │
         │  ┌─────────────┐              ┌─────────────┐   │
         │  │   MySQL     │              │    Redis    │   │
         │  │  (主数据库)  │              │   (缓存)    │   │
         │  └─────────────┘              └─────────────┘   │
         └─────────────────────────────────────────────────┘
```

## 环境要求

### 服务器配置
- **CPU**: 4核心以上
- **内存**: 8GB以上
- **存储**: 100GB以上 SSD
- **网络**: 带宽10Mbps以上
- **操作系统**: CentOS 8+ / Ubuntu 20.04+

### 软件版本
- **Java**: OpenJDK 11+
- **Node.js**: 16.x+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Nginx**: 1.18+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+

## 部署方式

### 方式一：Docker Compose 部署（推荐）

#### 1. 准备部署文件
```bash
# 克隆项目
git clone https://github.com/your-org/garment-production.git
cd garment-production

# 创建部署目录
mkdir -p /opt/garment-production
cp -r . /opt/garment-production/
cd /opt/garment-production
```

#### 2. 配置环境变量
```bash
# 复制环境变量模板
cp .env.example .env

# 编辑环境变量
vim .env
```

`.env` 文件内容：
```bash
# 基础配置
COMPOSE_PROJECT_NAME=garment-production
ENVIRONMENT=production

# 数据库配置
MYSQL_ROOT_PASSWORD=your_strong_password_here
MYSQL_DATABASE=garment_production
MYSQL_USER=garment_user
MYSQL_PASSWORD=your_db_password_here

# Redis配置
REDIS_PASSWORD=your_redis_password_here

# 应用配置
JWT_SECRET=your_jwt_secret_key_here_must_be_very_long_and_secure
APP_DOMAIN=yourdomain.com
SSL_ENABLED=true

# 文件存储
UPLOAD_PATH=/opt/garment-data/uploads
LOG_PATH=/opt/garment-data/logs

# 邮件配置（可选）
SMTP_HOST=smtp.qq.com
SMTP_PORT=587
SMTP_USER=your_email@qq.com
SMTP_PASSWORD=your_email_password
```

#### 3. 初始化数据目录
```bash
# 创建数据目录
mkdir -p /opt/garment-data/{mysql,redis,uploads,logs,backup}

# 设置权限
chown -R 1000:1000 /opt/garment-data
chmod -R 755 /opt/garment-data
```

#### 4. 启动服务
```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

#### 5. 初始化数据库
```bash
# 等待MySQL启动完成
sleep 30

# 执行数据库初始化脚本
docker-compose exec mysql mysql -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE < docker/mysql/init/01-init-database.sql

# 创建初始管理员账户
docker-compose exec backend java -jar app.jar --init-admin
```

### 方式二：传统部署

#### 1. 安装依赖软件

##### MySQL 8.0
```bash
# CentOS 8
dnf install mysql-server
systemctl start mysqld
systemctl enable mysqld

# 设置root密码
mysql_secure_installation

# 创建数据库和用户
mysql -uroot -p
CREATE DATABASE garment_production CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'garment_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON garment_production.* TO 'garment_user'@'%';
FLUSH PRIVILEGES;
```

##### Redis 6.0
```bash
# CentOS 8
dnf install redis
systemctl start redis
systemctl enable redis

# 配置Redis密码
echo "requirepass your_redis_password" >> /etc/redis/redis.conf
systemctl restart redis
```

##### Java 11
```bash
# 安装OpenJDK 11
dnf install java-11-openjdk-devel

# 验证安装
java -version
```

##### Nginx
```bash
# 安装Nginx
dnf install nginx
systemctl start nginx
systemctl enable nginx
```

#### 2. 部署后端应用

```bash
# 创建应用目录
mkdir -p /opt/garment-backend
cd /opt/garment-backend

# 上传编译好的JAR文件
# 或者在服务器上编译
git clone https://github.com/your-org/garment-production.git
cd garment-production/garment-backend
./mvnw clean package -DskipTests

# 复制JAR文件
cp garment-admin/target/garment-admin-1.0.0.jar /opt/garment-backend/
cp garment-production/target/garment-production-1.0.0.jar /opt/garment-backend/
cp garment-payroll/target/garment-payroll-1.0.0.jar /opt/garment-backend/
cp garment-stats/target/garment-stats-1.0.0.jar /opt/garment-backend/
cp garment-print/target/garment-print-1.0.0.jar /opt/garment-backend/

# 创建配置文件
cat > application-prod.yml << EOF
server:
  port: 8080
  
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/garment_production
    username: garment_user
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    
logging:
  file:
    name: /opt/garment-backend/logs/application.log
  level:
    com.garment: info
EOF

# 创建启动脚本
cat > start.sh << 'EOF'
#!/bin/bash
nohup java -jar -Xms2g -Xmx4g -Dspring.profiles.active=prod garment-admin-1.0.0.jar > logs/admin.log 2>&1 &
echo $! > pids/admin.pid

nohup java -jar -Xms1g -Xmx2g -Dspring.profiles.active=prod garment-production-1.0.0.jar --server.port=8081 > logs/production.log 2>&1 &
echo $! > pids/production.pid

nohup java -jar -Xms1g -Xmx2g -Dspring.profiles.active=prod garment-payroll-1.0.0.jar --server.port=8082 > logs/payroll.log 2>&1 &
echo $! > pids/payroll.pid

nohup java -jar -Xms1g -Xmx2g -Dspring.profiles.active=prod garment-stats-1.0.0.jar --server.port=8083 > logs/stats.log 2>&1 &
echo $! > pids/stats.pid

nohup java -jar -Xms1g -Xmx2g -Dspring.profiles.active=prod garment-print-1.0.0.jar --server.port=8084 > logs/print.log 2>&1 &
echo $! > pids/print.pid

echo "所有服务已启动"
EOF

chmod +x start.sh

# 创建停止脚本
cat > stop.sh << 'EOF'
#!/bin/bash
for pid_file in pids/*.pid; do
    if [ -f "$pid_file" ]; then
        pid=$(cat "$pid_file")
        kill $pid
        rm "$pid_file"
        echo "已停止进程 $pid"
    fi
done
EOF

chmod +x stop.sh

# 创建必要目录
mkdir -p logs pids

# 启动服务
./start.sh
```

#### 3. 部署前端应用

```bash
# 安装Node.js和npm
curl -fsSL https://rpm.nodesource.com/setup_16.x | bash -
dnf install nodejs

# 创建前端目录
mkdir -p /opt/garment-frontend
cd /opt/garment-frontend

# 上传前端代码并构建
git clone https://github.com/your-org/garment-production.git
cd garment-production/garment-frontend

# 安装依赖
npm install

# 配置生产环境
cat > .env.production << EOF
VITE_API_BASE_URL=https://yourdomain.com/api
VITE_APP_TITLE=服装计件生产管理系统
EOF

# 构建生产版本
npm run build

# 复制构建文件到nginx目录
cp -r dist/* /var/www/html/
```

#### 4. 配置Nginx

```bash
# 备份默认配置
cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak

# 创建站点配置
cat > /etc/nginx/conf.d/garment.conf << 'EOF'
# 前端静态文件服务
server {
    listen 80;
    server_name yourdomain.com;
    
    # 重定向到HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;
    
    # SSL证书配置
    ssl_certificate /etc/ssl/certs/yourdomain.com.crt;
    ssl_certificate_key /etc/ssl/private/yourdomain.com.key;
    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_stapling on;
    ssl_stapling_verify on;
    
    # 前端静态文件
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # API代理
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # 缓冲设置
        proxy_buffering on;
        proxy_buffer_size 64k;
        proxy_buffers 32 64k;
    }
    
    # 文件上传
    location /uploads/ {
        alias /opt/garment-data/uploads/;
        expires 1y;
        add_header Cache-Control "public";
    }
    
    # 健康检查
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}

# 负载均衡配置（如果有多个后端实例）
upstream backend {
    server localhost:8080 weight=1 max_fails=3 fail_timeout=30s;
    # server localhost:8081 weight=1 max_fails=3 fail_timeout=30s;
    keepalive 32;
}
EOF

# 测试配置
nginx -t

# 重启Nginx
systemctl restart nginx
```

## SSL证书配置

### 使用Let's Encrypt免费证书

```bash
# 安装certbot
dnf install certbot python3-certbot-nginx

# 获取证书
certbot --nginx -d yourdomain.com

# 设置自动续期
echo "0 12 * * * /usr/bin/certbot renew --quiet" | crontab -
```

## 监控和日志

### 1. 系统监控

#### 安装Prometheus和Grafana
```bash
# 创建监控目录
mkdir -p /opt/monitoring
cd /opt/monitoring

# 下载Prometheus
wget https://github.com/prometheus/prometheus/releases/download/v2.40.0/prometheus-2.40.0.linux-amd64.tar.gz
tar xzf prometheus-2.40.0.linux-amd64.tar.gz
mv prometheus-2.40.0.linux-amd64 prometheus

# 配置Prometheus
cat > prometheus/prometheus.yml << EOF
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'garment-backend'
    static_configs:
      - targets: ['localhost:8080', 'localhost:8081', 'localhost:8082', 'localhost:8083', 'localhost:8084']
    metrics_path: '/actuator/prometheus'
    
  - job_name: 'node'
    static_configs:
      - targets: ['localhost:9100']
      
  - job_name: 'mysql'
    static_configs:
      - targets: ['localhost:9104']
EOF

# 启动Prometheus
nohup ./prometheus/prometheus --config.file=prometheus/prometheus.yml --storage.tsdb.path=prometheus/data > prometheus/prometheus.log 2>&1 &
```

### 2. 日志管理

#### 配置日志轮转
```bash
# 创建logrotate配置
cat > /etc/logrotate.d/garment << EOF
/opt/garment-backend/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    create 0644 garment garment
    postrotate
        # 重启应用以重新打开日志文件
        /opt/garment-backend/restart.sh > /dev/null 2>&1 || true
    endscript
}
EOF
```

## 备份策略

### 1. 数据库备份

```bash
# 创建备份脚本
cat > /opt/garment-data/backup/backup-db.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/garment-data/backup"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="garment_production"
DB_USER="garment_user"
DB_PASS="your_password"

# 创建备份
mysqldump -u$DB_USER -p$DB_PASS $DB_NAME | gzip > $BACKUP_DIR/db_backup_$DATE.sql.gz

# 删除7天前的备份
find $BACKUP_DIR -name "db_backup_*.sql.gz" -mtime +7 -delete

echo "数据库备份完成: db_backup_$DATE.sql.gz"
EOF

chmod +x /opt/garment-data/backup/backup-db.sh

# 设置定时备份
echo "0 2 * * * /opt/garment-data/backup/backup-db.sh" | crontab -
```

### 2. 文件备份

```bash
# 创建文件备份脚本
cat > /opt/garment-data/backup/backup-files.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/garment-data/backup"
DATE=$(date +%Y%m%d_%H%M%S)

# 备份上传文件
tar -czf $BACKUP_DIR/uploads_backup_$DATE.tar.gz -C /opt/garment-data uploads/

# 备份配置文件
tar -czf $BACKUP_DIR/config_backup_$DATE.tar.gz /opt/garment-backend/*.yml /etc/nginx/conf.d/garment.conf

# 删除30天前的备份
find $BACKUP_DIR -name "*_backup_*.tar.gz" -mtime +30 -delete

echo "文件备份完成: uploads_backup_$DATE.tar.gz, config_backup_$DATE.tar.gz"
EOF

chmod +x /opt/garment-data/backup/backup-files.sh

# 设置定时备份
echo "0 3 * * 0 /opt/garment-data/backup/backup-files.sh" | crontab -
```

## 性能优化

### 1. JVM优化

```bash
# 编辑启动脚本，添加JVM参数
cat > /opt/garment-backend/jvm-options.txt << EOF
-Xms2g
-Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UnlockExperimentalVMOptions
-XX:+UseJVMCICompiler
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-Xloggc:logs/gc.log
-XX:+UseGCLogFileRotation
-XX:NumberOfGCLogFiles=5
-XX:GCLogFileSize=10M
EOF
```

### 2. MySQL优化

```bash
# 编辑MySQL配置
cat >> /etc/my.cnf << EOF
[mysqld]
# 基本设置
max_connections = 500
max_connect_errors = 1000
table_open_cache = 1024
max_allowed_packet = 128M

# 内存设置
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_log_buffer_size = 16M
innodb_flush_log_at_trx_commit = 2

# 查询缓存
query_cache_size = 128M
query_cache_type = 1

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
EOF

systemctl restart mysqld
```

### 3. Redis优化

```bash
# 编辑Redis配置
cat >> /etc/redis/redis.conf << EOF
# 内存优化
maxmemory 1gb
maxmemory-policy allkeys-lru

# 持久化优化
save 900 1
save 300 10
save 60 10000

# 网络优化
tcp-keepalive 300
timeout 300
EOF

systemctl restart redis
```

## 安全配置

### 1. 防火墙配置

```bash
# 开启防火墙
systemctl start firewalld
systemctl enable firewalld

# 开放必要端口
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https
firewall-cmd --permanent --add-port=22/tcp

# 限制数据库端口只允许本机访问
firewall-cmd --permanent --add-rich-rule="rule family='ipv4' source address='127.0.0.1' port port='3306' protocol='tcp' accept"

# 重载防火墙规则
firewall-cmd --reload
```

### 2. 应用安全配置

```bash
# 创建专用用户
useradd -r -s /bin/false garment
chown -R garment:garment /opt/garment-backend
chown -R garment:garment /opt/garment-data

# 设置文件权限
chmod 750 /opt/garment-backend
chmod 640 /opt/garment-backend/*.yml
chmod 755 /opt/garment-backend/*.sh
```

## 故障排除

### 1. 常见问题

#### 服务无法启动
```bash
# 检查端口占用
netstat -tlnp | grep :8080

# 检查日志
tail -f /opt/garment-backend/logs/application.log

# 检查Java进程
ps aux | grep java
```

#### 数据库连接失败
```bash
# 检查MySQL状态
systemctl status mysqld

# 测试连接
mysql -ugarment_user -p -h localhost garment_production

# 检查防火墙
firewall-cmd --list-all
```

#### 前端页面无法访问
```bash
# 检查Nginx状态
systemctl status nginx

# 检查Nginx配置
nginx -t

# 查看Nginx错误日志
tail -f /var/log/nginx/error.log
```

### 2. 性能问题排查

```bash
# 查看系统资源使用
top
htop
iotop

# 查看Java内存使用
jstat -gc [pid]
jmap -histo [pid]

# 查看数据库性能
mysql -e "SHOW PROCESSLIST;"
mysql -e "SHOW ENGINE INNODB STATUS;"
```

## 运维脚本

### 1. 服务管理脚本

```bash
cat > /opt/garment-backend/service.sh << 'EOF'
#!/bin/bash

case "$1" in
    start)
        echo "启动服务..."
        ./start.sh
        ;;
    stop)
        echo "停止服务..."
        ./stop.sh
        ;;
    restart)
        echo "重启服务..."
        ./stop.sh
        sleep 5
        ./start.sh
        ;;
    status)
        echo "服务状态:"
        for pid_file in pids/*.pid; do
            if [ -f "$pid_file" ]; then
                pid=$(cat "$pid_file")
                service_name=$(basename "$pid_file" .pid)
                if ps -p $pid > /dev/null; then
                    echo "$service_name: 运行中 (PID: $pid)"
                else
                    echo "$service_name: 已停止"
                fi
            fi
        done
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
EOF

chmod +x /opt/garment-backend/service.sh
```

### 2. 健康检查脚本

```bash
cat > /opt/garment-backend/health-check.sh << 'EOF'
#!/bin/bash

SERVICES=("admin:8080" "production:8081" "payroll:8082" "stats:8083" "print:8084")
EMAIL="admin@yourdomain.com"

for service in "${SERVICES[@]}"; do
    name=${service%:*}
    port=${service#*:}
    
    if ! curl -f -s http://localhost:$port/actuator/health > /dev/null; then
        echo "服务 $name 健康检查失败，尝试重启..."
        
        # 发送告警邮件
        echo "服务 $name 在 $(date) 健康检查失败，已尝试重启" | mail -s "服务告警" $EMAIL
        
        # 重启服务
        ./service.sh restart
        
        break
    fi
done
EOF

chmod +x /opt/garment-backend/health-check.sh

# 设置定时健康检查
echo "*/5 * * * * /opt/garment-backend/health-check.sh" | crontab -
```

---

**部署完成后的验证步骤**:

1. 访问 `https://yourdomain.com` 确认前端页面正常
2. 使用默认管理员账户登录测试
3. 创建测试数据验证各功能模块
4. 检查日志文件确认无错误
5. 运行性能测试确认系统稳定性

**注意事项**:
- 首次部署前请仔细阅读所有配置项
- 生产环境密码必须使用强密码
- 定期检查系统日志和监控指标
- 建议设置自动化部署流程




