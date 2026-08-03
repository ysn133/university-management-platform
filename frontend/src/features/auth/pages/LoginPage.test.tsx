import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { AuthContext, type AuthContextValue } from "../context/AuthContext";
import type { AuthenticatedUser } from "../model/auth-types";
import { LoginPage } from "./LoginPage";

const rootUser: AuthenticatedUser = {
  userAccountId: "bc9ab3ca-7802-4d86-b502-66b87f25f70e",
  role: "ROOT_SUPER_ADMIN",
  roleEntityId: "6308849f-64be-46ec-b25c-58c725e451a1",
  establishmentId: null,
  universityEmail: "root@uiz.ac.ma",
  firstName: "Root",
  lastName: "Admin",
  accountStatus: "ACTIVE",
};

describe("LoginPage", () => {
  it("validates credentials and resumes a protected route in the returned role workspace", async () => {
    const user = userEvent.setup();
    const login = vi.fn().mockResolvedValue(rootUser);
    const context: AuthContextValue = {
      user: null,
      isRestoring: false,
      login,
      logout: vi.fn(),
      changePassword: vi.fn(),
    };

    render(
      <AuthContext value={context}>
        <MemoryRouter
          initialEntries={[{ pathname: "/management/login", state: { returnTo: "/management/account/password" } }]}
        >
          <Routes>
            <Route path="/management/login" element={<LoginPage portal="management" />} />
            <Route path="/management/account/password" element={<p>Account security</p>} />
          </Routes>
        </MemoryRouter>
      </AuthContext>,
    );

    await user.type(screen.getByLabelText("University email"), "root@uiz.ac.ma");
    await user.type(screen.getByLabelText("Password"), "change-me-now");
    await user.click(screen.getByRole("button", { name: "Sign in" }));

    expect(login).toHaveBeenCalledWith({
      universityEmail: "root@uiz.ac.ma",
      password: "change-me-now",
    });
    expect(await screen.findByText("Account security")).toBeVisible();
  });
});
