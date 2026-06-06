<template>
  <div class="checkin-container">
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
            <label>班级名称</label>
            <el-input v-model="searchForm.className" placeholder="请输入班级名称" clearable />
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
            <label>老师姓名</label>
            <el-input v-model="searchForm.teacherName" placeholder="请输入老师姓名" clearable />
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
        <el-table-column prop="className" label="班级" width="150" align="center" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学生" width="90" align="center" show-overflow-tooltip />
        <el-table-column prop="studentId" label="学号" width="170" align="center" />
        <el-table-column prop="teacherName" label="老师" width="90" align="center" show-overflow-tooltip />
        <el-table-column prop="totalDays" label="所属毕设计划总天数" width="160" align="center" />
        <el-table-column prop="checkedDays" label="已签到天数" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="getCheckinStatusType(row)" size="small">{{ row.checkedDays }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastCheckinTime" label="最后签到操作时间" width="250" align="center" />
        <el-table-column prop="lastCheckinLocation" label="最后签到操作地址" min-width="130" align="center" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              查看详情
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

    <el-dialog v-model="detailDialogVisible" title="签到详情" width="900px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="班级">{{ currentRecord?.className }}</el-descriptions-item>
        <el-descriptions-item label="学生">{{ currentRecord?.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRecord?.studentId }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ currentRecord?.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="计划总天数">{{ currentRecord?.totalDays }} 天</el-descriptions-item>
        <el-descriptions-item label="已签到天数">
          <el-tag :type="getCheckinStatusType(currentRecord)" size="large">{{ currentRecord?.checkedDays }} 天</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签到率" :span="2">
          <el-progress 
            :percentage="currentRecord ? Math.round((currentRecord.checkedDays / currentRecord.totalDays) * 100) : 0" 
            :status="getProgressStatus(currentRecord)"
            :stroke-width="20"
            :text-inside="true"
          />
        </el-descriptions-item>
        <el-descriptions-item label="最后签到时间" :span="2">{{ currentRecord?.lastCheckinTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后签到地址" :span="2">{{ currentRecord?.lastCheckinLocation || '-' }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, View, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'
import request from '@/utils/request'

const loading = ref(false)
const tableRef = ref(null)
const detailDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  semester: '2025-2026学年第1学期(当)',
  className: '',
  studentName: '',
  teacherName: ''
})

const currentRecord = ref(null)

// 从API获取的数据
const tableData = ref([])
const totalRecords = ref(0)

/**
 * 从后端获取签到记录列表
 */
async function fetchSignIns() {
  loading.value = true
  try {
    const res = await request.get('/v1/teacher/signins')
    
    if (res.data && Array.isArray(res.data)) {
      // 转换数据格式以匹配前端表格
      tableData.value = res.data.map((record, index) => ({
        id: record.sign_id || index + 1,
        className: record.class_name || '未分配',
        studentName: record.student_name || '未知',
        studentId: record.student_no || '-',
        teacherName: record.teacher_name || '未分配',
        totalDays: record.total_days || 68,
        checkedDays: record.checked_days || 0,
        lastCheckinTime: record.last_sign_time ? formatDate(record.last_sign_time) : '-',
        lastCheckinLocation: record.location || '-',
        // 保存原始数据供详情查看使用
        rawData: record
      }))
      
      totalRecords.value = tableData.value.length
    }
  } catch (error) {
    console.error('获取签到记录列表失败:', error)
    ElMessage.error('获取签到记录列表失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
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
  fetchSignIns()
})

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (searchForm.className && !item.className.includes(searchForm.className)) return false
    if (searchForm.studentName && !item.studentName.includes(searchForm.studentName)) return false
    if (searchForm.teacherName && !item.teacherName.includes(searchForm.teacherName)) return false
    return true
  })
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredData.value.slice(start, end)
})

function getCheckinStatusType(row) {
  if (!row || !row.totalDays) return 'info'
  
  const rate = row.checkedDays / row.totalDays
  
  if (rate === 0) return 'info'       // 未签到 - 灰色
  if (rate < 0.5) return 'danger'     // 签到率<50% - 红色
  if (rate < 0.8) return 'warning'   // 签到率50%-80% - 橙色
  return 'success'                   // 签到率≥80% - 绿色
}

function getProgressStatus(record) {
  if (!record || !record.totalDays) return ''
  
  const rate = record.checkedDays / record.totalDays
  
  if (rate >= 0.9) return 'success'
  if (rate >= 0.7) return ''
  if (rate >= 0.5) return 'warning'
  return 'exception'
}

function handleSearch() {
  loading.value = true
  currentPage.value = 1
  
  // 重新从后端获取数据
  fetchSignIns().then(() => {
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条记录`)
  })
}

function handleReset() {
  searchForm.className = ''
  searchForm.studentName = ''
  searchForm.teacherName = ''
  searchForm.semester = '2025-2026学年第1学期(当)'
  currentPage.value = 1
  pageSize.value = 10
  
  // 重新获取所有数据
  fetchSignIns()
  ElMessage.info('已重置搜索条件')
}

function handleExport() {
  if (filteredData.value.length === 0) {
    ElMessage.warning('没有可导出的签到记录')
    return
  }

  const headers = ['班级', '学生', '学号', '指导教师', '计划总天数', '已签到天数', '签到率(%)', '最后签到时间', '最后签到地址']
  const data = filteredData.value.map(row => {
    const rate = row.totalDays > 0 ? Math.round((row.checkedDays / row.totalDays) * 100) : 0
    return {
      '班级': row.className,
      '学生': row.studentName,
      '学号': row.studentId,
      '指导教师': row.teacherName,
      '计划总天数': row.totalDays,
      '已签到天数': row.checkedDays,
      '签到率(%)': rate,
      '最后签到时间': row.lastCheckinTime || '-',
      '最后签到地址': row.lastCheckinLocation || '-'
    }
  })

  const ws = XLSX.utils.json_to_sheet(data)
  ws['!cols'] = [
    { wch: 16 },
    { wch: 10 },
    { wch: 16 },
    { wch: 12 },
    { wch: 12 },
    { wch: 12 },
    { wch: 12 },
    { wch: 22 },
    { wch: 20 }
  ]

  XLSX.utils.sheet_add_aoa(ws, [headers], { origin: 'A1' })

  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '签到情况')

  const fileName = `学生签到记录_${searchForm.semester}_${new Date().toISOString().slice(0, 10)}.xlsx`
  XLSX.writeFile(wb, fileName)

  ElMessage.success(`成功导出 ${filteredData.value.length} 条签到记录`)
}

function handleViewDetail(row) {
  currentRecord.value = row
  detailDialogVisible.value = true
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
.checkin-container {
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
</style>
