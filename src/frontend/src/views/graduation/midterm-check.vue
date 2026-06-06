<template>
  <div class="midterm-container">
    <div class="search-section">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>学期</label>
            <el-select v-model="searchForm.semester" placeholder="请选择学期" clearable style="width: 100%">
              <el-option label="2025-2026学年第1学期(当)" value="2025-2026-1" />
              <el-option label="2024-2025学年第2学期" value="2024-2025-2" />
              <el-option label="2024-2025学年第1学期" value="2024-2025-1" />
              <el-option label="2023-2024学年第2学期" value="2023-2024-2" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>学生姓名</label>
            <el-input v-model="searchForm.studentName" placeholder="请输入学生姓名" clearable />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>指导教师</label>
            <el-input v-model="searchForm.teacherName" placeholder="请输入指导教师姓名" clearable />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>检查状态</label>
            <el-select v-model="searchForm.status" placeholder="请选择检查状态" clearable style="width: 100%">
              <el-option label="未提交" value="未提交" />
              <el-option label="待审核" value="待审核" />
              <el-option label="已通过" value="已通过" />
              <el-option label="需修改" value="需修改" />
            </el-select>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" align="middle" style="margin-top: 16px;">
        <el-col :span="24">
          <div class="search-buttons">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              &nbsp;搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              &nbsp;重置
            </el-button>
            <el-button type="warning" plain @click="handleExport">
              <el-icon><Download /></el-icon>
              &nbsp;导出
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="table-section">
      <el-table
        ref="tableRef"
        :data="paginatedData"
        :table-layout="'fixed'"
        class="custom-table"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" align="center" fixed="left" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="studentName" label="学生" width="100" align="center" show-overflow-tooltip />
        <el-table-column prop="studentId" label="学号" width="150" align="center" />
        <el-table-column prop="teacherName" label="指导教师" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="topicName" label="毕设题目" min-width="200" align="center" show-overflow-tooltip />
        <el-table-column prop="checkTime" label="检查时间" width="180" align="center" />
        <el-table-column prop="progress" label="完成进度" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="检查状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleApprove(row)">
              <el-icon><EditPen /></el-icon>
              &nbsp;审批
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

    <el-dialog v-model="detailDialogVisible" title="中期检查详情" width="900px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentRecord?.id }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ currentRecord?.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRecord?.studentId }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ currentRecord?.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="毕设题目" :span="2">{{ currentRecord?.topicName }}</el-descriptions-item>
        <el-descriptions-item label="检查时间">{{ currentRecord?.checkTime }}</el-descriptions-item>
        <el-descriptions-item label="完成进度">
          <el-progress :percentage="currentRecord?.progress" :status="getProgressStatus(currentRecord?.progress)" :stroke-width="16" :text-inside="true" />
        </el-descriptions-item>
        <el-descriptions-item label="检查状态">
          <el-tag :type="getStatusType(currentRecord?.status)" size="large">{{ currentRecord?.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检查意见" :span="2">
          <div style="white-space: pre-wrap; line-height: 1.6; background-color: #f5f7fa; padding: 10px; border-radius: 4px;">{{ currentRecord?.comment || '暂无检查意见' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="提交内容" :span="2">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentRecord?.content || '暂无提交内容' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <template v-if="currentRecord?.status === '待审核'">
            <el-button type="success" @click="handleDialogApprove">
              <el-icon><Check /></el-icon>
              通过
            </el-button>
            <el-button type="danger" @click="handleDialogReject">
              <el-icon><Close /></el-icon>
              驳回
            </el-button>
          </template>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, EditPen, Check, Close, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import request from '@/utils/request'

const loading = ref(false)
const tableRef = ref(null)
const detailDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  semester: '2025-2026学年第1学期(当)',
  studentName: '',
  teacherName: '',
  status: ''
})

const currentRecord = ref(null)

// 从API获取的数据
const tableData = ref([])
const totalRecords = ref(0)

/**
 * 从后端获取中期检查列表
 */
async function fetchMidtermChecks() {
  loading.value = true
  try {
    const res = await request.get('/v1/teacher/midterm/all')
    
    if (res.data && Array.isArray(res.data)) {
      // 转换数据格式以匹配前端表格
      tableData.value = res.data.map((check, index) => ({
        id: check.check_id,
        studentName: check.student_name || '未知',
        studentId: check.student_no || '-',
        teacherName: check.teacher_name || '未分配',
        topicName: check.topic_name || '未选择题目',
        checkTime: check.submit_time ? formatDate(check.submit_time) : null,
        progress: check.progress || 0,
        status: formatStatus(check.status),
        content: check.report_content || '',
        comment: check.teacher_comment || '',
        // 保存原始数据供详情查看使用
        rawData: check
      }))
      
      totalRecords.value = tableData.value.length
    }
  } catch (error) {
    console.error('获取中期检查列表失败:', error)
    ElMessage.error('获取中期检查列表失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

/**
 * 格式化状态显示
 */
function formatStatus(status) {
  const statusMap = {
    'draft': '未提交',
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '需修改'
  }
  return statusMap[status] || status || '未知'
}

/**
 * 格式化日期时间
 */
function formatDate(dateStr) {
  if (!dateStr) return null
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).replace(/\//g, '-')
}

// 页面加载时获取数据
onMounted(() => {
  fetchMidtermChecks()
})

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (searchForm.studentName && !item.studentName.includes(searchForm.studentName)) return false
    if (searchForm.teacherName && !item.teacherName.includes(searchForm.teacherName)) return false
    if (searchForm.status && item.status !== searchForm.status) return false
    return true
  })
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredData.value.slice(start, end)
})

function getStatusType(status) {
  const map = {
    '未提交': 'info',
    '待审核': 'warning',
    '已通过': 'success',
    '需修改': 'danger'
  }
  return map[status] || 'info'
}

function getProgressStatus(progress) {
  if (progress >= 80) return 'success'
  if (progress >= 50) return ''
  if (progress >= 30) return 'warning'
  return 'exception'
}

function handleSearch() {
  loading.value = true
  currentPage.value = 1
  
  // 重新从后端获取数据
  fetchMidtermChecks().then(() => {
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条记录`)
  })
}

function handleReset() {
  searchForm.studentName = ''
  searchForm.teacherName = ''
  searchForm.status = ''
  searchForm.semester = '2025-2026学年第1学期(当)'
  currentPage.value = 1
  pageSize.value = 10
  
  // 重新获取所有数据
  fetchMidtermChecks()
  ElMessage.info('已重置搜索条件')
}

function handleViewDetail(row) {
  currentRecord.value = row
  detailDialogVisible.value = true
}

function handleApprove(row) {
  currentRecord.value = row
  detailDialogVisible.value = true
}

function handleDialogApprove() {
  if (!currentRecord.value) return

  ElMessageBox.prompt('请输入审核意见', '通过审核', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入审核意见（选填）'
  }).then(({ value }) => {
    const index = tableData.value.findIndex(item => item.id === currentRecord.value.id)
    if (index > -1) {
      tableData.value[index].status = '已通过'
      tableData.value[index].comment = value || '审核通过'
      currentRecord.value = tableData.value[index]
      ElMessage.success(`已通过学生 ${currentRecord.value.studentName} 的中期检查`)
    }
  }).catch(() => {})
}

function handleDialogReject() {
  if (!currentRecord.value) return

  ElMessageBox.prompt('请输入驳回理由', '驳回修改', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入驳回理由（必填）',
    inputValidator: (value) => {
      if (!value.trim()) {
        return '驳回理由不能为空'
      }
    }
  }).then(({ value }) => {
    const index = tableData.value.findIndex(item => item.id === currentRecord.value.id)
    if (index > -1) {
      tableData.value[index].status = '需修改'
      tableData.value[index].comment = value
      currentRecord.value = tableData.value[index]
      ElMessage.success(`已将学生 ${currentRecord.value.studentName} 的中期检查驳回修改`)
    }
  }).catch(() => {})
}

function handleExport() {
  if (filteredData.value.length === 0) {
    ElMessage.warning('没有可导出的中期检查记录')
    return
  }

  const headers = ['学生', '学号', '指导教师', '毕设题目', '检查时间', '完成进度(%)', '检查状态', '检查意见']
  const data = filteredData.value.map(row => ({
    '学生': row.studentName,
    '学号': row.studentId,
    '指导教师': row.teacherName,
    '毕设题目': row.topicName,
    '检查时间': row.checkTime,
    '完成进度(%)': row.progress,
    '检查状态': row.status,
    '检查意见': row.comment || '-'
  }))

  const ws = XLSX.utils.json_to_sheet(data)
  ws['!cols'] = [
    { wch: 10 },
    { wch: 16 },
    { wch: 12 },
    { wch: 30 },
    { wch: 20 },
    { wch: 14 },
    { wch: 12 },
    { wch: 40 }
  ]

  XLSX.utils.sheet_add_aoa(ws, [headers], { origin: 'A1' })

  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '中期检查记录')

  const fileName = `中期检查记录_${searchForm.semester}_${new Date().toISOString().slice(0, 10)}.xlsx`
  XLSX.writeFile(wb, fileName)

  ElMessage.success(`成功导出 ${filteredData.value.length} 条中期检查记录`)
}

function handleSizeChange(val) {
  pageSize.value = val
  currentPage.value = 1
}

function handleCurrentChange(val) {
  currentPage.value = val

  const totalPages = Math.ceil(filteredData.value.length / pageSize.value)
  if (val > totalPages && totalPages > 0) {
    currentPage.value = totalPages
  }
}
</script>

<style scoped>
.midterm-container {
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
  align-items: center;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.custom-table {
  --el-table-border-color: transparent;
}

.custom-table :deep(.el-table__header-wrapper th) {
  background-color: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
  border-right: none !important;
  border-bottom: 2px solid #e4e7ed !important;
  padding: 14px 0 !important;
}

.custom-table :deep(.el-table__header-wrapper .cell),
.custom-table :deep(.el-table__body-wrapper .cell) {
  padding: 0;
  line-height: normal;
}

.custom-table :deep(.el-table__body-wrapper td) {
  border-right: none !important;
  border-bottom: 1px solid #ebeef5 !important;
  padding: 16px 0 !important;
}

.custom-table :deep(.el-table__row:hover > td) {
  background-color: #f5f7fa !important;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
