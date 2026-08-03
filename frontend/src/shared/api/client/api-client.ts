import createClient from "openapi-fetch";
import type { paths } from "../generated/schema";
import { env } from "@/shared/config/env";
import { authenticatedFetch } from "./authenticated-fetch";

export const apiClient = createClient<paths>({
  baseUrl: env.apiBaseUrl,
  fetch: authenticatedFetch,
});
