package io.github.timemachinelab.api.req;

import jakarta.validation.constraints.Size;

/**
 * Platform proxy asset binding candidate search request.
 */
public class ListPlatformProxyAssetCandidateReq {

    @Size(max = 128, message = "Keyword must be less than or equal to 128 characters")
    private String keyword;

    private String status;

    @Size(max = 36, message = "Bound profile id must be less than or equal to 36 characters")
    private String boundProfileId;

    private int page = 1;

    private int size = 20;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBoundProfileId() {
        return boundProfileId;
    }

    public void setBoundProfileId(String boundProfileId) {
        this.boundProfileId = boundProfileId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
