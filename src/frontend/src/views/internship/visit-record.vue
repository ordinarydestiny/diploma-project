<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
        <el-select
          v-model="queryForm.planId"
          placeholder="实习计划"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="item in planOptions"
            :key="item.id"
            :label="item.planName"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="queryForm.visitType"
          placeholder="巡访方式"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="item in visitTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-input
          v-model="queryForm.studentName"
          placeholder="学生姓名"
          clearable
          style="width: 140px"
        />
        <el-input
          v-model="queryForm.teacherName"
          placeholder="指导老师"
          clearable
          style="width: 140px"
        />
        <el-date-picker
          v-model="queryForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          format="YYYY年MM月DD日"
          style="width: 260px"
        />
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
          v-if="userStore.isTeacher()"
          type="primary"
          :icon="Plus"
          @click="handleAdd"
        >
          新增巡访记录
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
          prop="visitDate"
          label="巡访日期"
          min-width="130"
          align="center"
        >
          <template #default="{ row }">
            {{ formatDate(row.visitDate) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="visitType"
          label="巡访方式"
          min-width="110"
        >
          <template #default="{ row }">
            <el-tag :type="visitTypeTagMap[row.visitType] || 'info'">
              {{ visitTypeLabelMap[row.visitType] || row.visitType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="studentName"
          label="学生姓名"
          min-width="100"
        />
        <el-table-column
          prop="companyName"
          label="实习单位"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column
          prop="content"
          label="巡访内容"
          min-width="250"
          show-overflow-tooltip
        />
        <el-table-column
          prop="evaluation"
          label="实习评价"
          min-width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.evaluation"
              :type="row.evaluation === '优秀' ? 'success' : row.evaluation === '良好' ? '' : row.evaluation === '一般' ? 'warning' : 'danger'"
            >
              {{ row.evaluation }}
            </el-tag>
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
              type="primary"
              link
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              v-if="userStore.isTeacher()"
              type="primary"
              link
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="userStore.isTeacher()"
              type="danger"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="650px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="150px"
      >
        <el-form-item
          label="实习计划"
          prop="planId"
        >
          <el-select
            v-model="form.planId"
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
        <el-form-item
          label="学生"
          prop="studentId"
        >
          <el-select
            v-model="form.studentId"
            placeholder="请选择学生"
            style="width: 100%"
          >
            <el-option
              v-for="item in studentOptions"
              :key="item.studentId"
              :label="item.studentName"
              :value="item.studentId"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="巡访日期"
          prop="visitDate"
        >
          <el-date-picker
            v-model="form.visitDate"
            type="date"
            placeholder="请选择巡访日期"
            value-format="YYYY-MM-DD"
            format="YYYY年MM月DD日"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          label="巡访方式"
          prop="visitType"
        >
          <el-select
            v-model="form.visitType"
            placeholder="请选择巡访方式"
            style="width: 100%"
          >
            <el-option
              v-for="item in visitTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="巡访内容"
          prop="content"
        >
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入巡访内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item
          label="学生实习情况评价"
          prop="evaluation"
        >
          <el-select
            v-model="form.evaluation"
            placeholder="请选择评价"
            style="width: 100%"
          >
            <el-option
              v-for="item in evaluationOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上传照片">
          <el-upload
            v-model:file-list="photoFileList"
            :auto-upload="false"
            list-type="picture-card"
            accept=".jpg,.jpeg,.png"
            :limit="5"
            :on-change="handlePhotoChange"
            :on-remove="handlePhotoRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="font-size: 12px; color: #999; margin-top: 4px">
            支持jpg、jpeg、png，小于10MB，最多上传5张
          </div>
        </el-form-item>
        <el-form-item label="上传附件">
          <el-upload
            v-model:file-list="attachmentFileList"
            :auto-upload="false"
            accept=".doc,.docx,.pdf"
            :limit="1"
            :on-change="handleAttachmentChange"
            :on-remove="handleAttachmentRemove"
          >
            <el-button
              type="primary"
              size="small"
            >
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
      v-model="viewDialogVisible"
      title="查看巡访记录"
      width="650px"
    >
      <el-descriptions
        :column="1"
        border
      >
        <el-descriptions-item label="巡访日期">
          {{ formatDate(viewData.visitDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="巡访方式">
          <el-tag :type="visitTypeTagMap[viewData.visitType] || 'info'">
            {{ visitTypeLabelMap[viewData.visitType] || viewData.visitType }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学生姓名">
          {{ viewData.studentName }}
        </el-descriptions-item>
        <el-descriptions-item label="实习单位">
          {{ viewData.companyName }}
        </el-descriptions-item>
        <el-descriptions-item label="巡访内容">
          <div style="white-space: pre-wrap">
            {{ viewData.content }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="学生实习情况评价">
          {{ viewData.evaluation }}
        </el-descriptions-item>
        <el-descriptions-item label="巡访照片">
          <div
            v-if="viewData.photos && viewData.photos.length"
            class="view-photos"
          >
            <el-image
              v-for="(url, index) in viewData.photos"
              :key="index"
              :src="url"
              :preview-src-list="viewData.photos"
              :initial-index="index"
              fit="cover"
              style="width: 100px; height: 100px; margin-right: 8px"
            />
          </div>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="附件">
          <el-link
            v-if="viewData.attachmentUrl"
            :href="viewData.attachmentUrl"
            target="_blank"
            type="primary"
          >
            {{ viewData.attachmentName || '下载附件' }}
          </el-link>
          <span v-else>无</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/dateFormat'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { useTableResize } from '@/composables/useTableResize.js'

const userStore = useUserStore()

const tableRef = ref(null)
useTableResize(tableRef)

const planOptions = ref([])
const studentOptions = ref([])
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const queryForm = reactive({
  current: 1,
  size: 10,
  planId: null,
  visitType: null,
  dateRange: null,
  studentName: '',
  teacherName: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增巡访记录')
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)
const photoFileList = ref([])
const attachmentFileList = ref([])

const viewDialogVisible = ref(false)
const viewData = ref({})

const form = reactive({
  planId: null,
  studentId: null,
  visitDate: '',
  visitType: '',
  content: '',
  evaluation: ''
})

const formRules = {
  planId: [{ required: true, message: '请选择实习计划', trigger: 'change' }],
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  visitDate: [{ required: true, message: '请选择巡访日期', trigger: 'change' }],
  visitType: [{ required: true, message: '请选择巡访方式', trigger: 'change' }],
  content: [{ required: true, message: '请输入巡访内容', trigger: 'blur' }],
  evaluation: [{ required: true, message: '请选择学生实习情况评价', trigger: 'change' }]
}

const API_PATH = '/internship/visit-records'

const visitTypeOptions = [
  { value: '实地巡访', label: '实地巡访' },
  { value: '线上巡访', label: '线上巡访' },
  { value: '电话巡访', label: '电话巡访' }
]

const visitTypeTagMap = {
  '实地巡访': 'success',
  '线上巡访': 'primary',
  '电话巡访': 'warning'
}

const visitTypeLabelMap = {
  '实地巡访': '实地巡访',
  '线上巡访': '线上巡访',
  '电话巡访': '电话巡访'
}

const evaluationOptions = [
  { value: '优秀', label: '优秀' },
  { value: '良好', label: '良好' },
  { value: '一般', label: '一般' },
  { value: '较差', label: '较差' }
]

async function loadPlanOptions() {
  try {
    const res = await request.get('/internship/plans', { params: { size: 999 } })
    planOptions.value = res.data.records || []
  } catch (e) {
    console.warn('获取实习计划列表失败', e.message)
  }
}

async function loadStudentsByPlan(planId) {
  if (!planId) {
    studentOptions.value = []
    return
  }
  try {
    const res = await request.get('/internship/plan-students', { params: { planId, size: 999 } })
    const data = res.data
    studentOptions.value = Array.isArray(data) ? data : (data.records || [])
  } catch (e) {
    console.warn('获取学生列表失败', e.message)
  }
}

function handlePlanChange(planId) {
  form.studentId = null
  loadStudentsByPlan(planId)
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      current: queryForm.current,
      size: queryForm.size,
      planId: queryForm.planId || undefined,
      visitType: queryForm.visitType || undefined,
      startDate: queryForm.dateRange ? queryForm.dateRange[0] : undefined,
      endDate: queryForm.dateRange ? queryForm.dateRange[1] : undefined,
      studentName: queryForm.studentName || undefined,
      teacherName: queryForm.teacherName || undefined
    }
    const res = await request.get(API_PATH, { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取巡访记录列表失败', e.message)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.planId = null
  form.studentId = null
  form.visitDate = ''
  form.visitType = ''
  form.content = ''
  form.evaluation = ''
  photoFileList.value = []
  attachmentFileList.value = []
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
  queryForm.planId = null
  queryForm.visitType = null
  queryForm.dateRange = null
  queryForm.studentName = ''
  queryForm.teacherName = ''
  queryForm.current = 1
  fetchData()
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增巡访记录'
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = { ...row }
  viewDialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  dialogTitle.value = '编辑巡访记录'
  editId.value = row.id
  form.planId = row.planId
  form.studentId = row.studentId
  form.visitDate = row.visitDate
  form.visitType = row.visitType
  form.content = row.content
  form.evaluation = row.evaluation || ''
  if (row.photos && row.photos.length) {
    photoFileList.value = row.photos.map((url, index) => ({
      name: `photo_${index}`,
      url,
      status: 'success'
    }))
  }
  if (row.attachmentUrl) {
    attachmentFileList.value = [{ name: row.attachmentName || '附件', url: row.attachmentUrl, status: 'success' }]
  }
  loadStudentsByPlan(row.planId)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = {
      planId: form.planId,
      studentId: form.studentId,
      visitDate: form.visitDate,
      visitType: form.visitType,
      content: form.content,
      evaluation: form.evaluation,
      photos: photoFileList.value.map(f => f.url || f.response?.data).filter(Boolean),
      attachmentPath: attachmentFileList.value.length > 0 ? (attachmentFileList.value[0].url || '') : ''
    }
    if (editId.value) {
      await request.put(`${API_PATH}/${editId.value}`, submitData)
      ElMessage.success('更新成功')
    } else {
      await request.post(API_PATH, submitData)
      ElMessage.success('提交成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('提交巡访记录失败', e.message)
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该巡访记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`${API_PATH}/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      console.warn('删除巡访记录失败', e.message)
    }
  }).catch(() => {})
}

function handlePhotoChange(file) {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('照片大小不能超过10MB')
    photoFileList.value = photoFileList.value.filter(f => f.uid !== file.uid)
  }
}

function handlePhotoRemove() {}

function handleAttachmentChange(file) {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('附件大小不能超过10MB')
    attachmentFileList.value = attachmentFileList.value.filter(f => f.uid !== file.uid)
  }
}

function handleAttachmentRemove() {}

onMounted(() => {
  loadPlanOptions()
  fetchData()
})

onBeforeUnmount(() => {
  dialogVisible.value = false
  viewDialogVisible.value = false
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

.view-photos {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
