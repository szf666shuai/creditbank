import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import basicSsl from '@vitejs/plugin-basic-ssl'
import { resolve } from 'path'

// 开发服务器默认启用 HTTPS（自签证书），便于局域网 / 云服务器使用摄像头、麦克风
const useHttps = process.env.VITE_HTTP !== '1'

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
