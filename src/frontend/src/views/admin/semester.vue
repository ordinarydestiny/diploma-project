<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
        <el-input
          v-model="querySemesterName"
          placeholder="请输入学期名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
        <el-date-picker
          v-model="queryStartDate"
          type="date"
          placeholder="请选择开始日期"
          value-format="YYYY-MM-DD"
          format="YYYY年MM月DD日"
          style="width: 200px"
        />
        <el-date-picker
          v-model="queryEndDate"
          type="date"
          placeholder="请选择结束日期"
          value-format="YYYY-MM-DD"
          format="YYYY年MM月DD日"
          style="width: 200px"
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

      <div class="toolbar-bar">
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
        <el-button
          type="danger"
          :icon="Delete"
          :disabled="selectedRows.length === 0"
          @click="handleBatchDelete"
        >
          删除
        </el-button>
        <el-button
          type="warning"
          :icon="Calendar"
          :disabled="selectedRows.length !== 1"
          @click="handleCalendar"
        >
          校历
        </el-button>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="50"
        />
        <el-table-column
          prop="id"
          label="学期ID"
          align="center"
        />
        <el-table-column
          prop="semesterName"
          label="学期名称"
          align="center"
        />
        <el-table-column
          prop="startDate"
          label="学期开始日期"
          align="center"
          min-width="140"
        >
          <template #default="{ row }">
            {{ formatDate(row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="endDate"
          label="学期结束日期"
          align="center"
          min-width="140"
        >
          <template #default="{ row }">
            {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="200"
          align="center"
          :resizable="false"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleCalendarRow(row)"
            >
              设置校历
            </el-button>
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
            >
              修改
            </el-button>
            <el-button
              type="primary"
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
          label="学期名称"
          prop="semesterName"
        >
          <el-input
            v-model="form.semesterName"
            placeholder="请输入学期名称"
          />
        </el-form-item>
        <el-form-item
          label="学期开始日期"
          prop="startDate"
        >
          <el-date-picker
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            format="YYYY年MM月DD日"
            placeholder="请选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          label="学期结束日期"
          prop="endDate"
        >
          <el-date-picker
            v-model="form.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            format="YYYY年MM月DD日"
            placeholder="请选择结束日期"
            style="width: 100%"
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Calendar } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/dateFormat'
import { useTableResize } from '@/composables/useTableResize.js'
import request from '@/utils/request.js'

const router = useRouter()

const tableRef = ref(null)
useTableResize(tableRef)

const querySemesterName = ref('')
const queryStartDate = ref('')
const queryEndDate = ref('')
const current = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const selectedRows = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增学期')
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  semesterName: '',
  startDate: '',
  endDate: ''
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

const formRules = {
  semesterName: [
    { required: true, message: '请输入学期名称', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, validator: validateEndDate, trigger: 'change' }
  ]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/semesters', {
      params: {
        current: current.value,
        size: size.value,
        semesterName: querySemesterName.value || undefined,
        startDate: queryStartDate.value || undefined,
        endDate: queryEndDate.value || undefined
      }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取学期列表失败', e.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  current.value = 1
  fetchList()
}

const handleReset = () => {
  querySemesterName.value = ''
  queryStartDate.value = ''
  queryEndDate.value = ''
  current.value = 1
  fetchList()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const resetForm = () => {
  form.semesterName = ''
  form.startDate = ''
  form.endDate = ''
  editId.value = null
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增学期'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  dialogTitle.value = '修改学期'
  editId.value = row.id
  form.semesterName = row.semesterName
  form.startDate = row.startDate
  form.endDate = row.endDate
  dialogVisible.value = true
}

const handleEditSelected = () => {
  if (selectedRows.value.length === 1) {
    handleEdit(selectedRows.value[0])
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该学期吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/admin/semesters/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('删除学期失败', e.message)
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 条学期数据吗？`, '提示', {
      type: 'warning'
    })
    for (const row of selectedRows.value) {
      await request.delete(`/admin/semesters/${row.id}`)
    }
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('删除学期失败', e.message)
    }
  }
}

const handleCalendar = () => {
  if (selectedRows.value.length === 1) {
    router.push({ path: '/admin/calendar', query: { semesterId: selectedRows.value[0].id } })
  }
}

const handleCalendarRow = (row) => {
  router.push({ path: '/admin/calendar', query: { semesterId: row.id } })
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      semesterName: form.semesterName,
      startDate: form.startDate,
      endDate: form.endDate
    }
    if (editId.value) {
      await request.put(`/admin/semesters/${editId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/semesters', data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.warn('提交学期失败', e.message)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchList()
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

.toolbar-bar {
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
