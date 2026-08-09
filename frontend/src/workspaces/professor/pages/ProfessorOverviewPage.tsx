import { Link } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";

export function ProfessorOverviewPage() {
  const { user } = useAuth();

  return (
    <div className="professor-portal-page">
      <header className="professor-portal-hero">
        <div><p>Professor workspace</p><h1>Welcome, {user?.firstName}</h1><span>Your teaching activity, students, assessments, and attendance will be managed from this workspace.</span></div>
        <div className="professor-identity-card"><span>{user?.firstName[0]}{user?.lastName[0]}</span><div><strong>{user?.firstName} {user?.lastName}</strong><small>{user?.universityEmail}</small><small>Professor account · Active establishment</small></div></div>
      </header>

      <section className="professor-portal-grid">
        <article><span>Teaching</span><h2>Assignments</h2><p>Assigned Course, TD, and TP teaching scopes will appear here.</p><strong>Ready for teaching allocation</strong></article>
        <article><span>Planning</span><h2>Schedule</h2><p>Your published weekly timetable will be available from the Professor workspace.</p><strong>No published schedule loaded</strong></article>
        <article><span>Academic work</span><h2>Grades and attendance</h2><p>Open assigned classes to record attendance and manage exam grade sheets.</p><strong>Available after assignment</strong></article>
      </section>

      <footer className="professor-portal-security"><div><strong>Account security</strong><span>Change the temporary password provided by administration after your first login.</span></div><Link to="/professor/account/password">Change password</Link></footer>
    </div>
  );
}
