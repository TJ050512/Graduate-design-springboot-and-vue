# 水务管理系统

基于 **Spring Boot 3 + Vue 3** 的水务管理系统，实现用户管理、水表管理、用水记录、缴费管理、报修工单等功能，支持阶梯水价计算和数据可视化。

## 项目预览

- 管理员：用户管理、水表管理、用水记录、缴费管理、公告管理、报修工单、数据统计
- 普通用户：我的用水、我的缴费、我的报修、系统公告
- 抄表员：水表管理、用水记录、报修工单、系统公告

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|-----|------|------|
| Spring Boot | 3.x | 核心框架 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| JWT | - | 身份认证 |
| Swagger/OpenAPI | 3.0 | API 文档 |
| Lombok | - | 简化代码 |
| Hutool | - | 工具类库 |

### 前端

| 技术 | 版本 | 说明 |
|-----|------|------|
| Vue | 3.x | 前端框架 |
| Element Plus | 2.x | UI 组件库 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | - | HTTP 请求 |
| ECharts | 5.x | 数据可视化 |
| Sass | - | CSS 预处理器 |

## 功能模块

### 1. 用户管理
- 用户登录/注销
- 用户 CRUD（管理员）
- 密码修改/重置
- **密码加盐安全存储**
- 三种用户角色：管理员、普通用户、抄表员

### 2. 水表管理
- 水表信息 CRUD
- 支持多种水表类型：家用表、商用表、工业表
- 水表状态管理：正常、停用、故障

### 3. 用水记录管理
- 抄表记录 CRUD
- **阶梯水价自动计算**
  - 居民用水：三档阶梯计价（0-15m³、15-25m³、25m³以上）
  - 商业/工业用水：固定单价
- 记录状态跟踪：待确认、已确认、已缴费

### 4. 缴费管理
- 缴费记录创建
- 在线支付模拟
- 支持多种支付方式：现金、微信、支付宝、银行卡

### 5. 公告管理
- 公告 CRUD
- 公告类型：系统公告、停水通知、价格调整
- 支持置顶和发布状态控制

### 6. 报修工单（新增）
- 用户提交报修
- 工单流转：待处理 → 处理中 → 已完成/已取消
- 报修类型：水表故障、漏水、水质问题、水压异常、其他
- 优先级：紧急、普通、低
- 用户评价（1-5星）

### 7. 数据统计与可视化
- Dashboard 概览统计
- **ECharts 图表**：
  - 用水量趋势图（柱状图+折线图）
  - 水表类型分布（环形饼图）
  - 用户类型分布（环形饼图）
  - 缴费状态统计（环形饼图）

### 8. 权限控制
- **基于 JWT 的身份认证**
- **AOP 注解式权限控制**（@RequireRole）
- 前端路由守卫

## 项目结构

```
graduate-design-springboot-and-vue/
├── src/main/java/com/waterworks/    # 后端源码
│   ├── annotation/                   # 自定义注解
│   │   └── RequireRole.java          # 权限注解
│   ├── aspect/                       # AOP 切面
│   │   └── RolePermissionAspect.java # 权限切面
│   ├── common/                       # 通用类
│   ├── config/                       # 配置类
│   ├── controller/                   # 控制器
│   │   ├── UserController.java
│   │   ├── WaterMeterController.java
│   │   ├── WaterUsageController.java
│   │   ├── PaymentController.java
│   │   ├── NoticeController.java
│   │   ├── RepairOrderController.java  # 报修工单
│   │   └── StatisticsController.java   # 统计接口
│   ├── entity/                       # 实体类
│   │   ├── User.java
│   │   ├── WaterMeter.java
│   │   ├── WaterUsage.java
│   │   ├── Payment.java
│   │   ├── WaterPrice.java
│   │   ├── Notice.java
│   │   └── RepairOrder.java          # 报修工单实体
│   ├── exception/                    # 异常处理
│   ├── interceptor/                  # 拦截器
│   ├── mapper/                       # Mapper 接口
│   ├── service/                      # 服务层
│   │   └── impl/
│   │       ├── WaterPriceServiceImpl.java  # 阶梯水价计算
│   │       └── ...
│   └── utils/                        # 工具类
├── frontend/                         # 前端源码
│   ├── src/
│   │   ├── api/                      # API 接口
│   │   ├── layout/                   # 布局组件
│   │   ├── router/                   # 路由配置
│   │   ├── stores/                   # Pinia 状态管理
│   │   ├── views/                    # 页面组件
│   │   │   ├── Dashboard.vue         # 首页（含 ECharts）
│   │   │   ├── user/                 # 用户管理
│   │   │   ├── waterMeter/           # 水表管理
│   │   │   ├── waterUsage/           # 用水记录
│   │   │   ├── payment/              # 缴费管理
│   │   │   ├── notice/               # 公告管理
│   │   │   ├── repair/               # 报修工单（管理员）
│   │   │   ├── myRepair/             # 我的报修（用户）
│   │   │   └── ...
│   │   └── main.js
│   └── package.json
├── sql/
│   ├── init.sql                      # 数据库初始化脚本
│   └── upgrade_v2.sql                # 升级脚本
└── pom.xml
```

