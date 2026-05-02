# Aether API HUB 后端代码开发规范文档

## 1. 目标

本文档是 Aether API HUB 后端开发宪法。

适用范围：

- Aether API HUB 全部 Java 后端代码
- 人工开发
- AI 生成、修改、重构代码

目标：

- 保证代码结构稳定
- 保证 DDD 边界清晰
- 保证 AI 开发可控
- 以快捷、简便、可演进为第一原则

## 2. 技术基线

- 语言：Java 17
- 框架：Spring Boot
- 构建：Maven
- 持久化：MyBatis-Plus
- 数据库：MySQL
- 其他技术选型不是本规范重点，未经确认不得自行扩展

## 3. 开发总则

### 3.1 总原则

- 采用工程实用版 DDD
- 严格限制模块职责和依赖方向
- 强调聚合、实体、值对象、领域服务、仓储接口
- 不强迫简单 CRUD 过度建模
- 简单场景允许以应用服务为主，领域模型逐步演进
- 禁止为了像 DDD 而制造空壳类、机械分层
- 优先可读、可维护、可交付，不追求炫技式设计

### 3.2 命名总则

- **包名前缀**：所有代码的 `package` 声明必须使用 `io.github.timemachinelab`
  - Maven `groupId` 统一使用 `io.github.timemachinelab`
  - **目录名（如 `aether-api-hub`）不等于包名，包名必须严格遵循 groupId**
  - 禁止使用 `io.github.aetherapihub` 或其他前缀
- DTO 对外命名统一使用 `Req`、`Resp`
- 应用层内部模型统一使用 `Command`、`Query`、`Model`、`Result`
- 命名必须与现有骨架风格一致
- 禁止引入风格冲突的包名、类名、接口名

## 4. 模块宪法

项目骨架按以下模块分层，遵循 TML-DDD 六边形架构：

- `aether-api-hub-app`
- `aether-api-hub-api`
- `aether-api-hub-adapter`
- `aether-api-hub-service`
- `aether-api-hub-domain`
- `aether-api-hub-infrastructure`
- `aether-api-hub-client`

> **包名前缀规则**：所有代码的 `package` 声明必须使用 `io.github.timemachinelab`，不得使用 `io.github.aetherapihub` 或其他前缀。目录名不等于包名，包名必须严格遵循 Maven `groupId = io.github.timemachinelab`。

> **Windows 兼容警告**：`port.in` 包名在 Windows 文件系统下与 `port.in_` 共享同一目录，务必使用 `port.in`，禁止使用 `port.in_`。

### 4.1 app

职责：启动应用、装配模块、放启动相关配置。

```
aether-api-hub-app/src/main/java/io/github/timemachinelab/
  App.java                          # Spring Boot 启动入口
  InfrastructureConfig.java         # 装配配置（如 MyBatis 配置类）
```

禁止：写业务规则、写 Controller、写 Repository。

### 4.2 api（对外契约层）

职责：定义对外服务契约，给"服务调用者"（前端、网关、其他微服务）提供稳定的调用接口。

**必须放在 `api` 的包：**

```
io.github.timemachinelab.api
  ├─ error/
  │    ErrorCode.java               # 模块错误码常量集中定义
  ├─ enums/
  │    XxxStatus.java              # 对外公开的枚举（如订单状态）
  ├─ req/
  │    XxxReq.java                 # 对外请求 DTO（命名：XxxReq）
  ├─ resp/
  │    XxxResp.java               # 对外响应 DTO（命名：XxxResp）
  └─ contract/
       ├─ openapi/                # OpenAPI 文件（可选）
       └─ proto/                  # Proto 文件（可选）
```

规则：

- 对外字段命名统一使用 `camelCase`
- 对外契约不得泄漏领域对象（不得出现 `ApiCategoryAggregate`、`CategoryModel` 等领域类型）
- 错误码必须集中管理，分类模块错误码放在 `api.error.CatalogErrorCodes`
- 不要在 `api` 包中写任何 `Controller`、`Repository`、领域模型

### 4.3 adapter（适配层）

职责：处理"外部世界如何调用你"，把外部协议（HTTP、MQ、RPC）翻译成内部用例调用。

**必须放在 `adapter` 的包：**

