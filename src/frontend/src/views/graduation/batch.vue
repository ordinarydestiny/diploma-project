<template>
  <div class="batch-container">
    <div class="search-section">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>年级</label>
            <el-select v-model="searchForm.grade" placeholder="请输入毕业设计系需年级" clearable style="width: 100%">
              <el-option label="2020级" value="2020" />
              <el-option label="2021级" value="2021" />
              <el-option label="2022级" value="2022" />
              <el-option label="2023级" value="2023" />
              <el-option label="2024级" value="2024" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>专业</label>
            <el-select v-model="searchForm.major" placeholder="请选择" clearable style="width: 100%">
              <el-option label="软件技术" value="软件技术" />
              <el-option label="计算机科学" value="计算机科学" />
              <el-option label="信息安全" value="信息安全" />
              <el-option label="大数据技术" value="大数据技术" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>学期</label>
            <el-input v-model="searchForm.semester" placeholder="请输入毕业设计系需学期" clearable />
          </div>
        </el-col>
        <el-col :xs="24" :sm="24" :md="6">
          <div class="search-item search-buttons-item">
            <div class="search-buttons-inline">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="action-section">
      <div class="action-buttons">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增
        </el-button>
        <el-button type="success" @click="handleEdit">
          <el-icon><EditPen /></el-icon>
          修改
        </el-button>
        <el-button type="danger" @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
        <el-button type="warning" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
      <div class="table-actions">
        <el-button circle @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <div class="table-section">
      <el-table
        ref="tableRef"
        :data="filteredData"
        border
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" align="center" fixed="left" />
        <el-table-column prop="batchId" label="毕业设计批次ID" width="160" align="center" />
        <el-table-column prop="grade" label="年级" width="100" align="center" />
        <el-table-column prop="majorName" label="专业" min-width="140" show-overflow-tooltip />
        <el-table-column prop="semester" label="学期" width="180" align="center" show-overflow-tooltip />
        <el-table-column prop="startDate" label="开始时间" width="130" align="center" />
        <el-table-column prop="endDate" label="结束时间" width="130" align="center" />
        <el-table-column prop="defenseWeight" label="答辩分值占比" width="130" align="center">
          <template #default="{ row }">
            {{ row.defenseWeight }}%
          </template>
        </el-table-column>
        <el-table-column prop="previewWeight" label="预告占比" width="110" align="center">
          <template #default="{ row }">
            {{ row.previewWeight }}%
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleRelation(row)">
              <el-icon><Connection /></el-icon>
              关联师生
            </el-button>
            <el-button type="primary" link size="small" @click="handleEditRow(row)">
              <el-icon><EditPen /></el-icon>
              修改
            </el-button>
            <el-button type="danger" link size="small" @click="handleDeleteRow(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filteredData.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="年级" prop="grade">
          <el-select v-model="formData.grade" placeholder="请选择年级" style="width: 100%">
            <el-option label="2020级" value="2020" />
            <el-option label="2021级" value="2021" />
            <el-option label="2022级" value="2022" />
            <el-option label="2023级" value="2023" />
            <el-option label="2024级" value="2024" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" prop="majorName">
          <el-select v-model="formData.majorName" placeholder="请选择专业" style="width: 100%">
            <el-option label="软件技术" value="软件技术" />
            <el-option label="计算机科学" value="计算机科学" />
            <el-option label="信息安全" value="信息安全" />
            <el-option label="大数据技术" value="大数据技术" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="formData.semester" placeholder="请输入学期，如：2024-2025学年第二学期" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startDate">
          <el-date-picker v-model="formData.startDate" type="date" placeholder="选择开始时间" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endDate">
          <el-date-picker v-model="formData.endDate" type="date" placeholder="选择结束时间" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="答辩分值占比" prop="defenseWeight">
              <el-input-number v-model="formData.defenseWeight" :min="0" :max="100" :precision="1" style="width: 100%" />%
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预告占比" prop="previewWeight">
              <el-input-number v-model="formData.previewWeight" :min="0" :max="100" :precision="1" style="width: 100%" />%
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Search, Refresh, Plus, EditPen, Delete, Download, Connection } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const tableRef = ref(null)
const formRef = ref(null)
const dialogVisible = ref(false)
const dialogTitle = ref('新增批次')
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  grade: '',
  major: '',
  semester: ''
})

const selectedRows = ref([])

let batchIdCounter = 4

