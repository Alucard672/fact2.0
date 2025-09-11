# Fact2.0 服装计件生产管理系统

## 项目简介

Fact2.0 是一个自部署服务器架构的服装生产管理系统，支持多租户SaaS模式，涵盖从裁床建单到计件结算的完整生产流程管理。

## 文档位置

所有项目文档已集中到 `docs/` 文件夹中，请查看：

- [项目说明文档](docs/README.md)
- [快速入门指南](docs/QUICK_START.md)
- [部署指南](docs/DEPLOYMENT_GUIDE.md)
- [技术设计文档](docs/)

## 项目结构

```
├── docs/              # 项目文档
├── garment-backend/   # 后端代码（Spring Boot）
├── garment-frontend/  # 前端代码（Vue 3）
├── garment-miniprogram-manager/  # 小程序代码
├── docker/            # Docker配置
└── scripts/           # 部署和开发脚本
```

## 快速开始

请查看 [docs/QUICK_START.md](docs/QUICK_START.md) 文件了解如何快速启动项目。