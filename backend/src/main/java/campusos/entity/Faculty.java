package campusos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "faculty",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_faculty_employee_id", columnNames = "employee_id"),
                @UniqueConstraint(name = "uk_faculty_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String designation;
}
