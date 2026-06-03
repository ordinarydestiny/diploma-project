<template>
  <div class="relation-container">
    <div class="search-section">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="12" :md="8">
          <div class="search-item">
            <label>批次名称</label>
            <el-select v-model="searchForm.batchId" placeholder="请选择批次" clearable style="width: 100%">
              <el-option label="BD20240001-2024级软件技术" value="BD20240001" />
              <el-option label="BD20230001-2023级计算机科学" value="BD20230001" />
              <el-option label="BD20220001-2022级大数据技术" value="BD20220001" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>老师姓名</label>
            <el-input v-model="searchForm.teacherName" placeholder="请输入老师姓名" clearable />
          </div>
        </el-col>
        <el-col :xs="24" :sm="24" :md="10">
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
        <el-button type="primary" @click="handleImportStudent">
          <el-icon><Plus /></el-icon>
          纳入学生
        </el-button>
        <el-button type="success" @click="handleAssignTeacher">
          <el-icon><UserFilled /></el-icon>
          分配指导教师
        </el-button>
        <el-button @click="handleImportRelation">
          <el-icon><Upload /></el-icon>
          导入分配关系
        </el-button>
        <el-button @click="handleExportRelation">
          <el-icon><Download /></el-icon>
          导出分配关系
        </el-button>
        <el-button type="danger" @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除
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
        :data="paginatedData"
        :table-layout="'fixed'"
        class="custom-table"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" align="center" fixed="left" />
        <el-table-column prop="studentName" label="姓名" align="center" />
        <el-table-column prop="studentId" label="学号" align="center" />
        <el-table-column prop="majorName" label="专业" align="center" show-overflow-tooltip />
        <el-table-column prop="className" label="班级" align="center" />
        <el-table-column prop="teacherName" label="指导教师" align="center">
          <template #default="{ row }">
            <span v-if="row.teacherName">{{ row.teacherName }}</span>
            <span v-else style="color: #909399">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleRemove(row)">
              <el-icon><Delete /></el-icon>
              移除
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

    <el-dialog v-model="importDialogVisible" title="纳入学生" width="900px" destroy-on-close class="import-dialog">
      <div class="class-filter">
        <el-select v-model="selectedClass" placeholder="请选择班级" style="width: 300px" @change="handleClassChange">
          <el-option label="软件技术-软件182" value="软件182" />
          <el-option label="软件技术-软件183" value="软件183" />
          <el-option label="软件技术-软件184" value="软件184" />
          <el-option label="计算机科学-计科181" value="计科181" />
        </el-select>
        <el-button type="success" plain>该计划已分配{{ assignedCount }}位学生</el-button>
      </div>

      <div class="student-table-wrapper">
        <el-table
          ref="studentTableRef"
          :data="paginatedStudents"
          border
          stripe
          v-loading="studentLoading"
          @selection-change="handleStudentSelectionChange"
          max-height="400"
          empty-text="暂无可纳入的学生"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="name" label="姓名" width="120" align="center" />
          <el-table-column prop="studentId" label="学号" width="160" align="center" />
          <el-table-column prop="majorName" label="专业" min-width="140" align="center" />
          <el-table-column prop="className" label="班级" width="140" align="center" />
        </el-table>

        <div v-if="availableStudents.length === 0 && !studentLoading" class="empty-tip">
          <el-empty description="该班级学生已全部纳入或暂无数据" :image-size="80">
            <template #description>
              <p style="color: #909399; font-size: 14px;">{{ selectedClass ? `${selectedClass} 班级的学生已全部纳入` : '所有班级的可选学生已全部纳入' }}</p>
            </template>
          </el-empty>
        </div>

        <div class="table-footer">
          <div class="footer-tip">默认为10条数据，全选某班 请自行调整</div>
          <el-pagination
            v-model:current-page="studentCurrentPage"
            v-model:page-size="studentPageSize"
            :total="availableStudents.length"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            size="small"
            @size-change="handleStudentSizeChange"
            @current-change="handleStudentPageChange"
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleImportSubmit" :disabled="selectedStudents.length === 0">纳入 ({{ selectedStudents.length }})</el-button>
          <el-button @click="importDialogVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="分配指导教师" width="600px" destroy-on-close>
      <el-alert v-if="selectedRows.length > 0" :title="`已选择 ${selectedRows.length} 名学生`" type="info" :closable="false" show-icon style="margin-bottom: 16px" />
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item label="指导教师" prop="teacherName">
          <el-select
            v-model="assignForm.teacherName"
            placeholder="请输入关键词搜索或选择指导教师"
            filterable
            remote
            reserve-keyword
            :remote-method="searchTeachers"
            :loading="teacherSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="teacher in filteredTeachers"
              :key="teacher.value"
              :label="teacher.label"
              :value="teacher.value"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ teacher.label }}</span>
                <el-tag size="small" type="info">{{ teacher.major || '软件技术' }}</el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="assignForm.teacherName">
          <div class="selected-teacher-info">
            <el-icon><UserFilled /></el-icon>
            <span>已选择：<strong>{{ assignForm.teacherName }}</strong></span>
            <el-button type="primary" link size="small" @click="clearTeacher">清除</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit">确定分配</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importRelationDialogVisible" title="分配关系批量导入" width="600px" destroy-on-close class="import-relation-dialog">
      <div class="upload-container">
        <el-upload
          ref="uploadRef"
          class="upload-dragger"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xls,.xlsx"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :on-exceed="handleExceed"
          :file-list="importFileList"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或 <em>点击上传</em>
          </div>
        </el-upload>

        <div class="upload-actions">
          <el-button type="primary" plain @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载模板 ↓
          </el-button>
          <span class="upload-tip">提示：仅允许导入 "xls" 或 "xlsx" 格式文件！</span>
        </div>

        <div v-if="selectedFile" class="file-info">
          <el-icon><Document /></el-icon>
          <span>已选择文件：<strong>{{ selectedFile.name }}</strong></span>
          <span class="file-size">({{ formatFileSize(selectedFile.size) }})</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="importRelationDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleImportSubmitConfirm" :disabled="!selectedFile">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Search, Refresh, Plus, UserFilled, Upload, Download, Delete, UploadFilled, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'

