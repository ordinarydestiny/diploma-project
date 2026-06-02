<template>
  <div class="page-container">
    <template v-if="userStore.isStudent()">
      <el-card v-loading="loading">
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
            <el-tag :type="getStatusType(studentForm.approvalStatus)">
              {{ getStatusText(studentForm.approvalStatus) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="Word模板下载">
            <el-link
              type="primary"
              href="/api/v1/files/templates/outer-assessment"
              :underline="false"
              target="_blank"
            >
              下载Word模板
            </el-link>
          </el-form-item>
          <el-form-item label="考核表文件" prop="file">
            <el-upload
              ref="uploadRef"
              action="#"
              :auto-upload="false"
              :limit="1"
              accept=".pdf"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">仅支持 PDF 格式</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item v-if="studentForm.filePath" label="已上传文件">
            <el-link
              type="primary"
              :href="studentForm.filePath"
              target="_blank"
            >
              {{ getFileName(studentForm.filePath) }}
            </el-link>
          </el-form-item>
          <el-form-item v-if="studentForm.gradeLevel" label="成绩等级">
            <el-tag :type="getGradeType(studentForm.gradeLevel)">
              {{ getGradeText(studentForm.gradeLevel) }}
            </el-tag>
          </el-form-item>
          <el-form-item v-if="studentForm.teacherComment" label="教师审核意见">
            <div style="white-space: pre-wrap;">{{ studentForm.teacherComment }}</div>
          </el-form-item>
        </el-form>

        <div v-if="rejectRecords.length > 0" class="reject-section">
          <h4 class="reject-title">驳回记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(record, index) in rejectRecords"
              :key="index"
              type="danger"
              :timestamp="formatDateTime(record.createTime)"
              placement="top"
            >
              <el-card shadow="never" class="reject-card">
                <p class="reject-reason">{{ record.rejectReason || '无驳回原因' }}</p>
                <p v-if="record.operatorName" class="reject-operator">驳回人：{{ record.operatorName }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>

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
          <template v-if="studentForm.approvalStatus === 0">
            <el-button disabled>待审核中，无法修改</el-button>
          </template>
          <template v-if="studentForm.approvalStatus === 2">
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
              <el-button @click="handleCancelEdit">取消修改</el-button>
            </template>
          </template>
          <template v-if="studentForm.approvalStatus === 1">
            <el-button disabled>审核已通过</el-button>
          </template>
          <template v-if="studentForm.approvalStatus === 4">
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
            <span class="card-title">校外考核表</span>
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
          <el-table-column prop="filePath" label="文件" min-width="200">
            <template #default="{ row }">
              <el-link v-if="row.filePath" type="primary" :href="row.filePath" target="_blank">查看文件</el-link>
              <span v-else>未上传</span>
            </template>
          </el-table-column>
          <el-table-column prop="gradeLevel" label="成绩等级" min-width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.gradeLevel" :type="getGradeType(row.gradeLevel)">{{ getGradeText(row.gradeLevel) }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="teacherComment" label="审核意见" min-width="150" show-overflow-tooltip />
          <el-table-column prop="approvalStatus" label="状态" min-width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.approvalStatus)">{{ getStatusText(row.approvalStatus) }}</el-tag>
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

      <el-dialog v-model="reviewVisible" title="批阅校外考核表" width="600px" destroy-on-close>
        <el-descriptions :column="1" border class="mb-4">
          <el-descriptions-item label="学生姓名">{{ reviewData.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ reviewData.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="文件">
            <el-link v-if="reviewData.filePath" type="primary" :href="reviewData.filePath" target="_blank">查看文件</el-link>
            <span v-else>未上传</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-form ref="reviewFormRef" :model="reviewFormData" :rules="reviewRules" label-width="100px">
          <el-form-item label="审核结果" prop="approvalStatus">
            <el-radio-group v-model="reviewFormData.approvalStatus">
              <el-radio :value="1">通过</el-radio>
              <el-radio :value="2">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="等级评定" prop="gradeLevel" v-if="reviewFormData.approvalStatus === 1">
            <el-select v-model="reviewFormData.gradeLevel" placeholder="请选择" style="width: 100%">
              <el-option label="优秀" value="EXCELLENT" />
              <el-option label="良好" value="GOOD" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="及格" value="PASS" />
              <el-option label="不及格" value="FAIL" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核意见" prop="teacherComment">
            <el-input v-model="reviewFormData.teacherComment" type="textarea" :rows="3" placeholder="请输入审核意见" />
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

      <el-dialog v-model="viewVisible" title="校外考核表详情" width="650px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生姓名">{{ viewData.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ viewData.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批状态">
            <el-tag :type="getStatusType(viewData.approvalStatus)">{{ getStatusText(viewData.approvalStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="成绩等级">
            <el-tag v-if="viewData.gradeLevel" :type="getGradeType(viewData.gradeLevel)">{{ getGradeText(viewData.gradeLevel) }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="文件" :span="2">
            <el-link v-if="viewData.filePath" type="primary" :href="viewData.filePath" target="_blank">{{ getFileName(viewData.filePath) }}</el-link>
            <span v-else>未上传</span>
          </el-descriptions-item>
          <el-descriptions-item label="教师审核意见" :span="2">{{ viewData.teacherComment || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{ formatDateTime(viewData.createTime) || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="viewRejectRecords.length > 0" class="reject-section">
          <h4 class="reject-title">驳回记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(record, index) in viewRejectRecords"
              :key="index"
              type="danger"
              :timestamp="formatDateTime(record.createTime)"
              placement="top"
            >
              <el-card shadow="never" class="reject-card">
                <p class="reject-reason">{{ record.rejectReason || '无驳回原因' }}</p>
                <p v-if="record.operatorName" class="reject-operator">驳回人：{{ record.operatorName }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
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
const loading = ref(false)
const submitLoading = ref(false)
const reviewLoading = ref(false)
const uploadRef = ref(null)
const studentFormRef = ref(null)
const reviewFormRef = ref(null)
const isEditMode = ref(false)

const planOptions = ref([])
const rejectRecords = ref([])
const viewRejectRecords = ref([])
const selectedRows = ref([])

const studentForm = reactive({
  id: null,
  planId: null,
  approvalStatus: null,
  filePath: '',
  gradeLevel: '',
  teacherComment: '',
  file: null
})

const isFormReadonly = computed(() => {
  if (studentForm.approvalStatus === 1) return true
  if (studentForm.approvalStatus === 0) return true
  if (studentForm.approvalStatus === 2) return !isEditMode.value
  if (studentForm.approvalStatus === 4) return false
  return false
})

const studentFormRules = {
  planId: [{ required: true, message: '请选择实习计划', trigger: 'change' }]
}

const queryForm = reactive({ studentName: '', approvalStatus: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const tableData = ref([])
const viewVisible = ref(false)
const reviewVisible = ref(false)
const viewData = ref({})
const reviewData = ref({})
const reviewFormData = ref({ approvalStatus: 1, gradeLevel: '', teacherComment: '', rejectReason: '' })
const reviewRules = {
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
      loadStudentAssessment()
    }
  } catch (e) {
    console.warn('获取实习计划失败', e.message)
  }
}

async function loadStudentAssessment() {
  if (!studentForm.planId) return
  loading.value = true
  isEditMode.value = false
  try {
    const res = await request.get('/internship/outer-assessments/my', { params: { planId: studentForm.planId } })
    const records = res.data.records || []
    if (records.length > 0) {
      const data = records[0]
      studentForm.id = data.id
      studentForm.planId = data.planId
      studentForm.approvalStatus = data.approvalStatus
      studentForm.filePath = data.filePath || ''
      studentForm.gradeLevel = data.gradeLevel || ''
      studentForm.teacherComment = data.teacherComment || ''
      studentForm.file = null
      await fetchRejectRecords(data.id)
    } else {
      studentForm.id = null
      studentForm.approvalStatus = null
      studentForm.filePath = ''
      studentForm.gradeLevel = ''
      studentForm.teacherComment = ''
      studentForm.file = null
      rejectRecords.value = []
    }
  } catch (e) {
    console.warn('获取考核表失败', e.message)
  } finally {
    loading.value = false
  }
}

function handlePlanChange() {
  loadStudentAssessment()
}

async function fetchRejectRecords(targetId) {
  try {
    const res = await request.get('/messages/reject-records/by-target', {
      params: { targetType: 'OUTER_ASSESSMENT', targetId }
    })
    rejectRecords.value = res.data || []
  } catch (e) {
    console.warn('获取驳回记录失败')
    rejectRecords.value = []
  }
}

async function fetchViewRejectRecords(targetId) {
  try {
    const res = await request.get('/messages/reject-records/by-target', {
      params: { targetType: 'OUTER_ASSESSMENT', targetId }
    })
    viewRejectRecords.value = res.data || []
  } catch (e) {
    console.warn('获取驳回记录失败')
    viewRejectRecords.value = []
  }
}

function handleFileChange(file) {
  studentForm.file = file.raw
}

function handleFileRemove() {
  studentForm.file = null
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

async function handleStudentSubmit() {
  if (!studentForm.planId) {
    ElMessage.warning('请选择实习计划')
    return
  }
  if (!studentForm.file && !studentForm.filePath) {
    ElMessage.warning('请选择考核表文件')
    return
  }
  submitLoading.value = true
  try {
    const fd = new FormData()
    fd.append('planId', studentForm.planId)
    if (studentForm.file) fd.append('file', studentForm.file)
    if (studentForm.id) {
      await request.put(`/internship/outer-assessments/${studentForm.id}`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    } else {
      await request.post('/internship/outer-assessments', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    }
    ElMessage.success(studentForm.id ? '重新提交成功' : '提交成功')
    isEditMode.value = false
    loadStudentAssessment()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/internship/outer-assessments', {
      params: { current: pagination.current, size: pagination.size, ...queryForm }
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

async function handleView(row) {
  viewData.value = { ...row }
  viewRejectRecords.value = []
  viewVisible.value = true
  if (row.approvalStatus === 2 || row.id) {
    await fetchViewRejectRecords(row.id)
  }
}

function handleReview(row) {
  reviewData.value = { ...row }
  reviewFormData.value = {
    approvalStatus: 1,
    gradeLevel: row.gradeLevel || '',
    teacherComment: row.teacherComment || '',
    rejectReason: ''
  }
  reviewVisible.value = true
}

async function handleReviewSubmit() {
  if (!reviewFormRef.value) return
  await reviewFormRef.value.validate()
  reviewLoading.value = true
  try {
    await request.put(`/internship/outer-assessments/${reviewData.value.id}/review`, reviewFormData.value)
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
  return row.approvalStatus !== 4 && (userStore.roleCode === 'TEACHER' || userStore.roleCode === 'ADMIN' || userStore.roleCode === 'DEPT_ADMIN')
}

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
      request.put(`/internship/outer-assessments/${row.id}/review`, {
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

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 4: 'info' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { 0: '待审', 1: '通过', 2: '驳回', 4: '已撤回' }
  return map[status] || '未提交'
}

function getGradeType(g) { return { EXCELLENT:'success', GOOD:'', MEDIUM:'info', PASS:'warning', FAIL:'danger' }[g]||'' }

function getGradeText(grade) {
  const map = { EXCELLENT: '优秀', GOOD: '良好', MEDIUM: '中等', PASS: '及格', FAIL: '不及格' }
  return map[grade] || grade
}

function getFileName(path) {
  if (!path) return ''
  return path.split('/').pop().split('\\').pop()
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
.mb-4 {
  margin-bottom: 16px;
}
.form-footer {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  gap: 10px;
}
.reject-section {
  margin-top: 20px;
}
.reject-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}
.reject-card {
  padding: 0;
}
.reject-card :deep(.el-card__body) {
  padding: 10px 14px;
}
.reject-reason {
  margin: 0 0 4px 0;
  font-size: 13px;
  color: #606266;
}
.reject-operator {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
</style>
