package campusos.Controller;

import campusos.dto.student.StudentRequest;
import campusos.dto.student.StudentResponse;
import campusos.Service.StudentService;
import campusos.exception.StudentNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student record lookup and administration")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STUDENT')")
    @Operation(summary = "List students", description = "Returns all student records. Requires a STUDENT, FACULTY, or ADMIN JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<StudentResponse>> getAllStudents() {

        return ResponseEntity.ok(
                studentService.getAllStudents()
        );
    }

    @GetMapping("/id/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STUDENT')")
    @Operation(summary = "Get student by ID", description = "Returns a student by generated database ID, or 404 if none exists.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<StudentResponse> getStudentById(
            @PathVariable Long id) {

        StudentResponse student =
                studentService.getStudentById(id);

        return ResponseEntity.ok(student);
    }

    @GetMapping("/{rollNumber}")
        @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STUDENT')")
    @Operation(summary = "Get student by roll number", description = "Returns the matching student record. Used by the authenticated AI verification service.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<StudentResponse> getStudentByRollNumber(
            @PathVariable String rollNumber) {

        StudentResponse student =
                studentService.getStudentByRollNumber(rollNumber);


        return ResponseEntity.ok(student);
    }

    @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create student", description = "Creates a student after validating the request. Requires an ADMIN JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<StudentResponse> createStudent(
            @Valid @RequestBody StudentRequest request) {

        StudentResponse student =
                studentService.createStudent(request);

        return ResponseEntity.status(201).body(student);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update student", description = "Replaces the student fields for the supplied ID. Requires an ADMIN JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {

        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete student", description = "Deletes a student by generated database ID. Requires an ADMIN JWT.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id) {

        boolean deleted =
                studentService.deleteStudent(id);

        if (!deleted) {
                        throw new StudentNotFoundException("Student not found with this id: " + id);
        }

        return ResponseEntity.noContent().build();
    }
}