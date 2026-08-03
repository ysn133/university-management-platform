import createClient from "openapi-fetch";
import type { paths } from "../generated/schema";
import { env } from "@/shared/config/env";

export const apiClient = createClient<paths>({
  baseUrl: env.apiBaseUrl,
});
