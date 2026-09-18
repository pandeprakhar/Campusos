package campusos.dto.student;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentResponse {

    private String rollNumber;
    private String name;
    private String email;
    private String department;
    private int semester;
}