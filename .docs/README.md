# 服装计件生产管理系统

## 文档目录说明

此文件夹用于集中管理项目的所有产品文档，包括以下类别：

## 项目说明文档
- README.md - 项目概述（本文件）
- README_COMPLETE.md - 完整的项目说明
- PROJECT_COMPLETION_REPORT.md - 项目完成报告
- DEVELOPMENT_STATUS.md - 开发状态报告

## 快速入门文档
- QUICK_START.md - 快速入门指南

## 部署文档
- DEPLOYMENT_GUIDE.md - 部署指南

## 技术设计文档
- 自部署服务器架构方案.md - 服务器架构设计
- 租户管理设计方案.md - 租户管理功能设计
- 服装计件生产小程序PRD_V0.2.md - 小程序产品需求文档

## API文档
- API_INTEGRATION_TEST.md - API集成测试文档

请保持文档的更新，确保与项目实际状态一致。

## 项目简介

服装计件生产管理系统是一个基于自部署服务器架构的企业级应用，专门为服装制造企业设计，支持多租户SaaS模式运营。系统涵盖从裁床建单到计件结算的完整生产流程管理。

## 🏗️ 技术架构

### 后端技术栈
- 框架: Spring Boot 2.7.18 + Spring Security + JWT
- 数据层: MyBatis Plus + MySQL 8.0 + Redis 6.0+
- 容器化: Docker + Docker Compose
- 工具库: Hutool + FastJSON + Apache Commons

### 前端技术栈
- 管理后台: Vue 3 + TypeScript + Element Plus + Vite
- 移动端: 微信小程序原生开发
- 部署: Nginx 反向代理 + SSL

### 基础设施
- 服务器: CentOS 8 / Ubuntu 20.04
- 数据库: MySQL 8.0 主从复制
- 缓存: Redis 6.0+ 集群
- 监控: Prometheus + Grafana

## 🚀 快速开始

### 环境要求
- Docker 20.10+
- Docker Compose 3.8+
- Java 11+
- Node.js 16+

### 本地开发部署

1. 克隆项目
```bash
git clone <repository-url>
cd fact2.0
```

2. 启动基础服务
```bash
# 启动 MySQL 和 Redis
docker-compose up -d mysql redis

# 等待数据库初始化完成（约30秒）
docker-compose logs mysql
```

3. 后端开发
```bash
cd garment-backend
mvn clean install
mvn spring-boot:run -pl garment-admin
```

4. 前端开发
```bash
cd garment-frontend
npm install
npm run dev
```

5. 完整部署
```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### 访问地址
- 管理后台：http://localhost (HTTPS: https://localhost)
- 后端API：http://localhost:8080
- 数据库：localhost:3306
- Redis：localhost:6379

### 默认账号
- 用户名：admin
- 密码：123456
- 手机号：13800138000

## 📋 核心功能

### 多租户管理
- ✅ 租户注册和企业信息管理
- ✅ 员工邀请和角色权限控制
- ✅ 数据隔离和租户切换
- ✅ 订阅套餐和功能限制

### 生产流程管理
- 🔄 裁床建单和智能分包
- 🔄 平均裁/分层段裁切模式
- 🔄 菲票打印（支持58mm/80mm蓝牙打印机）
- 🔄 扫码领工和收包流程
- 🔄 返修流程和质量管理

### 计件工资系统
- 🔄 灵活工价策略（款式+工序+车间）
- 🔄 自动计件和实时统计
- 🔄 加扣项管理（奖励/扣款/借支）
- 🔄 结算单生成和锁账机制
- 🔄 工资导出和批量发放

### 统计分析
- 🔄 实时生产看板
- 🔄 车间效率排名
- 🔄 多维度统计报表
- 🔄 数据导出（Excel/CSV）

图例：✅ 已完成 | 🔄 开发中 | ⏳ 计划中

## 📁 项目结构

```
fact2.0/
├── docker-compose.yml          # Docker编排文件
├── docker/                     # Docker配置目录
│   ├── mysql/                  # MySQL配置和初始化脚本
│   ├── redis/                  # Redis配置
│   └── nginx/                  # Nginx配置
├── garment-backend/            # 后端项目
│   ├── garment-common/         # 公共模块
│   ├── garment-auth/           # 认证模块
│   ├── garment-tenant/         # 租户管理
│   ├── garment-production/     # 生产管理
│   ├── garment-print/          # 打印服务
│   ├── garment-stats/          # 统计分析
│   └── garment-admin/          # 系统管理
├── garment-frontend/           # 管理后台前端
└── garment-miniprogram/        # 微信小程序
```

## 🔧 开发指南

### 后端开发

#### 环境配置
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/garment_production
    username: garment_user
    password: garment_pass_2024
  redis:
    host: localhost
    port: 6379
    password: garment_redis_2024
```

#### API规范
- 统一响应格式：ApiResponse<T>
- 分页查询：PageResponse<T>
- 异常处理：GlobalExceptionHandler
- 多租户支持：TenantContext

#### 数据库操作
```java
// 自动注入租户ID
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis Plus自动处理租户隔离
}
```

### 前端开发

#### 技术栈
- Vue 3 Composition API
- TypeScript 严格模式
- Element Plus UI组件库
- Vite 构建工具

#### 项目规范
- ESLint + Prettier 代码规范
- Husky Git Hook
- 组件化开发
- TypeScript 类型检查

### 小程序开发

#### 技术要点
- 原生小程序开发
- HTTP请求替代云开发调用
- 蓝牙打印机集成
- 扫码功能实现

## 📊 部署方案

### 生产环境部署

#### 服务器配置建议
- CPU: 4核心以上
- 内存: 8GB以上
- 存储: SSD 100GB以上
- 带宽: 10Mbps以上

#### 部署步骤
1. 服务器环境准备
2. Docker和Docker Compose安装
3. 项目代码部署
4. 环境变量配置
5. SSL证书配置
6. 服务启动和健康检查

#### 监控和维护
- Prometheus指标收集
- Grafana可视化监控
- 日志集中管理
- 定时备份策略

## 🤝 贡献指南

### 开发流程
1. Fork项目到个人仓库
2. 创建功能分支 (git checkout -b feature/AmazingFeature)
3. 提交更改 (git commit -m 'Add some AmazingFeature')
4. 推送到分支 (git push origin feature/AmazingFeature)
5. 创建Pull Request

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用统一的代码格式化配置
- 编写单元测试
- 更新相关文档

## 📄 许可证

本项目采用 MIT 许可证 - 详见 LICENSE 文件

## 📞 联系我们

- 项目维护者：Garment Team
- 邮箱：support@garment.com
- 官网：https://www.garment.com

---

注意: 这是一个企业级生产系统，请在生产环境中进行充分的测试和安全评估。
