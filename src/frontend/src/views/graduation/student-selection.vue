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
        <el-table-column prop="rejectReason" label="毕业设计题目驳回理由" min-width="140" align="center" show-overflow-tooltip />
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
          :total="filteredData.length"
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
        <el-descriptions-item v-if="currentRecord?.rejectReason" label="驳回理由" :span="2">
          <div style="color: #f56c6c; white-space: pre-wrap; line-height: 1.6;">{{ currentRecord?.rejectReason }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Search, Refresh, RefreshRight, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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

const tableData = ref([
  {
    id: 1746,
    major: '云计算技术',
    className: '云计算181',
    studentName: '陈俊',
    studentId: '183160710',
    phone: '185966603647',
    teacherName: '李太平',
    topicName: '校园云桌面',
    topicDescription: '部雾方云设计与实现',
    status: '已通过',
    rejectReason: ''
  },
  {
    id: 1747,
    major: '软件技术',
    className: '软件184',
    studentName: '朱颖',
    studentId: '1831613108',
    phone: '13800138001',
    teacherName: '廖清科',
    topicName: '无线宏站勘察系统设计与实现',
    topicDescription: '基于移动通信网络优化需求，设计并开发一套无线宏站勘察系统...',
    status: '待审核',
    rejectReason: ''
  },
  {
    id: 1748,
    major: '计算机科学',
    className: '计科182',
    studentName: '王云凡',
    studentId: '1631613426',
    phone: '13900139002',
    teacherName: '王海洋',
    topicName: '电商平台后台管理系统',
    topicDescription: '设计并实现一个功能完善的电商平台后台管理系统...',
    status: '已驳回',
    rejectReason: '题目描述过于简单，缺乏创新性。建议增加更多功能模块或采用更先进的技术方案。'
  },
  {
    id: 1749,
    major: '大数据技术',
    className: '大数据181',
    studentName: '李明',
    studentId: '1731615200',
    phone: '13700137003',
    teacherName: '张三',
    topicName: '数据可视化分析平台',
    topicDescription: '使用ECharts和Python构建多维度数据可视化分析平台...',
    status: '已通过',
    rejectReason: ''
  },
  {
    id: 1750,
    major: '人工智能',
    className: '智能181',
    studentName: '赵敏',
    studentId: '1831617105',
    phone: '13600136004',
    teacherName: '李四',
    topicName: '智能客服聊天机器人',
    topicDescription: '基于NLP技术构建支持多轮对话的智能客服系统...',
    status: '待审核',
    rejectReason: ''
  },
  {
    id: 1751,
    major: '信息安全',
    className: '安全181',
    studentName: '孙强',
    studentId: '1731618200',
    phone: '13500135005',
    teacherName: '王五',
    topicName: '区块链供应链溯源系统',
    topicDescription: '利用区块链不可篡改特性设计产品溯源系统...',
    status: '未提交',
    rejectReason: ''
  },
  {
    id: 1752,
    major: '软件技术',
    className: '软件182',
    studentName: '周芳',
    studentId: '1831621100',
    phone: '13400134006',
    teacherName: '赵六',
    topicName: '在线教育平台开发',
    topicDescription: '开发支持视频课程、作业提交、学习进度跟踪的在线教育平台...',
    status: '已通过',
    rejectReason: ''
  },
  {
    id: 1753,
    major: '计算机科学',
    className: '计科181',
    studentName: '吴磊',
    studentId: '1631613410',
    phone: '13300133007',
    teacherName: '钱七',
    topicName: '微服务架构实践',
    topicDescription: '采用Spring Cloud全家桶构建分布式微服务架构...',
    status: '已驳回',
    rejectReason: '技术栈描述不够详细，需要明确使用哪些具体组件和版本，以及如何解决服务间通信问题。'
  },
  {
    id: 1754,
    major: '软件技术',
    className: '软件183',
    studentName: '郑华',
    studentId: '1831622100',
    phone: '13200132008',
    teacherName: '孙八',
    topicName: '移动端健康App',
    topicDescription: '开发支持运动记录、饮食管理、健康数据分析的健康管理应用...',
    status: '待审核',
    rejectReason: ''
  },
  {
    id: 1755,
    major: '人工智能',
    className: '智能182',
    studentName: '冯丽',
    studentId: '1831617200',
    phone: '13100131009',
    teacherName: '周九',
    topicName: '图像识别系统',
    topicDescription: '利用卷积神经网络实现多场景图像识别分类系统...',
    status: '已通过',
    rejectReason: ''
  }
])

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
  setTimeout(() => {
    loading.value = false
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条记录`)
  }, 300)
}

function handleReset() {
  searchForm.topicName = ''
  searchForm.topicStatus = ''
  searchForm.major = ''
  searchForm.studentName = ''
  searchForm.teacherName = ''
  currentPage.value = 1
  pageSize.value = 10
  ElMessage.info('已重置搜索条件')
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function handleViewDetail(row) {
  currentRecord.value = row
  detailDialogVisible.value = true
}

function handleResetStatus(row) {
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
  ).then(() => {
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value[index].status = '待审核'
      tableData.value[index].rejectReason = ''

      if (currentRecord.value && currentRecord.value.id === row.id) {
        currentRecord.value = { ...tableData.value[index] }
      }

      ElMessage.success(`✅ 已成功重置学生 ${row.studentName} 的选题状态为"待审核"`)
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
