import { Link, NavLink, Outlet } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { getWorkspacePath } from "@/features/auth/model/auth-types";

export interface WorkspaceNavigationItem {
  label: string;
  to: string;
  end?: boolean;
}

interface WorkspaceLayoutProps {
  workspaceName: string;
  scopeLabel: string;
  navigation: WorkspaceNavigationItem[];
  variant?: "default" | "management";
}

export function WorkspaceLayout({
  workspaceName,
  scopeLabel,
  navigation,
  variant = "default",
}: WorkspaceLayoutProps) {
  const { user, logout } = useAuth();

  return (
    <div className={`workspace-shell workspace-shell--${variant}`}>
      <aside className="workspace-sidebar">
        <header className="brand-lockup">
          <span className="brand-mark">UIZ</span>
          <div>
            <strong>Université Ibn Zohr</strong>
            <small>{workspaceName}</small>
          </div>
        </header>

        <nav aria-label={`${workspaceName} navigation`}>
          {navigation.map((item) => (
            <NavLink
              className={({ isActive }) =>
                isActive ? "workspace-nav-link is-active" : "workspace-nav-link"
              }
              end={item.end}
              key={item.to}
              to={item.to}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <footer className="workspace-sidebar-footer">
          <span>Scope</span>
          <strong>{scopeLabel}</strong>
        </footer>
      </aside>

      <div className="workspace-main">
        <header className="workspace-topbar">
          <span>University Management Platform</span>
          {user && (
            <div className="account-menu">
              <div>
                <strong>{user.firstName} {user.lastName}</strong>
                <small>{user.role.replaceAll("_", " ")}</small>
              </div>
              <Link to={`${getWorkspacePath(user.role)}/account/password`}>Security</Link>
              <button onClick={() => void logout()} type="button">Sign out</button>
            </div>
          )}
        </header>
        <main className="workspace-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