const route = useRoute()

const loading = ref(false)
const tableRef = ref(null)
const studentTableRef = ref(null)
const assignFormRef = ref(null)
const importDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const importRelationDialogVisible = ref(false)
const uploadRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  batchId: route.query?.batchId || '',
  teacherName: ''
})

const selectedRows = ref([])
const selectedClass = ref('')
const assignedCount = computed(() => tableData.value.length)
const studentLoading = ref(false)
const studentCurrentPage = ref(1)
const studentPageSize = ref(10)
const selectedStudents = ref([])
const importFileList = ref([])
const selectedFile = ref(null)

const allAvailableStudents = [
  { name: '邹成龙', studentId: '1831613446', majorName: '软件技术', className: '软件182' },
  { name: '黎优', studentId: '1831613319', majorName: '软件技术', className: '软件182' },
  { name: '杨海英', studentId: '1831613206', majorName: '软件技术', className: '软件182' },
  { name: '杜宇阳', studentId: '1831613412', majorName: '软件技术', className: '软件182' },
  { name: '刘黎', studentId: '1831613123', majorName: '软件技术', className: '软件182' },
  { name: '李佳雨', studentId: '1831613203', majorName: '软件技术', className: '软件182' },
  { name: '樊海涛', studentId: '1831613113', majorName: '软件技术', className: '软件182' },
  { name: '翁秀洞', studentId: '1631613132', majorName: '软件技术', className: '软件182' },
  { name: '潘朝阳', studentId: '1831613088', majorName: '软件技术', className: '软件182' },
  { name: '朱颖', studentId: '1831613108', majorName: '软件技术', className: '软件184' },
  { name: '王云凡', studentId: '1631613426', majorName: '软件技术', className: '软件184' },
  { name: '李志明', studentId: '1831613122', majorName: '软件技术', className: '软件184' },
  { name: '张小龙', studentId: '1831613144', majorName: '软件技术', className: '软件184' }
]

