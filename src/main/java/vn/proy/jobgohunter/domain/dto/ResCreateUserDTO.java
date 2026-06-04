package vn.proy.jobgohunter.domain.dto;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.proy.jobgohunter.util.enums.GenderEnum;

@Getter
@Setter
public class ResCreateUserDTO {

    private Long id;

    private String email;
    private String name;
    private int age;

    private GenderEnum gender;
    private String address;

    private Instant createdAt;
}
