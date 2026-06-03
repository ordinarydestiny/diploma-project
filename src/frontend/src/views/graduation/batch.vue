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
              <el-option label="软件技术" value="software" />
              <el-option label="计算机科学" value="cs" />
              <el-option label="信息安全" value="security" />
              <el-option label="大数据技术" value="bigdata" />
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
          <div class="search-buttons">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
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
        :data="tableData"
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
        <el-table-column prop="semester" label="学期" width="120" align="center" />
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
          :total="total"
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
import { ref, reactive } from 'vue'
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
const total = ref(3)

const searchForm = reactive({
  grade: '',
  major: '',
  semester: ''
})

const selectedRows = ref([])

const tableData = ref([
  {
    batchId: 'BD20240001',
    grade: '2024',
    majorName: '软件技术',
    semester: '2024-2025学年第二学期',
    startDate: '2025-03-01',
    endDate: '2025-06-30',
    defenseWeight: 40.0,
    previewWeight: 10.0
  },
  {
    batchId: 'BD20230001',
    grade: '2023',
    majorName: '计算机科学',
    semester: '2023-2024学年第二学期',
    startDate: '2024-03-01',
    endDate: '2024-06-30',
    defenseWeight: 35.0,
    previewWeight: 15.0
  },
  {
    batchId: 'BD20220001',
    grade: '2022',
    majorName: '大数据技术',
    semester: '2022-2023学年第二学期',
    startDate: '2023-03-01',
    endDate: '2023-06-30',
    defenseWeight: 45.0,
    previewWeight: 10.0
  }
])

const formData = reactive({
  batchId: '',
  batchName: '',
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
  setTimeout(() => {
    loading.value = false
    ElMessage.success('搜索完成')
  }, 500)
}

function handleReset() {
  searchForm.grade = ''
  searchForm.major = ''
  searchForm.semester = ''
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
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条数据吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleDeleteRow(row) {
  ElMessageBox.confirm(`确定要删除批次 ${row.batchId} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleExport() {
  ElMessage.success('导出成功')
}

function handleRefresh() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功')
  }, 500)
}

function handleRelation(row) {
  router.push({ path: '/graduation/batch/relation', query: { batchId: row.batchId } })
}

function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (valid) {
      dialogVisible.value = false
      ElMessage.success(dialogTitle.value === '新增批次' ? '新增成功' : '修改成功')
    }
  })
}

function handleSizeChange(val) {
  pageSize.value = val
  handleSearch()
}

function handleCurrentChange(val) {
  currentPage.value = val
  handleSearch()
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
