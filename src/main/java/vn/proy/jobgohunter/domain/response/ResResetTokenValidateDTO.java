package vn.proy.jobgohunter.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResResetTokenValidateDTO {
    private boolean valid;
    private String message;
}
