import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import AssessmentInner from '@/views/internship/assessment-inner.vue'

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

const elementStubs = {
  'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' },
  'el-card': { template: '<div><slot /><slot name="header" /></div>' },
  'el-input': { template: '<input />' },
  'el-table': { template: '<div />' },
  'el-table-column': { template: '<div />' },
  'el-pagination': { template: '<div />' },
  'el-select': { template: '<div><slot /></div>' },
  'el-option': { template: '<div />' },
  'el-form': { template: '<div><slot /></div>' },
  'el-form-item': { template: '<div><slot /></div>' },
  'el-tag': { template: '<span><slot /></span>' },
  'el-dialog': { template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>', props: ['modelValue'] },
  'el-input-number': { template: '<input type="number" />' },
  'el-rate': { template: '<div />' },
  'el-descriptions': { template: '<div><slot /></div>' },
  'el-descriptions-item': { template: '<div><slot /></div>' },
  'el-radio-group': { template: '<div><slot /></div>' },
  'el-radio': { template: '<div />' },
  'el-timeline': { template: '<div><slot /></div>' },
  'el-timeline-item': { template: '<div><slot /></div>' },
  'el-alert': { template: '<div />' }
}

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/internship/assessment-inner', name: 'AssessmentInner', component: AssessmentInner }
    ]
  })
}

