package campusos.repository;

import campusos.entity.Course;
import campusos.entity.Enrollment;
import campusos.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudent(Student student);

    List<Enrollment> findByCourse(Course course);

    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);
}
