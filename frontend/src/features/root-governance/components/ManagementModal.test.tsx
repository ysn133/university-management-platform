import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { ManagementModal } from "./ManagementModal";

describe("ManagementModal", () => {
  it("keeps the active input focused when its parent rerenders", async () => {
    const user = userEvent.setup();
    const { rerender } = render(
      <ManagementModal onClose={vi.fn()} title="Create Department">
        <input aria-label="Department name" />
      </ManagementModal>,
    );
    const input = screen.getByRole("textbox", { name: "Department name" });

    await user.click(input);
    await user.type(input, "Computer");
    rerender(
      <ManagementModal onClose={vi.fn()} title="Create Department">
        <input aria-label="Department name" />
      </ManagementModal>,
    );

    expect(input).toHaveFocus();
    await user.type(input, " Science");
    expect(input).toHaveValue("Computer Science");
  });
});
