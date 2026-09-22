package campusos.dto.enrollment;

import campusos.entity.EnrollmentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrollmentRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @Min(value = 1, message = "Semester must be at least 1")
    private int semester;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotNull(message = "Status is required")
    private EnrollmentStatus status;
}
