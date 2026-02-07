# 水务管理系统

基于 **Spring Boot 3 + Vue 3** 的水务管理系统（毕业设计），实现用户管理、水表管理、水表读数自动模拟、抄表任务流转、用水记录、缴费管理、催缴提醒、报修工单等功能，支持阶梯水价计算和数据可视化。

> **当前版本：v1.2** | 更新日期：2026-02-07

## 项目预览

| 角色 | 功能概览 |
|------|---------|
| 管理员 | 数据统计、用户管理、水表管理（含模拟器控制）、用水记录（通知抄表）、缴费管理（催缴提醒）、公告管理、报修工单（转派/取消） |
| 普通用户 | 首页概览、我的用水、我的缴费（在线支付）、我的报修、系统公告、未缴费提醒弹窗 |
| 抄表员 | 水表管理（只读）、用水记录（待处理抄表任务面板）、系统公告、抄表任务提醒弹窗 |
| 维修人员 | 报修工单（接单/处理/完成/失败）、系统公告 |

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.3 | 核心框架 |
| Java | 21 | JDK 版本 |
| MyBatis-Plus | 3.5.16 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | - | 缓存 |
| JWT (jjwt) | 0.12.6 | 身份认证 |
| SpringDoc OpenAPI | 2.8.6 | API 文档 |
| Lombok | - | 简化代码 |
| Hutool | 5.8.20 | 工具类库 |
| FastJSON2 | 2.0.53 | JSON 处理 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| Element Plus | 2.x | UI 组件库 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | - | HTTP 请求 |
| ECharts | 5.x | 数据可视化 |
| Sass | - | CSS 预处理器 |

## 功能模块

### 1. 用户管理
- 用户登录 / 注销
- 用户 CRUD（管理员）
- 密码修改 / 重置
- **密码加盐安全存储**（MD5 + Salt）
- 四种用户角色：管理员、普通用户、抄表员、维修人员

### 2. 水表管理
- 水表信息 CRUD（仅管理员可编辑，抄表员只读）
- 支持多种水表类型：家用表、商用表、工业表
- 水表状态管理：正常、停用、故障
- **水表读数自动模拟**（管理员可控制开关、速度、手动触发）
- 管理员不可手动修改当前读数（由模拟器自动更新）

### 3. 水表读数模拟器
- **自动模拟用水**：根据水表类型（家用/商用/工业）模拟不同用水速率
- **时段因子**：模拟真实的用水高峰和低谷时段
- **随机波动**：加入随机因子使数据更真实
- 管理员面板：开关控制、速度调节（1-100倍）、手动触发
- 10 秒更新一次读数，支持 4 位小数精度

### 4. 抄表任务流转
- 管理员查看读数有变化的水表列表
- 管理员一键 / 单个通知抄表员
- 抄表员收到待处理任务面板，显示上次读数和当前读数
- 抄表员确认读数并提交，自动生成用水记录
- 抄表时间自动记录（精确到天）

### 5. 用水记录管理
- 抄表记录管理
- **阶梯水价自动计算**
  - 居民用水：三档阶梯计价（0-15m³、15-25m³、25m³ 以上）
  - 商业用水：固定单价 4.50 元/m³
  - 工业用水：固定单价 3.50 元/m³
- 记录状态跟踪：待确认、已确认、已缴费

### 6. 缴费管理
- 管理员创建缴费单
- **管理员催缴提醒**：单条催缴 / 批量催缴
- **用户登录提醒弹窗**：未缴费账单自动弹窗提醒
- 用户在线支付模拟
- 支持多种支付方式：现金、微信、支付宝、银行卡
- 管理员无支付按钮（仅催缴 + 详情）

### 7. 公告管理
- 公告 CRUD
- 公告类型：系统公告、停水通知、价格调整
- 支持置顶和发布状态控制

### 8. 报修工单
- 用户提交报修（含紧急度选择）
- 完整工单流转：待处理 → 处理中 → 已完成 / 已失败 / 已取消
- 报修类型：水表故障、漏水、水质问题、水压异常、其他
- 优先级：紧急、普通、低（影响排序）
- **维修人员专属操作**：接单、处理、完成、标记失败（管理员不可操作）
- **管理员专属操作**：取消工单、转派
- **接单互斥**：已接单的工单其他维修人员无法接手，显示"他人处理中"
- 处理结果自动记录处理人姓名和时间戳
- 用户评价（1-5 星）
- 用户端可查看紧急度

### 9. 数据统计与可视化
- Dashboard 概览统计（用户数、水表数、用水量、缴费额）
- **ECharts 图表**：
  - 用水量趋势图（柱状图 + 折线图）
  - 水表类型分布（环形饼图）
  - 用户类型分布（环形饼图）
  - 缴费状态统计（环形饼图）

