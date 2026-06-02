<template>
  <div class="page-container">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable style="width: 180px">
            <el-option v-for="item in semesterOptions" :key="item.id" :label="item.semesterName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="queryForm.majorId" placeholder="请选择专业" clearable style="width: 180px">
            <el-option v-for="item in majorOptions" :key="item.id" :label="item.majorName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="queryForm.gradeId" placeholder="请选择年级" clearable style="width: 180px">
            <el-option v-for="item in gradeOptions" :key="item.id" :label="item.gradeName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="success" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div v-if="!isTeacher" class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增</el-button>
        <el-button type="success" :icon="Edit" :disabled="selectedRows.length !== 1" @click="handleEditSelected">修改</el-button>
        <el-button type="danger" :icon="Delete" :disabled="selectedRows.length === 0" @click="handleBatchDelete">删除</el-button>
        <el-button type="primary" :icon="User" :disabled="selectedRows.length !== 1" @click="handleAssignSelected">分配学生</el-button>
      </div>

      <el-table ref="tableRef" v-loading="loading" :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column v-if="!isTeacher" type="selection" width="50" />
        <el-table-column prop="planName" label="计划名称" />
        <el-table-column label="学期" align="center">
          <template #default="{ row }">{{ getSemesterName(row.semesterId) }}</template>
        </el-table-column>
        <el-table-column label="专业">
          <template #default="{ row }">{{ getMajorName(row.majorId) }}</template>
        </el-table-column>
        <el-table-column label="年级" align="center">
          <template #default="{ row }">{{ getGradeName(row.gradeId) }}</template>
        </el-table-column>
        <el-table-column prop="internshipType" label="实习类型" align="center" />
        <el-table-column prop="startDate" label="开始日期" align="center">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" align="center">
          <template #default="{ row }">
            {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" :resizable="false">
          <template #default="{ row }">
            <el-button v-if="!isTeacher" type="primary" link @click="handleAssignRow(row)">分配学生</el-button>
            <el-button v-if="!isTeacher" type="primary" link @click="handleEdit(row)">修改</el-button>
            <el-button v-if="!isTeacher" type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="computedRules" label-width="160px">
        <el-divider content-position="left">基础信息</el-divider>
        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="form.planName" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="学期" prop="semesterId">
          <el-select v-model="form.semesterId" placeholder="请选择学期" style="width: 100%">
            <el-option v-for="item in semesterOptions" :key="item.id" :label="item.semesterName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="gradeId">
          <el-select v-model="form.gradeId" placeholder="请选择年级" style="width: 100%">
            <el-option v-for="item in gradeOptions" :key="item.id" :label="item.gradeName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" prop="majorId">
          <el-select v-model="form.majorId" placeholder="请选择专业" style="width: 100%">
            <el-option v-for="item in majorOptions" :key="item.id" :label="item.majorName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程编号" prop="courseCode">
          <el-input v-model="form.courseCode" placeholder="请输入课程编号" />
        </el-form-item>
        <el-form-item label="实习类型" prop="internshipType">
          <el-select v-model="form.internshipType" placeholder="请选择实习类型" style="width: 100%">
            <el-option v-for="item in internshipTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="实习开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" placeholder="请选择开始日期" value-format="YYYY-MM-DD" format="YYYY年MM月DD日" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实习结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" placeholder="请选择结束日期" value-format="YYYY-MM-DD" format="YYYY年MM月DD日" style="width: 100%" />
        </el-form-item>

        <el-divider content-position="left">保险补贴</el-divider>
        <el-form-item label="是否购买保险" prop="hasInsurance">
          <el-radio-group v-model="form.hasInsurance">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.hasInsurance === 1" label="保险费用" prop="insuranceFee">
          <el-input-number v-model="form.insuranceFee" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实习补贴" prop="subsidy">
          <el-input-number v-model="form.subsidy" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>

        <el-divider content-position="left">成绩占比配置</el-divider>
        <el-form-item label="签到占比" prop="weightCheckin">
          <el-input-number v-model="form.weightCheckin" :min="0" :max="100" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="日报占比" prop="weightDailyReport">
          <el-input-number v-model="form.weightDailyReport" :min="0" :max="100" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="周报占比" prop="weightWeeklyReport">
          <el-input-number v-model="form.weightWeeklyReport" :min="0" :max="100" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="月报占比" prop="weightMonthlyReport">
          <el-input-number v-model="form.weightMonthlyReport" :min="0" :max="100" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="校内考核占比" prop="weightInnerAssessment">
          <el-input-number v-model="form.weightInnerAssessment" :min="0" :max="100" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="校外考核占比" prop="weightOuterAssessment">
          <el-input-number v-model="form.weightOuterAssessment" :min="0" :max="100" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实习报告占比" prop="weightReport">
          <el-input-number v-model="form.weightReport" :min="0" :max="100" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="占比总和">
          <span :class="{ 'weight-error': weightSum !== 100 }">{{ weightSum }} / 100</span>
        </el-form-item>

        <el-divider content-position="left">详细说明</el-divider>
        <el-form-item label="详细说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入详细说明" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, User } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { formatDate } from '@/utils/dateFormat'
