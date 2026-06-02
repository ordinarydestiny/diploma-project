<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
          <el-input
            v-model="queryForm.teacherNo"
            placeholder="请输入工号"
            clearable
            style="width: 200px"
          />
          <el-input
            v-model="queryForm.name"
            placeholder="请输入姓名"
            clearable
            style="width: 200px"
          />
          <el-input
            v-model="queryForm.phone"
            placeholder="请输入手机号码"
            clearable
            style="width: 200px"
          />
          <el-select
            v-model="queryForm.status"
            placeholder="工号状态"
            clearable
            style="width: 200px"
          >
            <el-option
              label="全部"
              value=""
            />
            <el-option
              label="启用"
              :value="1"
            />
            <el-option
              label="停用"
              :value="0"
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
          @click="handleEditSelected"
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
          label="用户编号"
        />
        <el-table-column
          prop="teacherNo"
          label="工号"
        />
        <el-table-column
          prop="name"
          label="老师姓名"
        />
        <el-table-column
          prop="teacherType"
          label="老师类型"
          align="center"
        >
          <template #default="{ row }">
            {{ teacherTypeMap[row.teacherType] }}
          </template>
        </el-table-column>
        <el-table-column
          label="部门"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ getDeptName(row.deptId) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="phone"
          label="手机号码"
        />
        <el-table-column
          label="状态"
          align="center"
        >
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column
          label="负责专业"
          align="center"
        >
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAssignMajors(row)">分配专业</el-button>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          min-width="180"
          :resizable="false"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
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
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="工号"
          prop="teacherNo"
        >
          <el-input
            v-model="form.teacherNo"
            maxlength="20"
            placeholder="请输入工号"
            :disabled="!!editId"
          />
        </el-form-item>
        <el-form-item
          label="姓名"
          prop="name"
        >
          <el-input
            v-model="form.name"
            placeholder="请输入姓名"
            :disabled="!!editId"
          />
        </el-form-item>
        <el-form-item
          label="手机号码"
          prop="phone"
        >
          <el-input
            v-model="form.phone"
            maxlength="11"
            placeholder="请输入手机号码"
          />
        </el-form-item>
        <el-form-item
          label="身份证号"
          prop="idCard"
        >
          <el-input
            v-model="form.idCard"
            maxlength="18"
            placeholder="请输入身份证号"
          />
        </el-form-item>
        <el-form-item
          label="老师类型"
          prop="teacherType"
        >
          <el-select
            v-model="form.teacherType"
            placeholder="请选择老师类型"
            style="width: 100%"
          >
            <el-option
              v-for="(label, value) in teacherTypeMap"
              :key="value"
              :label="label"
              :value="Number(value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="所属部门"
          prop="deptId"
        >
          <el-select
            v-model="form.deptId"
            placeholder="请选择所属部门"
            :disabled="isDeptAdmin"
            style="width: 100%"
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
          label="邮箱"
          prop="email"
        >
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
        <el-form-item
          v-if="!editId"
          label="初始密码"
        >
          <el-input
            model-value="默认工号后6位"
            readonly
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
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
    <el-dialog
      v-model="majorDialogVisible"
      title="分配负责专业"
      width="500px"
      destroy-on-close
    >
      <p style="margin-bottom: 12px; color: #909399;">为教师 <strong>{{ majorAssignTeacherName }}</strong> 分配负责的专业：</p>
      <el-select
        v-model="selectedMajorIds"
        multiple
        placeholder="请选择专业"
        style="width: 100%"
      >
        <el-option
          v-for="major in allMajorOptions"
          :key="major.id"
          :label="major.majorName"
          :value="major.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="majorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="majorSubmitLoading" @click="handleMajorSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/dateFormat'
import { useTableResize } from '@/composables/useTableResize.js'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()

const tableRef = ref(null)
useTableResize(tableRef)
const isDeptAdmin = computed(() => userStore.isDeptAdmin())
const deptAdminDeptId = computed(() => userStore.userInfo?.deptId || null)

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增老师')
const editId = ref(null)
const formRef = ref(null)
const deptOptions = ref([])
const selectedRows = ref([])
const majorDialogVisible = ref(false)
const majorAssignTeacherId = ref(null)
const majorAssignTeacherName = ref('')
const selectedMajorIds = ref([])
const allMajorOptions = ref([])
const majorSubmitLoading = ref(false)

const teacherTypeMap = {
  1: '校内专任教师',
  2: '校外兼职教师',
  3: '行政人员',
  4: '实习指导教师'
}

