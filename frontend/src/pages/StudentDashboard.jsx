export default function StudentDashboard({ user }) {
  const enrolledCount = 0;

  return (
    <div className="page-section">
      <h1>Student Dashboard</h1>

      <div className="stats-grid">
        <div className="stat-card">
          <label>Logged in as</label>
          <strong>{user?.name || 'Student'}</strong>
        </div>
        <div className="stat-card">
          <label>Role</label>
          <strong>{user?.role || 'STUDENT'}</strong>
        </div>
        <div className="stat-card">
          <label>Enrolled Courses</label>
          <strong>{enrolledCount}</strong>
        </div>
      </div>

      <div className="info-box">
        <h3>Quick Links</h3>
        <ul>
          <li>View all courses</li>
          <li>Review faculty</li>
          <li>Check my enrollments</li>
        </ul>
      </div>
    </div>
  );
}
