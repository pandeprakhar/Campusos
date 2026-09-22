import { useState, useEffect } from 'react';
import { Routes, Route, Navigate, Link, useNavigate } from 'react-router-dom';
import { apiClient, getAuthToken, setAuthToken, clearAuthToken, getCurrentUserFromToken } from './services/api';
import LoginPage from './pages/LoginPage';
import StudentDashboard from './pages/StudentDashboard';
import CoursesPage from './pages/CoursesPage';
import FacultyPage from './pages/FacultyPage';
import MyEnrollmentsPage from './pages/MyEnrollmentsPage';
import AdminPage from './pages/AdminPage';

function AppLayout({ user, onLogout }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <h2>CampusOS</h2>
        </div>

        <nav className="nav-links">
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/courses">Courses</Link>
          <Link to="/faculty">Faculty</Link>
          <Link to="/enrollments">My Enrollments</Link>
          {user?.role === 'ADMIN' && <Link to="/admin">Admin</Link>}
        </nav>

        <div className="user-card">
          <strong>{user?.name || 'User'}</strong>
          <span>{user?.role || 'ROLE'}</span>
        </div>

        <button className="logout-button" onClick={handleLogout}>Logout</button>
      </aside>

      <main className="main-panel">
        <Routes>
          <Route path="/dashboard" element={<StudentDashboard user={user} />} />
          <Route path="/courses" element={<CoursesPage />} />
          <Route path="/faculty" element={<FacultyPage />} />
          <Route path="/enrollments" element={<MyEnrollmentsPage user={user} />} />
          {user?.role === 'ADMIN' && <Route path="/admin" element={<AdminPage />} />}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  const [user, setUser] = useState(() => getCurrentUserFromToken());
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    const token = getAuthToken();
    if (token) {
      const parsedUser = getCurrentUserFromToken();
      setUser(parsedUser);
    }
    setIsReady(true);
  }, []);

  const handleLogin = (token) => {
    setAuthToken(token);
    setUser(getCurrentUserFromToken());
  };

  const handleLogout = () => {
    clearAuthToken();
    setUser(null);
  };

  if (!isReady) return <div className="page-center">Loading...</div>;

  if (!user) {
    return <LoginPage onLogin={handleLogin} />;
  }

  return <AppLayout user={user} onLogout={handleLogout} />;
}
