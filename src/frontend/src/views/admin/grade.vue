<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
        <el-input
          v-model="queryGradeName"
          placeholder="请输入年级名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
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
          :disabled="!selectedRows.length"
          @click="handleBatchDelete"
        >
          删除
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
          label="年级ID"
          align="center"
          min-width="100"
        />
        <el-table-column
          prop="gradeName"
          label="年级名称"
          align="center"
          min-width="150"
        />
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
          label="年级名称"
          prop="gradeName"
        >
          <el-input
            v-model="form.gradeName"
            placeholder="请输入年级名称，如：2023级"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { useTableResize } from '@/composables/useTableResize.js'

const tableRef = ref(null)
useTableResize(tableRef)

const queryGradeName = ref('')
const current = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const selectedRows = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增年级')
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  gradeName: ''
})

const formRules = {
  gradeName: [
    { required: true, message: '请输入年级名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/grades', {
      params: {
        current: current.value,
        size: size.value,
        gradeName: queryGradeName.value || undefined
      }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取年级列表失败', e.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  current.value = 1
  fetchList()
}

const handleReset = () => {
  queryGradeName.value = ''
  current.value = 1
  fetchList()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const resetForm = () => {
  form.gradeName = ''
  editId.value = null
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增年级'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  dialogTitle.value = '修改年级'
  editId.value = row.id
  form.gradeName = row.gradeName
  dialogVisible.value = true
}

const handleEditSelected = () => {
  if (selectedRows.value.length === 1) {
    handleEdit(selectedRows.value[0])
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该年级吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/admin/grades/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('删除年级失败', e.message)
    }
  }
}

const handleBatchDelete = async () => {
  if (!selectedRows.value.length) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 个年级吗？`, '提示', {
      type: 'warning'
    })
    for (const row of selectedRows.value) {
      await request.delete(`/admin/grades/${row.id}`)
    }
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('批量删除年级失败', e.message)
    }
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (editId.value) {
      await request.put(`/admin/grades/${editId.value}`, { gradeName: form.gradeName })
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/grades', { gradeName: form.gradeName })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.warn('提交年级失败', e.message)
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
