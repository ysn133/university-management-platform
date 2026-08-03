import { NavLink, Outlet } from "react-router-dom";

export interface WorkspaceNavigationItem {
  label: string;
  to: string;
  end?: boolean;
}

interface WorkspaceLayoutProps {
  workspaceName: string;
  scopeLabel: string;
  navigation: WorkspaceNavigationItem[];
}

export function WorkspaceLayout({
  workspaceName,
  scopeLabel,
  navigation,
}: WorkspaceLayoutProps) {
  return (
    <div className="workspace-shell">
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
          <span className="environment-badge">Foundation</span>
        </header>
        <main className="workspace-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
