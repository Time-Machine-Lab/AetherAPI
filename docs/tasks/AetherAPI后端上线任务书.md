# AetherAPI 后端上线任务书

## 1. 任务综述 (Task Context)

### 1.1 任务名称与定义

**AetherAPI 后端生产环境部署**：将 AetherAPI 后端项目（aether-api-hub）从本地开发环境打包部署至生产环境服务器（61.184.13.101），完成生产环境配置适配、数据库连接加密、服务部署与启动，最终实现后端服务的对外稳定运行。

### 1.2 核心目标 (The "Why")

AetherAPI 项目已完成开发阶段，具备可交付的代码资产。为了让项目能够对外提供服务，必须将其部署至公网可达的生产环境服务器。本次部署是项目从"开发态"转向"运营态"的关键里程碑，是后续前端联调、业务验证、用户验收的必要前提。

### 1.3 当前进度 (Current Status)

- **已完成 (Done)：**
  - 后端代码开发完成，位于 `aether-api-hub` 目录
  - 已确定生产环境数据库连接信息（MySQL on 38.246.252.230:24167）
  - 已确定目标生产服务器（61.184.13.101）
  - 已获取服务器登录凭据（root / wGccmnLi4IdgK8YR）
- **待完成 (To-do)：**
  - 创建生产环境配置文件（application-prod.yml 或 application.yml）
  - 对数据库密码等敏感配置进行加密处理
  - 修改 pom.xml 支持 Maven 打包为可执行 Spring Boot JAR
  - 执行 Maven 构建，生成 fat JAR
  - 上传 JAR 包至服务器 /opt 或 /home 目录
  - 在服务器上安装 Java 运行时环境（JRE/JDK 17+）
  - 配置服务启动脚本（systemd service 或 shell 脚本）
  - 启动后端服务并验证进程存活
  - 配置 CI/CD 流水线（推荐 GitHub Actions）
  - 验证服务 HTTP 端点可访问性
  - 在内部群通知部署完成，提供访问地址和端口

### 1.4 重要性与紧急程度


| 维度       | 评估                        |
| -------- | ------------------------- |
| **优先级**  | **P0** — 项目核心里程碑，上线交付的阻塞项 |
| **截止日期** | **2026/4/23 24:00**       |
| **影响范围** | 全项目（所有依赖后端 API 的功能均受影响）   |


---

## 2. 任务上下文

### 2.1 代码仓库与结构


| 属性         | 值                                                                                              |
| ---------- | ---------------------------------------------------------------------------------------------- |
| **仓库地址**   | [https://github.com/Time-Machine-Lab/AetherAPI](https://github.com/Time-Machine-Lab/AetherAPI) |
| **分支策略**   | main 分支（生产部署代码源）                                                                               |
| **后端代码目录** | `aether-api-hub/`                                                                              |
| **后端主模块**  | `aether-api-hub/aether-api-hub-app/`                                                           |
| **配置文件目录** | `aether-api-hub/aether-api-hub-app/src/main/resources/`                                        |


### 2.2 服务器与数据库信息


| 类型      | 属性       | 值                                                                                                                          |
| ------- | -------- | -------------------------------------------------------------------------------------------------------------------------- |
| **服务器** | IP 地址    | `61.184.13.101`                                                                                                            |
|         | SSH 用户   | `root`                                                                                                                     |
|         | SSH 密码   | `wGccmnLi4IdgK8YR`                                                                                                         |
|         | 推荐部署目录   | `/opt/aether-api-hub/` 或 `/home/aether-api-hub/`                                                                           |
| **数据库** | 类型       | MySQL                                                                                                                      |
|         | JDBC URL | `jdbc:mysql://38.246.252.230:24167/aether_api_hub?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false` |
|         | 用户名      | `root`                                                                                                                     |
|         | 密码       | `mysql_7ZJY7w`                                                                                                             |
|         | 驱动类      | `com.mysql.cj.jdbc.Driver`                                                                                                 |


### 2.3 构建与部署依赖


| 依赖项              | 版本要求    | 备注                          |
| ---------------- | ------- | --------------------------- |
| **Java**         | JDK 17+ | Spring Boot 3.x 要求最低 JDK 17 |
| **Maven**        | 3.6+    | 用于后端构建                      |
| **MySQL Client** | 任意版本    | 用于数据库初始化（如需要）               |


### 2.4 权限与密钥

当前已获取服务器 root 权限，具备完整的系统操作权限。**建议后续创建专用部署用户以遵循最小权限原则。**

---

## 3. 注意事项

### 3.1 已知问题与 Bug


| 序号  | 问题描述                       | 影响           | 建议处理方式                                                                            |
| --- | -------------------------- | ------------ | --------------------------------------------------------------------------------- |
| 1   | pom.xml 打包方式未确认            | 无法生成可执行 JAR  | 需要在 pom.xml 中显式指定 `<packaging>jar</packaging>` 并确保 spring-boot-maven-plugin 已正确配置 |
| 2   | 配置文件加密方式未确定                | 数据库密码明文暴露风险  | **必须**在部署前完成加密，建议使用 Jasypt 或环境变量方式（见 3.2）                                         |
| 3   | 数据库 `aether_api_hub` 是否已创建 | 服务启动时可能报连接错误 | 部署前需确认目标数据库已初始化，或准备初始化 SQL 脚本                                                     |


### 3.2 关键技术细节

#### 3.2.1 配置文件加密方案（推荐）

**推荐方案：Jasypt 加密**

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

在 `application-prod.yml` 中使用 `ENC(...)` 包裹加密值：

```yaml
spring:
  datasource:
    password: ENC(加密后的密文)
```

启动时通过环境变量传入解密密钥：

```bash
java -jar aether-api-hub.jar --jasypt.encryptor.password=your-secret-key
```

#### 3.2.2 替代方案：环境变量注入

如果不想引入额外依赖，可以将敏感信息通过环境变量注入：

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}
```

部署启动时：

```bash
export DB_PASSWORD='mysql_7ZJY7w'
java -jar aether-api-hub.jar
```

#### 3.2.3 pom.xml 打包配置检查清单

确保以下配置存在：

```xml
<packaging>jar</packaging>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <executable>true</executable>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 3.3 外部依赖与沟通


