package io.github.timemachinelab.api.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Console sign-in request.
 */
public class ConsoleSignInReq {

    @NotBlank(message = "Login name must not be blank")
    @Size(max = 128, message = "Login name must not exceed 128 characters")
    @JsonProperty("loginName")
    private String loginName;

    @NotBlank(message = "Password must not be blank")
    @Size(max = 256, message = "Password must not exceed 256 characters")
    @JsonProperty("password")
    private String password;

    public ConsoleSignInReq() {
    }

    public ConsoleSignInReq(String loginName, String password) {
        this.loginName = loginName;
        this.password = password;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