const queryForm = reactive({
  current: 1,
  size: 10,
  teacherNo: '',
  name: '',
  phone: '',
  status: ''
})

const form = reactive({
  teacherNo: '',
  name: '',
  phone: '',
  idCard: '',
  teacherType: null,
  deptId: null,
  email: '',
  remark: ''
})

const phoneValidator = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

const rules = {
  teacherNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { validator: phoneValidator, trigger: 'blur' }
  ],
  idCard: [
    { len: 18, message: '身份证号必须为18位', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  teacherType: [{ required: true, message: '请选择老师类型', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
}

function getDeptName(deptId) {
  const dept = deptOptions.value.find(item => item.id === deptId)
  return dept ? dept.deptName : deptId
}

async function loadDeptOptions() {
  try {
    const res = await request.get('/admin/depts', { params: { current: 1, size: 1000 } })
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      deptOptions.value = res.data.records.filter(d => d.id === deptAdminDeptId.value)
    } else {
      deptOptions.value = res.data.records
    }
  } catch (e) {
    console.warn('获取部门列表失败', e.message)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      params.deptId = deptAdminDeptId.value
    }
    const res = await request.get('/admin/teachers', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取老师列表失败', e.message)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchData()
}

function handleReset() {
  queryForm.teacherNo = ''
  queryForm.name = ''
  queryForm.phone = ''
  queryForm.status = ''
  queryForm.current = 1
  fetchData()
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function handleAdd() {
  editId.value = null
  dialogTitle.value = '新增老师'
  Object.assign(form, {
    teacherNo: '',
    name: '',
    phone: '',
    idCard: '',
    teacherType: null,
    deptId: isDeptAdmin.value ? deptAdminDeptId.value : null,
    email: '',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEditSelected() {
  if (selectedRows.value.length !== 1) return
  handleEdit(selectedRows.value[0])
}

function handleEdit(row) {
  editId.value = row.id
  dialogTitle.value = '修改老师'
  Object.assign(form, {
    teacherNo: row.teacherNo,
    name: row.name,
    phone: row.phone,
    idCard: row.idCard || '',
    teacherType: row.teacherType,
    deptId: row.deptId,
    email: row.email || '',
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editId.value) {
      await request.put(`/admin/teachers/${editId.value}`, form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/teachers', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('提交失败', e.message)
  }
}

function handleStatusChange(row) {
  if (row.status === 0) {
    ElMessageBox.confirm('确定要停用该教师账号吗？停用后该教师将无法登录系统。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await request.put(`/admin/teachers/${row.id}/status`, { status: 0 })
        ElMessage.success('停用成功')
        fetchData()
      } catch (e) {
        row.status = 1
        console.warn('停用失败', e.message)
      }
    }).catch(() => {
      row.status = 1
    })
  } else {
    request.put(`/admin/teachers/${row.id}/status`, { status: 1 }).then(() => {
      ElMessage.success('启用成功')
    }).catch((e) => {
      row.status = 0
      console.warn('启用失败', e.message)
    })
  }
}

onMounted(() => {
  loadDeptOptions()
  loadAllMajorOptions()
  fetchData()
})

async function loadAllMajorOptions() {
  try {
    const params = { current: 1, size: 100 }
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      params.deptId = deptAdminDeptId.value
    }
    const res = await request.get('/admin/majors', { params })
    allMajorOptions.value = res.data.records
  } catch (e) {
    console.warn('获取专业列表失败', e.message)
  }
}

async function handleAssignMajors(row) {
  majorAssignTeacherId.value = row.id
  majorAssignTeacherName.value = row.name
  try {
    const res = await request.get(`/admin/teachers/${row.id}/majors`)
    selectedMajorIds.value = (res.data || []).map(m => m.id)
  } catch (e) {
    selectedMajorIds.value = []
  }
  majorDialogVisible.value = true
}

async function handleMajorSubmit() {
  majorSubmitLoading.value = true
  try {
    await request.put(`/admin/teachers/${majorAssignTeacherId.value}/majors`, {
      majorIds: selectedMajorIds.value
    })
    ElMessage.success('专业分配成功')
    majorDialogVisible.value = false
  } catch (e) {
    console.warn('专业分配失败', e.message)
  } finally {
    majorSubmitLoading.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 10px;
}

.filter-bar :deep(.el-input),
.filter-bar :deep(.el-select) {
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
</style>