### 10. 权限控制
- **基于 JWT 的身份认证**（24 小时过期）
- **AOP 注解式权限控制**（`@RequireRole`）
- 前端路由守卫 + 菜单动态渲染
- 四种角色精细化权限分配

## 项目结构

```
graduate-design-springboot-and-vue/
├── src/main/java/com/waterworks/         # 后端源码
│   ├── WaterManagementApplication.java   # 主启动类（@EnableScheduling）
│   ├── annotation/                       # 自定义注解
│   │   └── RequireRole.java              # 权限注解
│   ├── aspect/                           # AOP 切面
│   │   └── RolePermissionAspect.java     # 权限切面
│   ├── common/                           # 通用类（Result, ResultCode, PageResult）
│   ├── config/                           # 配置类
│   │   ├── WebMvcConfig.java
│   │   ├── CorsConfig.java
│   │   ├── MyBatisPlusConfig.java
│   │   ├── RedisConfig.java
│   │   └── Knife4jConfig.java
│   ├── controller/                       # 控制器
│   │   ├── UserController.java
│   │   ├── WaterMeterController.java
│   │   ├── WaterUsageController.java
│   │   ├── PaymentController.java        # 含催缴提醒接口
│   │   ├── NoticeController.java
│   │   ├── RepairOrderController.java
│   │   ├── StatisticsController.java
│   │   ├── SimulatorController.java      # 模拟器控制接口
│   │   └── MeterReadTaskController.java  # 抄表任务接口
│   ├── entity/                           # 实体类
│   │   ├── User.java
│   │   ├── WaterMeter.java
│   │   ├── WaterUsage.java
│   │   ├── Payment.java                  # 含 needRemind 字段
│   │   ├── WaterPrice.java
│   │   ├── Notice.java
│   │   ├── RepairOrder.java
│   │   └── MeterReadTask.java            # 抄表任务实体
│   ├── exception/                        # 异常处理
│   ├── interceptor/                      # JWT 拦截器
│   ├── mapper/                           # Mapper 接口
│   ├── service/                          # 服务层
│   │   └── impl/
│   │       ├── WaterPriceServiceImpl.java    # 阶梯水价计算
│   │       ├── RepairOrderServiceImpl.java   # 含处理人签名逻辑
│   │       ├── PaymentServiceImpl.java       # 含催缴提醒逻辑
│   │       └── ...
│   ├── simulator/                        # 模拟器
│   │   └── WaterMeterSimulator.java      # 水表读数自动模拟
│   └── utils/                            # 工具类（JwtUtil）
├── frontend/                             # 前端源码
│   ├── src/
│   │   ├── api/                          # API 接口
│   │   │   ├── user.js
│   │   │   ├── waterMeter.js             # 含模拟器 API
│   │   │   ├── waterUsage.js
│   │   │   ├── payment.js                # 含催缴提醒 API
│   │   │   ├── repair.js
│   │   │   ├── notice.js
│   │   │   ├── statistics.js
│   │   │   └── meterReadTask.js          # 抄表任务 API
│   │   ├── layout/Index.vue              # 布局（含登录提醒弹窗）
│   │   ├── router/index.js              # 路由配置
│   │   ├── stores/                       # Pinia 状态管理
│   │   ├── styles/index.scss            # 全局样式
│   │   └── views/                        # 页面组件
│   │       ├── Login.vue                 # 登录页
│   │       ├── Dashboard.vue             # 首页（ECharts 图表）
│   │       ├── user/Index.vue            # 用户管理
│   │       ├── waterMeter/Index.vue      # 水表管理（含模拟器面板）
│   │       ├── waterUsage/Index.vue      # 用水记录（含抄表任务面板）
│   │       ├── payment/Index.vue         # 缴费管理（管理员）
│   │       ├── notice/Index.vue          # 公告管理
│   │       ├── repair/Index.vue          # 报修工单（管理员/维修员）
│   │       ├── myUsage/Index.vue         # 我的用水
│   │       ├── myPayment/Index.vue       # 我的缴费
│   │       ├── myRepair/Index.vue        # 我的报修
│   │       └── noticeList/Index.vue      # 系统公告
│   └── package.json
├── sql/
│   ├── init.sql                          # 数据库初始化脚本（含全部表）
│   └── upgrade_v2.sql                    # v1.0 → v1.1 升级脚本
└── pom.xml
```

## 数据库表结构

| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表（管理员/普通用户/抄表员/维修人员） |
| `water_meter` | 水表表（含类型、状态、当前读数） |
| `water_usage` | 用水记录表（抄表读数、用量、金额） |
| `water_price` | 水价配置表（阶梯定价） |
| `payment` | 缴费记录表（含催缴提醒标记） |
| `notice` | 公告表 |
| `repair_order` | 报修工单表（含优先级、处理人、评价） |
| `meter_read_task` | 抄表任务表（通知-待抄-完成） |

