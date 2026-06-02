<template>
  <div class="page-container">
    <template v-if="userStore.isStudent()">
      <el-card>
        <el-form
          ref="studentFormRef"
          :model="studentForm"
          :rules="studentFormRules"
          label-width="120px"
          :disabled="isFormReadonly"
        >
          <el-form-item label="实习计划" prop="planId">
            <el-select
              v-model="studentForm.planId"
              placeholder="请选择实习计划"
              style="width: 100%"
              @change="handlePlanChange"
            >
              <el-option
                v-for="item in planOptions"
                :key="item.id"
                :label="item.planName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="审批状态">
            <el-tag :type="statusTagType(studentForm.approvalStatus)">
              {{ statusLabel(studentForm.approvalStatus) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="自我总结" prop="selfSummary">
            <el-input
              v-model="studentForm.selfSummary"
              type="textarea"
              :rows="6"
              placeholder="请输入实习期间的自我总结，包括工作内容、收获、不足等"
            />
          </el-form-item>
          <template v-if="studentForm.gradeLevel || studentForm.teacherComment">
            <div class="section-title">教师审核意见</div>
            <el-form-item label="教师评分">
              <el-tag v-if="studentForm.gradeLevel" :type="getGradeType(studentForm.gradeLevel)">{{ getGradeText(studentForm.gradeLevel) }}</el-tag>
              <span v-else>-</span>
            </el-form-item>
            <el-form-item label="教师意见">
              <span>{{ studentForm.teacherComment || '-' }}</span>
            </el-form-item>
          </template>
        </el-form>
        <template v-if="rejectRecords.length > 0">
          <div class="section-title">驳回记录</div>
          <el-timeline class="reject-timeline">
            <el-timeline-item
              v-for="(record, index) in rejectRecords"
              :key="index"
              type="danger"
              :timestamp="record.rejectTime"
              placement="top"
            >
              <div class="reject-item">
                <div class="reject-reason">{{ record.reason }}</div>
                <div class="reject-meta">审批人：{{ record.reviewer || '-' }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </template>
        <div v-if="studentForm.planId" class="form-footer">
          <template v-if="studentForm.approvalStatus == null">
            <el-button
              type="primary"
              :loading="submitLoading"
              @click="handleStudentSubmit"
            >
              提交
            </el-button>
          </template>
          <template v-else-if="studentForm.approvalStatus === 0">
            <el-button disabled>
              待审核中，无法修改
            </el-button>
          </template>
          <template v-else-if="studentForm.approvalStatus === 2">
            <template v-if="!isEditMode">
              <el-button
                type="primary"
                @click="handleEnterEditMode"
              >
                修改
              </el-button>
            </template>
            <template v-else>
              <el-button
                type="primary"
                :loading="submitLoading"
                @click="handleStudentSubmit"
              >
                重新提交
              </el-button>
              <el-button @click="handleCancelEdit">
                取消修改
              </el-button>
            </template>
          </template>
          <template v-else-if="studentForm.approvalStatus === 1">
            <el-button disabled>
              审核已通过
            </el-button>
          </template>
          <template v-else-if="studentForm.approvalStatus === 4">
            <el-button
              type="primary"
              :loading="submitLoading"
              @click="handleStudentSubmit"
            >
              重新提交
            </el-button>
          </template>
        </div>
      </el-card>
    </template>

    <template v-else>
      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">校内考核表</span>
          </div>
        </template>

        <el-form :model="queryForm" inline class="search-form">
          <el-form-item label="学生姓名">
            <el-input v-model="queryForm.studentName" placeholder="请输入学生姓名" clearable />
          </el-form-item>
          <el-form-item label="审批状态">
            <el-select v-model="queryForm.approvalStatus" placeholder="全部" clearable style="width: 120px">
              <el-option label="待审" :value="0" />
              <el-option label="通过" :value="1" />
              <el-option label="驳回" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchData">搜索</el-button>
            <el-button @click="resetQuery">重置</el-button>
            <el-button type="success" :disabled="!selectedRows.length" @click="handleBatchReview">批量批阅</el-button>
          </el-form-item>
        </el-form>

        <el-table ref="tableRef" :data="tableData" border stripe v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="45" />
          <el-table-column prop="studentName" label="学生姓名" min-width="100" />
          <el-table-column prop="studentNo" label="学号" min-width="120" />
          <el-table-column prop="selfSummary" label="自我总结" min-width="200" show-overflow-tooltip />
          <el-table-column prop="gradeLevel" label="教师评分" min-width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.gradeLevel" :type="getGradeType(row.gradeLevel)">{{ getGradeText(row.gradeLevel) }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="teacherComment" label="教师意见" min-width="150" show-overflow-tooltip />
          <el-table-column prop="approvalStatus" label="状态" min-width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.approvalStatus)">{{ statusLabel(row.approvalStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" min-width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right" :resizable="false">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
              <el-button
                v-if="canReview(row)"
                link
                type="success"
                size="small"
                @click="handleReview(row)"
              >批阅</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchData"
            @current-change="fetchData"
          />
        </div>
      </el-card>

      <el-dialog v-model="reviewVisible" title="批阅校内考核表" width="600px" destroy-on-close>
        <el-form ref="reviewFormRef" :model="reviewFormData" :rules="reviewRules" label-width="100px">
          <el-descriptions :column="1" border class="mb-4">
            <el-descriptions-item label="学生姓名">{{ reviewData.studentName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ reviewData.studentNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="自我总结" :span="1">{{ reviewData.selfSummary || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-form-item label="评分" prop="gradeLevel">
            <el-select v-model="reviewFormData.gradeLevel" placeholder="请选择等级">
              <el-option label="优秀" value="EXCELLENT" />
              <el-option label="良好" value="GOOD" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="及格" value="PASS" />
              <el-option label="不及格" value="FAIL" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核结果" prop="approvalStatus">
            <el-radio-group v-model="reviewFormData.approvalStatus">
              <el-radio :value="1">通过</el-radio>
              <el-radio :value="2">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="教师意见" prop="teacherComment">
            <el-input v-model="reviewFormData.teacherComment" type="textarea" :rows="3" placeholder="请输入批阅意见" />
          </el-form-item>
          <el-form-item label="驳回原因" prop="rejectReason" v-if="reviewFormData.approvalStatus === 2">
            <el-input v-model="reviewFormData.rejectReason" type="textarea" :rows="2" placeholder="请输入驳回原因" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reviewVisible = false">取消</el-button>
          <el-button type="primary" :loading="reviewLoading" @click="handleReviewSubmit">确认批阅</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="viewVisible" title="考核表详情" width="700px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生姓名">{{ viewData.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ viewData.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="自我总结" :span="2">{{ viewData.selfSummary || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批状态">
            <el-tag :type="statusTagType(viewData.approvalStatus)">{{ statusLabel(viewData.approvalStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDateTime(viewData.createTime) || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title" v-if="viewData.gradeLevel || viewData.teacherComment">教师审核意见</div>
        <el-descriptions :column="2" border v-if="viewData.gradeLevel || viewData.teacherComment">
          <el-descriptions-item label="教师评分">
            <el-tag v-if="viewData.gradeLevel" :type="getGradeType(viewData.gradeLevel)">{{ getGradeText(viewData.gradeLevel) }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="教师意见">{{ viewData.teacherComment || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title" v-if="viewRejectRecords.length > 0">驳回记录</div>
        <el-timeline v-if="viewRejectRecords.length > 0" class="reject-timeline">
          <el-timeline-item
            v-for="(record, index) in viewRejectRecords"
            :key="index"
            type="danger"
            :timestamp="record.rejectTime"
            placement="top"
          >
            <div class="reject-item">
              <div class="reject-reason">{{ record.reason }}</div>
              <div class="reject-meta">审批人：{{ record.reviewer || '-' }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/dateFormat'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { useTableResize } from '@/composables/useTableResize.js'

const userStore = useUserStore()

const tableRef = ref(null)
useTableResize(tableRef)

const STATUS_PENDING = 0
const STATUS_APPROVED = 1
const STATUS_REJECTED = 2
const STATUS_WITHDRAWN = 4

const statusMap = {
  [STATUS_PENDING]: { label: '待审', tagType: 'warning' },
  [STATUS_APPROVED]: { label: '通过', tagType: 'success' },
  [STATUS_REJECTED]: { label: '驳回', tagType: 'danger' },
  [STATUS_WITHDRAWN]: { label: '已撤回', tagType: 'info' }
}

function statusTagType(status) {
  if (status == null) return 'info'
  return statusMap[status]?.tagType || 'info'
}

function statusLabel(status) {
  if (status == null) return '未提交'
  return statusMap[status]?.label || '未知'
}

function getGradeText(grade) {
  const map = { EXCELLENT: '优秀', GOOD: '良好', MEDIUM: '中等', PASS: '及格', FAIL: '不及格' }
  return map[grade] || '-'
}

function getGradeType(grade) {
  const map = { EXCELLENT: 'success', GOOD: '', MEDIUM: 'info', PASS: 'warning', FAIL: 'danger' }
  return map[grade] || ''
}

const planOptions = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const isEditMode = ref(false)

const studentFormRef = ref(null)
const studentForm = reactive({
  id: null,
  planId: null,
  selfSummary: '',
  approvalStatus: null,
  gradeLevel: null,
  teacherComment: ''
})

const rejectRecords = ref([])

const studentFormRules = {
  planId: [{ required: true, message: '请选择实习计划', trigger: 'change' }],
  selfSummary: [{ required: true, message: '请输入自我总结', trigger: 'blur' }]
}

const isFormReadonly = computed(() => {
  if (studentForm.approvalStatus === STATUS_APPROVED) return true
  if (studentForm.approvalStatus === STATUS_PENDING) return true
  if (studentForm.approvalStatus === STATUS_REJECTED) return !isEditMode.value
  return false
})

const queryForm = reactive({
  studentName: '',
  approvalStatus: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const reviewVisible = ref(false)
const viewVisible = ref(false)
const reviewLoading = ref(false)
const viewData = ref({})
const reviewData = ref({})
const viewRejectRecords = ref([])

const reviewFormData = ref({
  gradeLevel: null,
  teacherComment: '',
  approvalStatus: 1,
  rejectReason: ''
})

const reviewRules = {
  gradeLevel: [{ required: true, message: '请选择评分', trigger: 'change' }],
  approvalStatus: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
}

const selectedRows = ref([])

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

async function handleBatchReview() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要批阅的记录')
    return
  }
  const pendingRows = selectedRows.value.filter(r => r.approvalStatus === 0)
  if (!pendingRows.length) {
    ElMessage.warning('所选记录中没有待审的记录')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要批量通过 ${pendingRows.length} 条待审记录吗？`, '批量批阅确认', { type: 'warning' })
    const results = await Promise.allSettled(pendingRows.map(row =>
      request.put(`/internship/inner-assessments/${row.id}/review`, {
        gradeLevel: 'MEDIUM',
        approvalStatus: 1,
        teacherComment: '批量批阅通过'
      })
    ))
    const successCount = results.filter(r => r.status === 'fulfilled').length
    const failCount = results.filter(r => r.status === 'rejected').length
    if (failCount === 0) {
      ElMessage.success(`批量批阅成功，共${successCount}条`)
    } else {
      ElMessage.warning(`批量批阅完成，成功${successCount}条，失败${failCount}条`)
    }
    fetchData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '批量批阅失败')
    }
  }
}

async function loadPlanOptions() {
  try {
    const url = userStore.isStudent() ? '/internship/plans/my-plans' : '/internship/plans'
    const res = await request.get(url, { params: { size: 999 } })
    planOptions.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
    if (userStore.isStudent() && !studentForm.planId && planOptions.value.length > 0) {
      const today = new Date().toISOString().slice(0, 10)
      const ongoing = planOptions.value.find(p => p.startDate <= today && p.endDate >= today)
      studentForm.planId = (ongoing || planOptions.value[0]).id
      loadStudentAssessment()
    }
  } catch (e) {
    console.warn('获取实习计划失败')
  }
}

async function loadStudentAssessment() {
  if (!studentForm.planId) return
  loading.value = true
  isEditMode.value = false
  try {
    const res = await request.get('/internship/inner-assessments/my', {
      params: { planId: studentForm.planId }
    })
    const records = res.data.records || []
    if (records.length > 0) {
      const data = records[0]
      studentForm.id = data.id
      studentForm.selfSummary = data.selfSummary || ''
      studentForm.approvalStatus = data.approvalStatus
      studentForm.gradeLevel = data.gradeLevel
      studentForm.teacherComment = data.teacherComment || ''
      rejectRecords.value = []
      if (data.id) {
        fetchRejectRecords(data.id)
      }
    } else {
      studentForm.id = null
      studentForm.selfSummary = ''
      studentForm.approvalStatus = null
      studentForm.gradeLevel = null
      studentForm.teacherComment = ''
      rejectRecords.value = []
    }
  } catch (e) {
    console.error('获取考核表失败', e.message)
  } finally {
    loading.value = false
  }
}

function handlePlanChange() {
  loadStudentAssessment()
}

async function handleStudentSubmit() {
  const valid = await studentFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = {
      planId: studentForm.planId,
      selfSummary: studentForm.selfSummary
    }
    if (studentForm.id) {
      await request.put(`/internship/inner-assessments/${studentForm.id}`, submitData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/internship/inner-assessments', submitData)
      ElMessage.success('提交成功')
    }
    isEditMode.value = false
    loadStudentAssessment()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

function handleEnterEditMode() {
  isEditMode.value = true
}

async function handleCancelEdit() {
  try {
    await ElMessageBox.confirm(
      '确定要取消修改吗？未保存的更改将丢失。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '继续编辑',
        type: 'warning'
      }
    )
    isEditMode.value = false
    loadStudentAssessment()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('取消修改失败', e.message)
    }
  }
}

async function fetchRejectRecords(targetId) {
  try {
    const res = await request.get('/messages/reject-records/by-target', {
      params: { targetType: 'INNER_ASSESSMENT', targetId }
    })
    rejectRecords.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
  } catch (e) {
    console.warn('获取驳回记录失败', e.message)
    rejectRecords.value = []
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/internship/inner-assessments', {
      params: {
        current: pagination.current,
        size: pagination.size,
        ...queryForm
      }
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {
    console.error('获取数据失败', e.message)
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.studentName = ''
  queryForm.approvalStatus = null
  pagination.current = 1
  fetchData()
}

function handleView(row) {
  viewData.value = { ...row }
  viewRejectRecords.value = []
  viewVisible.value = true
  if (row.id) {
    fetchViewRejectRecords(row.id)
  }
}

async function fetchViewRejectRecords(targetId) {
  try {
    const res = await request.get('/messages/reject-records/by-target', {
      params: { targetType: 'INNER_ASSESSMENT', targetId }
    })
    viewRejectRecords.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
  } catch (e) {
    console.warn('获取驳回记录失败', e.message)
    viewRejectRecords.value = []
  }
}

function handleReview(row) {
  reviewData.value = { ...row }
  reviewFormData.value = {
    gradeLevel: row.gradeLevel,
    teacherComment: row.teacherComment || '',
    approvalStatus: row.approvalStatus || 1,
    rejectReason: row.rejectReason || ''
  }
  reviewVisible.value = true
}

async function handleReviewSubmit() {
  reviewLoading.value = true
  try {
    await request.put(`/internship/inner-assessments/${reviewData.value.id}/review`, reviewFormData.value)
    ElMessage.success('批阅成功')
    reviewVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    reviewLoading.value = false
  }
}

function canReview(row) {
  return row.approvalStatus !== STATUS_WITHDRAWN && (userStore.roleCode === 'TEACHER' || userStore.roleCode === 'ADMIN' || userStore.roleCode === 'DEPT_ADMIN')
}

onMounted(() => {
  loadPlanOptions()
  if (!userStore.isStudent()) {
    fetchData()
  }
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
}
.search-form {
  margin-bottom: 16px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.mb-4 {
  margin-bottom: 16px;
}
.ml-2 {
  margin-left: 8px;
}
.text-gray-500 {
  color: #909399;
  font-size: 12px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-top: 20px;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}
.reject-timeline {
  padding: 8px 0 0 8px;
}
.reject-item {
  line-height: 1.6;
}
.reject-reason {
  color: #303133;
  font-size: 13px;
}
.reject-meta {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
.form-footer {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  gap: 10px;
}
</style>
