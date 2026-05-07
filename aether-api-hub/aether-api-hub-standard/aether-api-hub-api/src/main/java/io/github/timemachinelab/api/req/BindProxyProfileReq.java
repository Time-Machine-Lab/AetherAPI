package io.github.timemachinelab.api.req;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to bind a proxy profile to an API asset.
 */
public class BindProxyProfileReq {

    @NotBlank
    private String profileId;

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
}
