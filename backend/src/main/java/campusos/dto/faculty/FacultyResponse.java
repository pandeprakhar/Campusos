package campusos.dto.faculty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FacultyResponse {

    private Long id;
    private String employeeId;
    private String name;
    private String email;
    private String department;
    private String designation;
}
