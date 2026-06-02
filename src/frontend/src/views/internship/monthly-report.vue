<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
        <el-input
          v-if="!userStore.isStudent()"
          v-model="queryForm.studentName"
          placeholder="学生姓名"
          clearable
          style="width: 160px"
        />
        <el-select
          v-model="queryForm.approvalStatus"
          placeholder="审批状态"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="item in approvalStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button
          type="success"
          :icon="Search"
          @click="handleSearch"
        >
          搜索
        </el-button>
        <el-button
          :icon="Refresh"
          @click="handleReset"
        >
          重置
        </el-button>
        <el-button
          v-if="userStore.isStudent()"
          type="primary"
          :icon="Plus"
          @click="handleAdd"
        >
          新增月报
        </el-button>
      </div>
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        stripe
      >
        <el-table-column
          v-if="!userStore.isStudent()"
          prop="studentName"
          label="学生姓名"
          min-width="100"
        />
        <el-table-column
          prop="reportDate"
          label="月份"
          min-width="150"
        >
          <template #default="{ row }">
            {{ formatMonth(row.reportDate) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="content"
          label="月报内容"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="approvalStatus"
          label="审批状态"
          min-width="100"
        >
          <template #default="{ row }">
            <el-tag :type="approvalStatusTagType(row.approvalStatus)">
              {{ approvalStatusLabel(row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="gradeLevel"
          label="评分"
          min-width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag v-if="row.gradeLevel" :type="getGradeType(row.gradeLevel)">{{ getGradeText(row.gradeLevel) }}</el-tag>
            <span v-else style="color: #c0c4cc">未评分</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="submitTime"
          label="提交时间"
          min-width="170"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="150"
          fixed="right"
          :resizable="false"
        >
          <template #default="{ row }">
            <el-button
              v-if="userStore.isStudent()"
              type="primary"
              link
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              v-if="userStore.isStudent() && row.approvalStatus === 2"
              type="warning"
              link
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="!userStore.isStudent() && row.approvalStatus === 0"
              type="warning"
              link
              @click="handleReview(row)"
            >
              批阅
            </el-button>
            <el-button
              v-if="!userStore.isStudent()"
              type="primary"
              link
              @click="handleView(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑月报' : '填写月报'"
      width="650px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item
          label="实习计划"
          prop="planId"
        >
          <el-select
            v-model="form.planId"
            placeholder="请选择实习计划"
            style="width: 100%"
          >
            <el-option
              v-for="item in planOptions"
              :key="item.id"
              :label="item.planName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="月报日期"
          prop="reportDate"
        >
          <el-date-picker
            v-model="form.reportDate"
            type="month"
            format="YYYY 年 MM 月"
            value-format="YYYY-MM-DD"
            placeholder="选择月报月份"
            :disabled-date="disableMonthDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          label="月报内容"
          prop="content"
        >
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            maxlength="2000"
            show-word-limit
            placeholder="请填写本月实习总结，包括工作成果、能力提升、问题与改进方向"
          />
        </el-form-item>
        <el-form-item label="上传照片">
          <el-upload
            v-model:file-list="form.photoList"
            action="/api/v1/files/upload"
            list-type="picture-card"
            accept=".jpg,.jpeg,.png"
            :limit="5"
            :headers="uploadHeaders"
            :on-success="handlePhotoSuccess"
            :on-remove="handlePhotoRemove"
            :before-upload="beforePhotoUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="font-size: 12px; color: #999; margin-top: 4px">
            支持jpg、jpeg、png，小于10MB;最多上传5张
          </div>
        </el-form-item>
        <el-form-item label="上传附件">
          <el-upload
            v-model:file-list="form.attachmentList"
            action="/api/v1/files/upload"
            accept=".doc,.docx,.pdf"
            :headers="uploadHeaders"
            :on-success="handleAttachmentSuccess"
            :on-remove="handleAttachmentRemove"
            :before-upload="beforeAttachmentUpload"
          >
            <el-button
              type="primary"
              size="small"
            >
              <el-icon><UploadFilled /></el-icon>
              点击上传
            </el-button>
          </el-upload>
          <div style="font-size: 12px; color: #999; margin-top: 4px">
            支持doc、docx、pdf，单个文件不超过10MB
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailDialogVisible"
      title="月报详情"
      width="650px"
    >
      <el-descriptions
        :column="1"
        border
      >
        <el-descriptions-item label="月份">
          {{ formatMonth(detailData.reportDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="approvalStatusTagType(detailData.approvalStatus)">
            {{ approvalStatusLabel(detailData.approvalStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item
          v-if="detailData.gradeLevel"
          label="评分"
        >
          <el-tag :type="getGradeType(detailData.gradeLevel)">{{ getGradeText(detailData.gradeLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item
          v-if="detailData.approvalStatus === 2 && detailData.rejectReason"
          label="驳回原因"
        >
          <span style="color: #f56c6c">{{ detailData.rejectReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatDateTime(detailData.submitTime) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="月报内容">
          <div style="white-space: pre-wrap">
            {{ detailData.content }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="月报照片">
          <div
            v-if="detailData.photos && detailData.photos.length"
            class="detail-photos"
          >
            <el-image
              v-for="(url, index) in detailData.photos"
              :key="index"
              :src="url"
              :preview-src-list="detailData.photos"
              :initial-index="index"
              fit="cover"
              style="width: 100px; height: 100px; margin-right: 8px"
            />
          </div>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="月报附件">
          <el-link
            v-if="detailData.attachment"
            :href="detailData.attachment"
            target="_blank"
            type="primary"
          >
            {{ detailData.attachmentName || '下载附件' }}
          </el-link>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item
          v-if="detailData.approvalStatus === 1"
          label="批阅意见"
        >
          {{ detailData.reviewComment || '无' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="reviewDialogVisible"
      title="批阅月报"
      width="650px"
    >
      <el-descriptions
        :column="1"
        border
        style="margin-bottom: 20px"
      >
        <el-descriptions-item label="学生姓名">
          {{ reviewData.studentName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="月份">
          {{ formatMonth(reviewData.reportDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="月报内容">
          <div style="white-space: pre-wrap">
            {{ reviewData.content }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="月报照片">
          <div
            v-if="reviewData.photos && reviewData.photos.length"
            class="detail-photos"
          >
            <el-image
              v-for="(url, index) in reviewData.photos"
              :key="index"
              :src="url"
              :preview-src-list="reviewData.photos"
              :initial-index="index"
              fit="cover"
              style="width: 100px; height: 100px; margin-right: 8px"
            />
          </div>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="月报附件">
          <el-link
            v-if="reviewData.attachment"
            :href="reviewData.attachment"
            target="_blank"
            type="primary"
          >
            {{ reviewData.attachmentName || '下载附件' }}
          </el-link>
          <span v-else>无</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewFormRules"
        label-width="100px"
      >
        <el-form-item
          label="评分"
          prop="score"
        >
          <el-rate
            v-model="reviewForm.score"
            :texts="scoreTexts"
            show-text
          />
        </el-form-item>
        <el-form-item
          label="批阅意见"
          prop="reviewComment"
        >
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请填写批阅意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="danger"
          :loading="rejectLoading"
          @click="handleReject"
        >
          驳回
        </el-button>
        <el-button
          type="primary"
          :loading="reviewLoading"
          @click="submitReview"
        >
          批阅通过
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回月报"
      width="500px"
    >
      <el-form
        ref="rejectFormRef"
        :model="rejectForm"
        :rules="rejectFormRules"
        label-width="100px"
      >
        <el-form-item
          label="驳回原因"
          prop="reason"
        >
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请填写驳回原因（至少10个字）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="danger"
          :loading="rejectLoading"
          @click="submitReject"
        >
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, UploadFilled } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateFormat'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { useTableResize } from '@/composables/useTableResize.js'

const userStore = useUserStore()
const REPORT_TYPE = 'MONTHLY'

const tableRef = ref(null)
useTableResize(tableRef)

const scoreTexts = ['不合格', '待改进', '普通', '良好', '优秀']

function starToGrade(star) {
  if (star >= 5) return 'EXCELLENT'
  if (star >= 4) return 'GOOD'
  if (star >= 3) return 'MEDIUM'
  if (star >= 2) return 'PASS'
  return 'FAIL'
}

function getGradeText(grade) {
  const map = { EXCELLENT: '优秀', GOOD: '良好', MEDIUM: '中等', PASS: '及格', FAIL: '不及格' }
  return map[grade] || '-'
}

function getGradeType(grade) {
  const map = { EXCELLENT: 'success', GOOD: '', MEDIUM: 'info', PASS: 'warning', FAIL: 'danger' }
  return map[grade] || ''
}

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const planOptions = ref([])

const queryForm = reactive({
  current: 1,
  size: 10,
  studentName: '',
  approvalStatus: ''
})

const approvalStatusOptions = [
  { value: '', label: '全部' },
  { value: 0, label: '未批阅' },
  { value: 1, label: '已批阅' },
  { value: 2, label: '已驳回' }
]

function approvalStatusLabel(status) {
  const map = { 0: '未批阅', 1: '已批阅', 2: '已驳回' }
  return map[status] || '未知'
}

function approvalStatusTagType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const dialogVisible = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  planId: null,
  reportDate: '',
  content: '',
  photoList: [],
  attachmentList: []
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const formRules = {
  planId: [{ required: true, message: '请选择实习计划', trigger: 'change' }],
  reportDate: [{ required: true, message: '请选择月报月份', trigger: 'change' }],
  content: [{ required: true, message: '请填写月报内容', trigger: 'blur' }]
}

const detailDialogVisible = ref(false)
const detailData = ref({})

const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const reviewFormRef = ref(null)
const reviewId = ref(null)
const reviewData = ref({})

const reviewForm = reactive({
  reviewComment: '',
  score: 0
})

const reviewFormRules = {
  score: [{ required: true, message: '请选择评分', trigger: 'change', type: 'number', min: 1 }],
  reviewComment: [{ required: true, message: '请填写批阅意见', trigger: 'blur' }]
}

const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({ reason: '' })
const rejectFormRules = {
  reason: [
    { required: true, message: '请填写驳回原因', trigger: 'blur' },
    { min: 10, message: '驳回原因至少10个字', trigger: 'blur' }
  ]
}

function formatMonth(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const mo = String(d.getMonth() + 1).padStart(2, '0')
  return `${y}年${mo}月`
}

function disableMonthDate(date) {
  if (date.getTime() > Date.now()) return true
  if (!editId.value) {
    const ym = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-01`
    return tableData.value.some(r => r.reportDate === ym)
  }
  return false
}

async function loadPlanOptions() {
  try {
    const res = await request.get('/internship/plans/my')
    const data = res.data
    planOptions.value = Array.isArray(data) ? data : data ? [data] : []
    if (!form.planId && planOptions.value.length > 0) {
      const today = new Date().toISOString().slice(0, 10)
      const ongoing = planOptions.value.find(p => p.startDate <= today && p.endDate >= today)
      form.planId = (ongoing || planOptions.value[0]).id
    }
  } catch (e) {
    console.warn('获取实习计划失败', e.message)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      current: queryForm.current,
      size: queryForm.size,
      reportType: REPORT_TYPE,
      studentName: queryForm.studentName || undefined,
      approvalStatus: queryForm.approvalStatus !== '' ? queryForm.approvalStatus : undefined
    }
    const res = await request.get('/internship/reports', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取月报列表失败', e.message)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.planId = null
  form.reportDate = ''
  form.content = ''
  form.photoList = []
  form.attachmentList = []
  editId.value = null
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

function handleSearch() {
  queryForm.current = 1
  fetchData()
}

function handleReset() {
  queryForm.studentName = ''
  queryForm.approvalStatus = ''
  queryForm.current = 1
  fetchData()
}

function handleAdd() {
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  editId.value = row.id
  form.planId = row.planId
  form.reportDate = row.reportDate
  form.content = row.content
  form.photoList = (row.photoPaths ? row.photoPaths.split(',').filter(Boolean) : []).map((url, index) => ({
    name: `photo_${index}`,
    url,
    status: 'success'
  }))
  form.attachmentList = row.attachmentPaths
    ? row.attachmentPaths.split(',').filter(Boolean).map((url, index) => ({ name: `attachment_${index}`, url, status: 'success' }))
    : []
  dialogVisible.value = true
}

function handleView(row) {
  detailData.value = {
    ...row,
    photos: row.photoPaths ? row.photoPaths.split(',').filter(Boolean) : [],
    attachment: row.attachmentPaths ? row.attachmentPaths.split(',')[0] : null,
    attachmentName: row.attachmentPaths ? '下载附件' : null
  }
  detailDialogVisible.value = true
}

function handlePhotoSuccess(response, uploadFile) {
  uploadFile.url = response.data.filePath
}

function handlePhotoRemove() {}

function beforePhotoUpload(file) {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('照片大小不能超过10MB')
    return false
  }
  return true
}

function handleAttachmentSuccess(response, uploadFile) {
  uploadFile.url = response.data.filePath
}

function handleAttachmentRemove() {}

function beforeAttachmentUpload(file) {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('附件大小不能超过10MB')
    return false
  }
  return true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!editId.value) {
    const dup = tableData.value.find(r => r.reportDate === form.reportDate)
    if (dup) {
      ElMessage.warning('该月已存在月报，不可重复提交')
      return
    }
  }

  submitLoading.value = true
  try {
    const photoPaths = form.photoList
      .map(f => f.url || f.response?.data?.filePath)
      .filter(Boolean)
      .join(',')
    const attachmentPaths = form.attachmentList
      .map(f => f.url || f.response?.data?.filePath)
      .filter(Boolean)
      .join(',')
    const submitData = {
      reportType: REPORT_TYPE,
      planId: form.planId,
      reportDate: form.reportDate,
      content: form.content,
      photoPaths,
      attachmentPaths
    }
    if (editId.value) {
      await request.put(`/internship/reports/${editId.value}`, submitData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/internship/reports', submitData)
      ElMessage.success('提交成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    const msg = e.response?.data?.message
    if (msg && msg.includes('重复')) {
      ElMessage.warning(msg)
    } else {
      console.warn('提交月报失败', e.message)
    }
  } finally {
    submitLoading.value = false
  }
}

function handleReview(row) {
  reviewId.value = row.id
  reviewData.value = {
    ...row,
    photos: row.photoPaths ? row.photoPaths.split(',').filter(Boolean) : [],
    attachment: row.attachmentPaths ? row.attachmentPaths.split(',')[0] : null,
    attachmentName: row.attachmentPaths ? '下载附件' : null
  }
  reviewForm.reviewComment = ''
  reviewForm.score = 0
  reviewDialogVisible.value = true
}

async function submitReview() {
  const valid = await reviewFormRef.value?.validate().catch(() => false)
  if (!valid) return

  reviewLoading.value = true
  try {
    await request.put(`/internship/reports/${reviewId.value}/review`, {
      reviewComment: reviewForm.reviewComment,
      gradeLevel: starToGrade(reviewForm.score)
    })
    ElMessage.success('批阅通过')
    reviewDialogVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('批阅月报失败', e.message)
  } finally {
    reviewLoading.value = false
  }
}

function handleReject() {
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

async function submitReject() {
  const valid = await rejectFormRef.value?.validate().catch(() => false)
  if (!valid) return

  rejectLoading.value = true
  try {
    await request.put(`/internship/reports/${reviewId.value}/reject`, {
      reason: rejectForm.reason
    })
    ElMessage.success('驳回成功')
    rejectDialogVisible.value = false
    reviewDialogVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('驳回月报失败', e.message)
  } finally {
    rejectLoading.value = false
  }
}

onMounted(() => {
  if (userStore.isStudent()) {
    loadPlanOptions()
  }
  fetchData()
})

onBeforeUnmount(() => {
  dialogVisible.value = false
  detailDialogVisible.value = false
  reviewDialogVisible.value = false
  rejectDialogVisible.value = false
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-photos {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
