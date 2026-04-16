package io.github.timemachinelab.service.model;

/**
 * 最近使用快照模型。
 */
public class LastUsedSnapshotModel {

    private final String lastUsedAt;
    private final String lastUsedChannel;
    private final String lastUsedResult;

    public LastUsedSnapshotModel(String lastUsedAt, String lastUsedChannel, String lastUsedResult) {
        this.lastUsedAt = lastUsedAt;
        this.lastUsedChannel = lastUsedChannel;
        this.lastUsedResult = lastUsedResult;
    }

    public String getLastUsedAt() {
        return lastUsedAt;
    }

    public String getLastUsedChannel() {
        return lastUsedChannel;
    }

    public String getLastUsedResult() {
        return lastUsedResult;
    }
}
