package io.github.timemachinelab.api.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Current-user API call log list request.
 */
public class ListApiCallLogReq {

    private String targetApiCode;
    private String invocationStartAt;
    private String invocationEndAt;

    @Min(value = 1, message = "Page must be greater than or equal to 1")
    private int page = 1;

    @Min(value = 1, message = "Size must be greater than or equal to 1")
    @Max(value = 100, message = "Size must be less than or equal to 100")
    private int size = 20;

    public ListApiCallLogReq() {
    }

    public String getTargetApiCode() {
        return targetApiCode;
    }

    public void setTargetApiCode(String targetApiCode) {
        this.targetApiCode = targetApiCode;
    }

    public String getInvocationStartAt() {
        return invocationStartAt;
    }

    public void setInvocationStartAt(String invocationStartAt) {
        this.invocationStartAt = invocationStartAt;
    }

    public String getInvocationEndAt() {
        return invocationEndAt;
    }

    public void setInvocationEndAt(String invocationEndAt) {
        this.invocationEndAt = invocationEndAt;
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
