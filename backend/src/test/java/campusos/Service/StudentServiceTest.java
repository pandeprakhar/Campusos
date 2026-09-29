package campusos.Service;

import campusos.dto.student.StudentRequest;
import campusos.dto.student.StudentResponse;
import campusos.entity.Student;
import campusos.exception.StudentNotFoundException;
import campusos.repository.StudentRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void getsAllStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(student(1L, "24CSE1001")));

        List<StudentResponse> result = studentService.getAllStudents();

        assertEquals(1, result.size());
        assertEquals("24CSE1001", result.getFirst().getRollNumber());
    }

    @Test
    void getsStudentById() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(1L, "24CSE1001")));

        StudentResponse result = studentService.getStudentById(1L);

        assertEquals(1L, result.getId());
        assertEquals("24CSE1001", result.getRollNumber());
        verify(studentRepository).findById(1L);
    }

    @Test
    void throwsWhenStudentIdDoesNotExist() {
        when(studentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> studentService.getStudentById(404L));
    }

    @Test
    void getsStudentByRollNumber() {
        when(studentRepository.findByRollNumber("24CSE1001"))
                .thenReturn(Optional.of(student(1L, "24CSE1001")));

        StudentResponse result = studentService.getStudentByRollNumber("24CSE1001");

        assertEquals("Ada Example", result.getName());
    }

    @Test
    void throwsWhenRollNumberDoesNotExist() {
        when(studentRepository.findByRollNumber("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                StudentNotFoundException.class,
                () -> studentService.getStudentByRollNumber("missing")
        );
    }

    @Test
    void createsStudentFromRequest() {
        StudentRequest request = request("24CSE1001", "Ada Example");
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponse result = studentService.createStudent(request);

        assertEquals("24CSE1001", result.getRollNumber());
        assertEquals("Ada Example", result.getName());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void updatesAllStudentFields() {
        Student existing = student(1L, "24CSE1001");
        StudentRequest request = request("24ECE2002", "Grace Example");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.save(existing)).thenReturn(existing);

        StudentResponse result = studentService.updateStudent(1L, request);

        assertEquals("24ECE2002", result.getRollNumber());
        assertEquals("Grace Example", result.getName());
        assertEquals("grace@example.test", result.getEmail());
        assertEquals("Electrical Engineering", result.getDepartment());
        assertEquals(4, result.getSemester());
    }

    @Test
    void throwsWhenUpdatingMissingStudent() {
        when(studentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                StudentNotFoundException.class,
                () -> studentService.updateStudent(404L, request("24CSE1001", "Ada Example"))
        );
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void deletesExistingStudent() {
        when(studentRepository.existsById(1L)).thenReturn(true);

        assertTrue(studentService.deleteStudent(1L));

        verify(studentRepository).deleteById(1L);
    }

    @Test
    void returnsFalseWhenDeletingMissingStudent() {
        when(studentRepository.existsById(404L)).thenReturn(false);

        assertFalse(studentService.deleteStudent(404L));

        verify(studentRepository, never()).deleteById(404L);
    }

    @Test
    void rejectsInvalidStudentRequest() {
        StudentRequest request = request("", "");
        request.setEmail("not-an-email");
        request.setSemester(9);

        assertEquals(4, validator.validate(request).size());
    }

    private static Student student(Long id, String rollNumber) {
        Student student = new Student();
        student.setId(id);
        student.setRollNumber(rollNumber);
        student.setName("Ada Example");
        student.setEmail("ada@example.test");
        student.setDepartment("Computer Science");
        student.setSemester(3);
        return student;
    }

    private static StudentRequest request(String rollNumber, String name) {
        StudentRequest request = new StudentRequest();
        request.setRollNumber(rollNumber);
        request.setName(name);
        request.setEmail("grace@example.test");
        request.setDepartment("Electrical Engineering");
        request.setSemester(4);
        return request;
    }
}