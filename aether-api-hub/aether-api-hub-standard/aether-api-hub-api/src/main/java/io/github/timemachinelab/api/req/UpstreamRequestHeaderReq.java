package io.github.timemachinelab.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

/**
 * 固定上游请求头请求。
 */
public class UpstreamRequestHeaderReq {

    @Size(min = 1, max = 128, message = "请求头名称长度必须为 1-128 个字符")
    @JsonProperty("name")
    private String name;

    @Size(min = 1, max = 4096, message = "请求头值长度必须为 1-4096 个字符")
    @JsonProperty("value")
    private String value;

    public UpstreamRequestHeaderReq() {
    }

    public UpstreamRequestHeaderReq(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
