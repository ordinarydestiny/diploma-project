/**
 * Axios请求封装单元测试
 * 测试请求实例配置、请求拦截器Token注入、响应拦截器错误处理和401跳转
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'

/** 路由跳转Mock函数 */
const mockPush = vi.fn()
vi.mock('@/router', () => ({
  default: { push: mockPush }
}))

vi.mock('element-plus', () => ({
  ElMessage: { error: vi.fn() }
}))

describe('Axios请求封装', () => {
  /** 请求实例 */
  let request

  beforeEach(async () => {
    vi.resetModules()
    localStorage.clear()
    mockPush.mockClear()
    /** 获取ElMessage Mock并清除调用记录 */
    const { ElMessage } = await import('element-plus')
    ElMessage.error.mockClear()

    /** 动态导入请求模块获取新实例 */
    const mod = await import('@/utils/request.js')
    request = mod.default
  })

  describe('实例配置', () => {
    it('创建axios实例配置正确（baseURL、timeout）', () => {
      expect(request.defaults.baseURL).toBe('/api/v1')
      expect(request.defaults.timeout).toBe(30000)
    })
  })

  describe('请求拦截器', () => {
    it('请求拦截器注入Token到Authorization头', async () => {
      localStorage.setItem('token', 'my-jwt-token')
      vi.resetModules()
      /** 动态导入获取新请求实例 */
      const mod = await import('@/utils/request.js')
      /** 新创建的请求实例 */
      const freshRequest = mod.default

      /** 请求配置对象 */
      const config = { headers: {} }
      /** 拦截器处理后的配置 */
      const result = freshRequest.interceptors.request.handlers[0].fulfilled(config)
      expect(result.headers.Authorization).toBe('Bearer my-jwt-token')
    })

    it('请求拦截器无Token时不设置Authorization头', async () => {
      localStorage.removeItem('token')
      vi.resetModules()
      /** 动态导入获取新请求实例 */
      const mod = await import('@/utils/request.js')
      /** 新创建的请求实例 */
      const freshRequest = mod.default

      /** 请求配置对象 */
      const config = { headers: {} }
      /** 拦截器处理后的配置 */
      const result = freshRequest.interceptors.request.handlers[0].fulfilled(config)
      expect(result.headers.Authorization).toBeUndefined()
    })
  })

  describe('响应拦截器', () => {
    it('响应拦截器code为200时返回data', async () => {
      /** 获取ElMessage Mock */
      const { ElMessage } = await import('element-plus')
      /** 模拟成功响应数据 */
      const response = {
        data: { code: 200, message: '成功', data: { id: 1 } }
      }
      /** 响应拦截器成功处理函数 */
      const handler = request.interceptors.response.handlers[0].fulfilled
      /** 拦截器处理结果 */
      const result = await handler(response)
      expect(result).toEqual({ code: 200, message: '成功', data: { id: 1 } })
      expect(ElMessage.error).not.toHaveBeenCalled()
    })

    it('响应拦截器code非200时弹出错误提示', async () => {
      /** 获取ElMessage Mock */
      const { ElMessage } = await import('element-plus')
      /** 模拟业务错误响应 */
      const response = {
        data: { code: 500, message: '服务器内部错误' }
      }
      /** 响应拦截器成功处理函数 */
      const handler = request.interceptors.response.handlers[0].fulfilled
      await expect(handler(response)).rejects.toThrow('服务器内部错误')
      expect(ElMessage.error).toHaveBeenCalledWith('服务器内部错误')
    })

    it('响应拦截器401状态码清除登录态并跳转登录页', async () => {
      /** 获取ElMessage Mock */
      const { ElMessage } = await import('element-plus')
      localStorage.setItem('token', 'old-token')
      localStorage.setItem('userInfo', '{}')
      localStorage.setItem('roleCode', 'ADMIN')
      localStorage.setItem('roles', '[]')

      /** 模拟401错误对象 */
      const error = {
        response: { status: 401, data: { message: '未认证' } }
      }
      /** 响应拦截器错误处理函数 */
      const handler = request.interceptors.response.handlers[0].rejected
      await expect(handler(error)).rejects.toBe(error)
      expect(ElMessage.error).toHaveBeenCalledWith('登录已过期，请重新登录')
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
      expect(localStorage.getItem('roleCode')).toBeNull()
      expect(localStorage.getItem('roles')).toBeNull()
      expect(mockPush).toHaveBeenCalledWith('/login')
    })
  })
})
