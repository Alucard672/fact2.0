# Git Flow 工作流配置指南

## 1. 概述

本指南详细说明如何在 Fact2.0 服装计件生产管理系统项目中应用 Git Flow 工作流，以实现高效的版本控制和协作开发。项目包含后端（Spring Boot）、前端（Vue 3）和微信小程序（原生开发）三个主要部分。

## 2. Git Flow 分支结构

### 2.1 长期分支

- **main**: 主分支，用于存放生产环境代码
- **develop**: 开发分支，用于集成分支开发，存放最新的开发代码

### 2.2 短期分支

- **feature/\<功能名称\>**: 特性分支，用于开发新功能
- **release/\<版本号\>**: 发布分支，用于准备新版本发布
- **hotfix/\<问题描述\>**: 热修复分支，用于修复生产环境问题
- **bugfix/\<问题描述\>**: 缺陷修复分支，用于修复开发环境问题

## 3. 分支创建与合并规则

### 3.1 初始化 Git Flow

```bash
# 初始化 Git Flow (仅首次配置时需要)
git flow init

# 按默认配置即可，主要确认以下分支命名:
# - 生产分支: main
# - 开发分支: develop
# - 特性分支前缀: feature/
# - 发布分支前缀: release/
# - 热修复分支前缀: hotfix/
```

### 3.2 开发新功能 (Feature)

```bash
# 从 develop 分支创建新的特性分支
git flow feature start <功能名称> 
# 例如: git flow feature start user-authentication

# 开发完成后，合并到 develop 分支
git flow feature finish <功能名称>
```

### 3.3 准备发布 (Release)

```bash
# 从 develop 分支创建发布分支
git flow release start <版本号>
# 例如: git flow release start 1.0.0

# 在发布分支上进行版本号更新、文档更新等
# 完成后，同时合并到 main 和 develop 分支
git flow release finish <版本号>

# 将发布推送到远程仓库
git push origin main
git push origin develop
git push origin --tags
```

### 3.4 修复生产问题 (Hotfix)

```bash
# 从 main 分支创建热修复分支
git flow hotfix start <问题描述>
# 例如: git flow hotfix start fix-login-error

# 修复完成后，同时合并到 main 和 develop 分支
git flow hotfix finish <问题描述>

# 将热修复推送到远程仓库
git push origin main
git push origin develop
git push origin --tags
```

### 3.5 修复开发环境问题 (Bugfix)

```bash
# 从 develop 分支创建缺陷修复分支
git checkout develop
git checkout -b bugfix/<问题描述>
# 例如: git checkout -b bugfix/fix-api-response

# 修复完成后，合并到 develop 分支
git checkout develop
git merge --no-ff bugfix/<问题描述>
git branch -d bugfix/<问题描述>
git push origin develop
```

## 4. 多模块项目的 Git Flow 应用

由于本项目包含后端、前端和小程序三个模块，特制定以下策略：

### 4.1 模块标识

在分支命名中加入模块标识，便于识别：

```
feature/<模块>-<功能名称>
# 例如:
# - feature/backend-user-management
# - feature/frontend-dashboard
# - feature/miniprogram-scan
```

### 4.2 模块间依赖管理

- 后端 API 变更应先在后端分支完成并合并到 develop
- 前端和小程序依赖后端 API 的功能，应在后端 API 稳定后开始开发
- 版本发布时，确保三个模块的版本号保持一致

### 4.3 版本号管理

使用语义化版本号（Semantic Versioning）：`MAJOR.MINOR.PATCH`

- `MAJOR`: 不兼容的API变更
- `MINOR`: 向下兼容的功能性新增
- `PATCH`: 向下兼容的问题修正

在 `version.json` 文件中统一管理版本信息：

```json
{
  "version": "1.0.0",
  "apiVersion": "v1",
  "frontend": {
    "version": "1.0.0",
    "compatibleApiVersions": ["v1"]
  },
  "backend": {
    "version": "1.0.0",
    "apiVersion": "v1"
  },
  "miniprogram": {
    "version": "1.0.0",
    "compatibleApiVersions": ["v1"]
  }
}
```

## 5. 提交规范

使用统一的提交消息格式，便于自动化版本日志生成：

```
<类型>: <描述>

[可选的详细说明]

[关联的Issue/任务编号]
```

提交类型包括：
- **feat**: 新功能
- **fix**: 修复问题
- **docs**: 文档变更
- **style**: 格式调整（不影响代码功能）
- **refactor**: 代码重构（不新增功能或修复问题）
- **test**: 测试相关
- **chore**: 构建过程或辅助工具变动

## 6. CI/CD 集成

Git Flow 与 CI/CD 流程集成，实现自动化测试和部署：

- **feature 分支**: 提交代码后自动运行单元测试
- **develop 分支**: 提交代码后自动运行集成测试和部署到测试环境
- **release 分支**: 自动运行系统测试和预发布环境部署
- **main 分支**: 自动部署到生产环境

## 7. 协作开发指南

### 7.1 代码同步

- 每天开始工作前，从远程仓库拉取最新代码
- 推送代码前，确保本地代码与远程代码同步

```bash
git checkout develop
git pull origin develop
git checkout <your-branch>
git merge develop
```

### 7.2 代码审查

- 所有代码合并到 develop 或 main 前都应进行代码审查
- 使用 Pull Request (PR) 或 Merge Request (MR) 进行代码审查

### 7.3 冲突解决

- 当合并代码时遇到冲突，应及时解决冲突
- 解决冲突后，重新运行测试确保代码正常工作

## 8. 工具推荐

- **Git Flow 工具**: Git Flow AVH Edition (命令行工具)
- **GUI 工具**: SourceTree, GitKraken (支持 Git Flow 可视化操作)
- **CI/CD 工具**: Jenkins, GitHub Actions, GitLab CI

## 9. 常见问题与解决方案

### 9.1 分支过多如何管理？
- 定期清理已完成的特性分支
- 保持主要分支（main、develop）的整洁

### 9.2 多模块版本不一致如何处理？
- 在发布分支统一更新所有模块的版本号
- 使用 `version.json` 文件集中管理版本信息

### 9.3 紧急问题需要优先修复怎么办？
- 使用 hotfix 分支快速修复生产环境问题
- 修复完成后及时合并到 main 和 develop 分支

## 10. 附录

### 10.1 Git Flow 命令速查表

| 操作 | 命令 |
|------|------|
| 初始化 | `git flow init` |
| 创建特性分支 | `git flow feature start <name>` |
| 完成特性分支 | `git flow feature finish <name>` |
| 创建发布分支 | `git flow release start <version>` |
| 完成发布分支 | `git flow release finish <version>` |
| 创建热修复分支 | `git flow hotfix start <name>` |
| 完成热修复分支 | `git flow hotfix finish <name>` |

### 10.2 参考资源

- [Git Flow 官方文档](https://nvie.com/posts/a-successful-git-branching-model/)
- [Git 官方文档](https://git-scm.com/doc)
- [语义化版本控制规范](https://semver.org/lang/zh-CN/)