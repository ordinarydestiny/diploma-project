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
        :data="tableData"
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
              &nbsp;移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
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
          :data="studentList"
          border
          stripe
          v-loading="studentLoading"
          @selection-change="handleStudentSelectionChange"
          max-height="400"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="name" label="姓名" width="120" align="center" />
          <el-table-column prop="studentId" label="学号" width="160" align="center" />
          <el-table-column prop="majorName" label="专业" min-width="140" align="center" />
          <el-table-column prop="className" label="班级" width="140" align="center" />
        </el-table>

        <div class="table-footer">
          <div class="footer-tip">默认为10条数据，全选某班 请自行调整</div>
          <el-pagination
            v-model:current-page="studentCurrentPage"
            v-model:page-size="studentPageSize"
            :total="studentTotal"
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
          <el-button type="primary" @click="handleImportSubmit" :disabled="selectedStudents.length === 0">纳入</el-button>
          <el-button @click="importDialogVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="分配指导教师" width="600px" destroy-on-close>
      <el-alert v-if="selectedRows.length > 0" :title="`已选择 ${selectedRows.length} 名学生`" type="info" :closable="false" show-icon style="margin-bottom: 16px" />
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item label="指导教师" prop="teacherName">
          <el-select v-model="assignForm.teacherName" placeholder="请选择指导教师" filterable style="width: 100%">
            <el-option label="廖清科" value="廖清科" />
            <el-option label="王海洋" value="王海洋" />
            <el-option label="张三" value="张三" />
            <el-option label="李四" value="李四" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit">确定分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { Search, Refresh, Plus, UserFilled, Upload, Download, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const loading = ref(false)
const tableRef = ref(null)
const assignFormRef = ref(null)
const importDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(9)

const searchForm = reactive({
  batchId: '',
  teacherName: ''
})

const selectedRows = ref([])
const fileList = ref([])

const studentTableRef = ref(null)
const selectedClass = ref('')
const assignedCount = ref(0)
const studentLoading = ref(false)
const studentCurrentPage = ref(1)
const studentPageSize = ref(10)
const studentTotal = ref(42)
const selectedStudents = ref([])

const studentList = ref([
  { name: '邹成龙', studentId: '1831613446', majorName: '软件技术', className: '软件182' },
  { name: '黎优', studentId: '1831613319', majorName: '软件技术', className: '软件182' },
  { name: '杨海英', studentId: '1831613206', majorName: '软件技术', className: '软件182' },
  { name: '杜宇阳', studentId: '1831613412', majorName: '软件技术', className: '软件182' },
  { name: '刘黎', studentId: '1831613123', majorName: '软件技术', className: '软件182' },
  { name: '李佳雨', studentId: '1831613203', majorName: '软件技术', className: '软件182' },
  { name: '樊海涛', studentId: '1831613113', majorName: '软件技术', className: '软件182' },
  { name: '翁秀洞', studentId: '1631613132', majorName: '软件技术', className: '软件182' },
  { name: '潘朝阳', studentId: '1831613088', majorName: '软件技术', className: '软件182' },
  { name: '朱颖', studentId: '1831613108', majorName: '软件技术', className: '软件184' }
])

const tableData = ref([
  { studentName: '朱颖', studentId: '1831613108', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { studentName: '王云凡', studentId: '1631613426', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { studentName: '李志明', studentId: '1831613122', majorName: '软件技术', className: '软件184', teacherName: '王海洋' },
  { studentName: '张小龙', studentId: '1831613144', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { studentName: '陈李海', studentId: '1831613409', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { studentName: '刘淇', studentId: '1831613423', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { studentName: '王勋', studentId: '1831613433', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { studentName: '何金龙', studentId: '1831613114', majorName: '软件技术', className: '软件184', teacherName: '廖清科' },
  { studentName: '廖双', studentId: '1831613222', majorName: '软件技术', className: '软件184', teacherName: '廖清科' }
])

const assignForm = reactive({
  teacherName: ''
})

const assignRules = {
  teacherName: [{ required: true, message: '请选择指导教师', trigger: 'change' }]
}

function handleSearch() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('搜索完成')
  }, 500)
}

function handleReset() {
  searchForm.batchId = ''
  searchForm.teacherName = ''
  ElMessage.info('已重置搜索条件')
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function handleImportStudent() {
  importDialogVisible.value = true
  selectedClass.value = ''
  selectedStudents.value = []
}

function handleAssignTeacher() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要分配的学生')
    return
  }
  assignDialogVisible.value = true
}

function handleImportRelation() {
  ElMessage.info('导入分配关系')
}

function handleExportRelation() {
  ElMessage.success('导出成功')
}

function handleDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的数据')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条数据吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleRefresh() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功')
  }, 500)
}

function handleRemove(row) {
  ElMessageBox.confirm(`确定要移除学生 ${row.studentName} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('移除成功')
  }).catch(() => {})
}

function handleStudentSelectionChange(rows) {
  selectedStudents.value = rows
}

function handleClassChange(val) {
  console.log('选择班级:', val)
  ElMessage.info(`已选择班级：${val}`)
}

function handleStudentSizeChange(val) {
  studentPageSize.value = val
}

function handleStudentPageChange(val) {
  studentCurrentPage.value = val
}

function handleFileChange(file) {
  fileList.value = [file]
}

function handleExceed() {
  ElMessage.warning('只能上传一个文件，请先删除已有文件')
}

function handleImportSubmit() {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请至少选择一位学生')
    return
  }
  importDialogVisible.value = false
  ElMessage.success(`成功纳入 ${selectedStudents.value.length} 位学生`)
  selectedStudents.value = []
  fileList.value = []
}

function handleAssignSubmit() {
  assignFormRef.value?.validate((valid) => {
    if (valid) {
      assignDialogVisible.value = false
      ElMessage.success(`成功为 ${selectedRows.value.length} 名学生分配指导教师：${assignForm.teacherName}`)
      assignForm.teacherName = ''
    }
  })
}

function handleSizeChange(val) {
  pageSize.value = val
  handleSearch()
}

function handleCurrentChange(val) {
  currentPage.value = val
  handleSearch()
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

.custom-table :deep(.el-table__header-wrapper .cell) {
  padding: 0;
  line-height: normal;
}

.custom-table :deep(.el-table__body-wrapper td) {
  border-right: none !important;
  border-bottom: 1px solid #ebeef5 !important;
  padding: 16px 0 !important;
}

.custom-table :deep(.el-table__body-wrapper .cell) {
  padding: 0;
  line-height: normal;
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
