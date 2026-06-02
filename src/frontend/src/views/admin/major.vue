<template>
  <div class="page-container">
    <el-card>
      <el-form
        :model="queryForm"
        inline
        class="filter-bar"
      >
        <div class="filter-row">
          <el-form-item label="专业层次">
            <el-select
              v-model="queryForm.majorLevel"
              placeholder="请选择专业层次"
              clearable
              style="width: 180px"
            >
              <el-option
                label="高职"
                value="高职"
              />
              <el-option
                label="本科"
                value="本科"
              />
              <el-option
                label="研究生"
                value="研究生"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="专业名称">
            <el-input
              v-model="queryForm.majorName"
              placeholder="请输入专业名称"
              clearable
              style="width: 180px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="专业代码">
            <el-input
              v-model="queryForm.majorCode"
              placeholder="请输入专业代码"
              clearable
              style="width: 180px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="专业所属学院">
            <el-select
              v-model="queryForm.deptId"
              placeholder="请选择专业所属学院"
              clearable
              :disabled="isDeptAdmin"
              style="width: 180px"
            >
              <el-option
                v-for="dept in deptOptions"
                :key="dept.id"
                :label="dept.deptName"
                :value="dept.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
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
          </el-form-item>
        </div>
      </el-form>

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
          @click="handleEdit"
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
          label="专业ID"
          align="left"
        />
        <el-table-column
          prop="majorName"
          label="专业名称"
          align="left"
        />
        <el-table-column
          prop="majorLevel"
          label="专业层次"
          align="center"
        />
        <el-table-column
          prop="majorCode"
          label="专业代码"
          align="center"
        />
        <el-table-column
          prop="schoolingYear"
          label="专业学制(年)"
          align="center"
        />
        <el-table-column
          label="专业所属学院"
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
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
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
          label="专业名称"
          prop="majorName"
        >
          <el-input
            v-model="form.majorName"
            placeholder="请输入专业名称"
          />
        </el-form-item>
        <el-form-item
          label="专业层次"
          prop="majorLevel"
        >
          <el-select
            v-model="form.majorLevel"
            placeholder="请选择专业层次"
            style="width: 100%"
          >
            <el-option
              label="高职"
              value="高职"
            />
            <el-option
              label="本科"
              value="本科"
            />
            <el-option
              label="研究生"
              value="研究生"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="专业代码"
          prop="majorCode"
        >
          <el-input
            v-model="form.majorCode"
            placeholder="请输入专业代码"
          />
        </el-form-item>
        <el-form-item
          label="专业学制"
          prop="schoolingYear"
        >
          <el-input-number
            v-model="form.schoolingYear"
            :min="2"
            :max="5"
            style="width: 100%"
          />
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
          >
            <el-option
              v-for="dept in deptOptions"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
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

const queryForm = reactive({
  current: 1,
  size: 10,
  majorLevel: '',
  majorName: '',
  majorCode: '',
  deptId: null
})

const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const selectedRows = ref([])

const deptOptions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增专业')
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  majorName: '',
  majorLevel: '',
  majorCode: '',
  schoolingYear: 4,
  deptId: null
})

const validateMajorCode = async (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入专业代码'))
  }
  try {
    const res = await request.get('/admin/majors', {
      params: { majorCode: value, size: 1 }
    })
    if (res.data.records && res.data.records.length > 0 && res.data.records[0].id !== editId.value) {
      callback(new Error('该专业代码已存在'))
    } else {
      callback()
    }
  } catch {
    callback()
  }
}

const formRules = {
  majorName: [
    { required: true, message: '请输入专业名称', trigger: 'blur' }
  ],
  majorLevel: [
    { required: true, message: '请选择专业层次', trigger: 'change' }
  ],
  majorCode: [
    { required: true, validator: validateMajorCode, trigger: 'blur' }
  ],
  schoolingYear: [
    { required: true, message: '请输入专业学制', trigger: 'blur' }
  ],
  deptId: [
    { required: true, message: '请选择所属学院', trigger: 'change' }
  ]
}

function getDeptName(deptId) {
  const dept = deptOptions.value.find(d => d.id === deptId)
  return dept ? dept.deptName : deptId
}

const fetchDeptOptions = async () => {
  try {
    const res = await request.get('/admin/depts', {
      params: { size: 100 }
    })
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      deptOptions.value = res.data.records.filter(d => d.id === deptAdminDeptId.value)
    } else {
      deptOptions.value = res.data.records
    }
  } catch (e) {
    console.warn('获取学院列表失败', e.message)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      current: queryForm.current,
      size: queryForm.size,
      majorName: queryForm.majorName || undefined,
      majorLevel: queryForm.majorLevel || undefined,
      majorCode: queryForm.majorCode || undefined,
      deptId: queryForm.deptId || undefined
    }
    if (isDeptAdmin.value && deptAdminDeptId.value) {
      params.deptId = deptAdminDeptId.value
    }
    const res = await request.get('/admin/majors', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取专业列表失败', e.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.current = 1
  fetchList()
}

const handleReset = () => {
  queryForm.majorLevel = ''
  queryForm.majorName = ''
  queryForm.majorCode = ''
  queryForm.deptId = isDeptAdmin.value ? deptAdminDeptId.value : null
  queryForm.current = 1
  fetchList()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const resetForm = () => {
  form.majorName = ''
  form.majorLevel = ''
  form.majorCode = ''
  form.schoolingYear = 4
  form.deptId = null
  editId.value = null
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增专业'
  if (isDeptAdmin.value && deptAdminDeptId.value) {
    form.deptId = deptAdminDeptId.value
  }
  dialogVisible.value = true
}

const handleEdit = () => {
  const row = selectedRows.value[0]
  resetForm()
  dialogTitle.value = '修改专业'
  editId.value = row.id
  form.majorName = row.majorName
  form.majorLevel = row.majorLevel
  form.majorCode = row.majorCode
  form.schoolingYear = row.schoolingYear
  form.deptId = row.deptId
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该专业吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/admin/majors/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('删除专业失败', e.message)
    }
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      majorName: form.majorName,
      majorLevel: form.majorLevel,
      majorCode: form.majorCode,
      schoolingYear: form.schoolingYear,
      deptId: form.deptId
    }
    if (editId.value) {
      await request.put(`/admin/majors/${editId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/majors', data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.warn('提交专业失败', e.message)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchDeptOptions()
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

.filter-bar .filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 0;
}

.filter-bar :deep(.el-form-item) {
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
