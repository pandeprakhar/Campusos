package campusos.dto.faculty;

import campusos.entity.Faculty;

public class FacultyMapper {

    public static FacultyResponse toResponse(Faculty faculty) {
        return new FacultyResponse(
                faculty.getId(),
                faculty.getEmployeeId(),
                faculty.getName(),
                faculty.getEmail(),
                faculty.getDepartment(),
                faculty.getDesignation()
        );
    }

    public static Faculty toEntity(FacultyRequest request) {
        Faculty faculty = new Faculty();
        faculty.setEmployeeId(request.getEmployeeId());
        faculty.setName(request.getName());
        faculty.setEmail(request.getEmail());
        faculty.setDepartment(request.getDepartment());
        faculty.setDesignation(request.getDesignation());
        return faculty;
    }
}
