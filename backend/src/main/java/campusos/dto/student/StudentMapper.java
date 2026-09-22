package campusos.dto.student;

import campusos.entity.Student;

public class StudentMapper {

    public static StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getRollNumber(),
                student.getName(),
                student.getEmail(),
                student.getDepartment(),
                student.getSemester()
        );
    }

    public static Student toEntity(StudentRequest request) {
        Student student = new Student();

        student.setRollNumber(request.getRollNumber());
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setDepartment(request.getDepartment());
        student.setSemester(request.getSemester());

        return student;
    }
}