import { z } from "zod";
import { authenticatedFetch } from "@/shared/api/client/authenticated-fetch";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { env } from "@/shared/config/env";

const aiNavigationResponseSchema = z.object({
  mode: z.enum(["NAVIGATE", "ANSWER"]),
  route: z.string(),
  message: z.string().min(1),
  diagnostics: z.lazy(() => aiNavigationDiagnosticsSchema).nullable(),
});

const aiNavigationApiCallSchema = z.object({
  path: z.string(),
  queryParameters: z.string(),
  status: z.number(),
  responsePreview: z.string(),
});

const aiNavigationDiagnosticsSchema = z.object({
  query: z.string(),
  currentRoute: z.string(),
  startedAt: z.string(),
  serverTotalMs: z.number(),
  retrievals: z.array(z.object({
    query: z.string(),
    durationMs: z.number(),
    matchCount: z.number(),
    contextCharacters: z.number(),
    matches: z.array(z.object({
      source: z.string(),
      title: z.string(),
      score: z.number(),
    })),
  })),
  modelCalls: z.array(z.object({
    label: z.string(),
    durationMs: z.number(),
    plan: z.unknown().nullable(),
  })),
  executions: z.array(z.object({
    label: z.string(),
    durationMs: z.number(),
    status: z.number(),
    outcome: z.string(),
    apiCalls: z.array(aiNavigationApiCallSchema),
  })),
});

const aiNavigationErrorSchema = z.object({
  error: z.number(),
  message: z.string(),
  diagnostics: aiNavigationDiagnosticsSchema.nullable(),
});

export type AiNavigationResponse = z.infer<typeof aiNavigationResponseSchema>;
export type AiNavigationDiagnostics = z.infer<typeof aiNavigationDiagnosticsSchema>;
export type AiNavigationHistoryMessage = {
  role: "USER" | "ASSISTANT";
  content: string;
};

export class AiNavigationRequestError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly diagnostics: AiNavigationDiagnostics | null,
  ) {
    super(message);
    this.name = "AiNavigationRequestError";
  }
}

export async function resolveAiNavigation(
  query: string,
  currentRoute: string,
  history: AiNavigationHistoryMessage[] = [],
): Promise<AiNavigationResponse> {
  const response = await authenticatedFetch(`${env.apiBaseUrl}/api/v1/ai/navigation`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ query, currentRoute, history: history.slice(-5) }),
  });
  const payload: unknown = await response.json().catch(() => null);

  if (!response.ok) {
    const parsedError = aiNavigationErrorSchema.safeParse(payload);
    if (parsedError.success) {
      throw new AiNavigationRequestError(
        parsedError.data.message,
        response.status,
        parsedError.data.diagnostics,
      );
    }
    throw apiRequestError(response, payload);
  }
  return aiNavigationResponseSchema.parse(payload);
}
