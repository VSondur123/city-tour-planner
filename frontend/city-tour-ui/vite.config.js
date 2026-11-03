import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Later: forwards /api requests to the Java backend (default Spring Boot port)
    proxy: { '/api': 'http://localhost:8080' },
  },
})