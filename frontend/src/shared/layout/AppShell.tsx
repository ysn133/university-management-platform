import { Outlet } from "react-router-dom";
import { AuthProvider } from "../../auth/AuthProvider";
import { BrandingProvider } from "../../branding/BrandingProvider";

export function AppShell() {
  return (
    <BrandingProvider>
      <AuthProvider>
        <div style={{ minHeight: "100vh", padding: "32px" }}>
          <Outlet />
        </div>
      </AuthProvider>
    </BrandingProvider>
  );
}
