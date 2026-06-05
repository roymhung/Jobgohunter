package vn.proy.jobgohunter.domain.request;

import jakarta.validation.constraints.NotBlank;

public class ReqLoginDTO {
    @NotBlank(message = "Username is required!")
    private String username;

    @NotBlank(message = "Password is required!")
    private String password;

    public ReqLoginDTO() {}

    public ReqLoginDTO(String password, String username) {
        this.password = password;
        this.username = username;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