import { useTableResize } from '@/composables/useTableResize.js'

const router = useRouter()
const userStore = useUserStore()

const tableRef = ref(null)
useTableResize(tableRef)

const isTeacher = computed(() => userStore.isTeacher())
const isMajorDirector = computed(() => userStore.isMajorDirector())

const internshipTypeOptions = [
  { value: '认识实习', label: '认识实习' },
  { value: '跟岗实习', label: '跟岗实习' },
  { value: '顶岗实习', label: '顶岗实习' }
]

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const selectedRows = ref([])

const semesterOptions = ref([])
const gradeOptions = ref([])
const majorOptions = ref([])

const queryForm = reactive({
  current: 1,
  size: 10,
  semesterId: null,
  majorId: null,
  gradeId: null
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增实习计划')
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  planName: '',
  semesterId: null,
  gradeId: null,
  majorId: null,
  courseCode: '',
  internshipType: '',
  startDate: '',
  endDate: '',
  hasInsurance: 0,
  insuranceFee: null,
  subsidy: null,
  weightCheckin: 0,
  weightDailyReport: 0,
  weightWeeklyReport: 0,
  weightMonthlyReport: 0,
  weightInnerAssessment: 0,
  weightOuterAssessment: 0,
  weightReport: 0,
  description: ''
})

const validateEndDate = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请选择结束日期'))
  } else if (form.startDate && value < form.startDate) {
    callback(new Error('结束日期必须晚于开始日期'))
  } else {
    callback()
  }
}

