package io.github.timemachinelab.api.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Create import agent session request.
 */
public class CreateImportAgentSessionReq {

    @Size(max = 1024)
    private String documentSource;

    @Size(max = 64000)
    private String documentSummary;

    @NotBlank
    @Size(max = 2000)
    private String importIntent;

    @Size(max = 128)
    private String publisherDisplayName;

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
}