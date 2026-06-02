<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
        <div class="filter-row">
          <el-input
            v-model="queryClassName"
            placeholder="请输入班级名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="queryGradeId"
            placeholder="请选择班级年级"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="grade in gradeOptions"
              :key="grade.id"
              :label="grade.gradeName"
              :value="grade.id"
            />
          </el-select>
          <el-select
            v-model="queryDeptId"
            placeholder="请选择专业所属学院"
            clearable
            :disabled="isDeptAdmin"
            style="width: 200px"
            @change="handleQueryDeptChange"
          >
            <el-option
              v-for="dept in deptOptions"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </div>
        <div class="filter-row">
          <el-select
            v-model="queryMajorId"
            :placeholder="queryDeptId ? '请选择该学院下的专业' : '请先选择所属学院'"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="major in filteredQueryMajorOptions"
              :key="major.id"
              :label="major.majorName"
              :value="major.id"
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
        </div>
      </div>

      <div class="toolbar">
        <el-button
          type="primary"
          :icon="Plus"
          @click="handleAdd"
        >
          新增
        </el-button>
        <el-button
          type="success"
          :icon="Edit"
          :disabled="selectedRows.length !== 1"
          @click="handleEdit(selectedRows[0])"
        >
          修改
        </el-button>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="50"
        />
        <el-table-column
          prop="id"
          label="班级ID"
          align="left"
        />
        <el-table-column
          prop="className"
          label="班级名称"
          align="left"
        />
        <el-table-column
          prop="gradeId"
          label="班级年级"
          align="center"
        >
          <template #default="{ row }">
            {{ getGradeName(row.gradeId) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="majorId"
          label="专业"
          align="left"
        >
          <template #default="{ row }">
            {{ getMajorName(row.majorId) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="deptId"
          label="所属学院"
          align="left"
          :resizable="false"
        >
          <template #default="{ row }">
            {{ getDeptName(row.deptId) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="current"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item
          label="班级名称"
          prop="className"
        >
          <el-input
            v-model="form.className"
            placeholder="如：软件201"
          />
        </el-form-item>
        <el-form-item
          label="班级年级"
          prop="gradeId"
        >
          <el-select
            v-model="form.gradeId"
            placeholder="请选择班级年级"
            style="width: 100%"
          >
            <el-option
              v-for="grade in gradeOptions"
              :key="grade.id"
              :label="grade.gradeName"
              :value="grade.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="所属学院"
          prop="deptId"
        >
          <el-select
            v-model="form.deptId"
            placeholder="请选择所属学院"
            :disabled="isDeptAdmin"
            style="width: 100%"
            @change="handleFormDeptChange"
          >
            <el-option
              v-for="dept in deptOptions"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="所属专业"
          prop="majorId"
        >
          <el-select
            v-model="form.majorId"
            placeholder="请选择所属专业"
            style="width: 100%"
          >
            <el-option
              v-for="major in filteredFormMajorOptions"
              :key="major.id"
              :label="major.majorName"
              :value="major.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="班主任"
          prop="classTeacher"
        >
          <el-input
            v-model="form.classTeacher"
            placeholder="请输入班主任"
          />
        </el-form-item>
        <el-form-item
          label="备注"
          prop="remark"
        >
          <el-input
            v-model="form.remark"
            type="textarea"
            placeholder="请输入备注"
          />
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit } from '@element-plus/icons-vue'
import { useTableResize } from '@/composables/useTableResize.js'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()

const tableRef = ref(null)
useTableResize(tableRef)
const isDeptAdmin = computed(() => userStore.isDeptAdmin())
const deptAdminDeptId = computed(() => userStore.userInfo?.deptId || null)

const queryClassName = ref('')
const queryGradeId = ref(null)
const queryDeptId = ref(null)
const queryMajorId = ref(null)
const current = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const selectedRows = ref([])

const deptOptions = ref([])
const majorOptions = ref([])
const gradeOptions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增班级')
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  className: '',
  gradeId: null,
  deptId: null,
  majorId: null,
  classTeacher: '',
  remark: ''
})

const formRules = {
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' }
  ],
  gradeId: [
    { required: true, message: '请选择班级年级', trigger: 'change' }
  ],
  deptId: [
    { required: true, message: '请选择所属学院', trigger: 'change' }
  ],
  majorId: [
    { required: true, message: '请选择所属专业', trigger: 'change' }
  ]
}

const filteredQueryMajorOptions = computed(() => {
  if (!queryDeptId.value) return majorOptions.value
  return majorOptions.value.filter(m => m.deptId === queryDeptId.value)
})

const filteredFormMajorOptions = computed(() => {
  if (!form.deptId) return majorOptions.value
  return majorOptions.value.filter(m => m.deptId === form.deptId)
})

function getMajorName(majorId) {
  const major = majorOptions.value.find(m => m.id === majorId)
  return major ? major.majorName : majorId
}

function getGradeName(gradeId) {
  const grade = gradeOptions.value.find(g => g.id === gradeId)
  return grade ? grade.gradeName : gradeId
}

function getDeptName(deptId) {
  const dept = deptOptions.value.find(d => d.id === deptId)
  return dept ? dept.deptName : deptId
}

function handleQueryDeptChange() {
  queryMajorId.value = null
}

function handleFormDeptChange() {
  form.majorId = null
}

const fetchDeptOptions = async () => {
  try {
    const res = await request.get('/admin/depts', { params: { size: 100 } })
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      deptOptions.value = res.data.records.filter(d => d.id === deptAdminDeptId.value)
    } else {
      deptOptions.value = res.data.records
    }
  } catch (e) {
    console.warn('获取学院列表失败', e.message)
  }
}

const fetchMajorOptions = async () => {
  try {
    const params = { size: 100 }
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      params.deptId = deptAdminDeptId.value
    }
    const res = await request.get('/admin/majors', { params })
    majorOptions.value = res.data.records
  } catch (e) {
    console.warn('获取专业列表失败', e.message)
  }
}

