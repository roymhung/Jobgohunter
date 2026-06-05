package vn.proy.jobgohunter.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.proy.jobgohunter.util.enums.GenderEnum;

@Getter
@Setter
public class ResUpdateUserDTO {
    private Long id;

    private String name;
    private int age;

    private GenderEnum gender;
    private String address;

    private Instant updatedAt;
}
