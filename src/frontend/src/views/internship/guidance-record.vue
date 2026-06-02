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
          v-model="queryForm.guidanceType"
          placeholder="指导方式"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="item in guidanceTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
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
          新增指导记录
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
          prop="guidanceDate"
          label="指导日期"
          min-width="130"
          align="center"
        >
          <template #default="{ row }">
            {{ formatDate(row.guidanceDate) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="guidanceType"
          label="指导方式"
          min-width="110"
        >
          <template #default="{ row }">
            <el-tag :type="guidanceTypeTagMap[row.guidanceType] || 'info'">
              {{ guidanceTypeLabelMap[row.guidanceType] || row.guidanceType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="topic"
          label="指导主题"
          min-width="100"
          show-overflow-tooltip
        />
        <el-table-column
          prop="studentName"
          label="学生姓名"
          min-width="90"
          show-overflow-tooltip
        />
        <el-table-column
          prop="content"
          label="指导内容"
          min-width="300"
          show-overflow-tooltip
        />
        <el-table-column
          label="操作"
          width="180"
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
        label-width="110px"
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
          label="指导日期"
          prop="guidanceDate"
        >
          <el-date-picker
            v-model="form.guidanceDate"
            type="date"
            placeholder="请选择指导日期"
            value-format="YYYY-MM-DD"
            format="YYYY年MM月DD日"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          label="指导方式"
          prop="guidanceType"
        >
          <el-select
            v-model="form.guidanceType"
            placeholder="请选择指导方式"
            style="width: 100%"
          >
            <el-option
              v-for="item in guidanceTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="指导主题"
          prop="topic"
        >
          <el-input
            v-model="form.topic"
            placeholder="请输入指导主题"
          />
        </el-form-item>
        <el-form-item
          label="指导内容"
          prop="content"
        >
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入指导内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="上传附件">
          <el-upload
            v-model:file-list="attachmentFileList"
            :auto-upload="false"
            accept=".doc,.docx,.pdf,.jpg,.png"
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
            支持doc、docx、pdf、jpg、png，单个文件不超过10MB
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
      title="查看指导记录"
      width="650px"
    >
      <el-descriptions
        :column="1"
        border
      >
        <el-descriptions-item label="指导日期">
          {{ formatDate(viewData.guidanceDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="指导方式">
          <el-tag :type="guidanceTypeTagMap[viewData.guidanceType] || 'info'">
            {{ guidanceTypeLabelMap[viewData.guidanceType] || viewData.guidanceType }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="指导主题">
          {{ viewData.topic }}
        </el-descriptions-item>
        <el-descriptions-item label="学生姓名">
          {{ viewData.studentName }}
        </el-descriptions-item>
        <el-descriptions-item label="指导内容">
          <div style="white-space: pre-wrap">
            {{ viewData.content }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="附件">
          <el-link
            v-if="viewData.attachmentPath"
            :href="viewData.attachmentPath"
            target="_blank"
            type="primary"
          >
            {{ viewData.attachmentPath ? '下载附件' : '' }}
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
  guidanceType: null,
  dateRange: null
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增指导记录')
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)
const attachmentFileList = ref([])

const viewDialogVisible = ref(false)
const viewData = ref({})

const form = reactive({
  planId: null,
  studentId: null,
  guidanceDate: '',
  guidanceType: '',
  topic: '',
  content: ''
})

const formRules = {
  planId: [{ required: true, message: '请选择实习计划', trigger: 'change' }],
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  guidanceDate: [{ required: true, message: '请选择指导日期', trigger: 'change' }],
  guidanceType: [{ required: true, message: '请选择指导方式', trigger: 'change' }],
  topic: [{ required: true, message: '请输入指导主题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入指导内容', trigger: 'blur' }]
}

const API_PATH = '/internship/guidance-records'

const guidanceTypeOptions = [
  { value: '线上', label: '线上' },
  { value: '线下', label: '线下' },
  { value: '电话', label: '电话' },
  { value: '微信', label: '微信' }
]

const guidanceTypeTagMap = {
  '线上': 'primary',
  '线下': 'success',
  '电话': 'warning',
  '微信': 'info'
}

const guidanceTypeLabelMap = {
  '线上': '线上',
  '线下': '线下',
  '电话': '电话',
  '微信': '微信'
}

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
      guidanceType: queryForm.guidanceType || undefined,
      startDate: queryForm.dateRange ? queryForm.dateRange[0] : undefined,
      endDate: queryForm.dateRange ? queryForm.dateRange[1] : undefined
    }
    const res = await request.get(API_PATH, { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取指导记录列表失败', e.message)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.planId = null
  form.studentId = null
  form.guidanceDate = ''
  form.guidanceType = ''
  form.topic = ''
  form.content = ''
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
  queryForm.guidanceType = null
  queryForm.dateRange = null
  queryForm.current = 1
  fetchData()
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增指导记录'
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = { ...row }
  viewDialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  dialogTitle.value = '编辑指导记录'
  editId.value = row.id
  form.planId = row.planId
  form.studentId = row.studentId
  form.guidanceDate = row.guidanceDate
  form.guidanceType = row.guidanceType
  form.topic = row.topic
  form.content = row.content
  if (row.attachmentPath) {
    attachmentFileList.value = [{ name: '附件', url: row.attachmentPath, status: 'success' }]
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
      guidanceDate: form.guidanceDate,
      guidanceType: form.guidanceType,
      topic: form.topic,
      content: form.content,
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
    console.warn('提交指导记录失败', e.message)
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该指导记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`${API_PATH}/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      console.warn('删除指导记录失败', e.message)
    }
  }).catch(() => {})
}

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
</style>
