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

- 语言：Java
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

- Maven `groupId` 统一使用 `io.github.timemachinelab`
- DTO 对外命名统一使用 `Req`、`Resp`
- 应用层内部模型统一使用 `Command`、`Query`、`Model`、`Result`
- 命名必须与现有骨架风格一致
- 禁止引入风格冲突的包名、类名、接口名

## 4. 模块宪法

项目骨架按以下模块分层：

- `aether-api-hub-app`
- `aether-api-hub-api`
- `aether-api-hub-adapter`
- `aether-api-hub-service`
- `aether-api-hub-domain`
- `aether-api-hub-infrastructure`
- `aether-api-hub-client`

### 4.1 app

职责：

- 启动应用
- 装配模块
- 放启动相关配置

禁止：

- 写业务规则
- 写 Controller
- 写 Repository

### 4.2 api

职责：

- 定义对外契约
- 定义 `Req`、`Resp`
- 定义公共枚举
- 定义统一错误码

规则：

- 对外字段命名统一使用 `camelCase`
- 对外契约不得泄漏领域对象
- 错误码必须集中管理

### 4.3 adapter

职责：

- 接入 HTTP 等协议
- 做参数解析
- 做 `Req/Resp` 与应用层模型转换
- 调用 `service` 层用例

规则：

- Controller 只做接入、转换、响应
- 不写业务规则
- 不直接操作数据库
- 不直接暴露 domain model

### 4.4 service

职责：

- 承载应用服务
- 编排用例流程
- 定义 `port.in`、`port.out`
- 管理事务边界

规则：

- 负责跨聚合协调
- 负责业务流程编排
- 不承载底层技术实现
- 不绕过端口直接依赖基础设施实现

### 4.5 domain

职责：

- 承载核心业务规则
- 定义聚合、实体、值对象、领域服务
- 表达领域约束

规则：

- 越纯越好
- 不依赖 Spring MVC、MyBatis-Plus、数据库实现细节
- 不写 Controller 风格代码
- 不写第三方协议适配逻辑

### 4.6 infrastructure

职责：

- 实现数据库、缓存、第三方服务等技术能力
- 实现 `service` 层定义的 `port.out`
- 管理 DO/PO 与领域对象转换

规则：

- 可以依赖技术框架
- 不反向依赖 adapter
- 不承载业务规则决策

### 4.7 client

职责：

- 对外提供 SDK 或客户端封装

规则：

- 基于 `api` 契约封装
- 不写服务内部业务逻辑

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

所有关于 Result 的返回，以及 Controller 层返回 Result，必须遵循 TML-SDK 规范，参考 [AutoResp 注解使用指南](https://github.com/Time-Machine-Lab/TmlFoundation/blob/develop/tml-sdk-spring-boot-starter-web/src/readme/AutoResp.md)。

硬规则：

- Controller 层统一使用 TML-SDK 的 `Result`
- 优先使用 `@AutoResp` 统一响应包装
- 已返回 `Result` 时，不重复包装
- 异常响应必须与 TML-SDK 统一响应机制保持一致

### 7.3 DTO 规则

- 对外对象只能使用 `Req`、`Resp`
- adapter 负责 `Req/Resp` 与 `Command/Query/Model/Result` 转换
- 禁止 Controller 直接返回 domain 实体

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

- 该功能属于哪个模块
- 该功能属于哪个用例
- 该功能是否需要聚合建模
- 对外契约是否新增或变更
- 是否需要新增错误码
- 是否可以复用现有端口与实现

若以上问题无法回答清楚，不得直接开始开发。

## 13. 文档结论

Aether API HUB 后端开发遵循“实用 DDD + 严格边界 + 简洁实现”的总方针。

所有人工开发与 AI 开发，都必须优先服从模块职责、依赖方向、统一 Result 规范与集中错误码规范。

当开发效率与结构规范冲突时，优先选择“边界不破坏前提下的简化实现”，不允许以效率为理由破坏架构。
