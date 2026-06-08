/**
 * 用户状态管理Store
 * 管理用户登录态、Token、用户信息、角色编码等全局状态
 * 数据持久化到localStorage，页面刷新后自动恢复
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request.js'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const roleCode = ref(localStorage.getItem('roleCode') || '')
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))

  const roleName = computed(() => {
    const current = roles.value.find(r => r.roleCode === roleCode.value)
    return current ? current.roleName : ''
  })

  const roleLabels = {
    // 与数据库 users.role 字段值完全匹配
    college_admin: '院级管理员',
    major_admin: '专业负责人',
    teacher: '指导教师',
    student: '学生'
  }

  const roleLabel = computed(() => roleLabels[roleCode.value] || '未知角色')
  const hasMultipleRoles = computed(() => roles.value.length > 1)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function setRoleCode(code) {
    roleCode.value = code
    localStorage.setItem('roleCode', code)
  }

  function setRoles(roleList) {
    roles.value = roleList
    localStorage.setItem('roles', JSON.stringify(roleList))
  }

  function handleLoginData(data) {
    setToken(data.token)
    setUserInfo({
      userId: data.userId,
      username: data.username,
      realName: data.realName || data.username,
      role: data.role,
      phone: data.phone || '',
      email: data.email || '',
      deptId: data.deptId || null,
      // 【新增】完整用户信息字段
      collegeId: data.collegeId || null,
      majorId: data.majorId || null,
      className: data.className || '',
      collegeName: data.collegeName || '',  // 学院名称
      majorName: data.majorName || ''       // 专业名称
    })
    setRoleCode(data.role)
    
    if (data.availableRoles && data.availableRoles.length > 0) {
      setRoles(data.availableRoles.map(r => ({
        roleId: r.roleId,
        roleCode: r.roleCode,
        roleName: r.roleName
      })))
    } else if (data.role) {
      setRoles([{ roleId: data.currentRoleId, roleCode: data.role, roleName: roleLabels[data.role] || data.role }])
    }
  }

  async function login(loginData) {
    const res = await request.post('/auth/login', loginData)
    handleLoginData(res.data)
  }

  async function switchRole(roleId) {
    const res = await request.post('/auth/switch-role', null, { params: { roleId } })
    handleLoginData(res.data)
  }

  async function fetchUserInfo() {
    const res = await request.get('/auth/me')
    const data = res.data
    setUserInfo({
      userId: data.userId,
      username: data.username,
      realName: data.realName || data.username,
      role: data.role,
      phone: data.phone || '',
      email: data.email || '',
      deptId: data.deptId || null,
      // 【新增】完整用户信息字段
      collegeId: data.collegeId || null,
      majorId: data.majorId || null,
      className: data.className || '',
      collegeName: data.collegeName || '',  // 学院名称
      majorName: data.majorName || ''       // 专业名称
    })
    setRoleCode(data.role)
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    roleCode.value = ''
    roles.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('roleCode')
    localStorage.removeItem('roles')
  }

  function isAdmin() {
    return roleCode.value === 'ADMIN'
  }

  function isDeptAdmin() {
    return roleCode.value === 'DEPT_ADMIN'
  }

  function isMajorDirector() {
    return roleCode.value === 'MAJOR_DIRECTOR'
  }

  function isTeacher() {
    return roleCode.value === 'TEACHER'
  }

  function isStudent() {
    return roleCode.value === 'STUDENT'
  }

  return {
    token,
    userInfo,
    roleCode,
    roles,
    roleName,
    roleLabel,
    hasMultipleRoles,
    setToken,
    setUserInfo,
    setRoleCode,
    setRoles,
    handleLoginData,
    login,
    switchRole,
    fetchUserInfo,
    logout,
    isAdmin,
    isDeptAdmin,
    isMajorDirector,
    isTeacher,
    isStudent
  }
})