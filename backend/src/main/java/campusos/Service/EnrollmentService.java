package campusos.Service;

import campusos.dto.enrollment.EnrollmentMapper;
import campusos.dto.enrollment.EnrollmentRequest;
import campusos.dto.enrollment.EnrollmentResponse;
import campusos.entity.Course;
import campusos.entity.Enrollment;
import campusos.entity.Student;
import campusos.exception.CourseNotFoundException;
import campusos.exception.EnrollmentNotFoundException;
import campusos.exception.StudentNotFoundException;
import campusos.repository.CourseRepository;
import campusos.repository.EnrollmentRepository;
import campusos.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + request.getStudentId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + request.getCourseId()));

        if (enrollmentRepository.findByStudentAndCourse(student, course).isPresent()) {
            throw new IllegalArgumentException("Student is already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setSemester(request.getSemester());
        enrollment.setAcademicYear(request.getAcademicYear());
        enrollment.setStatus(request.getStatus());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentMapper.toResponse(savedEnrollment);
    }

    public EnrollmentResponse getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found with id: " + id));
        return EnrollmentMapper.toResponse(enrollment);
    }

    public List<EnrollmentResponse> getEnrollmentsByStudentId(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + studentId));

        return enrollmentRepository.findByStudent(student)
                .stream()
                .map(EnrollmentMapper::toResponse)
                .toList();
    }

    public List<EnrollmentResponse> getEnrollmentsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        return enrollmentRepository.findByCourse(course)
                .stream()
                .map(EnrollmentMapper::toResponse)
                .toList();
    }

    public boolean deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            return false;
        }

        enrollmentRepository.deleteById(id);
        return true;
    }
}
