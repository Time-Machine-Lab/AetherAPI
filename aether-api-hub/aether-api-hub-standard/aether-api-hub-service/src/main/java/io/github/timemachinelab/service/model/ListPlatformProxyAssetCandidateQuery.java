package io.github.timemachinelab.service.model;

/**
 * Query for platform proxy asset binding candidates.
 */
public class ListPlatformProxyAssetCandidateQuery {

    private final String actorRole;
    private final String keyword;
    private final String status;
    private final String boundProfileId;
    private final int page;
    private final int size;

    public ListPlatformProxyAssetCandidateQuery(
            String actorRole,
            String keyword,
            String status,
            String boundProfileId,
            int page,
            int size) {
        this.actorRole = actorRole;
        this.keyword = keyword;
        this.status = status;
        this.boundProfileId = boundProfileId;
        this.page = page;
        this.size = size;
    }

    public String getActorRole() { return actorRole; }
    public String getKeyword() { return keyword; }
    public String getStatus() { return status; }
    public String getBoundProfileId() { return boundProfileId; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
