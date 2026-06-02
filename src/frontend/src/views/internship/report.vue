<template>
  <div class="page-container">
    <template v-if="userStore.isStudent()">
      <el-card>
        <el-form
          ref="studentFormRef"
          :model="studentForm"
          :rules="studentFormRules"
          label-width="180px"
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
          <el-form-item label="报告标题" prop="title">
            <el-input
              v-model="studentForm.title"
              placeholder="请输入报告标题"
            />
          </el-form-item>
          <el-form-item label="报告内容" prop="content">
            <el-input
              v-model="studentForm.content"
              type="textarea"
              :rows="10"
              placeholder="请输入完整的实习报告内容"
            />
          </el-form-item>
          <el-form-item label="Word模板">
            <el-link
              type="primary"
              href="/api/v1/files/templates/report"
              :underline="false"
              target="_blank"
            >
              下载Word模板
            </el-link>
          </el-form-item>
          <el-form-item label="附件上传">
            <el-upload
              ref="uploadRef"
              action="#"
              :auto-upload="false"
              :limit="3"
              accept=".pdf"
              :on-change="onFileChange"
              :on-remove="onFileRemove"
              :before-upload="beforePdfUpload"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">仅支持PDF格式文件</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item v-if="studentForm.filePath" label="已上传文件">
            <el-link
              type="primary"
              :href="studentForm.filePath"
              :underline="false"
              target="_blank"
            >
              查看已上传文件
            </el-link>
          </el-form-item>
          <el-form-item label="对指导老师满意度" prop="teacherSatisfactionScore">
            <el-input-number
              v-model="studentForm.teacherSatisfactionScore"
              :min="0"
              :max="100"
              :step="1"
            />
            <span class="ml-2 text-gray-500">（0-100分，校内指导教师不可见）</span>
          </el-form-item>
          <el-form-item
            v-if="studentForm.gradeLevel || studentForm.teacherComment"
            label="教师审核意见"
          >
            <div>
              <p v-if="studentForm.gradeLevel">
                <span class="text-gray-500">评分：</span><el-tag :type="getGradeType(studentForm.gradeLevel)">{{ getGradeText(studentForm.gradeLevel) }}</el-tag>
              </p>
              <p v-if="studentForm.teacherComment">
                <span class="text-gray-500">评语：</span>{{ studentForm.teacherComment }}
              </p>
            </div>
          </el-form-item>
          <el-form-item v-if="rejectRecords.length" label="驳回记录">
            <el-timeline>
              <el-timeline-item
                v-for="(record, index) in rejectRecords"
                :key="index"
                :timestamp="formatDateTime(record.createTime)"
                placement="top"
                type="danger"
              >
                <p>{{ record.rejectReason || record.reason || '无驳回原因' }}</p>
                <p v-if="record.operatorName" class="reject-operator">驳回人：{{ record.operatorName }}</p>
              </el-timeline-item>
            </el-timeline>
          </el-form-item>
        </el-form>
        <div v-if="studentForm.approvalStatus === STATUS_UNSUBMITTED" class="form-footer">
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="handleStudentSubmit"
          >
            提交
          </el-button>
        </div>
        <div v-if="studentForm.approvalStatus === STATUS_PENDING" class="form-footer">
          <el-button disabled>
            待审核中，无法修改
          </el-button>
        </div>
        <div v-if="studentForm.approvalStatus === STATUS_REJECTED" class="form-footer">
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
        </div>
        <div v-if="studentForm.approvalStatus === STATUS_APPROVED" class="form-footer">
          <el-button disabled>
            审核已通过
          </el-button>
        </div>
        <div v-if="studentForm.approvalStatus === STATUS_WITHDRAWN" class="form-footer">
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="handleStudentSubmit"
          >
            重新提交
          </el-button>
        </div>
      </el-card>
    </template>

    <template v-else>
      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">实习报告管理</span>
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
          <el-table-column prop="title" label="报告标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="gradeLevel" label="教师评分" min-width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.gradeLevel" :type="getGradeType(row.gradeLevel)">{{ getGradeText(row.gradeLevel) }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="teacherComment" label="评语" min-width="150" show-overflow-tooltip />
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

      <el-dialog v-model="viewVisible" title="实习报告详情" width="700px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生姓名">{{ viewData.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ viewData.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报告标题" :span="2">{{ viewData.title || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批状态">
            <el-tag :type="statusTagType(viewData.approvalStatus)">{{ statusLabel(viewData.approvalStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDateTime(viewData.createTime) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报告内容" :span="2" class="report-content">{{ viewData.content || '-' }}</el-descriptions-item>
          <el-descriptions-item label="教师评分">
            <el-tag v-if="viewData.gradeLevel" :type="getGradeType(viewData.gradeLevel)">{{ getGradeText(viewData.gradeLevel) }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="教师评语">{{ viewData.teacherComment || '-' }}</el-descriptions-item>
          <el-descriptions-item label="对指导老师满意度" :span="2" v-if="!isTeacher">
            {{ viewData.teacherSatisfactionScore != null ? viewData.teacherSatisfactionScore + '分' : '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="reject-records-section">
          <h4 class="section-title">驳回记录</h4>
          <el-timeline v-if="viewRejectRecords.length">
            <el-timeline-item
              v-for="(record, index) in viewRejectRecords"
              :key="index"
              :timestamp="formatDateTime(record.createTime)"
              placement="top"
              type="danger"
            >
              <p>{{ record.rejectReason || record.reason || '无驳回原因' }}</p>
              <p v-if="record.operatorName" class="reject-operator">驳回人：{{ record.operatorName }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无驳回记录" :image-size="60" />
        </div>
      </el-dialog>

      <el-dialog v-model="reviewVisible" title="批阅实习报告" width="600px" destroy-on-close>
        <el-descriptions :column="1" border class="mb-4">
          <el-descriptions-item label="学生姓名">{{ reviewData.studentName || '-' }}（{{ reviewData.studentNo || '' }}）</el-descriptions-item>
          <el-descriptions-item label="报告标题">{{ reviewData.title || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-form ref="reviewFormRef" :model="reviewFormData" :rules="reviewRules" label-width="100px">
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

const STATUS_UNSUBMITTED = null
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

function getGradeText(grade) {
  const map = { EXCELLENT: '优秀', GOOD: '良好', MEDIUM: '中等', PASS: '及格', FAIL: '不及格' }
  return map[grade] || '-'
}

function getGradeType(grade) {
  const map = { EXCELLENT: 'success', GOOD: '', MEDIUM: 'info', PASS: 'warning', FAIL: 'danger' }
  return map[grade] || ''
}

function statusTagType(status) {
  if (status === STATUS_UNSUBMITTED) return 'info'
  return statusMap[status]?.tagType || 'info'
}

function statusLabel(status) {
  if (status === STATUS_UNSUBMITTED) return '未提交'
  return statusMap[status]?.label || '未知'
}

const isTeacher = computed(() => userStore.roleCode === 'TEACHER')

const loading = ref(false)
const submitLoading = ref(false)
const reviewLoading = ref(false)
const isEditMode = ref(false)

const planOptions = ref([])
const attachments = ref([])
const rejectRecords = ref([])
const viewRejectRecords = ref([])

const studentFormRef = ref(null)
const uploadRef = ref(null)

const studentForm = reactive({
  id: null,
  planId: null,
  title: '',
  content: '',
  teacherSatisfactionScore: null,
  approvalStatus: STATUS_UNSUBMITTED,
  filePath: '',
  gradeLevel: null,
  teacherComment: ''
})

const studentFormRules = {
  planId: [{ required: true, message: '请选择实习计划', trigger: 'change' }],
  title: [{ required: true, message: '请输入报告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入报告内容', trigger: 'blur' }],
  teacherSatisfactionScore: [{ required: true, message: '请填写对指导老师的满意度', trigger: 'change' }]
}

const isFormReadonly = computed(() => {
  if (studentForm.approvalStatus === STATUS_APPROVED) return true
  if (studentForm.approvalStatus === STATUS_PENDING) return true
  if (studentForm.approvalStatus === STATUS_REJECTED) return !isEditMode.value
  if (studentForm.approvalStatus === STATUS_WITHDRAWN) return false
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
const viewVisible = ref(false)
const reviewVisible = ref(false)
const reviewFormRef = ref(null)

const viewData = ref({})
const reviewData = ref({})

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

async function fetchPlans() {
  try {
    const res = await request.get('/internship/plans/my-plans', { params: { size: 999 } })
    planOptions.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
    if (!studentForm.planId && planOptions.value.length > 0) {
      const today = new Date().toISOString().slice(0, 10)
      const ongoing = planOptions.value.find(p => p.startDate <= today && p.endDate >= today)
      studentForm.planId = (ongoing || planOptions.value[0]).id
      loadStudentReport()
    }
  } catch (e) {
    console.warn('获取实习计划失败', e.message)
  }
}

async function loadStudentReport() {
  loading.value = true
  isEditMode.value = false
  try {
    const res = await request.get('/internship/final-reports/my', {
      params: { planId: studentForm.planId }
    })
    const records = res.data.records || res.data || []
    const report = Array.isArray(records) ? records[0] : records
    if (report && report.id) {
      studentForm.id = report.id
      studentForm.planId = report.planId
      studentForm.title = report.title || ''
      studentForm.content = report.content || ''
      studentForm.teacherSatisfactionScore = report.teacherSatisfactionScore ?? null
      studentForm.approvalStatus = report.approvalStatus ?? STATUS_PENDING
      studentForm.filePath = report.filePath || ''
      studentForm.gradeLevel = report.gradeLevel ?? null
      studentForm.teacherComment = report.teacherComment || ''
      if (report.id) {
        fetchRejectRecords(report.id)
      }
    } else {
      studentForm.id = null
      studentForm.title = ''
      studentForm.content = ''
      studentForm.teacherSatisfactionScore = null
      studentForm.approvalStatus = STATUS_UNSUBMITTED
      studentForm.filePath = ''
      studentForm.gradeLevel = null
      studentForm.teacherComment = ''
      rejectRecords.value = []
    }
  } catch (e) {
    console.warn('获取报告数据失败', e.message)
    studentForm.id = null
    studentForm.title = ''
    studentForm.content = ''
    studentForm.teacherSatisfactionScore = null
    studentForm.approvalStatus = STATUS_UNSUBMITTED
    studentForm.filePath = ''
    studentForm.gradeLevel = null
    studentForm.teacherComment = ''
    rejectRecords.value = []
  } finally {
    loading.value = false
  }
}

async function fetchRejectRecords(targetId) {
  try {
    const res = await request.get('/messages/reject-records/by-target', {
      params: { targetType: 'REPORT', targetId }
    })
    rejectRecords.value = res.data || []
  } catch (e) {
    console.warn('获取驳回记录失败', e.message)
    rejectRecords.value = []
  }
}

async function fetchViewRejectRecords(targetId) {
  try {
    const res = await request.get('/messages/reject-records/by-target', {
      params: { targetType: 'REPORT', targetId }
    })
    viewRejectRecords.value = res.data || []
  } catch (e) {
    console.warn('获取驳回记录失败', e.message)
    viewRejectRecords.value = []
  }
}

function handlePlanChange() {
  loadStudentReport()
}

function beforePdfUpload(file) {
  const isPdf = file.type === 'application/pdf'
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isPdf) {
    ElMessage.error('仅支持上传PDF格式文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  return true
}

function onFileChange(file) {
  attachments.value.push(file.raw)
}

function onFileRemove(file, fileList) {
  attachments.value = fileList.map(f => f.raw)
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
    loadStudentReport()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('取消修改失败', e.message)
    }
  }
}

async function handleStudentSubmit() {
  if (!studentFormRef.value) return
  await studentFormRef.value.validate()
  submitLoading.value = true
  try {
    const fd = new FormData()
    fd.append('title', studentForm.title)
    fd.append('planId', studentForm.planId)
    fd.append('content', studentForm.content)
    if (studentForm.teacherSatisfactionScore != null) {
      fd.append('teacherSatisfactionScore', studentForm.teacherSatisfactionScore)
    }
    attachments.value.forEach(f => fd.append('files', f))
    if (studentForm.id) {
      await request.put(`/internship/final-reports/${studentForm.id}`, fd, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    } else {
      await request.post('/internship/final-reports', fd, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }
    ElMessage.success(studentForm.id ? '更新成功' : '提交成功')
    isEditMode.value = false
    attachments.value = []
    loadStudentReport()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...queryForm
    }
    const res = await request.get('/internship/final-reports', { params })
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
  fetchViewRejectRecords(row.id)
}

function handleReview(row) {
  reviewData.value = { ...row }
  reviewFormData.value = {
    gradeLevel: row.gradeLevel,
    teacherComment: row.teacherComment || '',
    approvalStatus: 1,
    rejectReason: ''
  }
  reviewVisible.value = true
}

async function handleReviewSubmit() {
  if (!reviewFormRef.value) return
  await reviewFormRef.value.validate()
  reviewLoading.value = true
  try {
    await request.put(`/internship/final-reports/${reviewData.value.id}/review`, reviewFormData.value)
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
      request.put(`/internship/final-reports/${row.id}/review`, {
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

onMounted(() => {
  if (userStore.isStudent()) {
    fetchPlans()
  } else {
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
.form-footer {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  gap: 10px;
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
.report-content {
  white-space: pre-wrap;
  line-height: 1.6;
}
.reject-records-section {
  margin-top: 20px;
}
.section-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.reject-operator {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