```
io.github.timemachinelab.adapter
  ├─ web/
  │    ├─ controller/
  │    │    XxxController.java    # HTTP 接入（仅接收请求、返回响应）
  │    ├─ delegate/
  │    │    XxxWebDelegate.java    # req → model 转换与用例调用编排
  │    ├─ handler/
  │    │    GlobalExceptionHandler.java  # 统一异常映射
  │    └─ interceptor/           # HTTP 拦截器（可选）
  ├─ mq/
  │    ├─ listener/              # MQ 消费入口
  │    └─ delegate/              # 消息体 → model 转换
  ├─ rpc/                         # gRPC/Dubbo 入口（可选）
  └─ scheduler/
       └─ job/                    # 定时任务入口
```

**典型调用链**：

```
HTTP JSON
  → Controller（协议接入）
      → Delegate（req → Command/Model）
          → ApplicationService（用例编排）
              → Repository（数据持久化）
```

规则：

- Controller 只做请求接收、参数校验、结果返回，不写任何业务逻辑
- Delegate 负责所有 DTO 转换（`req → Command/Model`，`Model → resp`）
- 不写业务规则、不直接操作数据库、不暴露领域模型
- `adapter` 不得直接依赖 `infrastructure` 实现类（只依赖 `service` 端口）

### 4.4 service（应用层）

职责：用例编排层，定义系统"能做什么"（`port.in`）和"需要什么外部能力"（`port.out`），编排领域对象完成业务流程。

**必须放在 `service` 的包：**

```
io.github.timemachinelab.service
  ├─ application/
  │    XxxApplicationService.java  # 用例实现（实现 port.in 接口）
  ├─ port/
  │    ├─ in/
  │    │    XxxUseCase.java        # 入站端口（用例接口）
  │    └─ out/
  │         XxxRepositoryPort.java # 出站端口（抽象外部能力，如 DB、缓存、第三方）
  ├─ model/
  │    ├─ XxxModel.java           # 服务内部业务模型（不是对外 DTO）
  │    ├─ XxxCommand.java         # 创建/更新命令
  │    └─ XxxResult.java          # 操作结果
  └─ process/                     # 跨用例编排（可选，简单场景可省略）
```

规则：

- `service` 层通过 `port.in` 接口对外暴露用例，`adapter` 只依赖这些接口
- 不承载底层技术实现（不写 SQL、不写 HTTP 调用）
- 不绕过端口直接依赖基础设施实现
- 事务边界默认放在 `service`（`@Transactional`）
- `service.model` 中的类是应用层内部模型，不对外暴露

### 4.5 domain（领域层）

职责：承载业务核心概念与规则，是最"纯"的业务层，不依赖任何技术框架。

**必须放在 `domain` 的包：**

```
io.github.timemachinelab.domain
  ├─ common/
  │    DomainException.java        # 领域公共异常基类
  ├─ catalog/                     # catalog 子域（按业务子域划分）
  │    ├─ model/
  │    │    ├─ XxxAggregate.java # 聚合根（核心业务实体）
  │    │    ├─ XxxId.java        # ID 值对象
  │    │    ├─ XxxCode.java      # 业务编码值对象（不可变）
  │    │    └─ XxxStatus.java    # 状态枚举
  │    ├─ service/
  │    │    XxxDomainService.java # 领域服务（跨聚合的业务规则）
  │    ├─ repository/
  │    │    XxxRepository.java     # 仓储接口（领域层定义）
  │    └─ event/
  │         XxxEvent.java         # 领域事件（可选）
  └─ common/                      # 跨子域共享的稳定概念
```

规则：

- 越纯越好：不依赖 Spring、MyBatis-Plus、数据库、HTTP
- 不写 Controller、Mapper、第三方协议适配
- 聚合根承担核心业务不变量和状态流转规则
- 仓储接口定义在 `domain`，具体实现放在 `infrastructure`
- 值对象（ID、Code 等）创建后应设计为不可变

### 4.6 infrastructure（基础设施层）

职责：实现 `service` 层定义的 `port.out` 接口，包括数据库持久化、缓存、第三方调用、消息发送等所有技术实现。

**必须放在 `infrastructure` 的包：**