## 环境要求

- **JDK 17+**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Node.js 16+**
- **npm 8+**

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-repo/graduate-design-springboot-and-vue.git
cd graduate-design-springboot-and-vue
```

### 2. 数据库配置

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source sql/init.sql
```

### 3. 修改后端配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/water_management
      username: root
      password: your_password
```

### 4. 启动后端

```bash
# 方式一：Maven
mvn spring-boot:run

# 方式二：IDE 运行 WaterManagementApplication.java
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 6. 访问系统

- 前端地址：http://localhost:5173
- 后端接口：http://localhost:8080/api
- API 文档：http://localhost:8080/api/doc.html

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|-----|--------|------|------|
| 管理员 | admin | admin123 | 系统管理员 |
| 普通用户 | user001 | 123456 | 测试用户 |
| 抄表员 | reader001 | 123456 | 测试抄表员 |

## API 接口列表

### 用户相关
| 方法 | 接口 | 说明 | 权限 |
|-----|------|------|------|
| POST | /api/user/login | 用户登录 | 公开 |
| GET | /api/user/info | 获取当前用户信息 | 登录用户 |
| GET | /api/user/page | 分页查询用户 | 管理员/抄表员 |
| POST | /api/user | 添加用户 | 管理员 |
| PUT | /api/user | 更新用户 | 管理员 |
| DELETE | /api/user/{id} | 删除用户 | 管理员 |

### 水表相关
| 方法 | 接口 | 说明 | 权限 |
|-----|------|------|------|
| GET | /api/waterMeter/page | 分页查询水表 | 管理员/抄表员 |
| POST | /api/waterMeter | 添加水表 | 管理员 |
| PUT | /api/waterMeter | 更新水表 | 管理员/抄表员 |
| DELETE | /api/waterMeter/{id} | 删除水表 | 管理员 |

### 用水记录相关
| 方法 | 接口 | 说明 | 权限 |
|-----|------|------|------|
| GET | /api/waterUsage/page | 分页查询用水记录 | 登录用户 |
| POST | /api/waterUsage | 添加用水记录 | 管理员/抄表员 |
| PUT | /api/waterUsage | 更新用水记录 | 管理员/抄表员 |
| PUT | /api/waterUsage/confirm/{id} | 确认记录 | 管理员 |
| DELETE | /api/waterUsage/{id} | 删除记录 | 管理员 |

### 缴费相关
| 方法 | 接口 | 说明 | 权限 |
|-----|------|------|------|
| GET | /api/payment/page | 分页查询缴费记录 | 管理员/用户 |
| POST | /api/payment | 创建缴费记录 | 管理员 |
| PUT | /api/payment/pay/{id} | 支付 | 登录用户 |

### 报修工单相关
| 方法 | 接口 | 说明 | 权限 |
|-----|------|------|------|
| GET | /api/repair/page | 分页查询工单 | 登录用户 |
| POST | /api/repair | 创建工单 | 登录用户 |
| PUT | /api/repair/handle/{id} | 开始处理 | 管理员/抄表员 |
| PUT | /api/repair/complete/{id} | 完成工单 | 管理员/抄表员 |
| PUT | /api/repair/cancel/{id} | 取消工单 | 登录用户 |
| PUT | /api/repair/feedback/{id} | 用户评价 | 登录用户 |

### 统计相关
| 方法 | 接口 | 说明 | 权限 |
|-----|------|------|------|
| GET | /api/statistics/overview | 概览统计 | 登录用户 |
| GET | /api/statistics/usage-trend | 用水趋势 | 管理员 |
| GET | /api/statistics/user-distribution | 用户分布 | 管理员 |
| GET | /api/statistics/meter-distribution | 水表分布 | 管理员/抄表员 |
| GET | /api/statistics/payment-status | 缴费状态 | 管理员 |

## 技术亮点

1. **阶梯水价计算** - 符合实际水务业务场景，支持多档位阶梯定价
2. **AOP 权限控制** - 基于注解的细粒度权限控制，代码优雅
3. **ECharts 可视化** - 多种图表展示统计数据，界面美观
4. **密码加盐存储** - MD5 + Salt 加密，提升安全性
5. **完整的工单流转** - 报修工单从创建到完成的完整生命周期

## 项目截图

（可在此处添加系统截图）

## 注意事项

1. 首次启动前需要执行 `sql/init.sql` 初始化数据库
2. 如果是从旧版本升级，需要执行 `sql/upgrade_v2.sql`
3. 前端开发服务器默认端口 5173，后端默认端口 8080
4. 生产环境部署建议修改 JWT 密钥和数据库密码

## 许可证

本项目仅用于学习交流，请勿用于商业用途。

---

**毕业设计** | Spring Boot 3 + Vue 3 | 2024
