import { useEffect, useState } from 'react';
import { apiClient } from '../services/api';

const emptyFaculty = {
  employeeId: '',
  name: '',
  email: '',
  department: '',
  designation: ''
};

const emptyCourse = {
  courseCode: '',
  courseName: '',
  credits: 3,
  department: '',
  semester: 1,
  facultyId: ''
};

const emptyEnrollment = {
  studentId: '',
  courseId: '',
  semester: 1,
  academicYear: '',
  status: 'ENROLLED'
};

export default function AdminPage() {
  const [faculty, setFaculty] = useState([]);
  const [courses, setCourses] = useState([]);
  const [enrollments, setEnrollments] = useState([]);
  const [facultyForm, setFacultyForm] = useState(emptyFaculty);
  const [courseForm, setCourseForm] = useState(emptyCourse);
  const [enrollmentForm, setEnrollmentForm] = useState(emptyEnrollment);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const refreshData = async () => {
    try {
      setLoading(true);
      const [facultyData, courseData, enrollmentData] = await Promise.all([
        apiClient.get('/api/faculty'),
        apiClient.get('/api/courses'),
        apiClient.get('/api/enrollments/student/1').catch(() => [])
      ]);

      setFaculty(facultyData || []);
      setCourses(courseData || []);
      setEnrollments(enrollmentData || []);
    } catch (err) {
      setError('Unable to load admin data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshData();
  }, []);

  const handleFacultySubmit = async (event) => {
    event.preventDefault();
    try {
      await apiClient.post('/api/faculty', facultyForm);
      setSuccess('Faculty created successfully.');
      setFacultyForm(emptyFaculty);
      refreshData();
    } catch (err) {
      setError(err.message || 'Unable to create faculty.');
    }
  };

  const handleCourseSubmit = async (event) => {
    event.preventDefault();
    try {
      await apiClient.post('/api/courses', {
        ...courseForm,
        facultyId: Number(courseForm.facultyId)
      });
      setSuccess('Course created successfully.');
      setCourseForm(emptyCourse);
      refreshData();
    } catch (err) {
      setError(err.message || 'Unable to create course.');
    }
  };

  const handleEnrollmentSubmit = async (event) => {
    event.preventDefault();
    try {
      await apiClient.post('/api/enrollments', {
        ...enrollmentForm,
        studentId: Number(enrollmentForm.studentId),
        courseId: Number(enrollmentForm.courseId),
        semester: Number(enrollmentForm.semester)
      });
      setSuccess('Enrollment created successfully.');
      setEnrollmentForm(emptyEnrollment);
      refreshData();
    } catch (err) {
      setError(err.message || 'Unable to create enrollment.');
    }
  };

  return (
    <div className="page-section">
      <h1>Admin</h1>

      {loading && <p>Loading admin data...</p>}
      {error && <div className="error-box">{error}</div>}
      {success && <div className="success-box">{success}</div>}

      <div className="admin-grid">
        <form className="panel-form" onSubmit={handleFacultySubmit}>
          <h3>Add Faculty</h3>
          <input value={facultyForm.employeeId} onChange={(e) => setFacultyForm({ ...facultyForm, employeeId: e.target.value })} placeholder="Employee ID" />
          <input value={facultyForm.name} onChange={(e) => setFacultyForm({ ...facultyForm, name: e.target.value })} placeholder="Name" />
          <input value={facultyForm.email} onChange={(e) => setFacultyForm({ ...facultyForm, email: e.target.value })} placeholder="Email" />
          <input value={facultyForm.department} onChange={(e) => setFacultyForm({ ...facultyForm, department: e.target.value })} placeholder="Department" />
          <input value={facultyForm.designation} onChange={(e) => setFacultyForm({ ...facultyForm, designation: e.target.value })} placeholder="Designation" />
          <button type="submit">Create Faculty</button>
        </form>

        <form className="panel-form" onSubmit={handleCourseSubmit}>
          <h3>Add Course</h3>
          <input value={courseForm.courseCode} onChange={(e) => setCourseForm({ ...courseForm, courseCode: e.target.value })} placeholder="Course Code" />
          <input value={courseForm.courseName} onChange={(e) => setCourseForm({ ...courseForm, courseName: e.target.value })} placeholder="Course Name" />
          <input type="number" value={courseForm.credits} onChange={(e) => setCourseForm({ ...courseForm, credits: Number(e.target.value) })} placeholder="Credits" />
          <input value={courseForm.department} onChange={(e) => setCourseForm({ ...courseForm, department: e.target.value })} placeholder="Department" />
          <input type="number" value={courseForm.semester} onChange={(e) => setCourseForm({ ...courseForm, semester: Number(e.target.value) })} placeholder="Semester" />
          <input type="number" value={courseForm.facultyId} onChange={(e) => setCourseForm({ ...courseForm, facultyId: e.target.value })} placeholder="Faculty ID" />
          <button type="submit">Create Course</button>
        </form>

        <form className="panel-form" onSubmit={handleEnrollmentSubmit}>
          <h3>Enroll Student</h3>
          <input type="number" value={enrollmentForm.studentId} onChange={(e) => setEnrollmentForm({ ...enrollmentForm, studentId: e.target.value })} placeholder="Student ID" />
          <input type="number" value={enrollmentForm.courseId} onChange={(e) => setEnrollmentForm({ ...enrollmentForm, courseId: e.target.value })} placeholder="Course ID" />
          <input type="number" value={enrollmentForm.semester} onChange={(e) => setEnrollmentForm({ ...enrollmentForm, semester: Number(e.target.value) })} placeholder="Semester" />
          <input value={enrollmentForm.academicYear} onChange={(e) => setEnrollmentForm({ ...enrollmentForm, academicYear: e.target.value })} placeholder="Academic Year" />
          <select value={enrollmentForm.status} onChange={(e) => setEnrollmentForm({ ...enrollmentForm, status: e.target.value })}>
            <option value="ENROLLED">ENROLLED</option>
            <option value="DROPPED">DROPPED</option>
            <option value="COMPLETED">COMPLETED</option>
          </select>
          <button type="submit">Create Enrollment</button>
        </form>
      </div>

      <div className="table-wrap">
        <h3>Faculty</h3>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Employee ID</th>
              <th>Department</th>
            </tr>
          </thead>
          <tbody>
            {faculty.map((member) => (
              <tr key={member.id}><td>{member.name}</td><td>{member.employeeId}</td><td>{member.department}</td></tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="table-wrap">
        <h3>Courses</h3>
        <table>
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Department</th>
            </tr>
          </thead>
          <tbody>
            {courses.map((course) => (
              <tr key={course.id}><td>{course.courseCode}</td><td>{course.courseName}</td><td>{course.department}</td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
