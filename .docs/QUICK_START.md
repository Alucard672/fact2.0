# 🚀 服装计件生产管理系统 - 快速启动指南

## 📋 系统要求

### 必需软件
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Node.js**: 16.0+ (用于前端构建)
- **Maven**: 3.6+ (用于后端构建)
- **Java**: 11+ (用于后端编译)

### 推荐配置
- **内存**: 8GB+
- **存储**: 20GB+ 可用空间
- **CPU**: 4核心+

## ⚡ 快速启动 (5分钟部署)

### 1. 克隆项目
```bash
git clone <repository-url>
cd fact2.0
```

### 2. 一键部署测试环境
```bash
# 给脚本执行权限
chmod +x scripts/deploy-test.sh

# 执行部署
./scripts/deploy-test.sh
```

### 3. 访问系统
部署完成后，可以通过以下地址访问：

- **前端管理系统**: http://localhost
- **API接口**: http://localhost/api
- **系统健康检查**: http://localhost/health

## 🔑 默认登录信息

### 系统管理员
- **用户名**: admin
- **密码**: Admin@123456

### 测试租户
- **租户代码**: test001
- **用户名**: testuser
- **密码**: Test@123456

## 🛠️ 开发环境设置

### 后端开发
```bash
cd garment-backend

# 编译所有模块
mvn clean compile

# 运行特定服务（以认证服务为例）
cd garment-auth
mvn spring-boot:run
```

### 前端开发
```bash
cd garment-frontend

# 安装依赖
npm install --registry=https://registry.npmmirror.com

# 启动开发服务器
npm run dev
```

### 小程序开发
1. 使用微信开发者工具打开 `garment-miniprogram-manager` 目录
2. 配置小程序AppID
3. 点击编译预览

## 📊 服务端口分配

| 服务 | 端口 | 描述 |
|------|------|------|
| Nginx | 80 | 前端和API网关 |
| 认证服务 | 8081 | 用户认证和授权 |
| 管理服务 | 8082 | 租户和系统管理 |
| 基础数据服务 | 8083 | 客户、款式、车间管理 |
| 生产管理服务 | 8084 | 裁床单、分包管理 |
| 打印服务 | 8085 | 菲票打印管理 |
| 统计分析服务 | 8086 | 数据统计和报表 |
| 计件工资服务 | 8087 | 工资计算和结算 |
| MySQL | 3307 | 数据库 |
| Redis | 6380 | 缓存服务 |

## 🧪 测试

### API测试
```bash
# 执行API联调测试
./scripts/test-api.sh
```

### 功能测试清单
- [ ] 用户登录注册
- [ ] 创建客户和款式
- [ ] 创建裁床单
- [ ] 生成扎包
- [ ] 扫码领工（小程序）
- [ ] 提交工作（小程序）
- [ ] 查看统计报表
- [ ] 工资结算

## 🔧 常见问题

### Q: 服务启动失败怎么办？
A: 检查服务日志
```bash
# 查看所有服务状态
docker-compose -f docker-compose.test.yml ps

# 查看特定服务日志
docker-compose -f docker-compose.test.yml logs garment-auth-test
```

### Q: 前端页面无法访问？
A: 检查Nginx状态
```bash
# 检查Nginx容器
docker logs garment-nginx-test

# 测试Nginx健康状态
curl http://localhost/health
```

### Q: 数据库连接失败？
A: 检查MySQL服务
```bash
# 检查MySQL容器
docker logs garment-mysql-test

# 测试数据库连接
docker exec -it garment-mysql-test mysql -u garment -p
```

### Q: 如何重置系统数据？
A: 重新部署环境
```bash
# 停止所有服务
docker-compose -f docker-compose.test.yml down

# 清理数据卷（注意：会删除所有数据）
docker-compose -f docker-compose.test.yml down -v

# 重新部署
./scripts/deploy-test.sh
```

## 📱 小程序配置

### 管理端小程序
1. 打开微信开发者工具
2. 导入项目：选择 `garment-miniprogram-manager` 目录
3. 修改 `utils/config.js` 中的API地址
4. 编译并预览

## 🚀 生产环境部署

### 准备工作
1. 准备生产服务器（推荐配置：4核8GB内存）
2. 安装Docker和Docker Compose
3. 配置域名和SSL证书
4. 修改生产环境配置文件

### 部署步骤
```bash
# 1. 上传项目文件到服务器
scp -r fact2.0/ user@server:/path/to/

# 2. 登录服务器
ssh user@server

# 3. 修改生产配置
cd /path/to/fact2.0
cp docker-compose.test.yml docker-compose.prod.yml
# 编辑生产配置...

# 4. 执行部署
./scripts/deploy-prod.sh
```

## 📞 技术支持

如果遇到问题，可以通过以下方式获取帮助：

1. **查看日志**: 使用Docker命令查看服务日志
2. **检查配置**: 确认环境变量和配置文件
3. **重启服务**: 尝试重启相关服务
4. **联系开发团队**: 提供详细的错误信息

## 📚 相关文档

- [项目完成报告](PROJECT_COMPLETION_REPORT.md)
- [部署指南](DEPLOYMENT_GUIDE.md)
- [API集成测试](API_INTEGRATION_TEST.md)
- [系统架构文档](README.md)

---

🎉 **祝您使用愉快！如有问题，请及时反馈。**




