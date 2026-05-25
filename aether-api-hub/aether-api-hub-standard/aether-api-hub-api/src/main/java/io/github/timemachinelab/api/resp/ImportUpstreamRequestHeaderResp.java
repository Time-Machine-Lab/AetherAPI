package io.github.timemachinelab.api.resp;

/**
 * Import Agent 计划中的上游请求头响应。
 */
public class ImportUpstreamRequestHeaderResp {

    private final String name;
    private final String value;

    public ImportUpstreamRequestHeaderResp(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
