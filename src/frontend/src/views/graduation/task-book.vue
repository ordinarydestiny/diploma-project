<template>
  <div class="taskbook-container">
    <div class="search-section">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>学期</label>
            <el-select v-model="searchForm.semester" placeholder="请选择学期" clearable style="width: 100%">
              <el-option label="2021-2022学年第二学期(当)" value="2021-2022-2" />
              <el-option label="2022-2023学年第一学期" value="2022-2023-1" />
              <el-option label="2022-2023学年第二学期" value="2022-2023-2" />
              <el-option label="2023-2024学年第一学期" value="2023-2024-1" />
              <el-option label="2023-2024学年第二学期" value="2023-2024-2" />
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
          <div class="search-item"></div>
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
          <div class="search-item">
            <label>状态</label>
            <el-select v-model="searchForm.status" placeholder="请选择毕业设计任务书状态" clearable style="width: 100%">
              <el-option label="未提交" value="未提交" />
              <el-option label="待审核" value="待审核" />
              <el-option label="已通过" value="已通过" />
              <el-option label="已驳回" value="已驳回" />
              <el-option label="已下达" value="已下达" />
            </el-select>
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
          :total="filteredData.length"
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
import { ref, reactive, computed } from 'vue'
import { Search, Refresh, Download, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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

const tableData = ref([
  {
    id: 2288,
    studentName: '余**',
    teacherName: '郑**',
    topicName: '基于Vue.js的在线教育平台设计与实现',
    topicStatus: '未提交',
    deliverTime: null,
    taskbookStatus: '未下达',
    taskContent: ''
  },
  {
    id: 2289,
    studentName: '朱**',
    teacherName: '廖清科',
    topicName: '无线宏站勘察系统设计与实现',
    topicStatus: '已通过',
    deliverTime: null,
    taskbookStatus: '未下达',
    taskContent: ''
  },
  {
    id: 2290,
    studentName: '王云凡',
    teacherName: '王海洋',
    topicName: '电商平台后台管理系统',
    topicStatus: '已通过',
    deliverTime: '2024-03-15 10:30:00',
    taskbookStatus: '已下达',
    taskContent: `一、课题背景与意义
本课题旨在设计并实现一个功能完善的电商平台后台管理系统...

二、研究目标与主要内容
1. 完成需求分析与系统设计
2. 实现商品管理、订单管理、用户管理等核心模块
3. 采用前后端分离架构，使用Vue.js + Spring Boot

三、进度安排
第1-2周：需求调研与分析
第3-4周：系统架构设计
第5-8周：核心功能开发
第9-10周：测试与优化`
  },
  {
    id: 2291,
    studentName: '李明',
    teacherName: '张三',
    topicName: '数据可视化分析平台',
    topicStatus: '已通过',
    deliverTime: '2024-03-16 14:20:00',
    taskbookStatus: '已下达',
    taskContent: `一、课题简介
构建一个多维度数据可视化分析平台...

二、技术栈
前端：ECharts、D3.js
后端：Python Flask、Pandas

三、主要功能
数据采集、清洗、分析、可视化展示...`
  },
  {
    id: 2292,
    studentName: '赵敏',
    teacherName: '李四',
    topicName: '智能客服聊天机器人',
    topicStatus: '待审核',
    deliverTime: null,
    taskbookStatus: '未下达',
    taskContent: ''
  },
  {
    id: 2293,
    studentName: '孙强',
    teacherName: '王五',
    topicName: '区块链供应链溯源系统',
    topicStatus: '已驳回',
    deliverTime: null,
    taskbookStatus: '未下达',
    taskContent: ''
  },
  {
    id: 2294,
    studentName: '周芳',
    teacherName: '赵六',
    topicName: '在线教育平台开发',
    topicStatus: '已通过',
    deliverTime: '2024-03-17 09:15:00',
    taskbookStatus: '待审核',
    taskContent: `一、项目概述
开发支持视频课程播放的在线教育平台...

二、功能模块
1. 用户管理（学生/教师/管理员）
2. 课程管理（上传、分类、搜索）
3. 学习进度跟踪
4. 在线作业与考试`
  },
  {
    id: 2295,
    studentName: '吴磊',
    teacherName: '钱七',
    topicName: '微服务架构实践',
    topicStatus: '已驳回',
    deliverTime: null,
    taskbookStatus: '未下达',
    taskContent: ''
  },
  {
    id: 2296,
    studentName: '郑华',
    teacherName: '孙八',
    topicName: '移动端健康App',
    topicStatus: '待审核',
    deliverTime: null,
    taskbookStatus: '未下达',
    taskContent: ''
  },
  {
    id: 2297,
    studentName: '冯丽',
    teacherName: '周九',
    topicName: '图像识别系统',
    topicStatus: '已通过',
    deliverTime: '2024-03-18 11:45:00',
    taskbookStatus: '已驳回',
    taskContent: `一、研究内容
利用卷积神经网络实现多场景图像识别...

二、技术方案
模型选择：ResNet-50
框架：PyTorch
数据集：ImageNet预训练 + 自定义数据集

三、预期成果
完成图像识别准确率达到85%以上...`
  }
])

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
    '未下达': 'info',
    '待审核': 'warning',
    '已通过': 'success',
    '已驳回': 'danger',
    '已下达': ''
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
  searchForm.semester = ''
  searchForm.topicName = ''
  searchForm.studentName = ''
  searchForm.teacherName = ''
  searchForm.status = ''
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

function handleDeliverTaskbook(row) {
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
  ).then(() => {
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value[index].taskbookStatus = '已下达'
      tableData.value[index].deliverTime = new Date().toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).replace(/\//g, '-')

      if (!tableData.value[index].taskContent) {
        tableData.value[index].taskContent = `一、课题背景与意义
本课题"${row.topicName}"的研究具有重要的理论意义和实际应用价值...

二、研究目标与主要内容
1. 完成${row.topicName}系统的需求分析与总体设计
2. 实现系统的核心功能模块
3. 完成系统测试与优化

三、进度安排
第1-2周：文献调研与需求分析
第3-4周：系统设计与技术选型
第5-8周：核心功能实现
第9-10周：系统集成与测试
第11-12周：论文撰写与答辩准备

四、参考文献
[1] 相关领域的经典文献...
[2] 最新研究成果...`
      }

      if (currentRecord.value && currentRecord.value.id === row.id) {
        currentRecord.value = { ...tableData.value[index] }
      }

      ElMessage.success(`✅ 已成功为学生 ${row.studentName} 下达任务书！`)
    }
  }).catch(() => {})
}

function handleResetTaskbookStatus(row) {
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
  ).then(() => {
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value[index].taskbookStatus = '未下达'
      tableData.value[index].deliverTime = null
      tableData.value[index].taskContent = ''

      if (currentRecord.value && currentRecord.value.id === row.id) {
        currentRecord.value = { ...tableData.value[index] }
      }

      ElMessage.success(`✅ 已成功重置学生 ${row.studentName} 的任务书状态`)
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
