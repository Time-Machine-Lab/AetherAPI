package io.github.timemachinelab.service.model;

/**
 * Create import agent session command.
 */
public class CreateImportAgentSessionCommand {

    private final String ownerUserId;
    private final String publisherDisplayName;
    private final String documentSource;
    private final String documentSummary;
    private final String importIntent;

    public CreateImportAgentSessionCommand(
            String ownerUserId,
            String publisherDisplayName,
            String documentSource,
            String documentSummary,
            String importIntent) {
        this.ownerUserId = ownerUserId;
        this.publisherDisplayName = publisherDisplayName;
        this.documentSource = documentSource;
        this.documentSummary = documentSummary;
        this.importIntent = importIntent;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public String getDocumentSource() {
        return documentSource;
    }

    public String getDocumentSummary() {
        return documentSummary;
    }

    public String getImportIntent() {
        return importIntent;
    }
}