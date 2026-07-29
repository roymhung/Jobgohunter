package vn.proy.jobgohunter.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResOAuthStatusDTO {
    private boolean google;
    private boolean github;
    private boolean facebook;
    private String message;
}
