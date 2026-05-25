package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 固定上游请求头响应。
 */
public class UpstreamRequestHeaderResp {

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;

    public UpstreamRequestHeaderResp() {
    }

    public UpstreamRequestHeaderResp(String name, String value) {
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
