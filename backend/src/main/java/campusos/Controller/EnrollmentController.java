package campusos.Controller;

import campusos.Service.EnrollmentService;
import campusos.dto.enrollment.EnrollmentRequest;
import campusos.dto.enrollment.EnrollmentResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STUDENT')")
    public ResponseEntity<EnrollmentResponse> createEnrollment(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.createEnrollment(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STUDENT')")
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudentId(studentId));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourseId(courseId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        boolean deleted = enrollmentService.deleteEnrollment(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
