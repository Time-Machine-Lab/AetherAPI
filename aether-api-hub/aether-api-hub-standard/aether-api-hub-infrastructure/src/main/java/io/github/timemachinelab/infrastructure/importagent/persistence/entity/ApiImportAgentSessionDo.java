package io.github.timemachinelab.infrastructure.importagent.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

/**
 * Import agent session persistence object.
 */
@TableName("api_import_agent_session")
public class ApiImportAgentSessionDo {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String ownerUserId;
    private String status;
    private String documentSource;
    private String documentSummary;
    private String importIntent;
    private String publisherDisplayName;
    private Integer currentPlanVersion;
    private Integer confirmedPlanVersion;
    private String planSnapshotJson;
    private String latestRunId;
    private LocalDateTime latestConfirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentSource() {
        return documentSource;
    }

    public void setDocumentSource(String documentSource) {
        this.documentSource = documentSource;
    }

    public String getDocumentSummary() {
        return documentSummary;
    }

    public void setDocumentSummary(String documentSummary) {
        this.documentSummary = documentSummary;
    }

    public String getImportIntent() {
        return importIntent;
    }

    public void setImportIntent(String importIntent) {
        this.importIntent = importIntent;
    }

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public void setPublisherDisplayName(String publisherDisplayName) {
        this.publisherDisplayName = publisherDisplayName;
    }

    public Integer getCurrentPlanVersion() {
        return currentPlanVersion;
    }

    public void setCurrentPlanVersion(Integer currentPlanVersion) {
        this.currentPlanVersion = currentPlanVersion;
    }

    public Integer getConfirmedPlanVersion() {
        return confirmedPlanVersion;
    }

    public void setConfirmedPlanVersion(Integer confirmedPlanVersion) {
        this.confirmedPlanVersion = confirmedPlanVersion;
    }

    public String getPlanSnapshotJson() {
        return planSnapshotJson;
    }

    public void setPlanSnapshotJson(String planSnapshotJson) {
        this.planSnapshotJson = planSnapshotJson;
    }

    public String getLatestRunId() {
        return latestRunId;
    }

    public void setLatestRunId(String latestRunId) {
        this.latestRunId = latestRunId;
    }

    public LocalDateTime getLatestConfirmedAt() {
        return latestConfirmedAt;
    }

    public void setLatestConfirmedAt(LocalDateTime latestConfirmedAt) {
        this.latestConfirmedAt = latestConfirmedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}