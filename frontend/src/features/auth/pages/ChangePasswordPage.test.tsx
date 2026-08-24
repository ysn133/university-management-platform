import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { AuthContext, type AuthContextValue } from "../context/AuthContext";
import type { AuthenticatedUser } from "../model/auth-types";
import { ChangePasswordPage } from "./ChangePasswordPage";

const student: AuthenticatedUser = {
  userAccountId: "bc9ab3ca-7802-4d86-b502-66b87f25f70e",
  role: "STUDENT",
  roleEntityId: "6308849f-64be-46ec-b25c-58c725e451a1",
  establishmentId: "cf4c1108-1879-43dc-bccd-339889b0037a",
  universityEmail: "student@uiz.ac.ma",
  firstName: "Sara",
  lastName: "Student",
  accountStatus: "ACTIVE",
};

describe("ChangePasswordPage", () => {
  it("shows the signed-in profile and changes its password", async () => {
    const user = userEvent.setup();
    const changePassword = vi.fn().mockResolvedValue(undefined);
    const context: AuthContextValue = {
      user: student,
      isRestoring: false,
      login: vi.fn(),
      logout: vi.fn(),
      changePassword,
    };

    render(<AuthContext value={context}><ChangePasswordPage /></AuthContext>);

    expect(screen.getByRole("heading", { name: "Sara Student" })).toBeVisible();
    expect(screen.getByText("student@uiz.ac.ma")).toBeVisible();

    await user.type(screen.getByLabelText("Current password"), "old-password");
    await user.type(screen.getByLabelText("New password"), "new-password");
    await user.type(screen.getByLabelText("Confirm new password"), "new-password");
    await user.click(screen.getByRole("button", { name: "Update password" }));

    expect(changePassword).toHaveBeenCalledWith({
      currentPassword: "old-password",
      newPassword: "new-password",
    });
    expect(await screen.findByText("Password updated")).toBeVisible();
  });
});
