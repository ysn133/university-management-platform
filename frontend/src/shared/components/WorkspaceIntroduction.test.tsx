import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { WorkspaceIntroduction } from "./WorkspaceIntroduction";

describe("WorkspaceIntroduction", () => {
  it("renders the workspace context and next delivery step", () => {
    render(
      <WorkspaceIntroduction
        description="Workspace description"
        eyebrow="Management workspace"
        nextStep="Implement authentication"
        title="University operations"
      />,
    );

    expect(screen.getByRole("heading", { name: "University operations" })).toBeVisible();
    expect(screen.getByText("Implement authentication")).toBeVisible();
  });
});
