import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { AuthContext, type AuthContextValue } from "@/features/auth/context/AuthContext";
import type { AuthenticatedUser } from "@/features/auth/model/auth-types";
import { WorkspaceLayout } from "./WorkspaceLayout";

const professor: AuthenticatedUser = {
  userAccountId: "bc9ab3ca-7802-4d86-b502-66b87f25f70e",
  role: "PROFESSOR",
  roleEntityId: "6308849f-64be-46ec-b25c-58c725e451a1",
  establishmentId: "cf4c1108-1879-43dc-bccd-339889b0037a",
  universityEmail: "amina@uiz.ac.ma",
  firstName: "Amina",
  lastName: "Professor",
  accountStatus: "ACTIVE",
};

afterEach(cleanup);

function authValue(logout = vi.fn()): AuthContextValue {
  return {
    user: professor,
    isRestoring: false,
    login: vi.fn(),
    logout,
    changePassword: vi.fn(),
  };
}

describe("WorkspaceLayout account menu", () => {
  it("opens from the avatar and links to the current workspace account", async () => {
    const user = userEvent.setup();

    render(
      <AuthContext value={authValue()}>
        <MemoryRouter initialEntries={["/professor"]}>
          <Routes>
            <Route
              path="/professor"
              element={(
                <WorkspaceLayout
                  accountPath="/professor/account"
                  navigation={[]}
                  scopeLabel="Teaching"
                  variant="management"
                  workspaceName="Professor"
                />
              )}
            >
              <Route index element={<p>Professor home</p>} />
              <Route path="account" element={<p>Account settings page</p>} />
            </Route>
          </Routes>
        </MemoryRouter>
      </AuthContext>,
    );

    await user.click(screen.getByRole("button", { name: "Open account menu" }));

    expect(screen.getByText("amina@uiz.ac.ma")).toBeVisible();
    await user.click(screen.getByRole("menuitem", { name: "Account settings" }));
    expect(screen.getByText("Account settings page")).toBeVisible();
  });

  it("signs out from the account menu", async () => {
    const user = userEvent.setup();
    const logout = vi.fn().mockResolvedValue(undefined);

    render(
      <AuthContext value={authValue(logout)}>
        <MemoryRouter>
          <Routes>
            <Route
              path="/"
              element={(
                <WorkspaceLayout
                  navigation={[]}
                  scopeLabel="Teaching"
                  variant="management"
                  workspaceName="Professor"
                />
              )}
            />
          </Routes>
        </MemoryRouter>
      </AuthContext>,
    );

    await user.click(screen.getByRole("button", { name: "Open account menu" }));
    await user.click(screen.getByRole("menuitem", { name: "Sign out" }));
    expect(logout).toHaveBeenCalledOnce();
  });
});
