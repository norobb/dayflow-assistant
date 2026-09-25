import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import {defineConfig} from 'vite';

export default defineConfig(() => {
  return {
    // Base URL for deployment.
    // Local development: defaults to '/' (no env var needed).
    // GitHub Pages (project site): set VITE_BASE_URL=/dayflow-assistant/ in CI.
    // Custom domain: set VITE_BASE_URL=/ in CI (or remove the env var entirely).
    base: process.env.VITE_BASE_URL ?? '/',
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        // Use import.meta.dirname (ESM-native) instead of __dirname.
        '@': import.meta.dirname,
      },
    },
    server: {
      // HMR is disabled in AI Studio via DISABLE_HMR env var.
      // Do not modify—file watching is disabled to prevent flickering during agent edits.
      hmr: process.env.DISABLE_HMR !== 'true',
      // Disable file watching when DISABLE_HMR is true to save CPU during agent edits.
      watch: process.env.DISABLE_HMR === 'true' ? null : {},
    },
  };
});
