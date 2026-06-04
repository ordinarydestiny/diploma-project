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
        <el-col :span="18">
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
        <el-table-column prop="topicName" label="毕设题目" min-width="200" align="center" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="指导老师" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="status" label="答辩状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="答辩分数" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined" style="font-weight: bold; color: #409eff;">{{ row.score }}</span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="成绩等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.grade" :type="getGradeType(row.grade)" size="small" effect="dark">{{ row.grade }}</el-tag>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              进入详情
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

    <el-dialog v-model="detailDialogVisible" title="答辩详情管理" width="1100px" destroy-on-close>
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
            <el-tag v-if="currentRecord?.submitMethod" :type="currentRecord.submitMethod === 'teacher' ? 'success' : 'warning'" size="small">
              {{ currentRecord.submitMethod === 'teacher' ? '👨‍🏫 老师直接录入' : '👨‍🎓 学生录入' }}
            </el-tag>
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
          :disabled="isFormDisabled"
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
                  <el-option label="优秀 (90-100)" value="优秀" />
                  <el-option label="良好 (80-89)" value="良好" />
                  <el-option label="中等 (70-79)" value="中等" />
                  <el-option label="及格 (60-69)" value="及格" />
                  <el-option label="不及格 (<60)" value="不及格" />
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
import { ref, reactive, computed } from 'vue'
import { Search, Refresh, EditPen, Check, Close, Download, Upload, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'

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

const tableData = ref([
  {
    id: 58996,
    studentName: '张三',
    studentId: '19310111',
    teacherName: '廖',
    topicName: 'DDS波形发生器',
    status: '未答辩',
    score: null,
    grade: null,
    submitMethod: null,
    pdfFile: null,
    pdfFileName: null,
    defenseDate: null,
    location: null,
    committee: null,
    comment: null
  },
  {
    id: 58997,
    studentName: '李四',
    studentId: '19310222',
    teacherName: '王海洋',
    topicName: '电商平台后台管理系统',
    status: '待提交',
    score: null,
    grade: null,
    submitMethod: null,
    pdfFile: null,
    pdfFileName: null,
    defenseDate: null,
    location: null,
    committee: null,
    comment: null
  },
  {
    id: 58998,
    studentName: '王五',
    studentId: '19310333',
    teacherName: '赵六',
    topicName: '数据可视化分析平台',
    status: '待审核',
    score: 85,
    grade: '良好',
    submitMethod: 'student',
    pdfFile: null,
    pdfFileName: '王五_答辩记录表.pdf',
    defenseDate: '2026-05-20 14:00:00',
    location: '教学楼A301',
    committee: '钱七(主席)、孙八、周九',
    comment: '该生在答辩过程中表现良好，对系统的设计思路清晰，能够较好地回答评委提出的问题。系统功能完整，代码质量较高。建议在后续工作中进一步优化用户体验。'
  },
  {
    id: 58999,
    studentName: '赵六',
    studentId: '19310444',
    teacherName: '钱七',
    topicName: '大数据技术在供应链管理中的应用',
    status: '已通过',
    score: 92,
    grade: '优秀',
    submitMethod: 'teacher',
    pdfFile: null,
    pdfFileName: '赵六_答辩记录表_最终版.pdf',
    defenseDate: '2026-05-20 15:30:00',
    location: '教学楼A302',
    committee: '廖清科(主席)、王海洋、赵六',
    comment: '该生的毕业设计选题具有实际应用价值，研究方法科学合理，论文写作规范。答辩时表述清晰，逻辑性强，能够准确回答问题。大数据分析模型设计合理，实验结果充分。总体评价优秀。'
  },
  {
    id: 59000,
    studentName: '孙七',
    studentId: '19310555',
    teacherName: '周八',
    topicName: '智能推荐算法研究与实现',
    status: '需修改',
    score: 78,
    grade: '中等',
    submitMethod: 'student',
    pdfFile: null,
    pdfFileName: '孙七_答辩记录表_v2.pdf',
    defenseDate: '2026-05-21 09:00:00',
    location: '教学楼B201',
    committee: '吴九(主席)、郑十、王十一',
    comment: '该生答辩表现一般，对部分核心算法的理解不够深入。需要补充实验数据分析，优化推荐算法的性能指标说明。请在一周内修改完毕重新提交。'
  }
])

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
  return ['已通过', '未答辩'].includes(currentRecord.value.status)
})

