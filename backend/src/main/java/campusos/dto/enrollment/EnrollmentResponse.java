package campusos.dto.enrollment;

import campusos.entity.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private int semester;
    private String academicYear;
    private EnrollmentStatus status;
}
