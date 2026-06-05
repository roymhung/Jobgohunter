package vn.proy.jobgohunter.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.proy.jobgohunter.util.enums.GenderEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResUserDTO {

    private Long id;

    private String email;
    private String name;
    private int age;

    private GenderEnum gender;
    private String address;

    private Instant createdAt;
    private Instant updatedAt;
}
