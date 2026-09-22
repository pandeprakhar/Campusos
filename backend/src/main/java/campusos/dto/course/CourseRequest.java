package campusos.dto.course;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRequest {

    @NotBlank(message = "Course code is required")
    private String courseCode;

    @NotBlank(message = "Course name is required")
    private String courseName;

    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 6, message = "Credits cannot exceed 6")
    private int credits;

    @NotBlank(message = "Department is required")
    private String department;

    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester cannot exceed 8")
    private int semester;

    @NotNull(message = "Faculty ID is required")
    private Long facultyId;
}
