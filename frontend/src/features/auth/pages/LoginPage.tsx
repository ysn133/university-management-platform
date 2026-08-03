import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { z } from "zod";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import { useAuth } from "../hooks/useAuth";
import { getWorkspacePath, type PortalType } from "../model/auth-types";

const loginSchema = z.object({
  universityEmail: z.string().trim().email("Enter a valid university email."),
  password: z.string().min(8, "Password must contain at least 8 characters."),
});

type LoginForm = z.infer<typeof loginSchema>;

const portalContent: Record<
  PortalType,
  { label: string; title: string; description: string }
> = {
  management: {
    label: "Administration",
    title: "Administration Portal",
    description: "Sign in with your university account to access management services.",
  },
  professor: {
    label: "Professor",
    title: "Professor Portal",
    description: "Sign in to access your teaching, schedules, attendance, and grade services.",
  },
  student: {
    label: "Student",
    title: "Student Portal",
    description: "Sign in to consult your schedule, examinations, grades, and academic record.",
  },
};

export function LoginPage({ portal }: { portal: PortalType }) {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [requestError, setRequestError] = useState<string | null>(null);
  const content = portalContent[portal];
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { universityEmail: "", password: "" },
  });

  async function submit(values: LoginForm) {
    setRequestError(null);
    try {
      const user = await login(values);
      const workspacePath = getWorkspacePath(user.role);
      const returnTo = (location.state as { returnTo?: unknown } | null)?.returnTo;
      const destination =
        typeof returnTo === "string" &&
        (returnTo === workspacePath || returnTo.startsWith(`${workspacePath}/`))
          ? returnTo
          : workspacePath;
      navigate(destination, { replace: true });
    } catch (error) {
      setRequestError(
        error instanceof ApiRequestError ? error.message : "Authentication is temporarily unavailable.",
      );
    }
  }

  return (
    <main className={`login-page login-page--${portal}`}>
      <div className="login-grid" aria-hidden="true" />

      <header className="login-header">
        <div className="login-brand">
          <span className="login-brand-mark">UIZ</span>
          <div>
            <strong>Université Ibn Zohr</strong>
            <small>University Management Platform</small>
          </div>
        </div>
        <span className="login-portal-badge">{content.label}</span>
      </header>

      <section className="login-card">
        <div className="login-card-heading">
          <span className="login-card-symbol" aria-hidden="true">UIZ</span>
          <p className="eyebrow">Secure university access</p>
          <h1>{content.title}</h1>
          <p>{content.description}</p>
        </div>

        <form noValidate onSubmit={handleSubmit(submit)}>
          <div className="login-field">
            <label htmlFor={`${portal}-email`}>University email</label>
            <input
              autoComplete="username"
              id={`${portal}-email`}
              placeholder="name@uiz.ac.ma"
              type="email"
              {...register("universityEmail")}
            />
            {errors.universityEmail && <p className="field-error">{errors.universityEmail.message}</p>}
          </div>

          <div className="login-field">
            <label htmlFor={`${portal}-password`}>Password</label>
            <input
              autoComplete="current-password"
              id={`${portal}-password`}
              placeholder="Enter your password"
              type="password"
              {...register("password")}
            />
            {errors.password && <p className="field-error">{errors.password.message}</p>}
          </div>

          {requestError && (
            <div className="form-alert" role="alert">
              {requestError}
            </div>
          )}

          <button className="primary-button" disabled={isSubmitting} type="submit">
            {isSubmitting ? "Signing in…" : "Sign in"}
          </button>
        </form>

        <nav className="portal-switcher" aria-label="Other platform access points">
          <span>Other access</span>
          {portal !== "management" && <Link to="/management/login">Administration</Link>}
          {portal !== "professor" && <Link to="/professor/login">Professor</Link>}
          {portal !== "student" && <Link to="/student/login">Student</Link>}
        </nav>
      </section>

      <footer className="login-footer">
        <span>Authorized university users only</span>
        <span>© Université Ibn Zohr</span>
      </footer>
    </main>
  );
}
