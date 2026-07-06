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

instance.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error),
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
