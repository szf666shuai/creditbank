import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import basicSsl from '@vitejs/plugin-basic-ssl'
import { resolve } from 'path'

const lanHttps = process.env.npm_lifecycle_event === 'dev:lan'

export default defineConfig({
  plugins: [vue(), ...(lanHttps ? [basicSsl()] : [])],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    host: lanHttps,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/videos': {
        target: 'http://localhost:80',
        changeOrigin: true,
      },
      '/materials': {
        target: 'http://localhost:80',
        changeOrigin: true,
      },
    },
  },
})
