import { useEffect, useState } from 'react';
import { apiClient } from '../services/api';

export default function FacultyPage() {
  const [faculty, setFaculty] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadFaculty = async () => {
      try {
        setLoading(true);
        const response = await apiClient.get('/api/faculty');
        setFaculty(response || []);
      } catch (err) {
        setError('Unable to load faculty. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    loadFaculty();
  }, []);

  return (
    <div className="page-section">
      <h1>Faculty</h1>

      {loading && <p>Loading faculty...</p>}
      {error && <div className="error-box">{error}</div>}

      {!loading && !error && faculty.length === 0 && (
        <div className="empty-state">No faculty members available.</div>
      )}

      {!loading && !error && faculty.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Employee ID</th>
                <th>Department</th>
                <th>Designation</th>
              </tr>
            </thead>
            <tbody>
              {faculty.map((member) => (
                <tr key={member.id}>
                  <td>{member.name}</td>
                  <td>{member.employeeId}</td>
                  <td>{member.department}</td>
                  <td>{member.designation}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
