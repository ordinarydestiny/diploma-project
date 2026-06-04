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
          :total="filteredData.length"
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
        <el-descriptions-item label="最后签到时间" :span="2">{{ currentRecord?.lastCheckinTime || '暂无记录' }}</el-descriptions-item>
        <el-descriptions-item label="最后签到地址" :span="2">{{ currentRecord?.lastCheckinLocation || '暂无记录' }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Search, Refresh, View, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'

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

const tableData = ref([
  {
    id: 1,
    className: '人工智能231',
    studentName: '购**',
    studentId: '2133229428',
    teacherName: '**',
    totalDays: 68,
    checkedDays: 0,
    lastCheckinTime: null,
    lastCheckinLocation: null
  },
  {
    id: 2,
    className: '软件技术231',
    studentName: '张三',
    studentId: '2133200101',
    teacherName: '廖清科',
    totalDays: 68,
    checkedDays: 45,
    lastCheckinTime: '2026-06-03 09:15:32',
    lastCheckinLocation: '教学楼A301'
  },
  {
    id: 3,
    className: '软件技术232',
    studentName: '李四',
    studentId: '2133201102',
    teacherName: '王海洋',
    totalDays: 65,
    checkedDays: 52,
    lastCheckinTime: '2026-06-03 08:58:21',
    lastCheckinLocation: '图书馆302'
  },
  {
    id: 4,
    className: '计算机科学231',
    studentName: '王五',
    studentId: '2133301203',
    teacherName: '赵六',
    totalDays: 70,
    checkedDays: 38,
    lastCheckinTime: '2026-06-02 14:22:15',
    lastCheckinLocation: '实验室B205'
  },
  {
    id: 5,
    className: '大数据技术231',
    studentName: '赵六',
    studentId: '2133402204',
    teacherName: '钱七',
    totalDays: 68,
    checkedDays: 60,
    lastCheckinTime: '2026-06-03 10:05:43',
    lastCheckinLocation: '机房C102'
  },
  {
    id: 6,
    className: '信息安全231',
    studentName: '孙七',
    studentId: '2133503205',
    teacherName: '孙八',
    totalDays: 66,
    checkedDays: 28,
    lastCheckinTime: '2026-05-30 11:33:08',
    lastCheckinLocation: '教学楼D405'
  },
  {
    id: 7,
    className: '软件技术231',
    studentName: '周八',
    studentId: '2133200106',
    teacherName: '廖清科',
    totalDays: 68,
    checkedDays: 55,
    lastCheckinTime: '2026-06-03 09:42:17',
    lastCheckinLocation: '教学楼A301'
  },
  {
    id: 8,
    className: '人工智能232',
    studentName: '吴九',
    studentId: '2133307207',
    teacherName: '郑十',
    totalDays: 72,
    checkedDays: 48,
    lastCheckinTime: '2026-06-02 16:18:29',
    lastCheckinLocation: '实验室B208'
  },
  {
    id: 9,
    className: '计算机科学232',
    studentName: '郑十',
    studentId: '2133301308',
    teacherName: '冯十一',
    totalDays: 68,
    checkedDays: 62,
    lastCheckinTime: '2026-06-03 08:45:56',
    lastCheckinLocation: '图书馆201'
  },
  {
    id: 10,
    className: '大数据技术232',
    studentName: '冯十一',
    studentId: '2133402309',
    teacherName: '陈十二',
    totalDays: 65,
    checkedDays: 35,
    lastCheckinTime: '2026-06-01 13:27:44',
    lastCheckinLocation: '机房C105'
  }
])

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
  setTimeout(() => {
    loading.value = false
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条记录`)
  }, 300)
}

function handleReset() {
  searchForm.className = ''
  searchForm.studentName = ''
  searchForm.teacherName = ''
  searchForm.semester = '2025-2026学年第1学期(当)'
  currentPage.value = 1
  pageSize.value = 10
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
