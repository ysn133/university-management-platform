import { afterEach, describe, expect, it, vi } from "vitest";
import { resolveAiNavigation } from "./ai-navigation-api";

describe("AI navigation API", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("sends the question and current route and parses the resolved action", async () => {
    const diagnostics = {
      query: "Open Zakaria's grades",
      currentRoute: "/management/students?status=ACTIVE",
      startedAt: "2026-08-26T12:00:00Z",
      serverTotalMs: 100,
      retrievals: [],
      modelCalls: [],
      executions: [],
    };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      mode: "NAVIGATE",
      route: "/management/students/student-1",
      message: "Opening the student record.",
      diagnostics,
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(resolveAiNavigation(
      "Open Zakaria's grades",
      "/management/students?status=ACTIVE",
      [{ role: "USER", content: "Find Zakaria" }],
    )).resolves.toEqual({
      mode: "NAVIGATE",
      route: "/management/students/student-1",
      message: "Opening the student record.",
      diagnostics,
    });

    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.method).toBe("POST");
    expect(request.url).toContain("/api/v1/ai/navigation");
    await expect(request.clone().json()).resolves.toEqual({
      query: "Open Zakaria's grades",
      currentRoute: "/management/students?status=ACTIVE",
      history: [{ role: "USER", content: "Find Zakaria" }],
    });
  });

  it("sends only the five most recent history messages", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      mode: "ANSWER",
      route: "",
      message: "Answer",
      diagnostics: null,
    }), { status: 200, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchMock);

    await resolveAiNavigation("Current question", "/management", [
      { role: "USER", content: "one" },
      { role: "ASSISTANT", content: "two" },
      { role: "USER", content: "three" },
      { role: "ASSISTANT", content: "four" },
      { role: "USER", content: "five" },
      { role: "ASSISTANT", content: "six" },
    ]);

    const request = fetchMock.mock.calls[0][0] as Request;
    const body = await request.clone().json() as { history: Array<{ content: string }> };
    expect(body.history.map((message) => message.content)).toEqual([
      "two", "three", "four", "five", "six",
    ]);
  });
});