const fetchGradeOptions = async () => {
  try {
    const res = await request.get('/admin/grades', { params: { size: 100 } })
    gradeOptions.value = res.data.records
  } catch (e) {
    console.warn('获取年级列表失败', e.message)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      current: current.value,
      size: size.value,
      className: queryClassName.value || undefined,
      gradeId: queryGradeId.value || undefined,
      deptId: queryDeptId.value || undefined,
      majorId: queryMajorId.value || undefined
    }
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      params.deptId = deptAdminDeptId.value
    }
    const res = await request.get('/admin/classes', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取班级列表失败', e.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  current.value = 1
  fetchList()
}

const handleReset = () => {
  queryClassName.value = ''
  queryGradeId.value = null
  queryDeptId.value = isDeptAdmin.value ? deptAdminDeptId.value : null
  queryMajorId.value = null
  current.value = 1
  fetchList()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const resetForm = () => {
  form.className = ''
  form.gradeId = null
  form.deptId = null
  form.majorId = null
  form.classTeacher = ''
  form.remark = ''
  editId.value = null
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增班级'
  if (isDeptAdmin.value && deptAdminDeptId.value) {
    form.deptId = deptAdminDeptId.value
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  dialogTitle.value = '修改班级'
  editId.value = row.id
  form.className = row.className
  form.gradeId = row.gradeId
  form.deptId = row.deptId
  form.majorId = row.majorId
  form.classTeacher = row.classTeacher || ''
  form.remark = row.remark || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (editId.value) {
      await request.put(`/admin/classes/${editId.value}`, { ...form })
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/classes', { ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.warn('提交班级失败', e.message)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchDeptOptions()
  fetchMajorOptions()
  fetchGradeOptions()
  fetchList()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.filter-bar {
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
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
</style>
