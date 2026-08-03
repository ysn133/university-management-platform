import { z } from "zod";

const envSchema = z.object({
  VITE_API_BASE_URL: z.string().url().default("http://localhost:8080"),
  VITE_APP_NAME: z.string().min(1).default("ysnUniversity"),
});

const parsedEnv = envSchema.parse(import.meta.env);

export const env = {
  apiBaseUrl: parsedEnv.VITE_API_BASE_URL,
  appName: parsedEnv.VITE_APP_NAME,
} as const;
