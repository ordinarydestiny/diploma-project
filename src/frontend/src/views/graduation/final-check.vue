<template>
  <div class="final-check-container">
    <div class="alert-section">
      <el-alert
        title="最终检查说明"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #default>
          <div class="alert-content">
            <p><strong>1. 定稿机制：</strong>学生提交的毕设报告可能需要多次修改，但<strong>一旦指导老师点击"通过"，该报告即为最终定稿版</strong>，后续不可修改。</p>
            <p><strong>2. 存档要求：</strong>学校将提取此页面被教师通过的毕设报告进行正式存档，请确保报告内容完整准确。</p>
            <p><strong>3. 教师评语：</strong>通过时填写的审核意见将作为该生毕设报告的<strong>教师评语</strong>进行存档，请认真填写。</p>
          </div>
        </template>
      </el-alert>
    </div>

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
              <el-option label="已定稿" value="已定稿" />
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
            <el-button type="success" plain @click="handleExport">
              <el-icon><Download /></el-icon>
              &nbsp;导出存档
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
        <el-table-column prop="submitTime" label="提交时间" width="180" align="center" />
        <el-table-column prop="version" label="版本" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small">V{{ row.version }}.0</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="检查状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" effect="dark">
              {{ row.status }}
              <el-icon v-if="row.status === '已定稿'" style="margin-left: 4px;"><Finished /></el-icon>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleApprove(row)">
              <el-icon><EditPen /></el-icon>
              审批
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

    <el-dialog v-model="detailDialogVisible" title="最终检查详情" width="1000px" destroy-on-close>
      <div class="dialog-header-info">
        <el-alert
          v-if="currentRecord?.status === '已定稿'"
          title="此报告已定稿"
          type="success"
          :closable="false"
          show-icon
          style="margin-bottom: 16px;"
        >
          <template #default>
            该生的毕设报告已于 {{ currentRecord?.finalizeTime }} 正式定稿，内容将用于学校存档。
          </template>
        </el-alert>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentRecord?.id }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ currentRecord?.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRecord?.studentId }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ currentRecord?.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="毕设题目" :span="2">{{ currentRecord?.topicName }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentRecord?.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="当前版本">
          <el-tag type="primary" effect="dark">V{{ currentRecord?.version }}.0</el-tag>
          <span v-if="currentRecord?.status === '已定稿'" style="margin-left: 8px; color: #67c23a; font-weight: bold;">(最终版)</span>
        </el-descriptions-item>
        <el-descriptions-item label="检查状态">
          <el-tag :type="getStatusType(currentRecord?.status)" size="large" effect="dark">
            {{ currentRecord?.status }}
            <el-icon v-if="currentRecord?.status === '已定稿'" style="margin-left: 4px;"><Finished /></el-icon>
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="定稿时间" v-if="currentRecord?.status === '已定稿'">
          <span style="color: #67c23a; font-weight: bold;">{{ currentRecord?.finalizeTime }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="教师评语（存档）" :span="2">
          <div class="teacher-comment" v-if="currentRecord?.teacherComment">
            <div class="comment-label">⭐ 该评语将作为正式的教师评语进行存档</div>
            <div class="comment-content">{{ currentRecord.teacherComment }}</div>
          </div>
          <div v-else style="color: #909399;">暂无教师评语</div>
        </el-descriptions-item>
        <el-descriptions-item label="毕设报告内容" :span="2">
          <div class="report-content">
            <div class="content-scroll">
              <pre>{{ currentRecord?.reportContent || '暂无报告内容' }}</pre>
            </div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="修改记录" :span="2" v-if="currentRecord?.revisionHistory?.length > 0">
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in currentRecord.revisionHistory"
              :key="index"
              :timestamp="item.time"
              :type="item.type"
              placement="top"
            >
              <div class="revision-item">
                <strong>V{{ item.version }}.0 - {{ item.action }}</strong>
                <p v-if="item.comment">{{ item.comment }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <template v-if="currentRecord?.status === '待审核'">
            <el-button type="danger" @click="handleDialogReject">
              <el-icon><Close /></el-icon>
              驳回修改
            </el-button>
            <el-button type="success" @click="handleDialogFinalize">
              <el-icon><Finished /></el-icon>
              通过并定稿
            </el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- ✨ 新增：定稿对话框（输入报告分数和教师评语） -->
    <el-dialog 
      v-model="finalizeDialogVisible" 
      title="通过并定稿" 
      width="600px" 
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-alert
        title="⚠️ 请认真填写以下信息，定稿后该报告将成为最终版本存档"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-form
        ref="finalizeFormRef"
        :model="finalizeForm"
        :rules="finalizeFormRules"
        label-width="120px"
        label-position="top"
      >
        <el-form-item label="* 报告成绩（0-100分）" prop="score">
          <el-input-number
            v-model="finalizeForm.score"
            :min="0"
            :max="100"
            :precision="1"
            :step="5"
            size="large"
            style="width: 100%;"
            placeholder="请输入报告分数（如：85、90、75等）"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            提示：根据论文质量、完成度、创新性等综合评定
          </div>
        </el-form-item>

        <el-form-item label="* 教师评语（不少于20字）" prop="comment">
          <el-input
            v-model="finalizeForm.comment"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请认真填写对该生毕设报告的评价和建议（必填，不少于20字）..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="finalizeDialogVisible = false">取 消</el-button>
          <el-button type="success" @click="handleSubmitFinalize" :loading="submittingFinalize">
            <el-icon><Finished /></el-icon>
            确认定稿
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, h } from 'vue'
import { Search, Refresh, EditPen, Finished, Close, Download } from '@element-plus/icons-vue'
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

// ✨ 新增：定稿对话框相关变量
const finalizeDialogVisible = ref(false)
const finalizeFormRef = ref(null)
const submittingFinalize = ref(false)
const finalizeForm = reactive({
          score: null,
          comment: '' // 教师评语
        })

// 定稿表单验证规则
const finalizeFormRules = {
  score: [
    { required: true, message: '请输入报告分数', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '分数必须在0-100之间', trigger: 'blur' }
  ],
  comment: [
    { required: true, message: '请输入教师评语', trigger: 'blur' },
    { min: 20, max: 500, message: '评语长度应在20-500字之间', trigger: 'blur' }
  ]
}

// 从API获取的数据
const tableData = ref([])
const totalRecords = ref(0)

/**
 * 从后端获取最终检查列表
 */
async function fetchFinalChecks() {
  loading.value = true
  try {
    const res = await request.get('/v1/teacher/final/all')
    
    if (res.data && Array.isArray(res.data)) {
      // 转换数据格式以匹配前端表格
      tableData.value = res.data.map((check, index) => ({
        id: check.check_id,
        studentName: check.student_name || '未知',
        studentId: check.student_no || '-',
        teacherName: check.teacher_name || '未分配',
        topicName: check.topic_name || '未选择题目',
        submitTime: check.submit_time ? formatDate(check.submit_time) : null,
        version: check.version || 1,
        status: formatStatus(check.status),
        reportContent: check.report_content || '',
        teacherComment: check.teacher_comment || '',
        finalizeTime: check.finalize_time ? formatDate(check.finalize_time) : null,
        isFinal: check.is_final === 1,
        // 保存原始数据供详情查看使用
        rawData: check
      }))
      
      totalRecords.value = tableData.value.length
    }
  } catch (error) {
    console.error('获取最终检查列表失败:', error)
    ElMessage.error('获取最终检查列表失败，请刷新页面重试')
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
    'approved': '已定稿',
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
  fetchFinalChecks()
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
    '已定稿': 'success',
    '需修改': 'danger'
  }
  return map[status] || 'info'
}

function handleSearch() {
  loading.value = true
  currentPage.value = 1
  
  // 重新从后端获取数据
  fetchFinalChecks().then(() => {
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
  fetchFinalChecks()
  ElMessage.info('已重置搜索条件')
}

function handleApprove(row) {
  currentRecord.value = row
  detailDialogVisible.value = true
}

// ✨ 修改：打开定稿对话框
function handleDialogFinalize() {
  if (!currentRecord.value) return

  // 重置表单数据为空
  finalizeForm.score = null
  finalizeForm.comment = ''
  
  // 打开定稿对话框
  finalizeDialogVisible.value = true
}

// ✨ 新增：提交定稿表单
async function handleSubmitFinalize() {
  // 表单验证
  if (!finalizeFormRef.value) return
  
  await finalizeFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请检查表单填写是否完整')
      return
    }

    submittingFinalize.value = true

    try {
      // 调用后端API：通过并定稿
      await request.put(`/final-checks/${currentRecord.value.id}/review`, null, {
        params: {
          status: 'approved',  // 通过状态
          comment: finalizeForm.comment,     // 教师评语
          score: finalizeForm.score           // 报告分数
        }
      })

      // 更新前端数据
      const index = tableData.value.findIndex(item => item.id === currentRecord.value.id)
      if (index > -1) {
        const now = new Date().toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        }).replace(/\//g, '-')

        tableData.value[index].status = '已定稿'
        tableData.value[index].teacherComment = finalizeForm.comment
        tableData.value[index].reportScore = finalizeForm.score
        tableData.value[index].finalizeTime = now

        if (!tableData.value[index].revisionHistory) {
          tableData.value[index].revisionHistory = []
        }

        tableData.value[index].revisionHistory.push({
          version: tableData.value[index].version,
          action: '通过并定稿',
          time: now,
          type: 'success',
          comment: `报告评分：${finalizeForm.score}分 | ${finalizeForm.comment.substring(0, 40)}...`
        })

        currentRecord.value = { ...tableData.value[index] }
        
        // 关闭所有弹窗
        finalizeDialogVisible.value = false
        detailDialogVisible.value = false
        
        ElMessage.success(`✅ 已通过并定稿学生 ${currentRecord.value.studentName} 的毕设报告\n\n` +
                       `📊 报告成绩：${finalizeForm.score}分\n` +
                       `📝 教师评语：${finalizeForm.comment.substring(0, 30)}...\n\n` +
                       `该报告将作为最终版本进行存档`)
      }
      
      // 刷新列表数据
      await fetchFinalChecks()
    } catch (error) {
      console.error('定稿失败:', error)
      ElMessage.error('❌ 定稿失败，请重试')
    } finally {
      submittingFinalize.value = false
    }
  })
}

