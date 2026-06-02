<template>
  <div class="page-container">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <div class="filter-row">
          <el-form-item label="学号">
            <el-input v-model="queryForm.studentNo" placeholder="请输入学号" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="queryForm.name" placeholder="请输入姓名" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item label="专业">
            <el-select v-model="queryForm.majorId" placeholder="请选择" clearable style="width: 180px">
              <el-option v-for="major in majorOptions" :key="major.id" :label="major.majorName" :value="major.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级">
            <el-select v-model="queryForm.classId" placeholder="请选择班级" clearable style="width: 180px">
              <el-option v-for="cls in classOptions" :key="cls.id" :label="cls.className" :value="cls.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="queryForm.gradeId" placeholder="请选择年级" clearable style="width: 180px">
              <el-option v-for="grade in gradeOptions" :key="grade.id" :label="grade.gradeName" :value="grade.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="是否免实习">
            <el-select v-model="queryForm.exemptInternship" placeholder="请选择是否免实习" clearable style="width: 180px">
              <el-option label="是" :value="1" />
              <el-option label="否" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="success" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </div>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增</el-button>
        <el-button type="success" :icon="Edit" :disabled="selectedRows.length !== 1" @click="handleEditSelected">修改</el-button>
      </div>

      <el-table ref="tableRef" v-loading="loading" :data="tableData" border stripe style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="studentNo" label="学号" min-width="130" />
        <el-table-column prop="gender" label="性别" align="center" min-width="80" />
        <el-table-column label="年级" align="center" min-width="100">
          <template #default="{ row }">{{ getGradeName(row.gradeId) }}</template>
        </el-table-column>
        <el-table-column label="院系" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ getDeptName(row.deptId) }}</template>
        </el-table-column>
        <el-table-column label="专业" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ getMajorName(row.majorId) }}</template>
        </el-table-column>
        <el-table-column label="班级" min-width="130">
          <template #default="{ row }">{{ getClassName(row.classId) }}</template>
        </el-table-column>
        <el-table-column prop="educationLevel" label="层次" align="center" min-width="80" />
        <el-table-column prop="schoolingYear" label="学生学制(年)" align="center" min-width="120" />
        <el-table-column prop="phone" label="电话" min-width="120" />
        <el-table-column label="是否免实习" align="center" min-width="110" :resizable="false">
          <template #default="{ row }">
            <el-tag :type="row.exemptInternship === 1 ? 'danger' : 'info'">{{ row.exemptInternship === 1 ? '是' : '否' }}</el-tag>
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
      <el-form ref="formRef" :model="form" :rules="computedRules" label-width="110px">
        <div class="dialog-form-columns">
          <div class="dialog-form-col">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="请输入学号" />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号码" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="入学年份">
              <el-input v-model="derivedGradeName" readonly placeholder="选择班级后自动填充" />
            </el-form-item>
            <el-form-item label="所属学院" prop="deptId">
              <el-select v-model="form.deptId" placeholder="请选择所属学院" :disabled="isDeptAdmin" style="width: 100%" @change="handleDeptChange">
                <el-option v-for="dept in deptOptions" :key="dept.id" :label="dept.deptName" :value="dept.id" />
              </el-select>
            </el-form-item>
          </div>
          <div class="dialog-form-col">
            <el-form-item label="所属专业" prop="majorId">
              <el-select v-model="form.majorId" placeholder="请选择所属专业" style="width: 100%" @change="handleMajorChange">
                <el-option v-for="major in formMajorOptions" :key="major.id" :label="major.majorName" :value="major.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="所属班级" prop="classId">
              <el-select v-model="form.classId" placeholder="请选择所属班级" style="width: 100%" @change="handleClassChange">
                <el-option v-for="cls in formClassOptions" :key="cls.id" :label="cls.className" :value="cls.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="学历层次">
              <el-input v-model="derivedEducationLevel" readonly placeholder="选择专业后自动填充" />
            </el-form-item>
            <el-form-item label="学制年限">
              <el-input v-model="derivedSchoolingYear" readonly placeholder="选择专业后自动填充" />
            </el-form-item>
            <el-form-item label="是否免实习" prop="exemptInternship">
              <el-radio-group v-model="form.exemptInternship">
                <el-radio :value="1">是</el-radio>
                <el-radio :value="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="form.exemptInternship === 1" label="免实习原因" prop="exemptReason">
              <el-input v-model="form.exemptReason" type="textarea" :rows="3" placeholder="请输入免实习原因（至少20字）" />
            </el-form-item>
            <el-form-item label="家庭住址" prop="homeAddress">
              <el-input v-model="form.homeAddress" placeholder="请输入家庭住址" />
            </el-form-item>
            <el-form-item label="家长联系电话" prop="parentPhone">
              <el-input v-model="form.parentPhone" placeholder="请输入家长联系电话" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </div>
        </div>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { useTableResize } from '@/composables/useTableResize.js'

