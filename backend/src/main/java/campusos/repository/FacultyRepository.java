package campusos.repository;

import campusos.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);
}
