import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import PlanAssign from '@/views/internship/plan-assign.vue'

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockPut = vi.fn()
const mockDelete = vi.fn()
vi.mock('@/utils/request.js', () => ({
  default: {
    get: (...args) => mockGet(...args),
    post: (...args) => mockPost(...args),
    put: (...args) => mockPut(...args),
    delete: (...args) => mockDelete(...args)
  }
}))

vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessageBox: {
      confirm: vi.fn(() => Promise.resolve())
    },
    ElMessage: {
      success: vi.fn(),
      warning: vi.fn(),
      error: vi.fn()
    }
  }
})

vi.mock('@element-plus/icons-vue', () => ({
  ArrowLeft: { template: '<span />' },
  Search: { template: '<span />' },
  Refresh: { template: '<span />' },
  Plus: { template: '<span />' },
  User: { template: '<span />' },
  Delete: { template: '<span />' }
}))

const elementStubs = {
  'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' },
  'el-card': { template: '<div><slot /><slot name="header" /></div>' },
  'el-descriptions': { template: '<div><slot /></div>' },
  'el-descriptions-item': { template: '<div><slot /></div>' },
  'el-input': { template: '<input />' },
  'el-table': {
    props: ['data'],
    template: '<div class="el-table"><slot /></div>'
  },
  'el-table-column': {
    template: '<div />'
  },
  'el-pagination': { template: '<div />' },
  'el-row': { template: '<div><slot /></div>' },
  'el-col': { template: '<div><slot /></div>' },
  'el-select': { template: '<div><slot /></div>' },
  'el-option': { template: '<div />' },
  'el-dialog': { template: '<div><slot /><slot name="footer" /></div>' },
  'el-alert': { template: '<div />' },
  'el-form': { template: '<div><slot /></div>' },
  'el-form-item': { template: '<div><slot /></div>' },
  'el-tag': { template: '<span><slot /></span>' }
}

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/internship/plan', name: 'InternshipPlan', component: { template: '<div/>' } },
      { path: '/internship/plan-assign/:id', name: 'PlanAssign', component: PlanAssign }
    ]
  })
}

