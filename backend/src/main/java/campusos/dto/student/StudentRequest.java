package campusos.dto.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentRequest {

    @NotBlank(message = "Roll number is required")
    private String rollNumber;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Department is required")
    private String department;

    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester cannot exceed 8")
    private int semester;
}