const availableStudents = ref([...allAvailableStudents])

const paginatedStudents = computed(() => {
  const start = (studentCurrentPage.value - 1) * studentPageSize.value
  const end = start + studentPageSize.value
  return availableStudents.value.slice(start, end)
})

const tableData = ref([
  { id: 1, studentName: '朱颖', studentId: '1831613108', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { id: 2, studentName: '王云凡', studentId: '1631613426', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { id: 3, studentName: '李志明', studentId: '1831613122', majorName: '软件技术', className: '软件184', teacherName: '王海洋' },
  { id: 4, studentName: '张小龙', studentId: '1831613144', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { id: 5, studentName: '陈李海', studentId: '1831613409', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { id: 6, studentName: '刘淇', studentId: '1831613423', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { id: 7, studentName: '王勋', studentId: '1831613433', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { id: 8, studentName: '何金龙', studentId: '1831613114', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { id: 9, studentName: '廖双', studentId: '1831613222', majorName: '软件技术', className: '软件184', teacherName: '廖清科' }
])

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (searchForm.batchId && !item.batchId) return true
    if (searchForm.teacherName && !item.teacherName?.includes(searchForm.teacherName)) return false
    return true
  })
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredData.value.slice(start, end)
})

const assignForm = reactive({
  teacherName: ''
})

const assignRules = {
  teacherName: [{ required: true, message: '请选择指导教师', trigger: 'change' }]
}

const allTeachers = [
  { label: '廖清科', value: '廖清科', major: '软件技术', phone: '138****1234', title: '副教授' },
  { label: '王海洋', value: '王海洋', major: '软件技术', phone: '139****5678', title: '讲师' },
  { label: '张三', value: '张三', major: '计算机科学', phone: '137****9012', title: '教授' },
  { label: '李四', value: '李四', major: '信息安全', phone: '136****3456', title: '副教授' },
  { label: '赵五', value: '赵五', major: '大数据技术', phone: '135****7890', title: '讲师' },
  { label: '孙六', value: '孙六', major: '软件工程', phone: '134****2345', title: '助教' },
  { label: '周七', value: '周七', major: '人工智能', phone: '133****6789', title: '教授' },
  { label: '吴八', value: '吴八', major: '网络工程', phone: '132****0123', title: '副教授' }
]

const filteredTeachers = ref([...allTeachers])
const teacherSearchLoading = ref(false)

function searchTeachers(query) {
  if (query !== '') {
    teacherSearchLoading.value = true
    setTimeout(() => {
      filteredTeachers.value = allTeachers.filter(teacher => {
        return (
          teacher.label.toLowerCase().includes(query.toLowerCase()) ||
          teacher.major.includes(query) ||
          teacher.title.includes(query)
        )
      })
      teacherSearchLoading.value = false
    }, 200)
  } else {
    filteredTeachers.value = [...allTeachers]
  }
}

function clearTeacher() {
  assignForm.teacherName = ''
}

function handleSearch() {
  loading.value = true
  currentPage.value = 1
  setTimeout(() => {
    loading.value = false
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条数据`)
  }, 300)
}

function handleReset() {
  searchForm.batchId = ''
  searchForm.teacherName = ''
  if (route.query?.batchId) searchForm.batchId = route.query.batchId
  currentPage.value = 1
  pageSize.value = 10
  ElMessage.info('已重置搜索条件')
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function handleImportStudent() {
  importDialogVisible.value = true
  selectedClass.value = ''
  selectedStudents.value = []
  studentCurrentPage.value = 1

  studentLoading.value = true
  setTimeout(() => {
    const existingStudentIds = tableData.value.map(s => s.studentId)
    availableStudents.value = allAvailableStudents.filter(s => !existingStudentIds.includes(s.studentId))
    studentLoading.value = false
  }, 300)
}

function handleAssignTeacher() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要分配的学生')
    return
  }
  assignDialogVisible.value = true
  assignForm.teacherName = ''
}

function handleImportRelation() {
  importRelationDialogVisible.value = true
  selectedFile.value = null
  importFileList.value = []
}

function handleFileChange(file, fileList) {
  const allowedTypes = [
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  ]
  const fileName = file.name.toLowerCase()
  const isExcel = fileName.endsWith('.xls') || fileName.endsWith('.xlsx')

  if (!isExcel) {
    ElMessage.error('仅支持上传 .xls 或 .xlsx 格式的文件！')
    importFileList.value = []
    selectedFile.value = null
    return
  }

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB！')
    importFileList.value = []
    selectedFile.value = null
    return
  }

  importFileList.value = [file]
  selectedFile.value = file
}

function handleFileRemove(file, fileList) {
  importFileList.value = []
  selectedFile.value = null
}

function handleExceed(files, fileList) {
  ElMessage.warning('只能选择1个文件，请先移除已选文件')
}

function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

function downloadTemplate() {
  const headers = ['姓名', '学号', '专业', '班级', '指导教师']
  const templateData = [
    { '姓名': '张三', '学号': '20240001', '专业': '软件技术', '班级': '软件241', '指导教师': '廖清科' },
    { '姓名': '李四', '学号': '20240002', '专业': '软件技术', '班级': '软件241', '指导教师': '王海洋' },
    { '姓名': '（示例）', '学号': '（示例）', '专业': '（示例）', '班级': '（示例）', '指导教师': '（示例）' }
  ]

  const ws = XLSX.utils.json_to_sheet(templateData)
  ws['!cols'] = [
    { wch: 12 },
    { wch: 16 },
    { wch: 14 },
    { wch: 12 },
    { wch: 14 }
  ]

  XLSX.utils.sheet_add_aoa(ws, [headers], { origin: 'A1' })

  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '导入模板')

  const fileName = `师生关系导入模板_${new Date().toISOString().slice(0, 10)}.xlsx`
  XLSX.writeFile(wb, fileName)

  ElMessage.success('模板下载成功！请按格式填写数据后上传')
}

function handleImportSubmitConfirm() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  ElMessageBox.confirm(
    `确定要导入文件 "${selectedFile.value.name}" 吗？<br/><br/>
     <small style="color: #909399;">注意：导入后将根据学号匹配学生，并更新指导教师信息</small>`,
    '确认导入',
    {
      confirmButtonText: '确认导入',
      cancelButtonText: '取消',
      type: 'info',
      dangerouslyUseHTMLString: true
    }
  ).then(() => {
    loading.value = true

    setTimeout(() => {
      let successCount = 0
      let updateCount = 0
      let failCount = 0

      const mockImportData = [
        { studentName: '测试学生1', studentId: '20240001', majorName: '软件技术', className: '软件241', teacherName: '廖清科' },
        { studentName: '测试学生2', studentId: '20240002', majorName: '软件技术', className: '软件241', teacherName: '王海洋' },
        { studentName: '测试学生3', studentId: '20240003', majorName: '计算机科学', className: '计科241', teacherName: '张三' }
      ]

      mockImportData.forEach(item => {
        const existingIndex = tableData.value.findIndex(s => s.studentId === item.studentId)

        if (existingIndex > -1) {
          tableData.value[existingIndex].teacherName = item.teacherName
          updateCount++
        } else {
          const newId = Math.max(...tableData.value.map(s => s.id), 0) + 1
          tableData.value.push({
            id: newId,
            studentName: item.studentName,
            studentId: item.studentId,
            majorName: item.majorName,
            className: item.className,
            teacherName: item.teacherName
          })
          successCount++
        }
      })

      loading.value = false
      importRelationDialogVisible.value = false
      selectedFile.value = null
      importFileList.value = []

      let message = `✅ 导入完成！<br/>`
      if (successCount > 0) message += `• 新增 ${successCount} 条数据<br/>`
      if (updateCount > 0) message += `• 更新 ${updateCount} 条数据<br/>`
      if (failCount > 0) message += `• 失败 ${failCount} 条数据<br/>`
      message += `当前共 ${tableData.value.length} 条记录`

      ElMessage({
        type: 'success',
        dangerouslyUseHTMLString: true,
        message: message,
        duration: 5000
      })
    }, 1500)
  }).catch(() => {})
}

function handleExportRelation() {
  if (filteredData.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  const headers = ['姓名', '学号', '专业', '班级', '指导教师']
  const data = filteredData.value.map(row => ({
    '姓名': row.studentName,
    '学号': row.studentId,
    '专业': row.majorName,
    '班级': row.className,
    '指导教师': row.teacherName || '未分配'
  }))

  const ws = XLSX.utils.json_to_sheet(data)
  ws['!cols'] = [
    { wch: 12 },
    { wch: 16 },
    { wch: 14 },
    { wch: 12 },
    { wch: 14 }
  ]

  XLSX.utils.sheet_add_aoa(ws, [headers], { origin: 'A1' })

  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '师生关系')

  const fileName = `师生关系数据_${new Date().toISOString().slice(0, 10)}.xlsx`
  XLSX.writeFile(wb, fileName)

  ElMessage.success(`成功导出 ${filteredData.value.length} 条师生关系数据`)
}

function handleDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的数据')
    return
  }
  ElMessageBox.confirm(
    `确定要移除选中的 ${selectedRows.value.length} 名学生吗？`,
    '确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const ids = selectedRows.value.map(row => row.id)
    tableData.value = tableData.value.filter(item => !ids.includes(item.id))

    const totalPages = Math.ceil(filteredData.value.length / pageSize.value)
    if (currentPage.value > totalPages && totalPages > 0) {
      currentPage.value = totalPages
    }

    ElMessage.success(`成功移除 ${ids.length} 名学生`)
    selectedRows.value = []
  }).catch(() => {})
}

function handleRefresh() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功，数据已更新')
  }, 500)
}

function handleRemove(row) {
  ElMessageBox.confirm(
    `确定要移除学生 ${row.studentName} (${row.studentId}) 吗？`,
    '确认',
    {
      confirmButtonText: '确定移除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value.splice(index, 1)
      availableStudents.value.push({
        name: row.studentName,
        studentId: row.studentId,
        majorName: row.majorName,
        className: row.className
      })

      const totalPages = Math.ceil(filteredData.value.length / pageSize.value)
      if (currentPage.value > totalPages && totalPages > 0) {
        currentPage.value = totalPages
      }

      ElMessage.success(`已移除学生：${row.studentName}`)
    }
  }).catch(() => {})
}

function handleStudentSelectionChange(rows) {
  selectedStudents.value = rows
}

function handleClassChange(val) {
  studentLoading.value = true
  studentCurrentPage.value = 1

  setTimeout(() => {
    let filtered = [...allAvailableStudents]

    if (val) {
      filtered = allAvailableStudents.filter(s => s.className === val)
    }

    const existingStudentIds = tableData.value.map(s => s.studentId)
    availableStudents.value = filtered.filter(s => !existingStudentIds.includes(s.studentId))

    studentLoading.value = false

    if (val) {
      const totalCount = allAvailableStudents.filter(s => s.className === val).length
      const alreadyIncluded = totalCount - availableStudents.value.length
      if (alreadyIncluded > 0) {
        ElMessage.info(`班级 ${val} 共 ${totalCount} 人，已纳入 ${alreadyIncluded} 人，可选 ${availableStudents.value.length} 人`)
      } else {
        ElMessage.success(`班级 ${val} 共 ${availableStudents.value.length} 人可纳入`)
      }
    } else {
      const totalAll = allAvailableStudents.length
      const alreadyIncludedAll = totalAll - availableStudents.value.length
      ElMessage.info(`全部班级共 ${totalAll} 人，已纳入 ${alreadyIncludedAll} 人，可选 ${availableStudents.value.length} 人`)
    }

    selectedStudents.value = []
  }, 300)
}

function handleStudentSizeChange(val) {
  studentPageSize.value = val
  studentCurrentPage.value = 1
}

function handleStudentPageChange(val) {
  studentCurrentPage.value = val
}

function handleImportSubmit() {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请至少选择一位学生')
    return
  }

  const existingStudentIds = tableData.value.map(s => s.studentId)
  const duplicateStudents = selectedStudents.value.filter(s => existingStudentIds.includes(s.studentId))
  const newStudents = selectedStudents.value.filter(s => !existingStudentIds.includes(s.studentId))

  if (duplicateStudents.length > 0) {
    ElMessageBox.confirm(
      `检测到 ${duplicateStudents.length} 名学生已被纳入，是否只纳入剩余 ${newStudents.length} 名新学生？`,
      '重复检测提示',
      {
        confirmButtonText: '确认纳入',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      addStudentsToTable(newStudents)
    }).catch(() => {})
  } else {
    addStudentsToTable(selectedStudents.value)
  }
}

function addStudentsToTable(students) {
  let newId = Math.max(...tableData.value.map(s => s.id), 0) + 1
  let successCount = 0

  students.forEach(student => {
    tableData.value.push({
      id: newId++,
      studentName: student.name,
      studentId: student.studentId,
      majorName: student.majorName,
      className: student.className,
      teacherName: null
    })
    successCount++
  })

  importDialogVisible.value = false

  const totalPages = Math.ceil(filteredData.value.length / pageSize.value)
  currentPage.value = totalPages

  if (successCount > 0) {
    ElMessage.success(`✅ 成功纳入 ${successCount} 位学生！当前批次共 ${tableData.value.length} 人`)
  }

  selectedStudents.value = []
  selectedClass.value = ''
}

function handleAssignSubmit() {
  assignFormRef.value?.validate((valid) => {
    if (valid) {
      selectedRows.value.forEach(student => {
        const item = tableData.value.find(s => s.id === student.id)
        if (item) {
          item.teacherName = assignForm.teacherName
        }
      })

      ElMessage.success(
        `成功为 ${selectedRows.value.length} 名学生分配指导教师：${assignForm.teacherName}`
      )
      assignDialogVisible.value = false
      assignForm.teacherName = ''
    }
  })
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
.relation-container {
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
  flex-wrap: wrap;
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

.import-dialog .dialog-header-info {
  text-align: center;
  margin-bottom: 16px;
}

.import-dialog .header-tip {
  color: #67c23a;
  font-size: 15px;
  font-weight: 500;
}

.class-filter {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.student-table-wrapper {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 0;
}

.empty-tip {
  padding: 40px 20px;
  text-align: center;
}

.table-footer {
  padding: 12px 16px;
  background-color: #fafafa;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-tip {
  color: #67c23a;
  font-size: 13px;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.selected-teacher-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  color: #67c23a;
}

.selected-teacher-info .el-icon {
  font-size: 18px;
}

.import-relation-dialog .upload-container {
  padding: 10px 0;
}

.import-relation-dialog .upload-dragger {
  width: 100%;
}

.import-relation-dialog .upload-dragger :deep(.el-upload) {
  width: 100%;
}

.import-relation-dialog .upload-dragger :deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  background-color: #fafafa;
  transition: all 0.3s ease;
}

.import-relation-dialog .upload-dragger :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.import-relation-dialog .upload-dragger :deep(.el-icon--upload) {
  font-size: 60px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.import-relation-dialog .upload-dragger :deep(.el-upload__text) {
  font-size: 14px;
  color: #606266;
}

.import-relation-dialog .upload-dragger :deep(.el-upload__text em) {
  color: #409eff;
  font-style: normal;
  font-weight: 500;
}

.import-relation-dialog .upload-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
  padding: 16px;
  background-color: #fff9e6;
  border: 1px solid #ffe58f;
  border-radius: 4px;
}

.import-relation-dialog .upload-tip {
  color: #fa8c16;
  font-size: 13px;
  font-weight: 500;
}

.import-relation-dialog .file-info {
  margin-top: 16px;
  padding: 12px 16px;
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #67c23a;
}

.import-relation-dialog .file-info .el-icon {
  font-size: 18px;
}

.import-relation-dialog .file-size {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
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
