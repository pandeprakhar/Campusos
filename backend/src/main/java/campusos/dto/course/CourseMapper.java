package campusos.dto.course;

import campusos.entity.Course;
import campusos.entity.Faculty;

public class CourseMapper {

    public static CourseResponse toResponse(Course course) {
        Faculty faculty = course.getFaculty();

        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getCourseName(),
                course.getCredits(),
                course.getDepartment(),
                course.getSemester(),
                faculty != null ? faculty.getId() : null,
                faculty != null ? faculty.getName() : null
        );
    }

    public static Course toEntity(CourseRequest request, Faculty faculty) {
        Course course = new Course();
        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setCredits(request.getCredits());
        course.setDepartment(request.getDepartment());
        course.setSemester(request.getSemester());
        course.setFaculty(faculty);
        return course;
    }
}