```
io.github.timemachinelab.infrastructure
  ├─ catalog/                      # catalog 子域的技术实现
  │    └─ persistence/
  │         ├─ entity/
  │         │    XxxDo.java      # 数据库持久化对象（DO/PO）
  │         ├─ mapper/
  │         │    XxxMapper.java  # MyBatis-Plus Mapper 接口
  │         ├─ converter/
  │         │    XxxConverter.java  # Do ↔ Aggregate 转换器
  │         └─ repository/
  │              XxxRepositoryImpl.java  # 仓储接口实现
  ├─ cache/                       # 缓存实现（可选）
  ├─ external/                    # 第三方 HTTP/RPC 调用实现（可选）
  ├─ mq/
  │    └─ producer/                # 消息生产者实现（可选）
  └─ config/                       # 技术配置（数据源、事务等）
```

规则：

- 实现 `service.port.out` 中定义的端口接口
- 允许依赖技术框架（MyBatis-Plus、Redis Client 等）
- 不承载业务规则决策（业务判断应在上层完成）
- DO/PO 不得直接传播到 `adapter` 或对外暴露
- 所有技术细节（SQL、Redis Key 规则）应封装在此层

### 4.7 client（对外 SDK 层）

职责：给其他服务或调用方提供"像本地方法一样调用"的 SDK。

```
io.github.timemachinelab.client
  ├─ api/
  │    XxxClient.java              # 对外暴露的 Client
  └─ internal/
       XxxClientImpl.java          # HTTP/gRPC 调用实现
```

规则：

- 基于 `api` 契约封装，不写服务内部业务逻辑
- 不得直接依赖 `service`/`domain`/`infrastructure`

## 5. 依赖方向

必须遵守以下方向：

- `app -> adapter`
- `adapter -> service`
- `service -> domain`
- `infrastructure -> service/domain`
- `client -> api`

硬性禁止：

- `adapter -> infrastructure` 直接写业务调用链
- `domain -> infrastructure`
- `domain -> adapter`
- `service` 直接依赖某个具体数据库实现类
- 任意层跨层绕过标准入口

## 6. DDD 落地规则

### 6.1 什么时候建聚合

以下场景必须优先考虑聚合建模：

- 存在明确业务一致性边界
- 存在多个字段联动约束
- 存在状态流转规则
- 存在核心业务不变量

以下场景允许先走简化模式：

- 明确的简单 CRUD
- 纯查询型场景
- 暂无复杂业务规则的配置类数据

### 6.2 简化模式规则

简化模式不是跳过分层，仍然必须满足：

- Controller 在 adapter
- 用例编排在 service
- 数据落库在 infrastructure
- 领域规则未来可回收至 domain

### 6.3 仓储规则

- 仓储接口定义在 `service` 或 `domain` 允许的边界内，具体实现放 `infrastructure`
- 不允许在 adapter 中写 Mapper 直调业务
- 不允许将 MyBatis-Plus 的持久化对象直接当成领域对象传播

## 7. Controller 与返回规范

### 7.1 Controller 规则

- Controller 必须放在 `adapter`
- Controller 只做请求接收、参数校验、结果返回
- Controller 不写业务编排
- Controller 不写领域规则

### 7.2 Result 规则

项目已引入 TML 团队 SDK。

硬规则：

- Controller 层统一使用 TML-SDK 的 `Result`
- 优先使用 `@AutoResp` 统一响应包装
- 已返回 `Result` 时，不重复包装
- 异常响应必须与 TML-SDK 统一响应机制保持一致
参考代码：
```
import io.github.timemachinelab.common.annotation.AutoResp;

// 整个Controller的所有方法都自动包装
@RestController
@AutoResp
public class OrderController {

    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody OrderDTO orderDTO) {
        return orderService.create(orderDTO);
    }
}
```

## 8. 错误码与异常规则

- 错误码必须集中管理
- 禁止临时拼接字符串错误信息作为标准返回
- 业务异常要有明确语义
- adapter 负责将异常映射为标准响应
- 不允许吞异常
- 不允许返回模糊错误

## 9. 数据访问规则

- 持久化使用 MyBatis-Plus
- 事务边界默认放在 `service`
- `domain` 不感知 MyBatis-Plus
- Mapper/DAO 不得直接暴露给 adapter
- DO/PO 与领域对象分离
- 表字段语义未经确认不得擅自修改

