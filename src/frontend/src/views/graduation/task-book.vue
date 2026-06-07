<template>
  <div class="taskbook-container">
    <div class="search-section">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>学期</label>
            <el-select v-model="searchForm.semester" placeholder="请选择学期" clearable style="width: 100%">
              <el-option label="2025-2026学年第2学期(当前)" value="2025-2026-2" />
              <el-option label="2024-2025学年第2学期" value="2024-2025-2" />
              <el-option label="2024-2025学年第1学期" value="2024-2025-1" />
              <el-option label="2023-2024学年第2学期" value="2023-2024-2" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>题目</label>
            <el-input v-model="searchForm.topicName" placeholder="请输入毕业设计题目" clearable />
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
            <label>状态</label>
            <el-select v-model="searchForm.status" placeholder="请选择任务书状态" clearable style="width: 100%">
              <el-option label="未提交" value="未提交" />
              <el-option label="待审核" value="待审核" />
              <el-option label="已通过" value="已通过" />
              <el-option label="已驳回" value="已驳回" />
              <el-option label="已下达" value="已下达" />
            </el-select>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" align="middle" style="margin-top: 16px;">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>指导教师</label>
            <el-input v-model="searchForm.teacherName" placeholder="请输入教师姓名" clearable />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
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

    <div class="table-section">
      <el-table
        ref="tableRef"
        :data="paginatedData"
        :table-layout="'fixed'"
        class="custom-table"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" align="center" fixed="left" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="studentName" label="学生" width="80" align="center" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="老师" width="80" align="center" show-overflow-tooltip />
        <el-table-column prop="topicName" label="毕业设计题目" min-width="160" align="center" show-overflow-tooltip />
        <el-table-column prop="topicStatus" label="毕业设计题目状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="getTopicStatusType(row.topicStatus)" size="small">{{ row.topicStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deliverTime" label="毕业设计任务书下达时间" width="180" align="center" />
        <el-table-column prop="taskbookStatus" label="毕业设计任务书状态" width="160" align="center">
          <template #default="{ row }">
            <el-tag :type="getTaskbookStatusType(row.taskbookStatus)" size="small">{{ row.taskbookStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons-wrapper">
              <el-button type="primary" link size="small" @click="handleDeliverTaskbook(row)" :disabled="row.taskbookStatus === '已下达'">
                <el-icon><Download /></el-icon>
                下达任务书
              </el-button>
              <el-button type="warning" link size="small" @click="handleResetTaskbookStatus(row)">
                <el-icon><RefreshRight /></el-icon>
                重置状态
              </el-button>
            </div>
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

    <el-dialog v-model="detailDialogVisible" title="任务书详情" width="900px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentRecord?.id }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ currentRecord?.studentName }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ currentRecord?.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="毕设题目">{{ currentRecord?.topicName }}</el-descriptions-item>
        <el-descriptions-item label="选题状态">{{ currentRecord?.topicStatus }}</el-descriptions-item>
        <el-descriptions-item label="任务书状态">
          <el-tag :type="getTaskbookStatusType(currentRecord?.taskbookStatus)" size="large">{{ currentRecord?.taskbookStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下达时间" :span="2">{{ currentRecord?.deliverTime || '暂未下达' }}</el-descriptions-item>
        <el-descriptions-item label="任务书内容" :span="2">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentRecord?.taskContent || '暂无内容' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Download, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableRef = ref(null)
const detailDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  semester: '',
  topicName: '',
  studentName: '',
  teacherName: '',
  status: ''
})

const selectedRows = ref([])
const currentRecord = ref(null)

// 从API获取的数据
const tableData = ref([])
const totalRecords = ref(0)

/**
 * 从后端获取任务书列表
 */
async function fetchTaskBooks() {
  loading.value = true
  try {
    const res = await request.get('/v1/teacher/taskbooks/mine')
    
    if (res.data && Array.isArray(res.data)) {
      // 转换数据格式以匹配前端表格
      tableData.value = res.data.map((taskbook, index) => ({
        id: taskbook.task_id,
        studentName: taskbook.student_name || '未知',
        teacherName: taskbook.teacher_name || '未分配',
        topicName: taskbook.topic_name || '未选择题目',
        topicStatus: formatTopicStatus(taskbook.selection_status),
        deliverTime: taskbook.issued_at ? formatDate(taskbook.issued_at) : null,
        taskbookStatus: formatTaskbookStatus(taskbook.taskbook_status),
        taskContent: taskbook.content || '',
        selectionId: taskbook.selection_id,
        version: taskbook.version,
        // 保存原始数据供详情查看使用
        rawData: taskbook
      }))
      
      totalRecords.value = tableData.value.length
    }
  } catch (error) {
    console.error('获取任务书列表失败:', error)
    ElMessage.error('获取任务书列表失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

/**
 * 格式化选题状态显示
 */
function formatTopicStatus(status) {
  const statusMap = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已驳回',
    'cancelled': '已取消'
  }
  return statusMap[status] || status || '未提交'
}

/**
 * 格式化任务书状态显示
 */
function formatTaskbookStatus(status) {
  const statusMap = {
    'draft': '草稿',
    'unissued': '未下达',
    'pending': '待审核',
    'approved': '已确认',
    'rejected': '已驳回',
    'issued': '已下达',
    'confirmed': '已确认'
  }
  return statusMap[status] || status || '未知状态'
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
  fetchTaskBooks()
})

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (searchForm.semester && item.semester !== searchForm.semester) return false
    if (searchForm.topicName && !item.topicName.includes(searchForm.topicName)) return false
    if (searchForm.studentName && !item.studentName.includes(searchForm.studentName)) return false
    if (searchForm.teacherName && !item.teacherName.includes(searchForm.teacherName)) return false
    if (searchForm.status && item.taskbookStatus !== searchForm.status) return false
    return true
  })
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredData.value.slice(start, end)
})

