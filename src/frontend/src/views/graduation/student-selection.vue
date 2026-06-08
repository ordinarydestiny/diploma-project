<template>
  <div class="selection-container">
    <div class="search-section">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>题目</label>
            <el-input v-model="searchForm.topicName" placeholder="请输入毕业设计题目" clearable />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>题目状态</label>
            <el-select v-model="searchForm.topicStatus" placeholder="请选择毕业设计题目状态" clearable style="width: 100%">
              <el-option label="已通过" value="已通过" />
              <el-option label="待审核" value="待审核" />
              <el-option label="已驳回" value="已驳回" />
              <el-option label="未提交" value="未提交" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>专业</label>
            <el-select v-model="searchForm.major" placeholder="请选择专业" clearable style="width: 100%">
              <el-option label="软件技术" value="软件技术" />
              <el-option label="计算机科学" value="计算机科学" />
              <el-option label="大数据技术" value="大数据技术" />
              <el-option label="人工智能" value="人工智能" />
              <el-option label="信息安全" value="信息安全" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>学生姓名</label>
            <el-input v-model="searchForm.studentName" placeholder="请输入学生姓名" clearable />
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" align="middle" style="margin-top: 16px;">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>老师姓名</label>
            <el-input v-model="searchForm.teacherName" placeholder="请输入老师姓名" clearable />
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
        <el-table-column prop="major" label="专业" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="className" label="班级" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学生" width="80" align="center" />
        <el-table-column prop="studentId" label="学号" width="130" align="center" />
        <el-table-column prop="phone" label="学生手机号" width="140" align="center" />
        <el-table-column prop="teacherName" label="老师" width="90" align="center" show-overflow-tooltip />
        <el-table-column prop="topicName" label="毕业设计题目" min-width="280" align="center" show-overflow-tooltip />
        <el-table-column prop="topicDescription" label="毕业设计题目描述" min-width="280" align="center" show-overflow-tooltip />
        <el-table-column prop="status" label="毕业设计题目状态" width="160" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="毕业设计题目驳回/通过理由" min-width="180" align="center" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button type="warning" link size="small" @click="handleResetStatus(row)">
              <el-icon><RefreshRight /></el-icon>
              重置状态
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

    <el-dialog v-model="detailDialogVisible" title="选题详情" width="900px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentRecord?.id }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ currentRecord?.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRecord?.studentId }}</el-descriptions-item>
        <el-descriptions-item label="学生手机号">{{ currentRecord?.phone }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentRecord?.major }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentRecord?.className }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ currentRecord?.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="选题状态" :span="2">
          <el-tag :type="getStatusType(currentRecord?.status)" size="large">{{ currentRecord?.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="毕设题目" :span="2">{{ currentRecord?.topicName }}</el-descriptions-item>
        <el-descriptions-item label="题目描述" :span="2">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentRecord?.topicDescription }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRecord?.rejectReason" :label="currentRecord?.status === '已通过' ? '通过理由' : '驳回理由'" :span="2">
          <div :style="{ color: currentRecord?.status === '已通过' ? '#67c23a' : '#f56c6c', whiteSpace: 'pre-wrap', lineHeight: 1.6 }">{{ currentRecord?.rejectReason }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        
        <!-- 待审核状态下显示审核按钮 -->
        <template v-if="currentRecord?.status === '待审核'">
          <el-button type="danger" @click="handleRejectSelection">
            驳回
          </el-button>
          <el-button type="success" @click="handleApproveSelection">
            通过
          </el-button>
        </template>
        
        <!-- 已通过或已驳回状态显示重置按钮 -->
        <template v-if="currentRecord?.status === '已通过' || currentRecord?.status === '已驳回'">
          <el-button type="warning" @click="handleResetFromDetail">
            重置状态
          </el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, RefreshRight, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableRef = ref(null)
const detailDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  topicName: '',
  topicStatus: '',
  major: '',
  studentName: '',
  teacherName: ''
})

const selectedRows = ref([])
const currentRecord = ref(null)

// 从API获取的数据
const tableData = ref([])
const totalRecords = ref(0)

/**
 * 从后端获取选题列表
 */
