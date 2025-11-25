# 水务管理系统 - 前端

## 项目简介

基于 Vue 3 + Element Plus 的水务管理系统前端项目。

## 技术栈

- **框架**: Vue 3.3.4
- **构建工具**: Vite 5.0.8
- **路由**: Vue Router 4.2.5
- **状态管理**: Pinia 2.1.7
- **UI框架**: Element Plus 2.4.4
- **HTTP客户端**: Axios 1.6.2
- **样式**: Sass

## 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口
│   │   ├── user.js        # 用户相关接口
│   │   ├── waterMeter.js  # 水表相关接口
│   │   ├── waterUsage.js  # 用水记录接口
│   │   ├── payment.js     # 缴费接口
│   │   └── notice.js      # 公告接口
│   ├── assets/            # 资源文件
│   ├── components/        # 公共组件
│   ├── layout/           # 布局组件
│   │   └── Index.vue     # 主布局
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # Pinia状态管理
│   │   └── user.js       # 用户状态
│   ├── styles/           # 样式文件
│   │   └── index.scss
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios封装
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── Dashboard.vue # 首页
│   │   ├── user/         # 用户管理
│   │   ├── waterMeter/   # 水表管理
│   │   ├── waterUsage/   # 用水记录
│   │   ├── payment/      # 缴费管理
│   │   └── notice/       # 公告管理
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML模板
├── package.json          # 依赖配置
├── vite.config.js       # Vite配置
└── README.md            # 项目说明
```

## 快速开始

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:3000

### 3. 构建生产版本

```bash
npm run build
```

构建产物在 `dist` 目录。

## 功能模块

### 已实现
- ✅ 用户登录/退出
- ✅ 用户管理（增删改查）
- ✅ 水表管理（列表、删除）
- ✅ 路由守卫
- ✅ Token认证
- ✅ 响应式布局

### 待实现
- ⏳ 用水记录管理
- ⏳ 缴费管理
- ⏳ 公告管理
- ⏳ 数据统计图表
- ⏳ 文件上传

## 默认账号

- 用户名：`admin`
- 密码：`admin123`

## 开发说明

### API请求

所有API请求都通过 `src/utils/request.js` 封装，自动添加Token和错误处理。

### 状态管理

使用Pinia管理全局状态，用户信息存储在 `stores/user.js`。

### 路由配置

路由配置在 `src/router/index.js`，包含路由守卫和权限控制。

## 注意事项

1. 确保后端服务已启动（http://localhost:8080）
2. 前端开发服务器运行在 http://localhost:3000
3. API请求会自动代理到后端（通过vite.config.js配置）

## 许可证

本项目仅用于学习交流。

