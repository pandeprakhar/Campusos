package campusos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "course",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_course_code", columnNames = "course_code")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String courseCode;

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private int semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;
}
