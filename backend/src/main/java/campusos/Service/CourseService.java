package campusos.Service;

import campusos.dto.course.CourseMapper;
import campusos.dto.course.CourseRequest;
import campusos.dto.course.CourseResponse;
import campusos.entity.Course;
import campusos.entity.Faculty;
import campusos.exception.CourseNotFoundException;
import campusos.exception.FacultyNotFoundException;
import campusos.repository.CourseRepository;
import campusos.repository.FacultyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(CourseMapper::toResponse)
                .toList();
    }

    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + id));
        return CourseMapper.toResponse(course);
    }

    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new IllegalArgumentException("Course code already exists");
        }

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with id: " + request.getFacultyId()));

        Course course = CourseMapper.toEntity(request, faculty);
        Course savedCourse = courseRepository.save(course);

        return CourseMapper.toResponse(savedCourse);
    }

    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + id));

        if (!course.getCourseCode().equals(request.getCourseCode())
                && courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new IllegalArgumentException("Course code already exists");
        }

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with id: " + request.getFacultyId()));

        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setCredits(request.getCredits());
        course.setDepartment(request.getDepartment());
        course.setSemester(request.getSemester());
        course.setFaculty(faculty);

        Course updatedCourse = courseRepository.save(course);
        return CourseMapper.toResponse(updatedCourse);
    }

    public boolean deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            return false;
        }

        courseRepository.deleteById(id);
        return true;
    }
}
