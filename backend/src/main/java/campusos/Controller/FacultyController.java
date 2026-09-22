package campusos.Controller;

import campusos.Service.FacultyService;
import campusos.dto.faculty.FacultyRequest;
import campusos.dto.faculty.FacultyResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STUDENT')")
    public ResponseEntity<List<FacultyResponse>> getAllFaculty() {
        return ResponseEntity.ok(facultyService.getAllFaculty());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY','STUDENT')")
    public ResponseEntity<FacultyResponse> getFacultyById(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.getFacultyById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyResponse> createFaculty(@Valid @RequestBody FacultyRequest request) {
        FacultyResponse faculty = facultyService.createFaculty(request);
        return ResponseEntity.status(201).body(faculty);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyResponse> updateFaculty(@PathVariable Long id, @Valid @RequestBody FacultyRequest request) {
        return ResponseEntity.ok(facultyService.updateFaculty(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        boolean deleted = facultyService.deleteFaculty(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