const userStore = useUserStore()
const isDeptAdmin = computed(() => userStore.isDeptAdmin())
const deptAdminDeptId = computed(() => userStore.userInfo?.deptId || null)

const tableRef = ref(null)
useTableResize(tableRef)

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增学生')
const editId = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)
const selectedRows = ref([])

const deptOptions = ref([])
const majorOptions = ref([])
const gradeOptions = ref([])
const classOptions = ref([])

const formMajorOptions = ref([])
const formClassOptions = ref([])

const derivedGradeName = ref('')
const derivedEducationLevel = ref('')
const derivedSchoolingYear = ref('')

const queryForm = reactive({
  current: 1,
  size: 10,
  studentNo: '',
  name: '',
  majorId: null,
  classId: null,
  gradeId: null,
  exemptInternship: null
})

const form = reactive({
  studentNo: '',
  name: '',
  gender: '',
  idCard: '',
  phone: '',
  email: '',
  gradeId: null,
  deptId: null,
  majorId: null,
  classId: null,
  educationLevel: '',
  schoolingYear: null,
  exemptInternship: 0,
  exemptReason: '',
  homeAddress: '',
  parentPhone: '',
  remark: ''
})

const phoneValidator = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

const exemptReasonValidator = (rule, value, callback) => {
  if (form.exemptInternship === 1) {
    if (!value || value.trim().length < 20) {
      callback(new Error('免实习原因至少输入20个字符'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const baseRules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^\d{10,12}$/, message: '学号为10-12位数字', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { validator: phoneValidator, trigger: 'blur' }
  ],
  deptId: [{ required: true, message: '请选择所属学院', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择所属专业', trigger: 'change' }],
  classId: [{ required: true, message: '请选择所属班级', trigger: 'change' }],
  exemptInternship: [{ required: true, message: '请选择是否免实习', trigger: 'change' }],
  exemptReason: [{ validator: exemptReasonValidator, trigger: 'blur' }],
  parentPhone: [{ validator: phoneValidator, trigger: 'blur' }]
}

const computedRules = computed(() => {
  const rules = { ...baseRules }
  if (form.exemptInternship === 1) {
    rules.exemptReason = [
      { required: true, message: '请输入免实习原因', trigger: 'blur' },
      { min: 20, message: '免实习原因至少输入20个字符', trigger: 'blur' }
    ]
  }
  return rules
})

function getDeptName(deptId) {
  const dept = deptOptions.value.find(d => d.id === deptId)
  return dept ? dept.deptName : deptId || ''
}

function getMajorName(majorId) {
  const major = majorOptions.value.find(m => m.id === majorId)
  return major ? major.majorName : majorId || ''
}

function getGradeName(gradeId) {
  const grade = gradeOptions.value.find(g => g.id === gradeId)
  return grade ? grade.gradeName : gradeId || ''
}

function getClassName(classId) {
  const cls = classOptions.value.find(c => c.id === classId)
  return cls ? cls.className : classId || ''
}

async function fetchDeptOptions() {
  try {
    const res = await request.get('/admin/depts', { params: { size: 1000 } })
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      deptOptions.value = res.data.records.filter(d => d.id === deptAdminDeptId.value)
    } else {
      deptOptions.value = res.data.records
    }
  } catch (e) {
    console.warn('获取院系列表失败', e.message)
  }
}

async function fetchMajorOptions() {
  try {
    const params = { size: 1000 }
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      params.deptId = deptAdminDeptId.value
    }
    const res = await request.get('/admin/majors', { params })
    majorOptions.value = res.data.records
  } catch (e) {
    console.warn('获取专业列表失败', e.message)
  }
}

async function fetchGradeOptions() {
  try {
    const res = await request.get('/admin/grades', { params: { size: 1000 } })
    gradeOptions.value = res.data.records
  } catch (e) {
    console.warn('获取年级列表失败', e.message)
  }
}

async function fetchClassOptions() {
  try {
    const res = await request.get('/admin/classes', { params: { size: 1000 } })
    classOptions.value = res.data.records
  } catch (e) {
    console.warn('获取班级列表失败', e.message)
  }
}

async function fetchFormMajorOptions(deptId) {
  if (!deptId) {
    formMajorOptions.value = []
    return
  }
  try {
    const res = await request.get('/admin/majors', { params: { deptId, size: 1000 } })
    formMajorOptions.value = res.data.records
  } catch (e) {
    console.warn('获取专业列表失败', e.message)
  }
}

async function fetchFormClassOptions(majorId) {
  if (!majorId) {
    formClassOptions.value = []
    return
  }
  try {
    const res = await request.get('/admin/classes', { params: { majorId, size: 1000 } })
    formClassOptions.value = res.data.records
  } catch (e) {
    console.warn('获取班级列表失败', e.message)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      current: queryForm.current,
      size: queryForm.size,
      studentNo: queryForm.studentNo || undefined,
      name: queryForm.name || undefined,
      majorId: queryForm.majorId || undefined,
      classId: queryForm.classId || undefined,
      gradeId: queryForm.gradeId || undefined,
      exemptInternship: queryForm.exemptInternship !== null && queryForm.exemptInternship !== undefined ? queryForm.exemptInternship : undefined
    }
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      params.deptId = deptAdminDeptId.value
    }
    const res = await request.get('/admin/students', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取学生列表失败', e.message)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchData()
}

function handleReset() {
  queryForm.studentNo = ''
  queryForm.name = ''
  queryForm.majorId = null
  queryForm.classId = null
  queryForm.gradeId = null
  queryForm.exemptInternship = null
  queryForm.current = 1
  fetchData()
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function resetForm() {
  form.studentNo = ''
  form.name = ''
  form.gender = ''
  form.idCard = ''
  form.phone = ''
  form.email = ''
  form.gradeId = null
  form.deptId = null
  form.majorId = null
  form.classId = null
  form.educationLevel = ''
  form.schoolingYear = null
  form.exemptInternship = 0
  form.exemptReason = ''
  form.homeAddress = ''
  form.parentPhone = ''
  form.remark = ''
  editId.value = null
  formMajorOptions.value = []
  formClassOptions.value = []
  derivedGradeName.value = ''
  derivedEducationLevel.value = ''
  derivedSchoolingYear.value = ''
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增学生'
  if (isDeptAdmin.value && deptAdminDeptId.value) {
    form.deptId = deptAdminDeptId.value
    fetchFormMajorOptions(form.deptId)
  }
  dialogVisible.value = true
}

async function handleEditSelected() {
  if (selectedRows.value.length !== 1) return
  const row = selectedRows.value[0]
  resetForm()
  dialogTitle.value = '编辑学生'
  editId.value = row.id
  form.studentNo = row.studentNo || ''
  form.name = row.name || ''
  form.gender = row.gender || ''
  form.idCard = row.idCard || ''
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.gradeId = row.gradeId || null
  form.deptId = row.deptId || null
  form.majorId = row.majorId || null
  form.classId = row.classId || null
  form.educationLevel = row.educationLevel || ''
  form.schoolingYear = row.schoolingYear != null ? Number(row.schoolingYear) : null
  form.exemptInternship = row.exemptInternship != null ? row.exemptInternship : 0
  form.exemptReason = row.exemptReason || ''
  form.homeAddress = row.homeAddress || ''
  form.parentPhone = row.parentPhone || ''
  form.remark = row.remark || ''
  if (form.deptId) {
    await fetchFormMajorOptions(form.deptId)
  }
  if (form.majorId) {
    await fetchFormClassOptions(form.majorId)
    const major = formMajorOptions.value.find(m => m.id === form.majorId)
    if (major) {
      derivedEducationLevel.value = major.majorLevel || ''
      derivedSchoolingYear.value = major.schoolingYear != null ? Number(major.schoolingYear) : null
    }
  }
  if (form.gradeId) {
    const grade = gradeOptions.value.find(g => g.id === form.gradeId)
    derivedGradeName.value = grade ? grade.gradeName : ''
  }
  dialogVisible.value = true
}

async function handleDeptChange(deptId) {
  form.majorId = null
  form.classId = null
  formMajorOptions.value = []
  formClassOptions.value = []
  derivedEducationLevel.value = ''
  derivedSchoolingYear.value = ''
  derivedGradeName.value = ''
  if (deptId) {
    await fetchFormMajorOptions(deptId)
  }
}

async function handleMajorChange(majorId) {
  form.classId = null
  derivedEducationLevel.value = ''
  derivedSchoolingYear.value = ''
  formClassOptions.value = []
  if (majorId) {
    const major = formMajorOptions.value.find(m => m.id === majorId)
    if (major) {
      derivedEducationLevel.value = major.majorLevel || ''
      derivedSchoolingYear.value = major.schoolingYear != null ? Number(major.schoolingYear) : null
    }
    await fetchFormClassOptions(majorId)
  }
}

function handleClassChange(classId) {
  derivedGradeName.value = ''
  if (classId) {
    const cls = formClassOptions.value.find(c => c.id === classId)
    if (cls && cls.gradeId) {
      const grade = gradeOptions.value.find(g => g.id === cls.gradeId)
      derivedGradeName.value = grade ? grade.gradeName : ''
    }
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (editId.value) {
      await request.put(`/admin/students/${editId.value}`, { ...form })
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/students', { ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('提交失败', e.message)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchDeptOptions()
  fetchMajorOptions()
  fetchGradeOptions()
  fetchClassOptions()
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.filter-form .filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 0;
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

.dialog-form-columns {
  display: flex;
  gap: 24px;
}

.dialog-form-col {
  flex: 1;
  min-width: 0;
}
</style>
