/**
 * Axios请求封装
 * 统一配置请求基础路径、超时时间、请求/响应拦截器
 * 自动注入JWT Token，处理401未认证跳转、业务错误提示
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/** Axios实例，配置基础路径和超时时间 */
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 * 自动从localStorage读取Token并注入到请求头Authorization字段
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 1. 业务错误（code !== 200）：弹出错误提示，401/Token过期则清除登录态并跳转登录页
 * 2. HTTP错误：根据状态码提示对应错误信息，401跳转登录页
 */
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401 || res.code === 4011 || res.code === 4012) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('roleCode')
        localStorage.removeItem('roles')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    return Promise.reject(error)
  }
)

/** 导出Axios请求实例 */
export default request