function handleDialogReject() {
  if (!currentRecord.value) return

  ElMessageBox.prompt('请输入驳回理由和修改建议', '驳回修改', {
    confirmButtonText: '确定驳回',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请详细说明需要修改的内容和建议（必填）',
    inputValidator: (value) => {
      if (!value || !value.trim()) {
        return '驳回理由不能为空'
      }
    }
  }).then(async ({ value }) => {
    try {
      // 调用后端API：驳回修改
      await request.put(`/final-checks/${currentRecord.value.id}/review`, null, {
        params: {
          status: 'rejected',  // 驳回状态
          comment: value        // 驳回理由
        }
      })

      // 更新前端数据
      const index = tableData.value.findIndex(item => item.id === currentRecord.value.id)
      if (index > -1) {
        const now = new Date().toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        }).replace(/\//g, '-')

        tableData.value[index].status = '需修改'
        tableData.value[index].version += 1

        if (!tableData.value[index].revisionHistory) {
          tableData.value[index].revisionHistory = []
        }

        tableData.value[index].revisionHistory.push({
          version: tableData.value[index].version - 1,
          action: '驳回修改',
          time: now,
          type: 'danger',
          comment: value
        })

        currentRecord.value = { ...tableData.value[index] }
        
        // 关闭弹窗
        detailDialogVisible.value = false

        ElMessage.success(`✅ 已将学生 ${currentRecord.value.studentName} 的毕设报告驳回修改\n当前版本已升级至 V${tableData.value[index].version}.0\n\n数据已同步到数据库`)
      }
      
      // 刷新列表数据
      await fetchFinalChecks()
    } catch (error) {
      console.error('驳回失败:', error)
      ElMessage.error('❌ 驳回失败，请重试')
    }
  }).catch(() => {})
}