| 依赖方         | 对接事项                        | 对接人 |
| ----------- | --------------------------- | --- |
| **前端团队**    | 后端部署完成后提供 API Base URL 和端口  | 待指定 |
| **运维/基础设施** | 确认服务器端口（8080/80/其他）是否已开放防火墙 | 待指定 |
| **测试团队**    | 部署完成后进行接口冒烟测试               | 待指定 |


### 3.4 “避坑”指南


| 序号  | 避坑事项                       | 说明                                                         |
| --- | -------------------------- | ---------------------------------------------------------- |
| 1   | **不要**在代码库中提交明文密码          | 数据库密码、Jasypt 密钥等敏感信息**禁止**提交至 Git，应通过环境变量或 CI/CD Secret 注入 |
| 2   | **不要**使用 root 用户运行 Java 进程 | 建议创建专用服务用户 `aether`，通过 systemd 管理服务                        |
| 3   | **不要**跳过服务启动验证             | 启动后必须立即检查日志确认无报错：`tail -f /var/log/aether-api-hub.log`     |
| 4   | **不要**使用默认端口 8080 直接对外暴露   | 建议通过 Nginx 反向代理至 80/443 端口，或在云服务器安全组中仅允许特定 IP 访问 8080      |
| 5   | **不要**忽略数据库连接池配置           | 生产环境应配置合理的连接池大小（HikariCP 默认 10），避免高并发场景耗尽连接                |


---

## 4. 验收标准与交付物 (Definition of Done)

### 4.1 部署前检查清单

- 生产环境配置文件已创建（`application-prod.yml`）
- 数据库密码已加密（或使用环境变量注入）
- `pom.xml` 已配置 Spring Boot Maven 插件，可正确打包 fat JAR
- 服务器上已安装 JDK 17+
- 数据库 `aether_api_hub` 已创建并初始化（如需要）
- 服务器部署目录已创建：`/opt/aether-api-hub/`

### 4.2 部署执行清单

- 本地执行 `mvn clean package -Pprod -DskipTests`，生成 JAR 文件
- 将 `aether-api-hub-app-*.jar` 上传至服务器 `/opt/aether-api-hub/`
- 在服务器创建启动脚本 `start.sh`（包含环境变量和 JVM 参数）
- 配置 systemd 服务或 supervisor 进程管理（确保开机自启）
- 执行启动脚本，确认进程启动：`ps aux | grep aether`
- 检查启动日志，确认无 ERROR：`journalctl -u aether-api-hub -f`

### 4.3 验收标准（必须全部通过）


| 序号  | 验收项      | 验证方法                                         | 预期结果                         |
| --- | -------- | -------------------------------------------- | ---------------------------- |
| 1   | 服务进程存活   | `ps aux | grep aether`                       | 存在 Java 进程                   |
| 2   | 端口监听     | `netstat -tlnp | grep 8080`                  | 8080 端口处于 LISTEN 状态          |
| 3   | 健康检查端点   | `curl http://localhost:8080/actuator/health` | 返回 `{"status":"UP"}`         |
| 4   | API 可访问性 | `curl http://61.184.13.101:8080/[任意已知API]`   | 返回正常 HTTP 响应（非 502/504/连接拒绝） |
| 5   | 日志无异常    | `tail -100 /opt/aether-api-hub/logs/app.log` | 无 ERROR 或 Exception 日志       |


### 4.4 CI/CD 流水线交付物

- GitHub Actions 工作流文件已创建（`.github/workflows/deploy.yml`）
- 工作流包含构建、测试、打包、上传制品等步骤
- 服务器部署通过 SSH Remote Commands 或制品拉取方式实现
- 部署触发条件配置完成（建议：push to main 分支时自动触发）

### 4.5 最终交付物清单


| 交付物                | 位置/说明                                                        |
| ------------------ | ------------------------------------------------------------ |
| 生产环境配置文件           | `aether-api-hub-app/src/main/resources/application-prod.yml` |
| 加密后的数据库密码密文        | 记录在安全的密码管理器中（如有使用 Jasypt）                                    |
| 服务启动脚本             | `/opt/aether-api-hub/start.sh` 或 systemd unit 文件             |
| 部署好的后端服务           | 运行在 `61.184.13.101:8080`                                     |
| GitHub Actions 流水线 | `.github/workflows/deploy.yml`                               |
| 部署通知               | 在内部群发送服务访问地址                                                 |


---

## 5. 支持与答疑 (Support Channel)

### 5.1 负责人/导师


| 角色        | 姓名  | 职责                  |
| --------- | --- | ------------------- |
| **项目负责人** | 待指定 | 对部署结果负责，决策关键技术选型    |
| **后端开发者** | 待指定 | 执行部署操作，处理部署过程中的技术问题 |


### 5.2 日常沟通方式

- **主沟通工具**：微信群 / 钉钉群 / 企业微信（待选择）
- **备份沟通**：GitHub Issues（用于记录部署过程中的技术问题和解决方案）
- **紧急联系**：电话（仅限生产故障场景）

### 5.3 答疑时间窗


| 工作日     | 可打扰时间段                      |
| ------- | --------------------------- |
| 周一 ~ 周五 | 10:00 ~ 12:00、14:00 ~ 18:00 |
| 周末/节假日  | 仅处理生产故障（P0 级别）              |


