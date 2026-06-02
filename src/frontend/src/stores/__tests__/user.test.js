/**
 * 用户Store单元测试
 * 测试用户状态管理的所有核心功能：Token管理、用户信息、角色判断、登录态处理
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '../user.js'

vi.mock('@/utils/request.js', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    post: vi.fn()
  }
}))

describe('用户Store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  describe('初始状态', () => {
    it('初始状态token为空', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      expect(store.token).toBe('')
    })

    it('初始状态userInfo为空对象', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      expect(store.userInfo).toEqual({})
    })

    it('初始状态roleCode为空', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      expect(store.roleCode).toBe('')
    })

    it('初始状态roles为空数组', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      expect(store.roles).toEqual([])
    })
  })

  describe('setToken', () => {
    it('setToken设置Token并持久化到localStorage', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      store.setToken('test-jwt-token')
      expect(store.token).toBe('test-jwt-token')
      expect(localStorage.getItem('token')).toBe('test-jwt-token')
    })
  })

  describe('setUserInfo', () => {
    it('setUserInfo设置用户信息', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      /** 测试用户信息 */
      const info = { userId: 1, username: 'zhangsan', realName: '学生测试账号' }
      store.setUserInfo(info)
      expect(store.userInfo).toEqual(info)
      expect(localStorage.getItem('userInfo')).toBe(JSON.stringify(info))
    })
  })

  describe('setRoleCode', () => {
    it('setRoleCode设置角色编码', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      store.setRoleCode('ADMIN')
      expect(store.roleCode).toBe('ADMIN')
      expect(localStorage.getItem('roleCode')).toBe('ADMIN')
    })
  })

  describe('setRoles', () => {
    it('setRoles设置角色列表', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      /** 测试角色列表 */
      const roleList = [
        { roleCode: 'ADMIN', roleName: '校级管理员' },
        { roleCode: 'TEACHER', roleName: '实习指导教师' }
      ]
      store.setRoles(roleList)
      expect(store.roles).toEqual(roleList)
      expect(localStorage.getItem('roles')).toBe(JSON.stringify(roleList))
    })
  })

  describe('handleLoginData', () => {
    it('handleLoginData处理登录响应数据', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      /** 登录响应数据 */
      const loginData = {
        token: 'login-token-123',
        userId: 1,
        username: 'zhangsan',
        realName: '学生测试账号',
        phone: '13800138000',
        email: 'zhangsan@test.com',
        currentRoleCode: 'STUDENT',
        roles: [
          { roleCode: 'STUDENT', roleName: '学生' },
          { roleCode: 'TEACHER', roleName: '实习指导教师' }
        ]
      }
      store.handleLoginData(loginData)
      expect(store.token).toBe('login-token-123')
      expect(store.userInfo).toEqual({
        userId: 1,
        username: 'zhangsan',
        realName: '学生测试账号',
        phone: '13800138000',
        email: 'zhangsan@test.com'
      })
      expect(store.roleCode).toBe('STUDENT')
      expect(store.roles).toEqual(loginData.roles)
      expect(localStorage.getItem('token')).toBe('login-token-123')
      expect(localStorage.getItem('roleCode')).toBe('STUDENT')
    })

    it('handleLoginData处理无roles字段时默认为空数组', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      /** 无roles字段的登录数据 */
      const loginData = {
        token: 'token-abc',
        userId: 2,
        username: 'lisi',
        realName: '李四',
        phone: '',
        email: '',
        currentRoleCode: 'TEACHER'
      }
      store.handleLoginData(loginData)
      expect(store.roles).toEqual([])
    })
  })

  describe('logout', () => {
    it('logout清除所有登录态', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      store.setToken('some-token')
      store.setUserInfo({ userId: 1, username: 'test' })
      store.setRoleCode('ADMIN')
      store.setRoles([{ roleCode: 'ADMIN', roleName: '校级管理员' }])

      store.logout()

      expect(store.token).toBe('')
      expect(store.userInfo).toEqual({})
      expect(store.roleCode).toBe('')
      expect(store.roles).toEqual([])
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
      expect(localStorage.getItem('roleCode')).toBeNull()
      expect(localStorage.getItem('roles')).toBeNull()
    })
  })

  describe('角色判断方法', () => {
    it('isAdmin判断校级管理员', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      store.setRoleCode('ADMIN')
      expect(store.isAdmin()).toBe(true)
      store.setRoleCode('TEACHER')
      expect(store.isAdmin()).toBe(false)
    })

    it('isDeptAdmin判断院系管理员', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      store.setRoleCode('DEPT_ADMIN')
      expect(store.isDeptAdmin()).toBe(true)
      store.setRoleCode('ADMIN')
      expect(store.isDeptAdmin()).toBe(false)
    })

    it('isMajorDirector判断专业负责人', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      store.setRoleCode('MAJOR_DIRECTOR')
      expect(store.isMajorDirector()).toBe(true)
      store.setRoleCode('ADMIN')
      expect(store.isMajorDirector()).toBe(false)
    })

    it('isTeacher判断实习指导教师', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      store.setRoleCode('TEACHER')
      expect(store.isTeacher()).toBe(true)
      store.setRoleCode('STUDENT')
      expect(store.isTeacher()).toBe(false)
    })

    it('isStudent判断学生', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      store.setRoleCode('STUDENT')
      expect(store.isStudent()).toBe(true)
      store.setRoleCode('TEACHER')
      expect(store.isStudent()).toBe(false)
    })
  })

  describe('roleLabel计算属性', () => {
    it('roleLabel计算属性返回正确中文标签', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()

      store.setRoleCode('ADMIN')
      expect(store.roleLabel).toBe('校级管理员')

      store.setRoleCode('DEPT_ADMIN')
      expect(store.roleLabel).toBe('院系管理员')

      store.setRoleCode('MAJOR_DIRECTOR')
      expect(store.roleLabel).toBe('专业负责人')

      store.setRoleCode('TEACHER')
      expect(store.roleLabel).toBe('实习指导教师')

      store.setRoleCode('STUDENT')
      expect(store.roleLabel).toBe('学生')
    })

    it('roleLabel未知角色返回未知角色', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      store.setRoleCode('UNKNOWN')
      expect(store.roleLabel).toBe('未知角色')
    })
  })

  describe('hasMultipleRoles计算属性', () => {
    it('hasMultipleRoles判断是否多角色', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()

      store.setRoles([{ roleCode: 'STUDENT', roleName: '学生' }])
      expect(store.hasMultipleRoles).toBe(false)

      store.setRoles([
        { roleCode: 'STUDENT', roleName: '学生' },
        { roleCode: 'TEACHER', roleName: '实习指导教师' }
      ])
      expect(store.hasMultipleRoles).toBe(true)
    })

    it('hasMultipleRoles空角色列表返回false', () => {
      /** 获取用户Store实例 */
      const store = useUserStore()
      store.setRoles([])
      expect(store.hasMultipleRoles).toBe(false)
    })
  })
})
