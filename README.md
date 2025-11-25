# 水务管理系统 - 后端

## 项目简介

基于Spring Boot框架的水务管理系统后端服务，提供用户管理、水表管理、用水记录、缴费管理等功能。

## 技术栈

- **核心框架**: Spring Boot 2.7.14
- **持久层**: MyBatis Plus 3.5.3.1
- **数据库**: MySQL 8.0
- **数据源**: Druid 1.2.18
- **缓存**: Redis
- **API文档**: Knife4j 3.0.3 (Swagger增强)
- **认证**: JWT (JSON Web Token)
- **工具类**: Hutool、Apache Commons
- **其他**: Lombok、FastJson

## 项目结构

```
water-management-system
├── src/main/java/com/waterworks
│   ├── common                 # 通用类
│   │   ├── Result.java        # 统一返回结果
│   │   ├── ResultCode.java    # 响应码枚举
│   │   └── PageResult.java    # 分页结果
│   ├── config                 # 配置类
│   │   ├── CorsConfig.java    # 跨域配置
│   │   ├── Knife4jConfig.java # API文档配置
│   │   ├── MyBatisPlusConfig.java  # MyBatis Plus配置
│   │   ├── RedisConfig.java   # Redis配置
│   │   └── WebMvcConfig.java  # Web MVC配置
│   ├── controller             # 控制器层
│   │   ├── UserController.java         # 用户控制器
│   │   ├── WaterMeterController.java   # 水表控制器
│   │   ├── WaterUsageController.java   # 用水记录控制器
│   │   ├── PaymentController.java      # 缴费控制器
│   │   └── NoticeController.java       # 公告控制器
│   ├── entity                 # 实体类
│   │   ├── BaseEntity.java    # 基础实体
│   │   ├── User.java          # 用户实体
│   │   ├── WaterMeter.java    # 水表实体
│   │   ├── WaterUsage.java    # 用水记录实体
│   │   ├── Payment.java       # 缴费记录实体
│   │   ├── WaterPrice.java    # 水费价格实体
│   │   └── Notice.java        # 公告实体
│   ├── exception              # 异常处理
│   │   ├── BusinessException.java         # 业务异常
│   │   └── GlobalExceptionHandler.java    # 全局异常处理器
│   ├── interceptor            # 拦截器
│   │   └── JwtInterceptor.java  # JWT拦截器
│   ├── mapper                 # Mapper层
│   │   ├── UserMapper.java
│   │   ├── WaterMeterMapper.java
│   │   ├── WaterUsageMapper.java
│   │   ├── PaymentMapper.java
│   │   ├── WaterPriceMapper.java
│   │   └── NoticeMapper.java
│   ├── service                # 服务层
│   │   ├── impl               # 服务实现
│   │   ├── UserService.java
│   │   ├── WaterMeterService.java
│   │   ├── WaterUsageService.java
│   │   ├── PaymentService.java
│   │   └── NoticeService.java
│   ├── utils                  # 工具类
│   │   └── JwtUtil.java       # JWT工具类
│   └── WaterManagementApplication.java  # 启动类
├── src/main/resources
│   ├── application.yml        # 主配置文件
│   ├── application-dev.yml    # 开发环境配置
│   └── application-prod.yml   # 生产环境配置
└── pom.xml                    # Maven配置文件
```

## 核心功能

### 1. 用户管理
- 用户登录/注销
- 用户信息管理
- 密码修改/重置
- 用户类型：管理员、普通用户、抄表员

### 2. 水表管理
- 水表信息录入
- 水表信息查询
- 水表状态管理
- 支持多种水表类型：家用表、商用表、工业表

### 3. 用水记录管理
- 抄表记录录入
- 用水量统计
- 水费计算
- 记录状态跟踪

### 4. 缴费管理
- 缴费记录创建
- 在线支付
- 缴费历史查询
- 支持多种支付方式

### 5. 公告管理
- 公告发布
- 公告查询
- 公告类型：系统公告、停水通知、价格调整

