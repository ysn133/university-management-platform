import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import fs from "node:fs";

const httpsCertificate = process.env.HTTPS_CERT_FILE;
const httpsKey = process.env.HTTPS_KEY_FILE;

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": "/src",
    },
  },
  server: {
    host: "0.0.0.0",
    port: 5173,
    https: httpsCertificate && httpsKey ? {
      cert: fs.readFileSync(httpsCertificate),
      key: fs.readFileSync(httpsKey),
    } : undefined,
    proxy: {
      "/api": {
        target: process.env.DEV_API_PROXY_TARGET ?? "http://localhost:8081",
        changeOrigin: true,
      },
    },
  },
  preview: {
    host: "0.0.0.0",
    port: 4173,
  },
});
