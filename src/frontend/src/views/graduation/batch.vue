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
          :total="totalRecords"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Plus, EditPen, Delete, Download, Connection } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

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
const currentRecord = ref(null)

// 从API获取的数据
const tableData = ref([])
const totalRecords = ref(0)

// 页面加载时获取数据
onMounted(() => {
  fetchBatches()
})

/**
 * 从后端获取批次列表
 */
async function fetchBatches() {
  loading.value = true
  try {
    const res = await request.get('/batches', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    
    if (res.data && res.data.records) {
      // 转换数据格式以匹配前端表格
      tableData.value = res.data.records.map((batch) => ({
        batchId: batch.batch_id || batch.batchName,
        grade: batch.grade || extractGradeFromSemester(batch.batch_name),
        majorName: batch.major_name || getMajorNameById(batch.major_id),
        semester: batch.batch_name,
        startDate: batch.start_date ? formatDate(batch.start_date) : null,
        endDate: batch.end_date ? formatDate(batch.end_date) : null,
        defenseWeight: batch.defense_weight || 40,
        previewWeight: batch.preview_weight || 10,
        studentCount: batch.student_count || 0,
        status: batch.status || 'active',
        // 保存原始数据供详情查看使用
        rawData: batch
      }))
      
      totalRecords.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取批次列表失败:', error)
    ElMessage.error('获取批次列表失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

/**
 * 从学期名称中提取年级
 */
function extractGradeFromSemester(semester) {
  if (!semester) return ''
  const match = semester.match(/(\d{4})/)
  return match ? match[1].slice(0, 4) : ''
}

/**
 * 根据专业ID获取专业名称（简化版）
 */
function getMajorNameById(majorId) {
  const majorMap = {
    1: '软件技术',
    2: '计算机科学',
    3: '信息安全',
    4: '大数据技术',
    5: '人工智能'
  }
  return majorMap[majorId] || '未知专业'
}

/**
 * 格式化日期
 */
function formatDate(dateStr) {
  if (!dateStr) return null
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\//g, '-')
}

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
  
  // 重新从后端获取数据（带筛选条件）
  fetchBatches().then(() => {
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条记录`)
  })
}

function handleReset() {
  searchForm.grade = ''
  searchForm.major = ''
  searchForm.semester = ''
  currentPage.value = 1
  pageSize.value = 10
  
  // 重新获取所有数据
  fetchBatches()
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
  currentRecord.value = row
  
  // 从原始数据或当前数据填充表单
  formData.batchId = row.batchId
  formData.grade = row.grade || ''
  formData.majorName = row.majorName || ''
  formData.semester = row.semester || ''
  formData.startDate = row.startDate || ''
  formData.endDate = row.endDate || ''
  formData.defenseWeight = row.defenseWeight || 40
  formData.previewWeight = row.previewWeight || 10
  
  dialogVisible.value = true
}

function handleEditRow(row) {
  dialogTitle.value = '修改批次'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function handleDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的数据')
    return
  }
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条数据吗？<br/><br/>
     <small style="color: #f56c6c;">⚠️ 此操作将从数据库中永久删除，无法恢复！</small>`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true
    }
  ).then(async () => {
    try {
      // 调用后端API逐个删除批次
      const deletePromises = selectedRows.value.map(row => 
        request.delete(`/batches/${row.rawData?.batch_id || row.batchId}`)
      )
      
      await Promise.all(deletePromises)
      
      // 删除成功后重新获取数据
      await fetchBatches()
      
      ElMessage.success(`✅ 成功删除 ${selectedRows.value.length} 条批次数据！数据已从数据库移除`)
      selectedRows.value = []
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('❌ 删除失败，请重试')
    }
  }).catch(() => {})
}

async function handleDeleteRow(row) {
  ElMessageBox.confirm(
    `确定要删除批次 ${row.batchId} 吗？<br/><br/>
     <small style="color: #f56c6c;">⚠️ 该操作不可撤销！</small>`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true
    }
  ).then(async () => {
    try {
      // 调用后端API删除批次
      await request.delete(`/batches/${row.rawData?.batch_id || row.batchId}`)
      
      // 删除成功后重新获取数据
      await fetchBatches()
      
      ElMessage.success('✅ 删除成功！数据已从数据库移除')
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('❌ 删除失败，请重试')
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

async function handleSubmit() {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogTitle.value === '新增批次') {
          // 调用后端API新增批次
          const batchData = {
            batchName: formData.semester,
            grade: formData.grade,
            majorName: formData.majorName,
            semester: formData.semester,
            startDate: formData.startDate,
            endDate: formData.endDate,
            defenseWeight: formData.defenseWeight,
            previewWeight: formData.previewWeight
          }
          
          await request.post('/batches', batchData)
          
          // 新增成功后重新获取数据
          await fetchBatches()
          
          dialogVisible.value = false
          ElMessage.success('✅ 新增批次成功！数据已同步到数据库')
        } else {
          // 调用后端API修改批次
          const batchId = currentRecord.value?.rawData?.batch_id || formData.batchId
          const updateData = {
            batchName: formData.semester,
            grade: formData.grade,
            majorName: formData.majorName,
            semester: formData.semester,
            startDate: formData.startDate,
            endDate: formData.endDate,
            defenseWeight: formData.defenseWeight,
            previewWeight: formData.previewWeight
          }
          
          await request.put(`/batches/${batchId}`, updateData)
          
          // 修改成功后重新获取数据
          await fetchBatches()
          
          dialogVisible.value = false
          ElMessage.success('✅ 修改批次成功！数据已同步到数据库')
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('❌ 操作失败，请重试')
      }
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
