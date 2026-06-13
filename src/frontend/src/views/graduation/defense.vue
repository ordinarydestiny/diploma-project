<template>
  <div class="defense-container">
    <div class="search-section">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>学期</label>
            <el-select v-model="searchForm.semester" placeholder="请选择学期" clearable style="width: 100%">
              <el-option label="2025-2026学年第1学期(当)" value="2025-2026-1" />
              <el-option label="2024-2025学年第2学期(当前)" value="2024-2025-2" />
              <el-option label="2024-2025学年第1学期" value="2024-2025-1" />
              <el-option label="2023-2024学年第2学期" value="2023-2024-2" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>毕设题目</label>
            <el-input v-model="searchForm.topicName" placeholder="请输入毕设题目" clearable />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>答辩状态</label>
            <el-select v-model="searchForm.status" placeholder="请选择答辩状态" clearable style="width: 100%">
              <el-option label="未答辩" value="未答辩" />
              <el-option label="待提交" value="待提交" />
              <el-option label="待审核" value="待审核" />
              <el-option label="已通过" value="已通过" />
              <el-option label="需修改" value="需修改" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>学生</label>
            <el-input v-model="searchForm.studentName" placeholder="请输入学生姓名" clearable />
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" align="middle" style="margin-top: 16px;">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>老师</label>
            <el-input v-model="searchForm.teacherName" placeholder="请输入老师姓名" clearable />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item search-buttons-item">
            <div class="search-buttons-inline">
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
        <el-table-column prop="topicName" label="毕设题目" min-width="200" align="center" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="指导老师" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="status" label="答辩状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="答辩分数" width="120" align="center">
          <template #default="{ row }">
            <!-- 只有已通过状态才显示分数 -->
            <span v-if="(row.status === '已通过' || row.status === 'approved') && row.score !== null && row.score !== undefined" style="font-weight: bold; color: #409eff;">{{ row.score }}</span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="成绩等级" width="100" align="center">
          <template #default="{ row }">
            <!-- 只有已通过状态才显示等级 -->
            <el-tag v-if="(row.status === '已通过' || row.status === 'approved') && row.grade" :type="getGradeType(row.grade)" size="small" effect="dark">{{ row.grade }}</el-tag>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 根据状态动态显示不同按钮 -->
            <!-- 未开始状态：显示"代为录入"按钮 -->
            <el-button 
              v-if="row.status === '未开始' || row.status === 'not_started'" 
              type="primary" 
              link 
              size="small" 
              @click="handleViewDetail(row)"
            >
              代为录入
            </el-button>
            <!-- 其他可编辑状态（无成绩）：显示"录入详情"按钮 -->
            <el-button 
              v-else-if="!row.score && row.status !== '待审核' && row.status !== '已通过'" 
              type="primary" 
              link 
              size="small" 
              @click="handleViewDetail(row)"
            >
              录入详情
            </el-button>
            <!-- 已有成绩或已审核：显示"查看详情"按钮 -->
            <el-button 
              v-else 
              type="primary" 
              link 
              size="small" 
              @click="handleViewDetail(row)"
            >
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

    <el-dialog v-model="detailDialogVisible" title="答辩详情管理" width="1100px" destroy-on-close @close="handleDialogClose">
      <div class="dialog-header-info">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="ID">{{ currentRecord?.id }}</el-descriptions-item>
          <el-descriptions-item label="学生姓名">{{ currentRecord?.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentRecord?.studentId }}</el-descriptions-item>
          <el-descriptions-item label="指导老师">{{ currentRecord?.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="毕设题目" :span="2">{{ currentRecord?.topicName }}</el-descriptions-item>
          <el-descriptions-item label="答辩状态">
            <el-tag :type="getStatusType(currentRecord?.status)" size="large">{{ currentRecord?.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交方式">
            <!-- 只有在非"未开始"状态下才显示提交方式 -->
            <template v-if="currentRecord?.status && currentRecord?.status !== '未开始' && currentRecord?.status !== 'not_started'">
              <el-tag v-if="currentRecord?.submitMethod" :type="currentRecord.submitMethod === 'teacher' ? 'success' : 'warning'" size="small">
                {{ currentRecord.submitMethod === 'teacher' ? '👨‍🏫 老师直接录入' : '👨‍🎓 学生录入' }}
              </el-tag>
              <span v-else style="color: #909399;">暂无</span>
            </template>
            <span v-else style="color: #909399;">暂无</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="!currentRecord?.score && !currentRecord?.pdfFile"
          title="提示：该生尚未填写答辩信息"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #default>
            若此时该生没填答辩成绩、没传答辩记录PDF，你可点击下方按钮<strong>"代为录入"</strong>，录入该生答辩成绩并上传答辩记录表PDF。
          </template>
        </el-alert>
      </div>

      <el-divider content-position="left">📋 答辩成绩与文档</el-divider>

      <div class="form-section">
        <el-form
          ref="defenseFormRef"
          :model="defenseForm"
          :rules="defenseRules"
          label-width="120px"
          :disabled="isFormDisabled && !canEdit"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="答辩分数" prop="score">
                <el-input-number
                  v-model="defenseForm.score"
                  :min="0"
                  :max="100"
                  :precision="1"
                  placeholder="请输入答辩分数(0-100)"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="成绩等级" prop="grade">
                <el-select v-model="defenseForm.grade" placeholder="请选择成绩等级" style="width: 100%;">
                  <el-option label="优秀 (90-100)" value="excellent" />
                  <el-option label="良好 (80-89)" value="good" />
                  <el-option label="中等 (70-79)" value="medium" />
                  <el-option label="及格 (60-69)" value="pass" />
                  <el-option label="不及格 (<60)" value="fail" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="答辩日期" prop="defenseDate">
                <el-date-picker
                  v-model="defenseForm.defenseDate"
                  type="datetime"
                  placeholder="选择答辩日期时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="答辩地点" prop="location">
                <el-input v-model="defenseForm.location" placeholder="请输入答辩地点" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="答辩委员会" prop="committee">
            <el-input v-model="defenseForm.committee" placeholder="请输入答辩委员会成员，如：张三(主席)、李四、王五" />
          </el-form-item>

          <el-form-item label="答辩评语" prop="comment">
            <el-input
              v-model="defenseForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入答辩评语（将作为正式存档）"
            />
          </el-form-item>

          <el-form-item label="答辩记录表" prop="pdfFile">
            <div class="upload-section">
              <el-upload
                ref="uploadRef"
                :auto-upload="false"
                :limit="1"
                accept=".pdf"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                :file-list="fileList"
              >
                <template #trigger>
                  <el-button type="primary" plain>
                    <el-icon><Upload /></el-icon>
                    选择PDF文件
                  </el-button>
                </template>
                <template #tip>
                  <div class="el-upload__tip">只能上传PDF文件，且不超过10MB</div>
                </template>
              </el-upload>

              <div v-if="currentRecord?.pdfFileName" class="existing-file">
                <el-tag type="success" closable @close="handleRemoveExistingFile">
                  📄 {{ currentRecord.pdfFileName }}
                </el-tag>
                <el-button type="primary" link size="small" style="margin-left: 10px;" @click="handlePreviewPdf">
                  查看文件
                </el-button>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>

          <template v-if="canEdit">
            <el-button type="primary" @click="handleSubmitAsTeacher">
              <el-icon><EditPen /></el-icon>
              代为录入（作为老师）
            </el-button>
          </template>

          <template v-if="currentRecord?.status === '待审核'">
            <el-button type="danger" @click="handleReject">
              <el-icon><Close /></el-icon>
              驳回重提交
            </el-button>
            <el-button type="success" @click="handleApprove">
              <el-icon><Check /></el-icon>
              审核通过
            </el-button>
          </template>

          <template v-if="currentRecord?.status === '已通过'">
            <el-button type="warning" @click="handleRejectAgain">
              <el-icon><RefreshLeft /></el-icon>
              驳回修改
            </el-button>
          </template>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, EditPen, Check, Close, Download, Upload, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import request from '@/utils/request'

const loading = ref(false)
const tableRef = ref(null)
const detailDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const defenseFormRef = ref(null)
const uploadRef = ref(null)

const searchForm = reactive({
  semester: '2024-2025学年第2学期(当前)',
  topicName: '',
  status: '',
  studentName: '',
  teacherName: ''
})

const currentRecord = ref(null)
const fileList = ref([])

const defenseForm = reactive({
  score: null,
  grade: '',
  defenseDate: '',
  location: '',
  committee: '',
  comment: '',
  pdfFile: null
})

const defenseRules = {
  score: [
    { required: true, message: '请输入答辩分数', trigger: 'blur' }
  ],
  grade: [
    { required: true, message: '请选择成绩等级', trigger: 'change' }
  ],
  defenseDate: [
    { required: true, message: '请选择答辩日期', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入答辩评语', trigger: 'blur' },
    { min: 10, message: '答辩评语不少于10个字', trigger: 'blur' }
  ]
}

// 从API获取的数据
const tableData = ref([])
const totalRecords = ref(0)

/**
 * 从后端获取答辩记录列表
 */
async function fetchDefenseRecords() {
  loading.value = true
  try {
    const res = await request.get('/v1/teacher/defenses')
    
    if (res.data && Array.isArray(res.data)) {
      // 转换数据格式以匹配前端表格
      tableData.value = res.data.map((record, index) => {
        const status = record.status || ''
        const isApproved = status === 'approved'
        
        return {
          id: record.defense_id,
          studentName: record.student_name || '未知',
          studentId: record.student_no || '-',
          teacherName: record.teacher_name || '未分配',
          topicName: record.topic_name || '未选择题目',
          status: formatStatus(status),
          // 只有已通过状态才显示分数和等级
          score: isApproved ? (record.defense_score_num || null) : null,
          grade: isApproved ? formatGrade(record.defense_score) : null,
          submitMethod: record.submitter_type || null,
          pdfFile: null,
          pdfFileName: record.file_name || null,
          defenseDate: record.defense_datetime ? formatDate(record.defense_datetime) : null,
          location: record.location || null,
          committee: record.committee || null,
          comment: record.review_comment || null,
          rawData: record
        }
      })
      
      totalRecords.value = tableData.value.length
    }
  } catch (error) {
    console.error('获取答辩记录列表失败:', error)
    ElMessage.error('获取答辩记录列表失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

/**
 * 格式化状态显示
 */
function formatStatus(status) {
  const statusMap = {
    'not_started': '未开始',
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '需修改',
    'draft': '草稿',
    'submitted': '已提交'
  }
  return statusMap[status] || status || '未知'
}

/**
 * 格式化成绩等级显示
 */
function formatGrade(grade) {
  if (!grade) return null
  const gradeMap = {
    'excellent': '优秀',
    'good': '良好',
    'medium': '中等',
    'pass': '及格',
    'fail': '不及格'
  }
  return gradeMap[grade] || grade
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
  fetchDefenseRecords()
})

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (searchForm.topicName && !item.topicName.includes(searchForm.topicName)) return false
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

const isFormDisabled = computed(() => {
  if (!currentRecord.value) return false
  // 已通过的记录默认禁用表单（防止误修改），但可以通过按钮启用
  return currentRecord.value.status === '已通过'
})

const canEdit = computed(() => {
  if (!currentRecord.value) return false
  
  // 指导老师在以下情况下可以代为录入/修改：
  // 1. 未开始 - 学生还没有开始答辩（初始状态）
  // 2. 未答辩 - 学生还没有答辩记录
  // 3. 待提交 - 学生已填写但未正式提交
  // 4. 需修改 - 被驳回后需要重新提交
  // 5. 已通过但无成绩 - 特殊情况允许补充录入（可选）
  
  const editableStatuses = ['未开始', 'not_started', '未答辩', '待提交', '需修改']
  
  // 如果是已通过但没有分数或PDF，也允许重新录入
  if (currentRecord.value.status === '已通过') {
    const hasScore = currentRecord.value.score && currentRecord.value.score > 0
    const hasFile = currentRecord.value.pdfFileName || fileList.value.length > 0
    if (!hasScore && !hasFile) {
      return true  // 允许补充录入
    }
  }
  
  return editableStatuses.includes(currentRecord.value.status)
})

function getStatusType(status) {
  const map = {
    '未答辩': 'info',
    '待提交': 'warning',
    '待审核': 'warning',
    '已通过': 'success',
    '需修改': 'danger'
  }
  return map[status] || 'info'
}

function getGradeType(grade) {
  const map = {
    '优秀': 'success',
    '良好': 'warning',
    '中等': 'primary',
    '及格': 'info',
    '不及格': 'danger'
  }
  return map[grade] || 'info'
}

function handleSearch() {
  loading.value = true
  currentPage.value = 1
  
  // 重新从后端获取数据
  fetchDefenseRecords().then(() => {
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条记录`)
  })
}

function handleReset() {
  searchForm.topicName = ''
  searchForm.status = ''
  searchForm.studentName = ''
  searchForm.teacherName = ''
  searchForm.semester = '2024-2025学年第2学期(当前)'
  currentPage.value = 1
  pageSize.value = 10
  
  // 重新获取所有数据
  fetchDefenseRecords()
  ElMessage.info('已重置搜索条件')
}

function handleViewDetail(row) {
  currentRecord.value = row

  // 【重要】根据状态设置合理的默认值
  const isNotStarted = row.status === '未开始' || row.status === 'not_started'
  
  if (isNotStarted) {
    // 未开始状态：设置默认值（避免验证失败）
    defenseForm.score = null  // 允许用户输入
    defenseForm.grade = ''     // 必须选择
    defenseForm.defenseDate = new Date().toISOString().slice(0, 19).replace('T', ' ')  // 默认当前时间
    defenseForm.location = ''
    defenseForm.committee = ''
    defenseForm.comment = ''   // 必须填写（≥10字）
  } else {
    // 其他状态：使用实际数据
    defenseForm.score = row.score || null
    defenseForm.grade = row.grade || ''
    defenseForm.defenseDate = row.defenseDate || ''
    defenseForm.location = row.location || ''
    defenseForm.committee = row.committee || ''
    defenseForm.comment = row.comment || ''
  }

  fileList.value = []
  if (row.pdfFileName) {
    fileList.value.push({
      name: row.pdfFileName,
      status: 'success'
    })
  }

  detailDialogVisible.value = true
}

function handleDialogClose() {
  // 重置表单验证状态和字段值
  if (defenseFormRef.value) {
    defenseFormRef.value.resetFields()
  }
  
  // 重置表单数据为初始状态
  defenseForm.score = null
  defenseForm.grade = ''
  defenseForm.defenseDate = ''
  defenseForm.location = ''
  defenseForm.committee = ''
  defenseForm.comment = ''
  defenseForm.pdfFile = null
  
  fileList.value = []
  currentRecord.value = null
}

function handleFileChange(file) {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  defenseForm.pdfFile = file.raw
}

function handleFileRemove() {
  defenseForm.pdfFile = null
}

function handleRemoveExistingFile() {
  ElMessageBox.confirm('确定要删除已上传的答辩记录表吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = tableData.value.findIndex(item => item.id === currentRecord.value.id)
    if (index > -1) {
      tableData.value[index].pdfFile = null
      tableData.value[index].pdfFileName = null
      currentRecord.value = { ...tableData.value[index] }
      fileList.value = []
      ElMessage.success('已删除答辩记录表')
    }
  }).catch(() => {})
}

function handlePreviewPdf() {
  ElMessage.info('预览功能开发中...')
}

async function handleSubmitAsTeacher() {
  if (!defenseFormRef.value) return
  
  // 【修复】在操作前先保存当前记录的引用，避免关闭弹窗后被清空
  const record = currentRecord.value
  
  if (!record) {
    ElMessage.error('❌ 未找到当前记录，请重试')
    return
  }

  try {
    await defenseFormRef.value.validate()

    if (!defenseForm.pdfFile && !record.pdfFileName) {
      ElMessage.warning('请上传答辩记录表PDF文件')
      return
    }

    ElMessageBox.confirm(
      '确定要以老师身份代为录入该生的答辩信息吗？录入后将直接标记为"待审核"状态。',
      '确认代为录入',
      {
        confirmButtonText: '确定录入',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(async () => {
      try {
        // 准备提交数据 - 传递完整的答辩信息（使用局部变量record）
        const submitData = new FormData()
        submitData.append('selectionId', record.selectionId || record.id)
        submitData.append('defenseScore', defenseForm.grade || '良好')  // 成绩等级
        submitData.append('defenseScoreNum', defenseForm.score || 0)  // 数值分数
        
        // 【新增】传递完整字段
        if (defenseForm.defenseDate) {
          submitData.append('defenseDatetime', defenseForm.defenseDate)  // 答辩日期时间
        }
        if (defenseForm.location) {
          submitData.append('location', defenseForm.location)  // 答辩地点
        }
        if (defenseForm.committee) {
          submitData.append('committee', defenseForm.committee)  // 答辩委员会
        }
        if (defenseForm.comment) {
          submitData.append('comment', defenseForm.comment)  // 答辩评语
        }
        
        // 如果有PDF文件，添加到FormData
        if (defenseForm.pdfFile) {
          submitData.append('pdfFile', defenseForm.pdfFile)
        } else if (record.pdfFileName) {
          // 如果已有文件，不需要重新上传（使用原有的recordFileId）
          // 这里可以不传或传null，后端会处理
        }
        
        // 调用后端API：教师录入答辩成绩（完整版）
        await request.post('/defenses/submitByTeacher', submitData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        
        // 录入成功后重新获取数据
        await fetchDefenseRecords()
        
        detailDialogVisible.value = false
        
        // 使用局部变量显示成功消息（避免currentRecord已被置空）
        ElMessage.success(`已成功代为录入 ${record.studentName} 的完整答辩信息！`)
      } catch (error) {
        console.error('录入答辩信息失败:', error)
        ElMessage.error('❌ 录入失败，请重试')
      }
    }).catch(() => {})
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

async function handleApprove() {
  // 【修复】在操作前先保存当前记录的引用，避免关闭弹窗后被清空
  const record = currentRecord.value
  
  if (!record) {
    ElMessage.error('❌ 未找到当前记录，请重试')
    return
  }
  
  ElMessageBox.confirm(
    '确定要审核通过该生的答辩记录吗？通过后仍可驳回要求重新提交。',
    '确认审核通过',
    {
      confirmButtonText: '确定通过',
      cancelButtonText: '取消',
      type: 'success'
    }
  ).then(async () => {
    try {
      // 调用后端API：审核通过（同时传递表单中的分数和等级）
      await request.put(`/defenses/${record.id}/review`, null, {
        params: {
          status: 'approved',
          comment: '答辩记录审核通过，成绩有效',
          // 【重要】传递表单中的分数和等级
          defenseScore: defenseForm.grade || null,
          defenseScoreNum: defenseForm.score || null
        }
      })
      
      // 审核成功后关闭弹窗并刷新数据
      detailDialogVisible.value = false
      await fetchDefenseRecords()
      
      // 使用局部变量显示成功消息（避免currentRecord已被置空）
      ElMessage.success(`已通过 ${record.studentName} 的答辩记录！`)
    } catch (error) {
      console.error('审核通过失败:', error)
      ElMessage.error('❌ 审核失败，请重试')
    }
  }).catch(() => {})
}

function handleReject() {
  // 【修复】在操作前先保存当前记录的引用，避免关闭弹窗后被清空
  const record = currentRecord.value
  
  if (!record) {
    ElMessage.error('❌ 未找到当前记录，请重试')
    return
  }
  
  ElMessageBox.prompt('请输入驳回理由', '驳回重提交', {
    confirmButtonText: '确定驳回',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请详细说明需要修改的内容（必填）',
    inputValidator: (value) => {
      if (!value || !value.trim()) {
        return '驳回理由不能为空'
      }
    }
  }).then(async ({ value }) => {
    try {
      // 调用后端API：驳回（使用局部变量record）
      await request.put(`/defenses/${record.id}/review`, null, {
        params: {
          status: 'rejected',
          comment: value
        }
      })
      
      // 驳回成功后关闭弹窗并刷新数据
      detailDialogVisible.value = false
      await fetchDefenseRecords()
      
      // 使用局部变量显示成功消息（避免currentRecord已被置空）
      ElMessage.success(`已将 ${record.studentName} 的答辩记录驳回`)
    } catch (error) {
      console.error('驳回失败:', error)
      ElMessage.error('❌ 驳回失败，请重试')
    }
  }).catch(() => {})
}

function handleRejectAgain() {
  // 【修复】在操作前先保存当前记录的引用，避免关闭弹窗后被清空
  const record = currentRecord.value
  
  if (!record) {
    ElMessage.error('❌ 未找到当前记录，请重试')
    return
  }
  
  ElMessageBox.prompt('请输入驳回理由（已通过的记录也可以驳回重提交）', '驳回修改', {
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
      // 调用后端API：驳回（使用局部变量record）
      await request.put(`/defenses/${record.id}/review`, null, {
        params: {
          status: 'rejected',
          comment: `[重新驳回] ${value}`
        }
      })
      
      // 驳回成功后关闭弹窗并刷新数据
      detailDialogVisible.value = false
      await fetchDefenseRecords()
      
      // 使用局部变量显示成功消息（避免currentRecord已被置空）
      ElMessage.success(`已将 ${record.studentName} 的答辩记录驳回修改`)
    } catch (error) {
      console.error('驳回失败:', error)
      ElMessage.error('❌ 驳回失败，请重试')
    }
  }).catch(() => {})
}

function handleExport() {
  const passedRecords = filteredData.value.filter(item => item.status === '已通过')

  if (passedRecords.length === 0) {
    ElMessage.warning('没有已通过的答辩记录可导出')
    return
  }

  const headers = ['ID', '学生', '学号', '指导老师', '毕设题目', '答辩分数', '成绩等级', '答辩日期', '答辩地点', '答辩评语']
  const data = passedRecords.map(row => ({
    'ID': row.id,
    '学生': row.studentName,
    '学号': row.studentId,
    '指导老师': row.teacherName,
    '毕设题目': row.topicName,
    '答辩分数': row.score,
    '成绩等级': row.grade,
    '答辩日期': row.defenseDate,
    '答辩地点': row.location,
    '答辩评语': row.comment
  }))

  const ws = XLSX.utils.json_to_sheet(data)
  ws['!cols'] = [
    { wch: 8 },
    { wch: 10 },
    { wch: 14 },
    { wch: 12 },
    { wch: 30 },
    { wch: 12 },
    { wch: 10 },
    { wch: 20 },
    { wch: 15 },
    { wch: 60 }
  ]

  XLSX.utils.sheet_add_aoa(ws, [headers], { origin: 'A1' })

  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '答辩记录存档')

  const fileName = `答辩记录存档_${searchForm.semester}_${new Date().toISOString().slice(0, 10)}.xlsx`
  XLSX.writeFile(wb, fileName)

  ElMessage.success(`成功导出 ${passedRecords.length} 条已通过的答辩记录（用于存档）`)
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
.defense-container {
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
  height: 32px;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.form-section {
  padding: 0 20px;
}

.upload-section {
  width: 100%;
}

.existing-file {
  margin-top: 10px;
  display: flex;
  align-items: center;
}
</style>
