import { useEffect, useState } from 'react';
import { apiClient } from '../services/api';

export default function CoursesPage() {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadCourses = async () => {
      try {
        setLoading(true);
        const response = await apiClient.get('/api/courses');
        setCourses(response || []);
      } catch (err) {
        setError('Unable to load courses. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    loadCourses();
  }, []);

  return (
    <div className="page-section">
      <h1>Courses</h1>

      {loading && <p>Loading courses...</p>}
      {error && <div className="error-box">{error}</div>}

      {!loading && !error && courses.length === 0 && (
        <div className="empty-state">No courses available.</div>
      )}

      {!loading && !error && courses.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Credits</th>
                <th>Department</th>
                <th>Semester</th>
                <th>Faculty</th>
              </tr>
            </thead>
            <tbody>
              {courses.map((course) => (
                <tr key={course.id}>
                  <td>{course.courseCode}</td>
                  <td>{course.courseName}</td>
                  <td>{course.credits}</td>
                  <td>{course.department}</td>
                  <td>{course.semester}</td>
                  <td>{course.facultyName}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