const tableData = ref([
  {
    batchId: 'BD20240001',
    grade: '2024',
    majorName: '软件技术',
    semester: '2024-2025学年第二学期',
    startDate: '2025-03-01',
    endDate: '2025-06-30',
    defenseWeight: 40.0,
    previewWeight: 10.0,
    studentCount: 9
  },
  {
    batchId: 'BD20230001',
    grade: '2023',
    majorName: '计算机科学',
    semester: '2023-2024学年第二学期',
    startDate: '2024-03-01',
    endDate: '2024-06-30',
    defenseWeight: 35.0,
    previewWeight: 15.0,
    studentCount: 15
  },
  {
    batchId: 'BD20220001',
    grade: '2022',
    majorName: '大数据技术',
    semester: '2022-2023学年第二学期',
    startDate: '2023-03-01',
    endDate: '2023-06-30',
    defenseWeight: 45.0,
    previewWeight: 10.0,
    studentCount: 20
  }
])

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (searchForm.grade && item.grade !== searchForm.grade) return false
    if (searchForm.major && item.majorName !== searchForm.major) return false
    if (searchForm.semester && !item.semester.includes(searchForm.semester)) return false
    return true
  })
})

const formData = reactive({
  batchId: '',
  grade: '',
  majorName: '',
  semester: '',
  startDate: '',
  endDate: '',
  defenseWeight: 40,
  previewWeight: 10
})

const formRules = {
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  majorName: [{ required: true, message: '请选择专业', trigger: 'change' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

function handleSearch() {
  loading.value = true
  currentPage.value = 1
  setTimeout(() => {
    loading.value = false
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条数据`)
  }, 300)
}

function handleReset() {
  searchForm.grade = ''
  searchForm.major = ''
  searchForm.semester = ''
  currentPage.value = 1
  ElMessage.info('已重置搜索条件')
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function handleAdd() {
  dialogTitle.value = '新增批次'
  Object.assign(formData, {
    batchId: '',
    grade: '',
    majorName: '',
    semester: '',
    startDate: '',
    endDate: '',
    defenseWeight: 40,
    previewWeight: 10
  })
  dialogVisible.value = true
}

function handleEdit() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要修改的数据')
    return
  }
  if (selectedRows.value.length > 1) {
    ElMessage.warning('一次只能修改一条数据')
    return
  }
  const row = selectedRows.value[0]
  dialogTitle.value = '修改批次'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

function handleEditRow(row) {
  dialogTitle.value = '修改批次'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

function handleDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的数据')
    return
  }
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条数据吗？删除后无法恢复！`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const ids = selectedRows.value.map(row => row.batchId)
    tableData.value = tableData.value.filter(item => !ids.includes(item.batchId))
    ElMessage.success(`成功删除 ${ids.length} 条数据`)
    selectedRows.value = []
  }).catch(() => {})
}

function handleDeleteRow(row) {
  ElMessageBox.confirm(
    `确定要删除批次 ${row.batchId} 吗？该操作不可撤销！`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = tableData.value.findIndex(item => item.batchId === row.batchId)
    if (index > -1) {
      tableData.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  }).catch(() => {})
}

function handleExport() {
  if (filteredData.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  const headers = ['批次ID', '年级', '专业', '学期', '开始时间', '结束时间', '答辩占比', '预告占比']
  const data = filteredData.value.map(row => [
    row.batchId,
    row.grade,
    row.majorName,
    row.semester,
    row.startDate,
    row.endDate,
    `${row.defenseWeight}%`,
    `${row.previewWeight}%`
  ])

  let csvContent = '\uFEFF'
  csvContent += headers.join(',') + '\n'
  data.forEach(row => {
    csvContent += row.join(',') + '\n'
  })

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `毕设批次数据_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()

  ElMessage.success(`成功导出 ${filteredData.value.length} 条数据`)
}

function handleRefresh() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功，数据已更新')
  }, 500)
}

function handleRelation(row) {
  router.push({ path: '/graduation/batch/relation', query: { batchId: row.batchId, batchInfo: JSON.stringify(row) } })
}

function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (valid) {
      if (dialogTitle.value === '新增批次') {
        formData.batchId = `BD${new Date().getFullYear()}${String(batchIdCounter++).padStart(4, '0')}`
        formData.studentCount = 0
        tableData.value.unshift({ ...formData })
        ElMessage.success(`新增批次成功！批次ID: ${formData.batchId}`)
      } else {
        const index = tableData.value.findIndex(item => item.batchId === formData.batchId)
        if (index > -1) {
          tableData.value[index] = { ...formData }
          ElMessage.success('修改批次成功！')
        }
      }
      dialogVisible.value = false
    }
  })
}

function handleSizeChange(val) {
  pageSize.value = val
  currentPage.value = 1
}

function handleCurrentChange(val) {
  currentPage.value = val
}
</script>

<style scoped>
.batch-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-item {
  margin-bottom: 10px;
}

.search-item label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.search-buttons {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  height: 100%;
  padding-top: 28px;
}

.search-buttons-item {
  padding-top: 30px;
}

.search-buttons-inline {
  display: flex;
  gap: 10px;
  align-items: center;
}

.action-section {
  background: #fff;
  padding: 16px 20px;
  border-radius: 4px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.table-actions {
  display: flex;
  gap: 10px;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .search-buttons {
    padding-top: 10px;
  }

  .action-section {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .action-buttons {
    flex-wrap: wrap;
  }
}
</style>
