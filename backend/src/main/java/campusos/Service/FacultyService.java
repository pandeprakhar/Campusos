package campusos.Service;

import campusos.dto.faculty.FacultyMapper;
import campusos.dto.faculty.FacultyRequest;
import campusos.dto.faculty.FacultyResponse;
import campusos.entity.Faculty;
import campusos.exception.FacultyNotFoundException;
import campusos.repository.FacultyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacultyService {

    @Autowired
    private FacultyRepository facultyRepository;

    public List<FacultyResponse> getAllFaculty() {
        return facultyRepository.findAll()
                .stream()
                .map(FacultyMapper::toResponse)
                .toList();
    }

    public FacultyResponse getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with id: " + id));
        return FacultyMapper.toResponse(faculty);
    }

    public FacultyResponse createFaculty(FacultyRequest request) {
        if (facultyRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("Faculty employee ID already exists");
        }

        if (facultyRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Faculty email already exists");
        }

        Faculty faculty = FacultyMapper.toEntity(request);
        Faculty savedFaculty = facultyRepository.save(faculty);
        return FacultyMapper.toResponse(savedFaculty);
    }

    public FacultyResponse updateFaculty(Long id, FacultyRequest request) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with id: " + id));

        if (!faculty.getEmployeeId().equals(request.getEmployeeId())
                && facultyRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("Faculty employee ID already exists");
        }

        if (!faculty.getEmail().equals(request.getEmail())
                && facultyRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Faculty email already exists");
        }

        faculty.setEmployeeId(request.getEmployeeId());
        faculty.setName(request.getName());
        faculty.setEmail(request.getEmail());
        faculty.setDepartment(request.getDepartment());
        faculty.setDesignation(request.getDesignation());

        Faculty updatedFaculty = facultyRepository.save(faculty);
        return FacultyMapper.toResponse(updatedFaculty);
    }

    public boolean deleteFaculty(Long id) {
        if (!facultyRepository.existsById(id)) {
            return false;
        }

        facultyRepository.deleteById(id);
        return true;
    }
}
