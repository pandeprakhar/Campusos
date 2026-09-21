package campusos.Service;

import campusos.dto.student.StudentMapper;
import campusos.dto.student.StudentRequest;
import campusos.dto.student.StudentResponse;
import campusos.entity.Student;
import campusos.exception.StudentNotFoundException;
import campusos.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public List<StudentResponse> getAllStudents() {

        return studentRepository.findAll()
                .stream()
                .map(StudentMapper::toResponse)
                .toList();
    }

    public StudentResponse getStudentById(Long id) {

        Student student =
                studentRepository.findById(id).orElseThrow(() ->
                        new StudentNotFoundException(
                                "Student not found with this id: " + id
                        )
                );

        return StudentMapper.toResponse(student);
    }

    public StudentResponse getStudentByRollNumber(String rollNumber) {

        Student student =
                studentRepository.findByRollNumber(rollNumber)
                        .orElseThrow(() ->
                new StudentNotFoundException(
                        "Student not found with roll number: " + rollNumber
                )
        );

        return StudentMapper.toResponse(student);
    }

    public StudentResponse createStudent(StudentRequest request) {

        Student student = StudentMapper.toEntity(request);

        Student savedStudent =
                studentRepository.save(student);

        return StudentMapper.toResponse(savedStudent);
    }

    public StudentResponse updateStudent(
            Long id,
            StudentRequest request) {

        Student existingStudent =
                studentRepository.findById(id).orElse(null);

        if (existingStudent == null) {
            return null;
        }

        existingStudent.setName(request.getName());
        existingStudent.setEmail(request.getEmail());
        existingStudent.setDepartment(request.getDepartment());
        existingStudent.setSemester(request.getSemester());

        Student updatedStudent =
                studentRepository.save(existingStudent);

        return StudentMapper.toResponse(updatedStudent);
    }

    public boolean deleteStudent(Long id) {

        if (!studentRepository.existsById(id)) {
            return false;
        }

        studentRepository.deleteById(id);
        return true;
    }
}