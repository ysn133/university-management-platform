import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <main className="standalone-page">
      <p className="eyebrow">404</p>
      <h1>This page is not part of the platform.</h1>
      <p>Return to the management workspace to continue.</p>
      <Link className="text-link" to="/management">
        Open management workspace
      </Link>
    </main>
  );
}
