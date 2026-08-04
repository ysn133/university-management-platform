import { Link, NavLink, Outlet } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { getWorkspacePath } from "@/features/auth/model/auth-types";

export interface WorkspaceNavigationItem {
  label: string;
  to: string;
  end?: boolean;
  icon?: "overview" | "establishments" | "information" | "leadership" | "admins";
  group?: string;
}

interface WorkspaceBreadcrumb {
  label: string;
  to?: string;
}

interface WorkspaceContext {
  name: string;
  meta: string;
  monogram: string;
  backLabel?: string;
  backTo?: string;
}

interface WorkspaceLayoutProps {
  workspaceName: string;
  scopeLabel: string;
  navigation: WorkspaceNavigationItem[];
  variant?: "default" | "management";
  breadcrumbs?: WorkspaceBreadcrumb[];
  context?: WorkspaceContext;
}

function NavigationIcon({ icon }: { icon?: WorkspaceNavigationItem["icon"] }) {
  const paths = {
    overview: <><rect height="7" rx="1" width="7" x="3" y="3" /><rect height="7" rx="1" width="7" x="14" y="3" /><rect height="7" rx="1" width="7" x="3" y="14" /><rect height="7" rx="1" width="7" x="14" y="14" /></>,
    establishments: <><path d="M3 21h18M5 21V9l7-5 7 5v12M9 21v-7h6v7" /></>,
    information: <><circle cx="12" cy="12" r="9" /><path d="M12 11v6M12 7h.01" /></>,
    leadership: <><circle cx="9" cy="8" r="3" /><path d="M3.5 20c.4-4 2.2-6 5.5-6s5.1 2 5.5 6M16 7h5M18.5 4.5v5" /></>,
    admins: <><circle cx="9" cy="8" r="3" /><path d="M3.5 20c.4-4 2.2-6 5.5-6s5.1 2 5.5 6M16 12h5M18.5 9.5v5" /></>,
  };

  return (
    <svg aria-hidden="true" className="workspace-nav-icon" fill="none" viewBox="0 0 24 24">
      {icon ? paths[icon] : paths.overview}
    </svg>
  );
}

export function WorkspaceLayout({
  workspaceName,
  scopeLabel,
  navigation,
  variant = "default",
  breadcrumbs = [],
  context,
}: WorkspaceLayoutProps) {
  const { user, logout } = useAuth();

  if (variant === "management") {
    let currentGroup = "";

    return (
      <div className="workspace-shell workspace-shell--management">
        <aside className="management-global-rail" aria-label="Global navigation">
          <Link className="global-rail-brand" to="/management" aria-label="Université Ibn Zohr management">UIZ</Link>
          <nav>
            <Link className="global-rail-link" to="/management" title="University home"><NavigationIcon icon="overview" /><span>Home</span></Link>
            {user?.role === "ROOT_SUPER_ADMIN" && <Link className="global-rail-link" to="/management/establishments" title="Establishments"><NavigationIcon icon="establishments" /><span>Structure</span></Link>}
          </nav>
          <Link className="global-rail-link global-rail-link--bottom" to="/management/account/password" title="Account security"><NavigationIcon icon="information" /><span>Account</span></Link>
        </aside>

        <aside className="workspace-sidebar">
          {context && (
            <header className="management-context-card">
              <span className="management-context-monogram">{context.monogram}</span>
              <div><strong>{context.name}</strong><small>{context.meta}</small></div>
            </header>
          )}
          {context?.backTo && <Link className="context-back-link" to={context.backTo}>← {context.backLabel}</Link>}

          <nav aria-label={`${workspaceName} navigation`}>
            {navigation.map((item) => {
              const showGroup = item.group && item.group !== currentGroup;
              if (item.group) currentGroup = item.group;
              return (
                <div className="workspace-nav-entry" key={item.to}>
                  {showGroup && <span className="workspace-nav-group">{item.group}</span>}
                  <NavLink className={({ isActive }) => isActive ? "workspace-nav-link is-active" : "workspace-nav-link"} end={item.end} to={item.to}>
                    <NavigationIcon icon={item.icon} />
                    <span>{item.label}</span>
                  </NavLink>
                </div>
              );
            })}
          </nav>

          <footer className="workspace-sidebar-footer"><span>Current scope</span><strong>{scopeLabel}</strong></footer>
        </aside>

        <div className="workspace-main">
          <header className="workspace-topbar">
            <nav className="workspace-breadcrumbs" aria-label="Breadcrumb">
              {breadcrumbs.length > 0 ? breadcrumbs.map((crumb, index) => (
                <span key={`${crumb.label}-${index}`}>
                  {index > 0 && <i>/</i>}
                  {crumb.to && index < breadcrumbs.length - 1 ? <Link to={crumb.to}>{crumb.label}</Link> : <strong>{crumb.label}</strong>}
                </span>
              )) : <strong>University Management</strong>}
            </nav>
            {user && <div className="account-menu"><span className="account-avatar">{user.firstName[0]}{user.lastName[0]}</span><div><strong>{user.firstName} {user.lastName}</strong><small>{user.role.replaceAll("_", " ")}</small></div><Link to="/management/account/password">Security</Link><button onClick={() => void logout()} type="button">Sign out</button></div>}
          </header>
          <main className="workspace-content"><Outlet /></main>
        </div>
      </div>
    );
  }

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
