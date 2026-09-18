package campusos.dto.student;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentRequest {

    private String rollNumber;
    private String name;
    private String email;
    private String department;
    private int semester;
}