## 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

## 快速开始

### 1. 克隆项目

```bash
git clone [项目地址]
cd graduate-design-springboot-and-vue
```

### 2. 创建数据库

创建数据库 `water_management`，并执行 `sql/init.sql` 初始化脚本。

```sql
CREATE DATABASE water_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/water_management?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
      username: root
      password: your_password
```

修改Redis配置：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_password
```

### 4. 编译运行

```bash
# 编译项目
mvn clean install

# 运行项目
mvn spring-boot:run
```

或者使用IDE直接运行 `WaterManagementApplication.java`

### 5. 访问接口文档

启动成功后，访问：http://localhost:8080/api/doc.html

## API接口

所有接口统一前缀：`/api`

### 用户相关
- `POST /api/user/login` - 用户登录
- `GET /api/user/info` - 获取用户信息
- `GET /api/user/page` - 分页查询用户
- `POST /api/user` - 添加用户
- `PUT /api/user` - 更新用户
- `DELETE /api/user/{id}` - 删除用户

### 水表相关
- `GET /api/waterMeter/page` - 分页查询水表
- `GET /api/waterMeter/{id}` - 根据ID查询水表
- `POST /api/waterMeter` - 添加水表
- `PUT /api/waterMeter` - 更新水表
- `DELETE /api/waterMeter/{id}` - 删除水表

### 用水记录相关
- `GET /api/waterUsage/page` - 分页查询用水记录
- `GET /api/waterUsage/{id}` - 根据ID查询记录
- `POST /api/waterUsage` - 添加用水记录
- `PUT /api/waterUsage/confirm/{id}` - 确认用水记录
- `DELETE /api/waterUsage/{id}` - 删除记录

### 缴费相关
- `GET /api/payment/page` - 分页查询缴费记录
- `GET /api/payment/{id}` - 根据ID查询记录
- `POST /api/payment` - 创建缴费记录
- `PUT /api/payment/pay/{id}` - 支付

### 公告相关
- `GET /api/notice/page` - 分页查询公告
- `GET /api/notice/{id}` - 根据ID查询公告
- `POST /api/notice` - 添加公告
- `PUT /api/notice` - 更新公告
- `PUT /api/notice/publish/{id}` - 发布公告
- `DELETE /api/notice/{id}` - 删除公告

## 配置说明

### JWT配置

```yaml
jwt:
  secret: waterManagementSystemSecretKey2024  # JWT密钥
  expiration: 24  # 过期时间(小时)
  token-prefix: Bearer   # token前缀
  header: Authorization  # token header key
```

### 数据库连接池配置

使用Druid连接池，配置详见 `application.yml`

监控页面：http://localhost:8080/api/druid/login.html
- 用户名：admin
- 密码：admin

## 开发说明

### 1. 统一返回格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1234567890
}
```

### 2. 分页返回格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "current": 1,
    "size": 10,
    "total": 100,
    "pages": 10,
    "records": []
  },
  "timestamp": 1234567890
}
```

### 3. 异常处理

所有异常都会被全局异常处理器捕获并返回统一格式的错误信息。

### 4. 接口鉴权

除了登录接口，其他接口都需要在Header中携带JWT token：

```
Authorization: Bearer {token}
```

## 注意事项

1. 首次启动前需要创建数据库并执行初始化脚本
2. 修改配置文件中的数据库和Redis连接信息
3. JWT密钥建议在生产环境中修改为复杂的字符串
4. 生产环境建议关闭Druid监控或设置复杂的账号密码
5. 默认管理员账号密码请在系统启动后及时修改

## 待完成功能

- [ ] 数据库初始化SQL脚本
- [ ] 单元测试
- [ ] 数据导出功能
- [ ] 报表统计功能
- [ ] 短信通知功能
- [ ] 图片上传功能
- [ ] 操作日志记录

## 许可证

本项目仅用于学习交流，请勿用于商业用途。