function getTopicStatusType(status) {
  const map = {
    '未提交': 'info',
    '待审核': 'warning',
    '已通过': 'success',
    '已驳回': 'danger'
  }
  return map[status] || 'info'
}

function getTaskbookStatusType(status) {
  const map = {
    '草稿': 'info',
    '未下达': 'info',
    '待审核': 'warning',
    '已确认': 'success',
    '已驳回': 'danger',
    '已下达': 'success'
  }
  return map[status] || 'info'
}

function handleSearch() {
  loading.value = true
  currentPage.value = 1
  
  // 重新从后端获取数据
  fetchTaskBooks().then(() => {
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条记录`)
  })
}

function handleReset() {
  searchForm.semester = ''
  searchForm.topicName = ''
  searchForm.studentName = ''
  searchForm.teacherName = ''
  searchForm.status = ''
  currentPage.value = 1
  pageSize.value = 10
  
  // 重新获取所有数据
  fetchTaskBooks()
  ElMessage.info('已重置搜索条件')
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function handleViewDetail(row) {
  currentRecord.value = row
  detailDialogVisible.value = true
}

async function handleDeliverTaskbook(row) {
  if (row.topicStatus !== '已通过') {
    ElMessage.warning('只能为选题状态为"已通过"的学生下达任务书')
    return
  }

  ElMessageBox.confirm(
    `确定要为学生 "${row.studentName}" 下达毕业设计任务书吗？<br/><br/>
     <small style="color: #909399;">题目：${row.topicName}</small>`,
    '确认下达',
    {
      confirmButtonText: '确认下达',
      cancelButtonText: '取消',
      type: 'success',
      dangerouslyUseHTMLString: true
    }
  ).then(async () => {
    try {
      // 调用后端API下达任务书
      const taskBookData = {
        selectionId: row.selectionId,
        content: `一、课题背景与意义\n本课题"${row.topicName}"的研究具有重要的理论意义和实际应用价值...\n\n二、研究目标与主要内容\n1. 完成${row.topicName}系统的需求分析与总体设计\n2. 实现系统的核心功能模块\n3. 完成系统测试与优化\n\n三、进度安排\n第1-2周：文献调研与需求分析\n第3-4周：系统设计与技术选型\n第5-8周：核心功能实现\n第9-10周：系统集成与测试\n第11-12周：论文撰写与答辩准备\n\n四、参考文献\n[1] 相关领域的经典文献...\n[2] 最新研究成果...`,
        deadline: '2026-05-15', // 可以从表单获取或设置默认值
        requirements: JSON.stringify([
          { id: 1, text: '完成系统需求分析' },
          { id: 2, text: '设计完整的数据库结构' },
          { id: 3, text: '实现核心功能模块' },
          { id: 4, text: '完成系统测试与优化' }
        ]),
        techParams: JSON.stringify([
          { name: '前端框架', value: 'Vue 3 + Element Plus', note: '响应式设计' },
          { name: '后端框架', value: 'Spring Boot', note: 'RESTful API' },
          { name: '数据库', value: 'MySQL 8.0', note: '关系型数据库' }
        ]),
        references: '[1] 相关领域的经典文献\n[2] 最新研究成果'
      }
      
      await request.post('/taskbooks', taskBookData)
      
      // 下达成功后重新获取数据
      await fetchTaskBooks()
      
      ElMessage.success(`已成功为学生 ${row.studentName} 下达任务书！数据已同步到数据库`)
    } catch (error) {
      console.error('下达任务书失败:', error)
      ElMessage.error('❌ 下达任务书失败，请重试')
    }
  }).catch(() => {})
}

async function handleResetTaskbookStatus(row) {
  ElMessageBox.confirm(
    `确定要重置学生 "${row.studentName}" 的任务书状态吗？<br/><br/>
     <small style="color: #909399;">当前状态：${row.taskbookStatus} → 将变为"未下达"</small>`,
    '确认重置',
    {
      confirmButtonText: '确定重置',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true
    }
  ).then(async () => {
    try {
      // 调用后端API重置任务书状态（保留记录，只更新状态）
      if (row.id) {
        await request.put(`/taskbooks/${row.id}/reset`)
      }

      // 重置成功后重新获取数据
      await fetchTaskBooks()

      ElMessage.success(`已成功重置学生 ${row.studentName} 的任务书状态为"未下达"！`)
    } catch (error) {
      console.error('重置任务书失败:', error)
      ElMessage.error('❌ 重置任务书失败，请重试')
    }
  }).catch(() => {})
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
.taskbook-container {
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
  padding: 14px 0 !important;
}

.custom-table :deep(.el-table__row:hover > td) {
  background-color: #f5f7fa !important;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.action-buttons-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
}

.action-buttons-wrapper :deep(.el-button) {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
}

@media (max-width: 768px) {
  .search-buttons-inline {
    flex-wrap: wrap;
  }
}
</style>
