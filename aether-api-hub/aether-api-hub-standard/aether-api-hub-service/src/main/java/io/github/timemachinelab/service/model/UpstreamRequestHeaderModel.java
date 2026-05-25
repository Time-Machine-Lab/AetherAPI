package io.github.timemachinelab.service.model;

/**
 * 固定上游请求头模型。
 */
public class UpstreamRequestHeaderModel {

    private final String name;
    private final String value;

    public UpstreamRequestHeaderModel(String name, String value) {
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
