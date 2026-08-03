import { mkdir, writeFile } from "node:fs/promises";
import openapiTS, { astToString } from "openapi-typescript";
import { loadEnv } from "vite";

const localEnv = loadEnv(process.env.NODE_ENV ?? "development", process.cwd(), "");
const source =
  process.env.OPENAPI_URL ?? localEnv.OPENAPI_URL ?? "http://localhost:8080/v3/api-docs";
const output = new URL("../src/shared/api/generated/schema.d.ts", import.meta.url);
const schema = await openapiTS(new URL(source));

await mkdir(new URL(".", output), { recursive: true });
await writeFile(output, astToString(schema));

console.log(`Generated API contracts from ${source}`);
