package io.github.timemachinelab.api.resp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 最近使用快照响应。
 */
public class LastUsedSnapshotResp {

    @JsonProperty("lastUsedAt")
    private String lastUsedAt;

    @JsonProperty("lastUsedChannel")
    private String lastUsedChannel;

    @JsonProperty("lastUsedResult")
    private String lastUsedResult;

    public LastUsedSnapshotResp() {
    }

    public LastUsedSnapshotResp(String lastUsedAt, String lastUsedChannel, String lastUsedResult) {
        this.lastUsedAt = lastUsedAt;
        this.lastUsedChannel = lastUsedChannel;
        this.lastUsedResult = lastUsedResult;
    }

    public String getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(String lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public String getLastUsedChannel() {
        return lastUsedChannel;
    }

    public void setLastUsedChannel(String lastUsedChannel) {
        this.lastUsedChannel = lastUsedChannel;
    }

    public String getLastUsedResult() {
        return lastUsedResult;
    }

    public void setLastUsedResult(String lastUsedResult) {
        this.lastUsedResult = lastUsedResult;
    }
}
