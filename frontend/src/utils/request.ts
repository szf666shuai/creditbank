import axios from 'axios'

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('credit_bank_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message =
      error.response?.data?.message || error.message || '网络请求失败'
    return Promise.reject(new Error(message))
  },
)

const request = {
  get<T>(url: string) {
    return instance.get<unknown, ApiResponse<T>>(url)
  },
  post<T>(url: string, data?: unknown) {
    return instance.post<unknown, ApiResponse<T>>(url, data)
  },
}

export default request
