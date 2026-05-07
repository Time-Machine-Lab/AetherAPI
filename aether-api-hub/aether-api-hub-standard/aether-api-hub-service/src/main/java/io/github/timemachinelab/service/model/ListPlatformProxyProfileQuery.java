package io.github.timemachinelab.service.model;

/**
 * Query for platform proxy profile list.
 */
public class ListPlatformProxyProfileQuery {

    private final String actorRole;
    private final Boolean enabled;
    private final String keyword;
    private final int page;
    private final int size;

    public ListPlatformProxyProfileQuery(String actorRole, Boolean enabled, String keyword, int page, int size) {
        this.actorRole = actorRole;
        this.enabled = enabled;
        this.keyword = keyword;
        this.page = page;
        this.size = size;
    }

    public String getActorRole() { return actorRole; }
    public Boolean getEnabled() { return enabled; }
    public String getKeyword() { return keyword; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
