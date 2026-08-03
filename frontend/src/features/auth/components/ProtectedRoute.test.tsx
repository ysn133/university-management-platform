import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { AuthContext, type AuthContextValue } from "../context/AuthContext";
import type { AuthenticatedUser } from "../model/auth-types";
import { ProtectedRoute } from "./ProtectedRoute";

const professor: AuthenticatedUser = {
  userAccountId: "bc9ab3ca-7802-4d86-b502-66b87f25f70e",
  role: "PROFESSOR",
  roleEntityId: "6308849f-64be-46ec-b25c-58c725e451a1",
  establishmentId: "cf4c1108-1879-43dc-bccd-339889b0037a",
  universityEmail: "professor@uiz.ac.ma",
  firstName: "Amina",
  lastName: "Professor",
  accountStatus: "ACTIVE",
};

function authValue(user: AuthenticatedUser | null): AuthContextValue {
  return {
    user,
    isRestoring: false,
    login: vi.fn(),
    logout: vi.fn(),
    changePassword: vi.fn(),
  };
}

function renderManagementRoute(user: AuthenticatedUser | null) {
  render(
    <AuthContext value={authValue(user)}>
      <MemoryRouter initialEntries={["/management"]}>
        <Routes>
          <Route element={<ProtectedRoute portal="management" />}>
            <Route path="/management" element={<p>Management content</p>} />
          </Route>
          <Route path="/management/login" element={<p>Management login</p>} />
          <Route path="/professor" element={<p>Professor content</p>} />
        </Routes>
      </MemoryRouter>
    </AuthContext>,
  );
}

describe("ProtectedRoute", () => {
  it("sends an unauthenticated visitor to the portal login", () => {
    renderManagementRoute(null);
    expect(screen.getByText("Management login")).toBeVisible();
  });

  it("redirects an authenticated user away from a different role workspace", () => {
    renderManagementRoute(professor);
    expect(screen.getByText("Professor content")).toBeVisible();
  });
});
