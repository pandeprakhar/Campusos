package campusos.dto.enrollment;

import campusos.entity.Course;
import campusos.entity.Enrollment;
import campusos.entity.Student;

public class EnrollmentMapper {

    public static EnrollmentResponse toResponse(Enrollment enrollment) {
        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();

        return new EnrollmentResponse(
                enrollment.getId(),
                student != null ? student.getId() : null,
                student != null ? student.getName() : null,
                course != null ? course.getId() : null,
                course != null ? course.getCourseCode() : null,
                course != null ? course.getCourseName() : null,
                enrollment.getSemester(),
                enrollment.getAcademicYear(),
                enrollment.getStatus()
        );
    }
}
