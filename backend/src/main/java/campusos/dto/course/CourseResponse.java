package campusos.dto.course;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private String courseCode;
    private String courseName;
    private int credits;
    private String department;
    private int semester;
    private Long facultyId;
    private String facultyName;
}
