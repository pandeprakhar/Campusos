package campusos.Controller;

import campusos.dto.student.StudentRequest;
import campusos.dto.student.StudentResponse;
import campusos.Service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {

        return ResponseEntity.ok(
                studentService.getAllStudents()
        );
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<StudentResponse> getStudentById(
            @PathVariable Long id) {

        StudentResponse student =
                studentService.getStudentById(id);

        return ResponseEntity.ok(student);
    }

    @GetMapping("/{rollNumber}")
    public ResponseEntity<StudentResponse> getStudentByRollNumber(
            @PathVariable String rollNumber) {

        StudentResponse student =
                studentService.getStudentByRollNumber(rollNumber);


        return ResponseEntity.ok(student);
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(
            @Valid @RequestBody StudentRequest request) {

        StudentResponse student =
                studentService.createStudent(request);

        return ResponseEntity.status(201).body(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @Valid @PathVariable Long id,
            @RequestBody StudentRequest request) {

        StudentResponse student =
                studentService.updateStudent(id, request);


        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id) {

        boolean deleted =
                studentService.deleteStudent(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}