describe('plan-assign师生分配子页面', () => {
  let router
  let pinia

  beforeEach(async () => {
    vi.clearAllMocks()
    pinia = createPinia()
    setActivePinia(pinia)
    router = createTestRouter()
    router.push('/internship/plan-assign/1')
    await router.isReady()

    mockGet.mockImplementation((url) => {
      if (url.includes('/internship/plans/1') && !url.includes('/students')) {
        return Promise.resolve({
          data: {
            id: 1,
            planName: '2024春季实习计划',
            semesterId: 1,
            majorId: 1,
            gradeId: 1,
            startDate: '2024-03-01',
            endDate: '2024-06-30'
          }
        })
      }
      if (url.includes('/internship/plans/1/students')) {
        return Promise.resolve({
          data: {
            records: [
              { id: 1, planId: 1, studentId: 101, teacherId: 201, studentName: '张三', studentNo: '2024001', majorName: '计算机科学与技术', className: '计科2401', teacherName: '李老师', teacherNo: 'T001' },
              { id: 2, planId: 1, studentId: 102, teacherId: null, studentName: '王五', studentNo: '2024002', majorName: '计算机科学与技术', className: '计科2401', teacherName: null, teacherNo: null }
            ],
            total: 2
          }
        })
      }
      if (url.includes('/admin/students')) {
        return Promise.resolve({
          data: { records: [{ id: 103, name: '赵六', studentNo: '2024003' }], total: 1 }
        })
      }
      if (url.includes('/admin/teachers')) {
        return Promise.resolve({
          data: { records: [{ id: 201, name: '李老师', teacherNo: 'T001', deptId: 1 }], total: 1 }
        })
      }
      if (url.includes('/admin/classes')) {
        return Promise.resolve({
          data: { records: [{ id: 10, className: '计科2401', majorId: 1, gradeId: 1 }], total: 1 }
        })
      }
      if (url.includes('/admin/semesters')) {
        return Promise.resolve({
          data: { semesterName: '2024春季学期' }
        })
      }
      if (url.includes('/admin/majors')) {
        return Promise.resolve({
          data: { majorName: '计算机科学与技术' }
        })
      }
      if (url.includes('/admin/grades')) {
        return Promise.resolve({
          data: { gradeName: '2024级' }
        })
      }
      return Promise.resolve({ data: {} })
    })
  })

  it('页面加载时请求计划详情和学生列表', async () => {
    mount(PlanAssign, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    expect(mockGet).toHaveBeenCalledWith('/internship/plans/1')
    expect(mockGet).toHaveBeenCalledWith('/internship/plans/1/students', expect.objectContaining({
      params: expect.objectContaining({ current: 1, size: 10 })
    }))
  })

  it('点击返回按钮跳转到实习计划列表页', async () => {
    const pushSpy = vi.spyOn(router, 'push')
    const wrapper = mount(PlanAssign, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.goBack()
    expect(pushSpy).toHaveBeenCalledWith('/internship/plan')
  })

  it('移除学生分配调用DELETE接口', async () => {
    mockDelete.mockResolvedValue({ code: 200, message: '成功' })

    const wrapper = mount(PlanAssign, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    await vm.handleRemove({ studentId: 101, studentName: '张三' })

    expect(mockDelete).toHaveBeenCalledWith('/internship/plans/1/students/101')
  })

  it('批量移除学生调用DELETE接口', async () => {
    mockDelete.mockResolvedValue({ code: 200, message: '成功' })

    const wrapper = mount(PlanAssign, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.selectedStudents = [
      { studentId: 101, studentName: '张三' },
      { studentId: 102, studentName: '王五' }
    ]
    await vm.handleBatchRemove()
    await vi.dynamicImportSettled()

    expect(mockDelete).toHaveBeenCalledWith('/internship/plans/1/students/101')
    expect(mockDelete).toHaveBeenCalledWith('/internship/plans/1/students/102')
  })

  it('确认纳入学生调用POST接口', async () => {
    mockPost.mockResolvedValue({ code: 200, message: '成功' })

    const wrapper = mount(PlanAssign, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.addStudentSelected = [{ id: 103, name: '赵六', studentNo: '2024003' }]
    await vm.confirmAddStudents()

    expect(mockPost).toHaveBeenCalledWith('/internship/plans/1/students', {
      studentIds: [103]
    })
  })

  it('确认分配教师调用PUT接口', async () => {
    mockPut.mockResolvedValue({ code: 200, message: '成功' })

    const wrapper = mount(PlanAssign, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.assignTeacherId = 201
    vm.selectedStudents = [{ studentId: 101, studentName: '张三' }]
    vm.assignMode = 'batch'
    await vm.confirmAssignTeacher()

    expect(mockPut).toHaveBeenCalledWith('/internship/plans/1/students/assign-teacher', {
      studentIds: [101],
      teacherId: 201
    })
  })

  it('批量导入未选择文件时提示警告', async () => {
    const wrapper = mount(PlanAssign, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.addStudentSelected = []
    await vm.confirmAddStudents()

    expect(mockPost).not.toHaveBeenCalled()
  })

  it('重置筛选清空筛选条件', async () => {
    const wrapper = mount(PlanAssign, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.queryForm.className = '计科2401'
    vm.queryForm.teacherName = '李老师'
    vm.queryForm.assigned = '1'
    vm.handleReset()

    expect(vm.queryForm.className).toBe('')
    expect(vm.queryForm.teacherName).toBe('')
    expect(vm.queryForm.assigned).toBe('')
  })

  it('onBeforeUnmount关闭对话框', async () => {
    const wrapper = mount(PlanAssign, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.addStudentDialogVisible = true
    vm.assignTeacherDialogVisible = true
    wrapper.unmount()

    expect(vm.addStudentDialogVisible).toBe(false)
    expect(vm.assignTeacherDialogVisible).toBe(false)
  })
})