describe('assessment-inner校内考核表页面', () => {
  let router
  let pinia

  beforeEach(async () => {
    vi.clearAllMocks()
    localStorage.clear()
    pinia = createPinia()
    setActivePinia(pinia)
    router = createTestRouter()
    router.push('/internship/assessment-inner')
    await router.isReady()

    mockGet.mockImplementation((url) => {
      if (url.includes('/internship/inner-assessments')) {
        return Promise.resolve({
          data: {
            records: [
              { id: 1, studentId: 101, planId: 201, selfSummary: '自我总结内容测试', approvalStatus: 0, gradeLevel: null, teacherComment: null }
            ],
            total: 1
          }
        })
      }
      if (url.includes('/internship/plans')) {
        return Promise.resolve({
          data: {
            records: [
              { id: 201, planName: '2025年春季实习计划' },
              { id: 202, planName: '2025年秋季实习计划' }
            ]
          }
        })
      }
      if (url.includes('/messages/reject-records/by-target')) {
        return Promise.resolve({
          data: [
            { reason: '自我总结不够详细', reviewer: '张老师', rejectTime: '2025-05-01 10:00:00' },
            { reason: '请补充实习收获', reviewer: '李老师', rejectTime: '2025-05-10 14:30:00' }
          ]
        })
      }
      return Promise.resolve({ data: {} })
    })
  })

  it('管理端页面加载时请求考核表列表', async () => {
    mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    expect(mockGet).toHaveBeenCalledWith(expect.stringContaining('/internship/inner-assessments'), expect.any(Object))
  })

  it('学生端页面加载时请求实习计划列表', async () => {
    localStorage.setItem('roleCode', 'STUDENT')

    mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    expect(mockGet).toHaveBeenCalledWith(expect.stringContaining('/internship/plans'), expect.any(Object))
  })

  it('statusTagType返回正确的标签类型', async () => {
    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    expect(vm.statusTagType(0)).toBe('warning')
    expect(vm.statusTagType(1)).toBe('success')
    expect(vm.statusTagType(2)).toBe('danger')
    expect(vm.statusTagType(4)).toBe('info')
    expect(vm.statusTagType(null)).toBe('info')
  })

  it('statusLabel返回正确的中文标签', async () => {
    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    expect(vm.statusLabel(0)).toBe('待审')
    expect(vm.statusLabel(1)).toBe('通过')
    expect(vm.statusLabel(2)).toBe('驳回')
    expect(vm.statusLabel(4)).toBe('已撤回')
    expect(vm.statusLabel(null)).toBe('未提交')
  })

  it('新增考核表调用POST接口', async () => {
    mockPost.mockResolvedValue({ code: 200, message: '成功' })

    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.studentForm.planId = 201
    vm.studentForm.selfSummary = '测试自我总结'
    vm.studentForm.id = null
    vm.studentFormRef = {
      validate: () => Promise.resolve(true)
    }

    await vm.handleStudentSubmit()

    expect(mockPost).toHaveBeenCalledWith('/internship/inner-assessments', {
      planId: 201,
      selfSummary: '测试自我总结'
    })
  })

  it('编辑考核表调用PUT接口', async () => {
    mockPut.mockResolvedValue({ code: 200, message: '成功' })

    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.studentForm.id = 1
    vm.studentForm.planId = 201
    vm.studentForm.selfSummary = '更新后的总结'
    vm.studentFormRef = {
      validate: () => Promise.resolve(true)
    }

    await vm.handleStudentSubmit()

    expect(mockPut).toHaveBeenCalledWith('/internship/inner-assessments/1', {
      planId: 201,
      selfSummary: '更新后的总结'
    })
  })

  it('教师审核调用PUT接口', async () => {
    mockPut.mockResolvedValue({ code: 200, message: '成功' })

    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.reviewData = { id: 1 }
    vm.reviewFormData = {
      gradeLevel: 'GOOD',
      teacherComment: '表现优秀',
      approvalStatus: 1,
      rejectReason: ''
    }
    vm.reviewFormRef = {
      validate: () => Promise.resolve(true)
    }

    await vm.handleReviewSubmit()

    expect(mockPut).toHaveBeenCalledWith('/internship/inner-assessments/1/review', {
      gradeLevel: 'GOOD',
      teacherComment: '表现优秀',
      approvalStatus: 1,
      rejectReason: ''
    })
  })

  it('查看详情时请求驳回记录', async () => {
    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.handleView({ id: 1, selfSummary: '测试', approvalStatus: 2 })

    await vi.dynamicImportSettled()

    expect(mockGet).toHaveBeenCalledWith('/messages/reject-records/by-target', expect.objectContaining({
      params: expect.objectContaining({
        targetType: 'INNER_ASSESSMENT',
        targetId: 1
      })
    }))
  })

  it('fetchViewRejectRecords正确解析驳回记录', async () => {
    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    await vm.fetchViewRejectRecords(1)

    expect(vm.viewRejectRecords).toHaveLength(2)
    expect(vm.viewRejectRecords[0].reason).toBe('自我总结不够详细')
    expect(vm.viewRejectRecords[1].reviewer).toBe('李老师')
  })

  it('fetchViewRejectRecords失败时清空驳回记录', async () => {
    mockGet.mockImplementation((url) => {
      if (url.includes('/messages/reject-records/by-target')) {
        return Promise.reject(new Error('网络错误'))
      }
      if (url.includes('/internship/inner-assessments')) {
        return Promise.resolve({ data: { records: [], total: 0 } })
      }
      return Promise.resolve({ data: {} })
    })

    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    await vm.fetchViewRejectRecords(999)

    expect(vm.viewRejectRecords).toEqual([])
  })

  it('isFormReadonly根据状态返回正确的只读状态', async () => {
    localStorage.setItem('roleCode', 'STUDENT')

    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm

    vm.studentForm.approvalStatus = null
    expect(vm.isFormReadonly).toBe(false)

    vm.studentForm.approvalStatus = 0
    expect(vm.isFormReadonly).toBe(true)

    vm.studentForm.approvalStatus = 1
    expect(vm.isFormReadonly).toBe(true)

    vm.studentForm.approvalStatus = 2
    vm.isEditMode = false
    expect(vm.isFormReadonly).toBe(true)
    vm.isEditMode = true
    expect(vm.isFormReadonly).toBe(false)

    vm.studentForm.approvalStatus = 4
    expect(vm.isFormReadonly).toBe(false)

    localStorage.removeItem('roleCode')
  })

  it('canReview教师端可批阅', async () => {
    localStorage.setItem('roleCode', 'TEACHER')

    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    expect(vm.canReview({ approvalStatus: 0 })).toBe(true)
    expect(vm.canReview({ approvalStatus: 4 })).toBe(false)

    localStorage.removeItem('roleCode')
  })

  it('loadPlanOptions正确解析计划列表', async () => {
    localStorage.setItem('roleCode', 'STUDENT')

    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    await vm.loadPlanOptions()

    expect(vm.planOptions).toHaveLength(2)
    expect(vm.planOptions[0].planName).toBe('2025年春季实习计划')

    localStorage.removeItem('roleCode')
  })

  it('handleEnterEditMode设置isEditMode为true', async () => {
    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.isEditMode = false
    vm.handleEnterEditMode()
    expect(vm.isEditMode).toBe(true)
  })

  it('handleView初始化驳回记录并打开对话框', async () => {
    const wrapper = mount(AssessmentInner, {
      global: {
        plugins: [router, pinia],
        stubs: elementStubs
      }
    })

    await vi.dynamicImportSettled()

    const vm = wrapper.vm
    vm.handleView({ id: 1, selfSummary: '测试', approvalStatus: 0 })

    expect(vm.viewData.selfSummary).toBe('测试')
    expect(vm.viewRejectRecords).toEqual([])
    expect(vm.viewVisible).toBe(true)
  })
})