### 9.1 后端测试执行方法论

- 后端测试必须使用 Java 17 与 Maven；若 `mvn` 不在 PATH，先在本机常见目录查找已安装 Maven/JDK，不要直接跳过测试。
- Windows 环境可临时设置 `JAVA_HOME` 与 `Path` 后执行 Maven，不要求修改全局环境变量。
- 优先运行与本次变更相关的聚焦测试，覆盖受影响的 `domain`、`service`、`adapter`、`infrastructure` 模块。
- 多模块聚焦测试使用 `-pl ... -am`，并在依赖模块无匹配测试时加 `-Dsurefire.failIfNoSpecifiedTests=false`。
- 测试命令与结果必须记录到变更说明或最终回复；若仍无法执行，必须说明具体阻塞原因。

参考命令：

```powershell
$env:JAVA_HOME='D:\Code\Language\Java\JDK\jdk-17.0.4'
$env:Path="$env:JAVA_HOME\bin;D:\Code\Tools\apache-maven-3.9.9\bin;$env:Path"
mvn -pl aether-api-hub-standard/aether-api-hub-domain,aether-api-hub-standard/aether-api-hub-service -am "-Dtest=目标测试类" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

## 10. AI 开发行为宪法

AI 在本项目开发时必须遵守以下规则：

- 不允许跨层直接调用，必须遵守模块边界
- 不允许把业务规则写进 Controller、Mapper、第三方适配器
- 不允许为了省事跳过 `domain/service` 分层
- 不允许引入未确认的基础设施依赖
- 不允许擅自修改公共契约、错误码、数据库字段语义
- 不允许生成与现有命名风格冲突的类名、包名、接口名
- 新增功能必须优先复用现有聚合、端口和基础设施组件
- 代码生成后必须附带必要测试或明确说明测试缺口

## 11. 禁令清单

以下行为一律禁止：

- 在 Controller 中写业务逻辑
- 在 adapter 中直接查库改库
- 在 domain 中写 SQL、Mapper、HTTP 调用
- 在 infrastructure 中写核心业务规则
- 直接将对外 DTO 当成领域对象使用
- 直接将 DO/PO 当成领域对象对外扩散
- 为了凑 DDD 结构而创建无意义空类
- 未经确认新增框架、组件、中间件
- 修改现有公共语义却不更新契约和错误码

## 12. 开发前检查

开始写代码前，必须先确认：

- 该功能属于哪个模块（app / api / adapter / service / domain / infrastructure / client）
- 该功能属于哪个用例
- 该功能是否需要聚合建模
- 对外契约是否新增或变更
- 是否需要新增错误码
- 是否可以复用现有端口与实现
- **包名前缀是否为 `io.github.timemachinelab`**（禁止使用 `io.github.aetherapihub` 或其他前缀）
- **包路径是否符合 TML-DDD 骨架约定**（各包存放内容见第 4 节）

若以上问题无法回答清楚，不得直接开始开发。

> **快速核对清单**：
> - [ ] 新增的类放在正确模块的正确包路径下
> - [ ] 聚合根放在 `domain.<子域>.model`
> - [ ] 值对象放在 `domain.<子域>.model`
> - [ ] 仓储接口放在 `domain.<子域>.repository`
> - [ ] 应用服务放在 `service.application`
> - [ ] 入站端口放在 `service.port.in`
> - [ ] 出站端口放在 `service.port.out`
> - [ ] 服务内部模型放在 `service.model`
> - [ ] Controller 放在 `adapter.web.controller`
> - [ ] Delegate 放在 `adapter.web.delegate`
> - [ ] 全局异常处理放在 `adapter.web.handler`
> - [ ] 错误码放在 `api.error`
> - [ ] 对外 Req 放在 `api.req`
> - [ ] 对外 Resp 放在 `api.resp`
> - [ ] DO/Mapper 放在 `infrastructure.<子域>.persistence`

## 13. 文档结论

Aether API HUB 后端开发遵循“实用 DDD + 严格边界 + 简洁实现”的总方针。

所有人工开发与 AI 开发，都必须优先服从模块职责、依赖方向、统一 Result 规范与集中错误码规范。

当开发效率与结构规范冲突时，优先选择“边界不破坏前提下的简化实现”，不允许以效率为理由破坏架构。
