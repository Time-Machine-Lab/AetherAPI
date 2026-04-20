## 1. API 权威文档

- [ ] 1.1 使用 `tml-docs-spec-generate` 生成 `docs/api/category.yaml`，并映射到 `CategoryController.java`，补齐分类列表、分类详情及相关 `400` 绑定失败场景。
- [ ] 1.2 使用 `tml-docs-spec-generate` 生成 `docs/api/api-asset.yaml`，并映射到 `ApiAssetController.java`，补齐资产详情及相关无效请求响应场景。
- [ ] 1.3 使用 `tml-docs-spec-generate` 生成 `docs/api/api-credential.yaml`，并映射到 `ApiCredentialController.java`，补齐当前用户 API Key 列表、详情及相关无效请求响应场景。

## 2. 回归覆盖

- [ ] 2.1 补充后端 Web 层测试，复现当前分类列表、分类详情、资产详情、当前用户 API Key 列表的参数绑定失败问题。
- [ ] 2.2 补充异常处理测试，验证参数绑定失败会返回对应接口族错误码，且响应消息不再暴露编译器或反射提示。

## 3. 参数绑定稳定性修复

- [ ] 3.1 调整后端构建配置与受影响 Controller 的方法签名或注解写法，使路径参数与查询参数在打包产物中也能稳定绑定。
- [ ] 3.2 调整全局异常处理逻辑，使分类、资产、当前用户 API Key 接口的框架级绑定失败不再统一落到 `CATEGORY_CODE_INVALID`。

## 4. 验证

- [ ] 4.1 运行覆盖受影响 Controller 与全局异常处理的后端定向测试集。
- [ ] 4.2 通过打包后端产物或等价集成路径对文档涉及接口进行烟雾验证，确认相关 `400` 回归问题消失。