const canEdit = computed(() => {
  if (!currentRecord.value) return false
  return ['待提交', '需修改'].includes(currentRecord.value.status)
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
    '良好': '',
    '中等': 'warning',
    '及格': 'info',
    '不及格': 'danger'
  }
  return map[grade] || 'info'
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
  searchForm.topicName = ''
  searchForm.status = ''
  searchForm.studentName = ''
  searchForm.teacherName = ''
  searchForm.semester = '2024-2025学年第2学期(当前)'
  currentPage.value = 1
  pageSize.value = 10
  ElMessage.info('已重置搜索条件')
}

function handleViewDetail(row) {
  currentRecord.value = row

  defenseForm.score = row.score
  defenseForm.grade = row.grade || ''
  defenseForm.defenseDate = row.defenseDate || ''
  defenseForm.location = row.location || ''
  defenseForm.committee = row.committee || ''
  defenseForm.comment = row.comment || ''

  fileList.value = []
  if (row.pdfFileName) {
    fileList.value.push({
      name: row.pdfFileName,
      status: 'success'
    })
  }

  detailDialogVisible.value = true
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

  try {
    await defenseFormRef.value.validate()

    if (!defenseForm.pdfFile && !currentRecord.value.pdfFileName) {
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
    ).then(() => {
      const index = tableData.value.findIndex(item => item.id === currentRecord.value.id)
      if (index > -1) {
        tableData.value[index].score = defenseForm.score
        tableData.value[index].grade = defenseForm.grade
        tableData.value[index].defenseDate = defenseForm.defenseDate
        tableData.value[index].location = defenseForm.location
        tableData.value[index].committee = defenseForm.committee
        tableData.value[index].comment = defenseForm.comment
        tableData.value[index].submitMethod = 'teacher'
        tableData.value[index].status = '待审核'

        if (defenseForm.pdfFile) {
          tableData.value[index].pdfFileName = `${currentRecord.value.studentName}_答辩记录表_${new Date().toISOString().slice(0, 10)}.pdf`
        }

        currentRecord.value = { ...tableData.value[index] }

        ElMessage.success(`✅ 已成功代为录入 ${currentRecord.value.studentName} 的答辩信息\n录入方式：老师直接录入\n当前状态：待审核`)
      }
    }).catch(() => {})
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

function handleApprove() {
  ElMessageBox.confirm(
    '确定要审核通过该生的答辩记录吗？通过后仍可驳回要求重新提交。',
    '确认审核通过',
    {
      confirmButtonText: '确定通过',
      cancelButtonText: '取消',
      type: 'success'
    }
  ).then(() => {
    const index = tableData.value.findIndex(item => item.id === currentRecord.value.id)
    if (index > -1) {
      tableData.value[index].status = '已通过'
      currentRecord.value = { ...tableData.value[index] }
      ElMessage.success(`✅ 已通过 ${currentRecord.value.studentName} 的答辩记录\n该记录已正式存档`)
    }
  }).catch(() => {})
}

function handleReject() {
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
  }).then(({ value }) => {
    const index = tableData.value.findIndex(item => item.id === currentRecord.value.id)
    if (index > -1) {
      tableData.value[index].status = '需修改'
      currentRecord.value = { ...tableData.value[index] }
      ElMessage.success(`已将 ${currentRecord.value.studentName} 的答辩记录驳回\n原因：${value}`)
    }
  }).catch(() => {})
}

function handleRejectAgain() {
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
  }).then(({ value }) => {
    const index = tableData.value.findIndex(item => item.id === currentRecord.value.id)
    if (index > -1) {
      tableData.value[index].status = '需修改'
      currentRecord.value = { ...tableData.value[index] }
      ElMessage.success(`已将 ${currentRecord.value.studentName} 的已通过记录驳回\n原因：${value}\n学生需重新提交修改后的材料`)
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
