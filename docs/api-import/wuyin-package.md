# Package_1.0

视频包装 API 接口，支持智能包装口播视频，可通过 AI 一键添加标题、字幕、音效，并提供丰富的模板，满足各类口播场景需求，让剪辑更简单高效。


**接口地址：** [https://api.wuyinkeji.com/api/async/video_package](https://api.wuyinkeji.com/api/async/video_package)

**返回格式：** application/json

**请求方式：** **HTTP**POST

**请求示例：** `https://api.wuyinkeji.com/api/async/video_package?key=你的密钥`

## 请求HEADER：

| 名称          | 值                              |
| ------------- | ------------------------------- |
| Authorization | 接口密钥,在控制台->密钥管理查看 |
| Content-Type  | application/json                |

## 请求参数说明：

| 名称        | 必填 | 类型   | 示例值                                                                | 说明        |
| ----------- | ---- | ------ | --------------------------------------------------------------------- | ----------- |
| video       | 是   | string | https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/xxx.mp4 | 视频地址    |
| template_id | 否   | string | 1                                                                     | 模板id 1-30 |

## 返回参数说明：

| 名称      | 类型   | 说明           |
| --------- | ------ | -------------- |
| code      | int    | 状态码         |
| msg       | string | 状态信息       |
| data      | string | 请求结果数据集 |
| data.id   | string | 请求结果id     |
| exec_time | float  | 执行耗时       |
| user_ip   | string | 客户端IP       |

#### 返回示例：

{ "code": 200, "msg": "成功", "data": { "id": video_4d39239e-776a-4cbd-a8eb-e2d9b4816829", "count": 10 }, "exec_time": 0.290186, "ip": "119.6.176.239"}



# 结果详情

结果查询接口 支持全模型生成结果查询

**接口地址：** [https://api.wuyinkeji.com/api/async/detail](https://api.wuyinkeji.com/api/async/detail)

**返回格式：** application/json

**请求方式：** **HTTP**GET

**请求示例：** `https://api.wuyinkeji.com/api/async/detail?key=你的密钥`

## 请求HEADER：

| 名称          | 值                              |
| ------------- | ------------------------------- |
| Content-Type  | application/json                |
| Authorization | 接口密钥,在控制台->密钥管理查看 |

## 请求参数说明：

| 名称 | 必填 | 类型   | 示例值                                     | 说明       |
| ---- | ---- | ------ | ------------------------------------------ | ---------- |
| id   | 是   | string | video_6c79c484-28b4-4ead-bf0f-ca92d507d9c5 | 接口返回ID |

## 返回参数说明：

| 名称         | 类型         | 说明                             |
| ------------ | ------------ | -------------------------------- |
| code         | int          | 状态码                           |
| msg          | string       | 状态信息                         |
| data         | string       | 请求结果数据集                   |
| data.status  | int          | 状态 0初始化 1进行中 2成功 3失败 |
| data.message | string       | 请求结果错误信息                 |
| debug        | string/array | 调试数据                         |
| exec_time    | float        | 执行耗时                         |
| user_ip      | string       | 客户端IP                         |
