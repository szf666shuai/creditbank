import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import basicSsl from '@vitejs/plugin-basic-ssl'
import { resolve } from 'path'

// 本地默认 HTTP；云服务器由 deploy.sh 设置 VITE_HTTPS=1 启用自签 HTTPS
const useHttps = process.env.VITE_HTTPS === '1'

export default defineConfig({
  plugins: [vue(), ...(useHttps ? [basicSsl()] : [])],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    host: true,
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
