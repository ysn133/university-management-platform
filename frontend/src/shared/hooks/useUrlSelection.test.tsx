import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, useLocation } from "react-router-dom";
import { afterEach, describe, expect, it } from "vitest";
import { useUrlSelection } from "./useUrlSelection";

const sections = ["overview", "grades", "attendance"] as const;

function SelectionProbe() {
  const [section, setSection] = useUrlSelection("tab", sections, "overview");
  const location = useLocation();

  return (
    <>
      <p>Section: {section}</p>
      <p>Location: {location.pathname}{location.search}</p>
      <button onClick={() => setSection("attendance")} type="button">Open attendance</button>
    </>
  );
}

afterEach(cleanup);

describe("useUrlSelection", () => {
  it("restores a valid selection from the URL and updates the route", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/management/students/1?tab=grades&semesterId=s1"]}>
        <SelectionProbe />
      </MemoryRouter>,
    );

    expect(screen.getByText("Section: grades")).toBeVisible();
    await user.click(screen.getByRole("button", { name: "Open attendance" }));

    expect(screen.getByText("Section: attendance")).toBeVisible();
    expect(screen.getByText("Location: /management/students/1?tab=attendance&semesterId=s1")).toBeVisible();
  });

  it("uses the default selection for an unsupported URL value", () => {
    render(
      <MemoryRouter initialEntries={["/management/students/1?tab=unknown"]}>
        <SelectionProbe />
      </MemoryRouter>,
    );

    expect(screen.getByText("Section: overview")).toBeVisible();
  });
});
