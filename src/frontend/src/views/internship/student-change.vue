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
          v-model="queryForm.changeType"
          placeholder="异动类型"
          clearable
          style="width: 160px"
        >
          <el-option
            v-for="item in changeTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
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
        <el-input
          v-model="queryForm.studentName"
          placeholder="学生姓名"
          clearable
          style="width: 120px"
        />
        <el-input
          v-model="queryForm.studentNo"
          placeholder="学号"
          clearable
          style="width: 130px"
        />
        <el-input
          v-model="queryForm.majorName"
          placeholder="专业"
          clearable
          style="width: 120px"
        />
        <el-input
          v-model="queryForm.className"
          placeholder="班级"
          clearable
          style="width: 120px"
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
      </div>
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        stripe
      >
        <el-table-column
          prop="studentName"
          label="学生姓名"
          min-width="90"
        />
        <el-table-column
          prop="studentNo"
          label="学号"
          min-width="120"
        />
        <el-table-column
          prop="changeType"
          label="异动类型"
          min-width="120"
        >
          <template #default="{ row }">
            <el-tag :type="changeTypeTagMap[row.changeType] || 'info'">
              {{ changeTypeLabelMap[row.changeType] || row.changeType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="approvalStatus"
          label="审批状态"
          min-width="90"
        >
          <template #default="{ row }">
            <el-tag :type="approvalStatusTagMap[row.approvalStatus] || 'info'">
              {{ approvalStatusLabelMap[row.approvalStatus] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="reason"
          label="异动原因"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column
          prop="operatorName"
          label="操作人"
          min-width="90"
        />
        <el-table-column
          prop="changeTime"
          label="异动时间"
          min-width="170"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.changeTime) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          fixed="right"
          width="200"
          :resizable="false"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleViewDetail(row)"
            >
              查看详情
            </el-button>
            <template v-if="(userStore.isTeacher() || userStore.isDeptAdmin() || userStore.isAdmin()) && row.approvalStatus === 0">
              <el-button
                type="success"
                link
                @click="handleApprove(row)"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                link
                @click="handleOpenReject(row)"
              >
                驳回
              </el-button>
            </template>
            <el-button
              v-if="(userStore.isDeptAdmin() || userStore.isAdmin()) && row.approvalStatus === 1 && row.changeType === 'exempt'"
              type="danger"
              link
              @click="handleCancelExempt(row)"
            >
              取消免实习
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
      v-model="detailDialogVisible"
      title="异动详情"
      width="600px"
      destroy-on-close
    >
      <el-descriptions
        :column="2"
        border
      >
        <el-descriptions-item label="学生姓名">
          {{ detailData.studentName }}
        </el-descriptions-item>
        <el-descriptions-item label="学号">
          {{ detailData.studentNo }}
        </el-descriptions-item>
        <el-descriptions-item label="专业">
          {{ detailData.majorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="班级">
          {{ detailData.className || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="异动类型">
          <el-tag :type="changeTypeTagMap[detailData.changeType] || 'info'">
            {{ changeTypeLabelMap[detailData.changeType] || detailData.changeType }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="approvalStatusTagMap[detailData.approvalStatus] || 'info'">
            {{ approvalStatusLabelMap[detailData.approvalStatus] || '未知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="原指导老师">
          {{ detailData.changeType === 'reassign' ? (detailData.oldTeacherName || '-') : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="新指导老师">
          {{ detailData.changeType === 'reassign' ? (detailData.newTeacherName || '-') : '-' }}
        </el-descriptions-item>
        <el-descriptions-item
          label="异动原因"
          :span="2"
        >
          {{ detailData.reason || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="异动时间">
          {{ formatDateTime(detailData.changeTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ detailData.operatorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="detailData.approvalStatus === 2"
          label="驳回原因"
          :span="2"
        >
          {{ detailData.rejectReason || '-' }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="detailData.approverName"
          label="审批人"
        >
          {{ detailData.approverName }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回异动申请"
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
          prop="rejectReason"
        >
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请填写驳回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="rejectLoading"
          @click="confirmReject"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { formatDateTime } from '@/utils/dateFormat'
import { useTableResize } from '@/composables/useTableResize.js'

const userStore = useUserStore()
const tableRef = ref(null)
useTableResize(tableRef)

const planOptions = ref([])
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const queryForm = reactive({
  current: 1,
  size: 10,
  planId: null,
  changeType: null,
  approvalStatus: null,
  studentName: '',
  studentNo: '',
  majorName: '',
  className: ''
})

const changeTypeOptions = [
  { value: 'exempt', label: '免实习' },
  { value: 'reassign', label: '指导关系变更' },
  { value: 'resign', label: '提前离职' }
]

const changeTypeTagMap = {
  'exempt': 'success',
  'reassign': 'warning',
  'resign': 'danger'
}

const changeTypeLabelMap = {
  'exempt': '免实习',
  'reassign': '指导关系变更',
  'resign': '提前离职'
}

const approvalStatusOptions = [
  { value: 0, label: '待审批' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已驳回' }
]

const approvalStatusTagMap = {
  0: 'warning',
  1: 'success',
  2: 'danger'
}

const approvalStatusLabelMap = {
  0: '待审批',
  1: '已通过',
  2: '已驳回'
}

const detailDialogVisible = ref(false)
const detailData = ref({})

const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({
  rejectReason: ''
})
const rejectFormRules = {
  rejectReason: [
    { required: true, message: '请填写驳回原因', trigger: 'blur' },
    { min: 10, message: '驳回原因至少10个字', trigger: 'blur' }
  ]
}
const rejectingRow = ref(null)

const API_PATH = '/internship/student-changes'

async function loadPlanOptions() {
  try {
    const res = await request.get('/internship/plans', { params: { size: 999 } })
    planOptions.value = res.data.records || []
  } catch (e) {
    console.warn('获取实习计划列表失败', e.message)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      current: queryForm.current,
      size: queryForm.size,
      planId: queryForm.planId || undefined,
      changeType: queryForm.changeType || undefined,
      approvalStatus: queryForm.approvalStatus !== null && queryForm.approvalStatus !== '' ? queryForm.approvalStatus : undefined,
      studentName: queryForm.studentName || undefined,
      studentNo: queryForm.studentNo || undefined,
      majorName: queryForm.majorName || undefined,
      className: queryForm.className || undefined
    }
    const res = await request.get(API_PATH, { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取学生异动列表失败', e.message)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchData()
}

function handleReset() {
  queryForm.planId = null
  queryForm.changeType = null
  queryForm.approvalStatus = null
  queryForm.studentName = ''
  queryForm.studentNo = ''
  queryForm.majorName = ''
  queryForm.className = ''
  queryForm.current = 1
  fetchData()
}

function handleViewDetail(row) {
  detailData.value = { ...row }
  detailDialogVisible.value = true
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm('确定通过该异动申请？', '审批确认', { type: 'warning' })
    await request.put(`${API_PATH}/${row.id}/approve`)
    ElMessage.success('审批通过')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('审批通过失败', e.message)
    }
  }
}

function handleOpenReject(row) {
  rejectingRow.value = row
  rejectForm.rejectReason = ''
  nextTick(() => {
    rejectFormRef.value?.clearValidate()
  })
  rejectDialogVisible.value = true
}

async function confirmReject() {
  const valid = await rejectFormRef.value?.validate().catch(() => false)
  if (!valid) return

  rejectLoading.value = true
  try {
    await request.put(`${API_PATH}/${rejectingRow.value.id}/reject`, {
      rejectReason: rejectForm.rejectReason
    })
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('驳回失败', e.message)
  } finally {
    rejectLoading.value = false
  }
}

async function handleCancelExempt(row) {
  try {
    await ElMessageBox.confirm('确定取消该学生的免实习资格？', '取消免实习', { type: 'warning' })
    await request.put(`${API_PATH}/${row.id}/cancel-exempt`)
    ElMessage.success('已取消免实习')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('取消免实习失败', e.message)
    }
  }
}

onMounted(() => {
  loadPlanOptions()
  fetchData()
})

onBeforeUnmount(() => {
  detailDialogVisible.value = false
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
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