## 环境要求

- **JDK 21+**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Redis**（可选，用于缓存）
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

# 执行初始化脚本（自动创建数据库和所有表）
source sql/init.sql
```

### 3. 修改后端配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/water_management
    username: root
    password: your_password

# 水表模拟器配置
waterworks:
  simulator:
    enabled: true           # 是否启用水表读数自动模拟
    speed-multiplier: 10    # 速度倍率（演示推荐 10-50，正常运行设为 1）
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

- 前端地址：http://localhost:3000
- 后端接口：http://localhost:8080/api
- API 文档：http://localhost:8080/api/doc.html

## 测试账号

| 角色 | 用户名 | 密码 | 姓名 | 说明 |
|------|--------|------|------|------|
| 管理员 | admin | admin123 | 系统管理员 | 全局管理权限 |
| 普通用户 | user001 | 123456 | 张三 | 查看用水/缴费/报修 |
| 抄表员 | reader001 | 123456 | 李四 | 抄表任务处理 |
| 维修人员 | repair001 | 123456 | 王五 | 报修工单处理 |

> 维修人员账号可在管理员界面手动添加更多，用于测试转派功能。

## API 接口列表

### 用户相关
| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/user/login | 用户登录 | 公开 |
| GET | /api/user/info | 获取当前用户信息 | 登录用户 |
| GET | /api/user/page | 分页查询用户 | 管理员/抄表员 |
| POST | /api/user | 添加用户 | 管理员 |
| PUT | /api/user | 更新用户 | 管理员 |
| DELETE | /api/user/{id} | 删除用户 | 管理员 |

### 水表相关
| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/waterMeter/page | 分页查询水表 | 管理员/抄表员 |
| POST | /api/waterMeter | 添加水表 | 管理员 |
| PUT | /api/waterMeter | 更新水表（不含读数） | 管理员 |
| DELETE | /api/waterMeter/{id} | 删除水表 | 管理员 |

### 模拟器相关
| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/simulator/status | 获取模拟器状态 | 管理员 |
| PUT | /api/simulator/toggle | 开启/关闭模拟器 | 管理员 |
| PUT | /api/simulator/speed | 设置模拟速度（1-100） | 管理员 |
| POST | /api/simulator/trigger | 手动触发一次模拟 | 管理员 |

### 抄表任务相关
| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/meterReadTask/needRead | 获取需要抄表的水表 | 管理员 |
| POST | /api/meterReadTask/notify | 通知抄表（单个） | 管理员 |
| POST | /api/meterReadTask/notifyAll | 一键通知全部抄表 | 管理员 |
| GET | /api/meterReadTask/pending | 获取待处理任务 | 抄表员 |
| GET | /api/meterReadTask/pending/count | 获取待处理任务数 | 抄表员 |
| PUT | /api/meterReadTask/complete/{id} | 完成抄表任务 | 抄表员 |

### 用水记录相关
| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/waterUsage/page | 分页查询用水记录 | 登录用户 |
| POST | /api/waterUsage | 添加用水记录 | 管理员/抄表员 |
| PUT | /api/waterUsage | 更新用水记录 | 管理员/抄表员 |
| PUT | /api/waterUsage/confirm/{id} | 确认记录 | 管理员 |
| DELETE | /api/waterUsage/{id} | 删除记录 | 管理员 |

### 缴费相关
| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/payment/page | 分页查询缴费记录 | 管理员/用户 |
| POST | /api/payment | 创建缴费记录 | 管理员 |
| PUT | /api/payment/pay/{id} | 用户支付 | 登录用户 |
| POST | /api/payment/remind/{id} | 发送催缴提醒 | 管理员 |
| POST | /api/payment/remind/batch | 批量催缴提醒 | 管理员 |
| GET | /api/payment/unpaid/reminders | 获取未缴费提醒 | 登录用户 |

### 报修工单相关
| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/repair/page | 分页查询工单 | 登录用户 |
| POST | /api/repair | 创建工单 | 登录用户 |
| PUT | /api/repair/handle/{id} | 接单处理 | 维修人员 |
| PUT | /api/repair/complete/{id} | 完成工单 | 维修人员 |
| PUT | /api/repair/fail/{id} | 标记失败 | 维修人员 |
| PUT | /api/repair/cancel/{id} | 取消工单 | 管理员 |
| PUT | /api/repair/reassign/{id} | 转派工单 | 管理员 |
| PUT | /api/repair/feedback/{id} | 用户评价 | 登录用户 |

### 统计相关
| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/statistics/overview | 概览统计 | 登录用户 |
| GET | /api/statistics/usage-trend | 用水趋势 | 管理员 |
| GET | /api/statistics/user-distribution | 用户分布 | 管理员 |
| GET | /api/statistics/meter-distribution | 水表分布 | 管理员/抄表员 |
| GET | /api/statistics/payment-status | 缴费状态 | 管理员 |

## 技术亮点

1. **水表读数自动模拟** — 基于 Spring 定时任务，根据水表类型、时段因子、随机波动真实模拟用水过程
2. **完整的抄表任务流转** — 管理员通知 → 抄表员处理 → 自动生成用水记录，贴近实际业务
3. **阶梯水价计算** — 符合实际水务场景，居民用水支持三档阶梯定价
4. **催缴提醒机制** — 管理员催缴 + 用户登录弹窗提醒，完整的欠费处理流程
5. **AOP 注解式权限控制** — `@RequireRole` 注解实现细粒度四角色权限控制
6. **报修工单精细化管理** — 接单互斥、处理人签名、优先级排序、转派功能
7. **ECharts 数据可视化** — 多种图表展示统计数据，界面美观
8. **密码加盐存储** — MD5 + Salt 加密，提升安全性

## 版本历史

### v1.2（2026-02-07）— 业务流程完善版

**新增功能：**
- 水表读数自动模拟系统（`WaterMeterSimulator`）
  - 根据水表类型（家用/商用/工业）模拟不同用水速率
  - 支持时段因子（高峰/低谷）和随机波动
  - 管理员可控制开关、速度倍率（1-100x）、手动触发
  - 禁止手动修改水表读数，读数完全由模拟器驱动
- 模拟器控制接口与前端面板（`SimulatorController`）
- 抄表任务流转系统（`MeterReadTaskController`）
  - 管理员查看待抄表水表（区分已通知/未通知）
  - 一键/单个通知抄表员
  - 抄表员待处理任务面板，确认读数自动生成用水记录
- 缴费催缴提醒
  - 管理员单条/批量催缴
  - 用户登录弹窗提醒未缴费账单
- 报修工单权限细化
  - 维修人员专属：接单、处理、完成、标记失败
  - 管理员专属：取消、转派
  - 接单互斥机制（已接单工单他人无法操作）
  - 处理结果自动记录处理人姓名 + 时间戳
- 新增维修人员角色（`userType=4`）
- 用户报修页面展示紧急度
- 报修工单优先级影响排序

**优化修复：**
- 修复 ECharts 饼图标签和图例重叠问题
- 修复 `el-tag` 文字重影/阴影问题
- 前端水表读数显示精度提升至 4 位小数
- 水表页面 10 秒自动刷新
- 管理员缴费页面移除不合理的"支付"按钮

**数据库变更：**
- `payment` 表新增 `need_remind` 字段
- 新增 `meter_read_task` 抄表任务表

### v1.1（2026-02-02）— 功能完善版

**新增功能：**
- 报修工单模块（用户报修、工单流转、用户评价）
- 数据统计模块（Dashboard、ECharts 图表）
- AOP 注解式权限控制（`@RequireRole`）
- 阶梯水价自动计算
- 普通用户专属页面（我的用水、我的缴费、我的报修）
- 系统公告查看页面
- 登录页面美化

**数据库变更：**
- 新增 `repair_order` 报修工单表
- 新增 `upgrade_v2.sql` 升级脚本

### v1.0（2026-01-15）— 基础功能版

**基础功能：**
- 用户管理（登录、CRUD、密码加盐）
- 水表管理（CRUD、多类型支持）
- 用水记录管理（CRUD）
- 缴费管理（创建/支付）
- 公告管理（CRUD、置顶、发布控制）
- JWT 身份认证

### v0.1（2025-11-25）— 框架搭建

- Spring Boot 3 + Vue 3 项目框架搭建
- 基础目录结构、数据库设计
- 前端基础布局与路由配置

## 注意事项

1. 首次启动前需要执行 `sql/init.sql` 初始化数据库（包含所有表和测试数据）
2. 如果是从 v1.0 升级到 v1.1，需要额外执行 `sql/upgrade_v2.sql`
3. 如果是从 v1.1 升级到 v1.2，需要手动执行以下 SQL：
   ```sql
   -- 在 payment 表添加催缴提醒字段
   ALTER TABLE payment ADD COLUMN need_remind TINYINT DEFAULT 0 COMMENT '是否需要提醒(0:不需要 1:需要)';
   
   -- 创建抄表任务表（参见 init.sql 中的 meter_read_task 建表语句）
   ```
4. 前端开发服务器端口 3000，后端默认端口 8080
5. 生产环境部署建议修改 JWT 密钥和数据库密码
6. 水表模拟器默认开启，速度倍率为 10（可在 `application.yml` 或管理员面板调整）

## 许可证

本项目仅用于学习交流，请勿用于商业用途。

---

**毕业设计** | Spring Boot 3 + Vue 3 | Java 21 | 2026

**作者** | 马骁骁