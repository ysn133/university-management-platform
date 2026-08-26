import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import {
  AiNavigationDiagnostics,
  AiNavigationRequestError,
  resolveAiNavigation,
} from "../api/ai-navigation-api";
import { AiNavigationWidget } from "./AiNavigationWidget";

vi.mock("../api/ai-navigation-api", async (importOriginal) => ({
  ...await importOriginal<typeof import("../api/ai-navigation-api")>(),
  resolveAiNavigation: vi.fn(),
}));

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

const diagnostics: AiNavigationDiagnostics = {
  query: "open Lina Idrissi's grades",
  currentRoute: "/management",
  startedAt: "2026-08-26T12:00:00Z",
  serverTotalMs: 120,
  retrievals: [{
    query: "student grades",
    durationMs: 5,
    matchCount: 1,
    contextCharacters: 2000,
    matches: [{ source: "API", title: "Professor endpoints", score: 0.912 }],
  }],
  modelCalls: [{
    label: "Initial plan",
    durationMs: 90,
    plan: { steps: [{ path: "/api/v1/professors" }] },
  }],
  executions: [{
    label: "Initial execution",
    durationMs: 10,
    status: 400,
    outcome: "AI requested an invalid read-only API path: /api/v1/professors",
    apiCalls: [{
      path: "/api/v1/professors",
      queryParameters: "{}",
      status: 400,
      responsePreview: "AI requested an invalid read-only API path: /api/v1/professors",
    }],
  }],
};

describe("AiNavigationWidget", () => {
  it("opens a resolved page automatically when diagnostics are disabled", async () => {
    vi.mocked(resolveAiNavigation).mockResolvedValue({
      mode: "NAVIGATE",
      route: "/management/students/student-1",
      message: "Opening Zakaria's grades.",
      diagnostics: null,
    });
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/management"]}>
        <AiNavigationWidget />
        <Routes>
          <Route path="/management" element={<p>Management</p>} />
          <Route path="/management/students/student-1" element={<p>Resolved student record</p>} />
        </Routes>
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: /Ask/ }));
    await user.type(
      screen.getByRole("textbox", { name: "Describe the page or record" }),
      "Open Zakaria's grades",
    );
    await user.click(screen.getByRole("button", { name: "Send request" }));

    expect(await screen.findByText("Resolved student record")).toBeVisible();
    expect(screen.queryByRole("region", { name: "Execution trace" })).not.toBeInTheDocument();
  });

  it("shows the complete trace before opening a resolved page", async () => {
    vi.mocked(resolveAiNavigation).mockResolvedValue({
      mode: "NAVIGATE",
      route: "/management/students/student-1",
      message: "Opening Zakaria's grades.",
      diagnostics,
    });
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/management/students?status=ACTIVE"]}>
        <AiNavigationWidget />
        <Routes>
          <Route path="/management/students" element={<p>Student directory</p>} />
          <Route path="/management/students/student-1" element={<p>Resolved student record</p>} />
        </Routes>
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: /Ask/ }));
    await user.type(
      screen.getByRole("textbox", { name: "Describe the page or record" }),
      "Open Zakaria's grades",
    );
    await user.click(screen.getByRole("button", { name: "Send request" }));

    expect(await screen.findByRole("region", { name: "Execution trace" })).toBeVisible();
    expect(screen.getByRole("region", { name: "Resolved destination" })).toHaveTextContent(
      "Opening Zakaria's grades.",
    );
    expect(resolveAiNavigation).toHaveBeenCalledWith(
      "Open Zakaria's grades",
      "/management/students?status=ACTIVE",
      [],
    );
    await user.click(screen.getByRole("button", { name: "Open page" }));
    expect(await screen.findByText("Resolved student record")).toBeVisible();
  });

  it("renders a direct answer without navigating", async () => {
    vi.mocked(resolveAiNavigation).mockResolvedValue({
      mode: "ANSWER",
      route: "",
      message: "Yassine teaches Tuesday from 10:30 to 12:30.",
      diagnostics,
    });
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/management"]}>
        <AiNavigationWidget />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: /Ask/ }));
    await user.type(
      screen.getByRole("textbox", { name: "Describe the page or record" }),
      "When does Yassine teach?",
    );
    await user.click(screen.getByRole("button", { name: "Send request" }));

    expect(await screen.findByRole("region", { name: "Answer" })).toHaveTextContent(
      "Yassine teaches Tuesday from 10:30 to 12:30.",
    );
    expect(screen.getByRole("button", { name: "Clear chat" })).toBeVisible();

    vi.mocked(resolveAiNavigation).mockResolvedValue({
      mode: "ANSWER",
      route: "",
      message: "He teaches in room A1.",
      diagnostics: null,
    });
    await user.type(
      screen.getByRole("textbox", { name: "Describe the page or record" }),
      "In which room?",
    );
    await user.click(screen.getByRole("button", { name: "Send request" }));

    expect(await screen.findByText("He teaches in room A1.")).toBeVisible();
    expect(resolveAiNavigation).toHaveBeenLastCalledWith(
      "In which room?",
      "/management",
      [
        { role: "USER", content: "When does Yassine teach?" },
        { role: "ASSISTANT", content: "Yassine teaches Tuesday from 10:30 to 12:30." },
      ],
    );
  });

  it("shows the complete execution trace when resolution fails", async () => {
    vi.mocked(resolveAiNavigation).mockRejectedValue(
      new AiNavigationRequestError(
        "AI requested an invalid read-only API path: /api/v1/professors",
        400,
        diagnostics,
      ),
    );
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/management"]}>
        <AiNavigationWidget />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: /Ask/ }));
    await user.type(
      screen.getByRole("textbox", { name: "Describe the page or record" }),
      "open Lina Idrissi's grades",
    );
    await user.click(screen.getByRole("button", { name: "Send request" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "AI requested an invalid read-only API path: /api/v1/professors",
    );
    await waitFor(() => expect(screen.getByRole("button", { name: "Clear chat" })).toBeEnabled());
    expect(screen.getByRole("region", { name: "Execution trace" })).toHaveTextContent(
      "/api/v1/professors",
    );
    expect(screen.getByText("Generated plans (1)")).toBeVisible();
  });
});
