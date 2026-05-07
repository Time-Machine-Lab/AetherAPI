package io.github.timemachinelab.api.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request to create a platform proxy profile.
 */
public class CreatePlatformProxyProfileReq {

    @NotBlank
    @Size(max = 64)
    private String profileCode;

    @NotBlank
    @Size(max = 128)
    private String profileName;

    @NotBlank
    private String proxyType;

    @NotBlank
    @Size(max = 255)
    private String proxyHost;

    @NotNull
    @Min(1)
    @Max(65535)
    private Integer proxyPort;

    @Size(max = 255)
    private String username;

    private String password;
    private Boolean enabled;

    public String getProfileCode() { return profileCode; }
    public void setProfileCode(String profileCode) { this.profileCode = profileCode; }
    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) { this.profileName = profileName; }
    public String getProxyType() { return proxyType; }
    public void setProxyType(String proxyType) { this.proxyType = proxyType; }
    public String getProxyHost() { return proxyHost; }
    public void setProxyHost(String proxyHost) { this.proxyHost = proxyHost; }
    public Integer getProxyPort() { return proxyPort; }
    public void setProxyPort(Integer proxyPort) { this.proxyPort = proxyPort; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