async function fetchSelections() {
  loading.value = true
  try {
    // 调用后端API获取所有选题（支持状态筛选）
    const params = {}
    if (searchForm.topicStatus) {
      params.status = searchForm.topicStatus
    }
    
    const res = await request.get('/v1/teacher/selections/all', { params })
    
    if (res.data && Array.isArray(res.data)) {
      // 转换数据格式以匹配前端表格
      tableData.value = res.data.map((selection, index) => ({
        id: selection.selection_id,
        major: selection.major_name || '未分配',
        className: selection.class_name || '未分配',
        studentName: selection.student_name || '未知',
        studentId: selection.student_no || '-',
        phone: selection.student_phone || '-',
        teacherName: selection.teacher_name || '未分配',
        topicName: selection.topic_name || '未选择题目',
        topicDescription: selection.topic_description || '',
        status: formatStatus(selection.status),
        rejectReason: selection.review_comment || '',
        rawData: selection
      }))
      
      totalRecords.value = tableData.value.length
    }
  } catch (error) {
    console.error('获取选题列表失败:', error)
    ElMessage.error('获取选题列表失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

/**
 * 格式化状态显示
 */
function formatStatus(status) {
  const statusMap = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已驳回',
    'cancelled': '已取消',
    'submitted': '已提交'
  }
  return statusMap[status] || status || '未知'
}

// 页面加载时获取数据
onMounted(() => {
  fetchSelections()
})

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (searchForm.topicName && !item.topicName.includes(searchForm.topicName)) return false
    if (searchForm.topicStatus && item.status !== searchForm.topicStatus) return false
    if (searchForm.major && item.major !== searchForm.major) return false
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

function getStatusType(status) {
  const map = {
    '已通过': 'success',
    '待审核': 'warning',
    '已驳回': 'danger',
    '未提交': 'info'
  }
  return map[status] || 'info'
}

function handleSearch() {
  loading.value = true
  currentPage.value = 1
  
  // 重新从后端获取数据（带筛选条件）
  fetchSelections().then(() => {
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条记录`)
  })
}

function handleReset() {
  searchForm.topicName = ''
  searchForm.topicStatus = ''
  searchForm.major = ''
  searchForm.studentName = ''
  searchForm.teacherName = ''
  currentPage.value = 1
  pageSize.value = 10
  
  // 重新获取所有数据
  fetchSelections()
  ElMessage.info('已重置搜索条件')
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function handleViewDetail(row) {
  currentRecord.value = row
  detailDialogVisible.value = true
}

async function handleResetStatus(row) {
  ElMessageBox.confirm(
    `确定要重置学生 "${row.studentName}" 的选题状态吗？<br/><br/>
     <small style="color: #909399;">当前状态：${row.status} → 将变为"待审核"</small>`,
    '确认重置',
    {
      confirmButtonText: '确定重置',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true
    }
  ).then(async () => {
    try {
      // 调用后端API重置选题状态
      await request.put(`/selections/${row.id}/reset`, null, {
        params: { reason: '教师手动重置' }
      })
      
      // 重置成功后重新获取数据
      await fetchSelections()
      
      ElMessage.success(`✅ 已成功重置学生 ${row.studentName} 的选题状态为"待审核"`)
    } catch (error) {
      console.error('重置选题失败:', error)
      ElMessage.error('❌ 重置选题失败，请重试')
    }
  }).catch(() => {})
}

/**
 * 审核通过选题（从详情弹窗）
 */
async function handleApproveSelection() {
  if (!currentRecord.value) return

  ElMessageBox.prompt('请输入通过理由', '通过选题', {
    confirmButtonText: '确定通过',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入通过理由（必填）',
    inputValidator: (value) => {
      if (!value || !value.trim()) {
        return '通过理由不能为空'
      }
    }
  }).then(async ({ value }) => {
    try {
      // 调用后端API审核通过
      await request.put(`/selections/${currentRecord.value.id}/review`, {
        status: 'approved',  // 通过
        comment: value
      })

      // 审核成功后关闭弹窗并刷新数据
      detailDialogVisible.value = false
      await fetchSelections()

      ElMessage.success(`✅ 已通过学生 ${currentRecord.value.studentName} 的选题！`)
    } catch (error) {
      console.error('审核通过失败:', error)
      ElMessage.error('❌ 审核通过失败，请重试')
    }
  }).catch(() => {})
}

/**
 * 驳回选题（从详情弹窗）
 */
async function handleRejectSelection() {
  if (!currentRecord.value) return
  
  ElMessageBox.prompt('请输入驳回理由', '驳回选题', {
    confirmButtonText: '确定驳回',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入驳回理由（必填）',
    inputValidator: (value) => {
      if (!value || !value.trim()) {
        return '驳回理由不能为空'
      }
    }
  }).then(async ({ value }) => {
    try {
      // 调用后端API驳回选题
      await request.put(`/selections/${currentRecord.value.id}/review`, {
        status: 'rejected',  // 驳回
        comment: value
      })
      
      // 驳回成功后关闭弹窗并刷新数据
      detailDialogVisible.value = false
      await fetchSelections()
      
      ElMessage.success(`已驳回学生 ${currentRecord.value.studentName} 的选题\n原因：${value}`)
    } catch (error) {
      console.error('驳回选题失败:', error)
      ElMessage.error('❌ 驳回选题失败，请重试')
    }
  }).catch(() => {})
}

/**
 * 从详情弹窗重置状态
 */
async function handleResetFromDetail() {
  if (!currentRecord.value) return
  
  ElMessageBox.confirm(
    `确定要重置学生 "${currentRecord.value.studentName}" 的选题状态吗？`,
    '确认重置',
    {
      confirmButtonText: '确定重置',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 调用后端API重置
      await request.put(`/selections/${currentRecord.value.id}/reset`, null, {
        params: { reason: '从详情页重置' }
      })
      
      // 重置成功后关闭弹窗并刷新数据
      detailDialogVisible.value = false
      await fetchSelections()
      
      ElMessage.success(`✅ 已重置学生 ${currentRecord.value.studentName} 的选题状态`)
    } catch (error) {
      console.error('重置失败:', error)
      ElMessage.error('❌ 重置失败，请重试')
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
.selection-container {
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

@media (max-width: 768px) {
  .search-buttons {
    flex-wrap: wrap;
  }
}
</style>