function handleExport() {
  const finalizedRecords = filteredData.value.filter(item => item.status === '已定稿')

  if (finalizedRecords.length === 0) {
    ElMessage.warning('没有已定稿的记录可导出')
    return
  }

  const headers = ['学生', '学号', '指导教师', '毕设题目', '定稿版本', '定稿时间', '教师评语']
  const data = finalizedRecords.map(row => ({
    '学生': row.studentName,
    '学号': row.studentId,
    '指导教师': row.teacherName,
    '毕设题目': row.topicName,
    '定稿版本': `V${row.version}.0`,
    '定稿时间': row.finalizeTime,
    '教师评语': row.teacherComment
  }))

  const ws = XLSX.utils.json_to_sheet(data)
  ws['!cols'] = [
    { wch: 10 },
    { wch: 16 },
    { wch: 12 },
    { wch: 35 },
    { wch: 12 },
    { wch: 20 },
    { wch: 80 }
  ]

  XLSX.utils.sheet_add_aoa(ws, [headers], { origin: 'A1' })

  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '最终检查存档')

  const fileName = `最终检查存档_${searchForm.semester}_${new Date().toISOString().slice(0, 10)}.xlsx`
  XLSX.writeFile(wb, fileName)

  ElMessage.success(`成功导出 ${finalizedRecords.length} 条已定稿记录（用于学校存档）`)
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
.final-check-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.alert-section {
  margin-bottom: 16px;
}

.alert-content p {
  margin: 8px 0;
  line-height: 1.6;
  color: #606266;
}

.alert-content strong {
  color: #e6a23c;
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

.teacher-comment {
  border: 2px solid #e6a23c;
  border-radius: 4px;
  padding: 12px;
  background-color: #fdf6ec;
}

.comment-label {
  color: #e6a23c;
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 13px;
}

.comment-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
  font-size: 14px;
}

.report-content {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px;
  background-color: #fafafa;
}

.content-scroll pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  font-size: 13px;
  line-height: 1.8;
  color: #303133;
}

.revision-item {
  line-height: 1.6;
}

.revision-item strong {
  color: #303133;
}

.revision-item p {
  margin: 4px 0 0 0;
  color: #606266;
  font-size: 13px;
}
</style>
