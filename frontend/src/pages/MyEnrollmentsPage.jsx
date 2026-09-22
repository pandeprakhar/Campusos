import { useEffect, useState } from 'react';
import { apiClient } from '../services/api';

export default function MyEnrollmentsPage({ user }) {
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const students = await apiClient.get('/api/students');
        const currentStudent = students.find((student) => student.email === user?.email);

        if (!currentStudent) {
          setEnrollments([]);
          return;
        }

        const data = await apiClient.get(`/api/enrollments/student/${currentStudent.id}`);
        setEnrollments(data || []);
      } catch (err) {
        setError('Unable to load enrollments. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    if (user?.email) {
      loadData();
    }
  }, [user]);

  return (
    <div className="page-section">
      <h1>My Enrollments</h1>

      {loading && <p>Loading enrollments...</p>}
      {error && <div className="error-box">{error}</div>}

      {!loading && !error && enrollments.length === 0 && (
        <div className="empty-state">No enrollments available.</div>
      )}

      {!loading && !error && enrollments.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Course</th>
                <th>Code</th>
                <th>Semester</th>
                <th>Academic Year</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {enrollments.map((item) => (
                <tr key={item.id}>
                  <td>{item.courseName}</td>
                  <td>{item.courseCode}</td>
                  <td>{item.semester}</td>
                  <td>{item.academicYear}</td>
                  <td>{item.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