const baseRules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  semesterId: [{ required: true, message: '请选择学期', trigger: 'change' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  internshipType: [{ required: true, message: '请选择实习类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, validator: validateEndDate, trigger: 'change' }],
  hasInsurance: [{ required: true, message: '请选择是否购买保险', trigger: 'change' }],
  weightCheckin: [{ required: true, message: '请输入签到占比', trigger: 'blur' }],
  weightDailyReport: [{ required: true, message: '请输入日报占比', trigger: 'blur' }],
  weightWeeklyReport: [{ required: true, message: '请输入周报占比', trigger: 'blur' }],
  weightMonthlyReport: [{ required: true, message: '请输入月报占比', trigger: 'blur' }],
  weightInnerAssessment: [{ required: true, message: '请输入校内考核占比', trigger: 'blur' }],
  weightOuterAssessment: [{ required: true, message: '请输入校外考核占比', trigger: 'blur' }],
  weightReport: [{ required: true, message: '请输入实习报告占比', trigger: 'blur' }]
}

const computedRules = computed(() => {
  const rules = { ...baseRules }
  if (form.hasInsurance === 1) {
    rules.insuranceFee = [{ required: true, message: '请输入保险费用', trigger: 'blur' }]
  }
  return rules
})

const weightSum = computed(() => {
  return (form.weightCheckin || 0) + (form.weightDailyReport || 0) + (form.weightWeeklyReport || 0) + (form.weightMonthlyReport || 0) + (form.weightInnerAssessment || 0) + (form.weightOuterAssessment || 0) + (form.weightReport || 0)
})

function getSemesterName(id) {
  const item = semesterOptions.value.find(s => s.id === id)
  return item ? item.semesterName : id || ''
}

function getGradeName(id) {
  const item = gradeOptions.value.find(g => g.id === id)
  return item ? item.gradeName : id || ''
}

function getMajorName(id) {
  const item = majorOptions.value.find(m => m.id === id)
  return item ? item.majorName : id || ''
}

async function loadSemesterOptions() {
  try {
    const res = await request.get('/admin/semesters', { params: { current: 1, size: 100 } })
    semesterOptions.value = res.data.records
  } catch (e) {
    console.warn('获取学期列表失败', e.message)
  }
}

async function loadGradeOptions() {
  try {
    const res = await request.get('/admin/grades', { params: { current: 1, size: 100 } })
    gradeOptions.value = res.data.records
  } catch (e) {
    console.warn('获取年级列表失败', e.message)
  }
}

async function loadMajorOptions() {
  try {
    if (isTeacher.value || isMajorDirector.value) {
      const res = await request.get('/internship/plans/teacher-majors')
      majorOptions.value = res.data || []
    } else {
      const res = await request.get('/admin/majors', { params: { current: 1, size: 100 } })
      majorOptions.value = res.data.records
    }
  } catch (e) {
    console.warn('获取专业列表失败', e.message)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/internship/plans', {
      params: {
        current: queryForm.current,
        size: queryForm.size,
        semesterId: queryForm.semesterId || undefined,
        majorId: queryForm.majorId || undefined,
        gradeId: queryForm.gradeId || undefined
      }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取实习计划列表失败', e.message)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchData()
}

function handleReset() {
  queryForm.semesterId = null
  queryForm.majorId = null
  queryForm.gradeId = null
  queryForm.current = 1
  fetchData()
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function resetForm() {
  form.planName = ''
  form.semesterId = null
  form.gradeId = null
  form.majorId = null
  form.courseCode = ''
  form.internshipType = ''
  form.startDate = ''
  form.endDate = ''
  form.hasInsurance = 0
  form.insuranceFee = null
  form.subsidy = null
  form.weightCheckin = 0
  form.weightDailyReport = 0
  form.weightWeeklyReport = 0
  form.weightMonthlyReport = 0
  form.weightInnerAssessment = 0
  form.weightOuterAssessment = 0
  form.weightReport = 0
  form.description = ''
  editId.value = null
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增实习计划'
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  dialogTitle.value = '修改实习计划'
  editId.value = row.id
  form.planName = row.planName || ''
  form.semesterId = row.semesterId || null
  form.gradeId = row.gradeId || null
  form.majorId = row.majorId || null
  form.courseCode = row.courseCode || ''
  form.internshipType = row.internshipType || ''
  form.startDate = row.startDate || ''
  form.endDate = row.endDate || ''
  form.hasInsurance = row.hasInsurance != null ? row.hasInsurance : 0
  form.insuranceFee = row.insuranceFee != null ? row.insuranceFee : null
  form.subsidy = row.subsidy != null ? row.subsidy : null
  form.weightCheckin = row.weightCheckin || 0
  form.weightDailyReport = row.weightDailyReport || 0
  form.weightWeeklyReport = row.weightWeeklyReport || 0
  form.weightMonthlyReport = row.weightMonthlyReport || 0
  form.weightInnerAssessment = row.weightInnerAssessment || 0
  form.weightOuterAssessment = row.weightOuterAssessment || 0
  form.weightReport = row.weightReport || 0
  form.description = row.description || ''
  dialogVisible.value = true
}

function handleEditSelected() {
  if (selectedRows.value.length !== 1) return
  handleEdit(selectedRows.value[0])
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (weightSum.value !== 100) {
    ElMessage.error('成绩占比总和必须等于100')
    return
  }

  submitLoading.value = true
  try {
    const submitData = { ...form }
    if (editId.value) {
      await request.put(`/internship/plans/${editId.value}`, submitData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/internship/plans', submitData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('提交实习计划失败', e.message)
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除实习计划"${row.planName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/internship/plans/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      console.warn('删除实习计划失败', e.message)
    }
  }).catch(() => {})
}

function handleBatchDelete() {
  if (selectedRows.value.length === 0) return
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条实习计划吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      for (const row of selectedRows.value) {
        await request.delete(`/internship/plans/${row.id}`)
      }
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      console.warn('删除实习计划失败', e.message)
    }
  }).catch(() => {})
}

function handleAssignRow(row) {
  router.push(`/internship/plan-assign/${row.id}`)
}

function handleAssignSelected() {
  if (selectedRows.value.length !== 1) return
  handleAssignRow(selectedRows.value[0])
}

onMounted(() => {
  loadSemesterOptions()
  loadGradeOptions()
  loadMajorOptions()
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.toolbar {
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

.weight-error {
  color: #f56c6c;
  font-weight: bold;
}
</style>
