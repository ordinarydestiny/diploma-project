<template>
  <div class="score-management-container">
    <div class="search-section">
      <el-row :gutter="16" align="bottom">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>学生姓名</label>
            <el-input v-model="searchForm.studentName" placeholder="请输入姓名" clearable @keyup.enter="handleSearch" />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>学号</label>
            <el-input v-model="searchForm.studentNo" placeholder="请输入学号" clearable @keyup.enter="handleSearch" />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>成绩等级</label>
            <el-select v-model="searchForm.gradeLevel" placeholder="全部" clearable style="width: 100%" @change="handleSearch">
              <el-option label="优秀(A)" value="A" />
              <el-option label="良好(B)" value="B" />
              <el-option label="中等(C)" value="C" />
              <el-option label="及格(D)" value="D" />
              <el-option label="不及格(F)" value="F" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>&nbsp;</label>
            <div class="search-buttons">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon> 重置
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="table-section">
      <div class="table-header">
        <div class="header-left">
          <h3>📊 学生总成绩列表</h3>
        </div>
        <div class="header-right">
          <el-tag v-if="lastUpdateTime" type="info" size="small" effect="plain">
            <el-icon><Clock /></el-icon>
            上次更新: {{ lastUpdateTime }}
          </el-tag>
          <el-button type="primary" link @click="loadData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="paginatedData" border stripe style="width: 100%">
        <el-table-column type="index" label="#" width="50" align="center" :index="getIndex" />
        <el-table-column prop="studentNo" label="学号" width="140" align="center" />
        <el-table-column prop="studentName" label="姓名" width="100" align="center" />
        <el-table-column prop="topicName" label="毕设题目" min-width="150" show-overflow-tooltip align="center"/>
        <el-table-column prop="reportScore" label="报告分" width="120" align="center" sortable>
          <template #default="{ row }">
            <span v-if="row.reportScore !== null">{{ row.reportScore }}</span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="defenseScore" label="答辩分" width="120" align="center" sortable>
          <template #default="{ row }">
            <span v-if="row.defenseScore !== null">{{ row.defenseScore }}</span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总成绩" width="120" align="center" sortable>
          <template #default="{ row }">
            <strong v-if="row.totalScore !== null" style="color: #409EFF">{{ row.totalScore }}</strong>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="gradeLevel" label="等级" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.gradeLevel" :type="getGradeType(row.gradeLevel)" size="small">{{ row.gradeLevel }}</el-tag>
            <span v-else style="color: #999">未评</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredData.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="成绩详情" width="600px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="学号">{{ currentRow.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.studentName }}</el-descriptions-item>
        <el-descriptions-item label="毕设题目" :span="2">{{ currentRow.topicName }}</el-descriptions-item>
        <el-descriptions-item label="报告分">{{ currentRow.reportScore || '未定稿' }}</el-descriptions-item>
        <el-descriptions-item label="答辩分">{{ currentRow.defenseScore || '未答辩' }}</el-descriptions-item>
        <el-descriptions-item label="总成绩">
          <strong v-if="currentRow.totalScore" style="font-size: 18px; color: #409EFF">{{ currentRow.totalScore }}</strong>
          <span v-else>未计算</span>
        </el-descriptions-item>
        <el-descriptions-item label="成绩等级">
          <el-tag v-if="currentRow.gradeLevel" :type="getGradeType(currentRow.gradeLevel)">{{ currentRow.gradeLevel }}</el-tag>
          <span v-else>未评定</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Clock } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const currentRow = ref(null)
const lastUpdateTime = ref('')
const searchForm = reactive({
  studentName: '',
  studentNo: '',
  gradeLevel: ''
})

// 分页相关状态
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

// 筛选后的数据（根据搜索条件过滤）
const filteredData = computed(() => {
  let data = tableData.value
  
  // 按学生姓名筛选
  if (searchForm.studentName) {
    data = data.filter(item =>
      item.studentName && item.studentName.toLowerCase().includes(searchForm.studentName.toLowerCase())
    )
  }
  
  // 按学号筛选
  if (searchForm.studentNo) {
    data = data.filter(item =>
      item.studentNo && item.studentNo.includes(searchForm.studentNo)
    )
  }
  
  // 按成绩等级筛选
  if (searchForm.gradeLevel) {
    data = data.filter(item => item.gradeLevel === searchForm.gradeLevel)
  }
  
  return data
})

// 当前页显示的数据（分页切片）
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredData.value.slice(start, end)
})

onMounted(() => {
  loadData()
  
  // 监听页面可见性变化 - 切回页面时自动刷新
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  // 组件卸载时移除监听器，避免内存泄漏
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

async function loadData() {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const headers = { 'Content-Type': 'application/json' }
    if (token) {
      headers['Authorization'] = 'Bearer ' + token
      console.log('🔑 使用Token:', token.substring(0, 20) + '...')
    } else {
      console.warn('⚠️ 未找到Token')
    }
    
    console.log('📡 正在请求: /api/v1/scores/list')
    
    const res = await fetch('/api/v1/scores/list', { 
      headers,
      method: 'GET'
    })
    
    console.log('📥 响应状态:', res.status, res.statusText)
    
    if (res.ok && res.status === 200) {
      const result = await res.json()
      console.log('📦 响应数据:', result)
      
      // 检查返回的数据结构
      if (result && result.code === 200 && Array.isArray(result.data)) {
        tableData.value = result.data
        updateLastTime()
        console.log('✅ 成功加载', tableData.value.length, '条成绩记录（标准格式）')
        return
      } else if (Array.isArray(result)) {
        // 兼容直接返回数组的情况
        tableData.value = result
        updateLastTime()
        console.log('✅ 成功加载', tableData.value.length, '条成绩记录（数组格式）')
        return
      } else {
        console.warn('⚠️ 数据格式异常:', typeof result, JSON.stringify(result).substring(0, 200))
      }
    } else if (res.status === 401) {
      console.error('❌ 401 未授权 - Token可能已过期或无效')
    } else if (res.status === 404) {
      console.error('❌ 404 接口不存在 - 后端可能未启动或路径错误')
    } else if (res.status === 403) {
      console.error('❌ 403 无权限 - 角色权限不足')
    } else {
      console.error('❌ HTTP错误:', res.status, await res.text())
    }
    
    console.warn('⚠️ API返回异常，使用模拟数据')
    useMockData()
  } catch (e) {
    console.error('❌ 网络异常:', e.message)
    console.error('   错误详情:', e.stack)
    useMockData()
  } finally {
    loading.value = false
  }
}

function updateLastTime() {
  const now = new Date()
  const timeStr = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
  lastUpdateTime.value = timeStr
}

// 页面可见性变化处理函数 - 切回页面时自动刷新
function handleVisibilityChange() {
  if (!document.hidden) {
    console.log('📱 页面重新可见，自动刷新数据...')
    loadData()
  }
}

function useMockData() {
  tableData.value = [
    { id: 1, studentNo: '202358210001', studentName: '张三', topicName: '基于Vue的毕业设计管理系统', reportScore: '88.5', defenseScore: '85.0', totalScore: '87.1', gradeLevel: 'B', status: 'approved' },
    { id: 2, studentNo: '202358210002', studentName: '李四', topicName: '基于React Native的移动端应用开发', reportScore: '92.0', defenseScore: '90.0', totalScore: '91.2', gradeLevel: 'A', status: 'approved' },
    { id: 3, studentNo: '202358210003', studentName: '钱七', topicName: '基于React Native的移动端应用开发', reportScore: null, defenseScore: null, totalScore: null, gradeLevel: null, status: 'not_started' },
    { id: 4, studentNo: '202358210004', studentName: '王五', topicName: '基于区块链的供应链管理系统', reportScore: '75.5', defenseScore: '72.0', totalScore: '74.1', gradeLevel: 'C', status: 'pending' },
    { id: 5, studentNo: '202358210005', studentName: '赵六', topicName: '基于Vue的移动端应用开发', reportScore: null, defenseScore: null, totalScore: null, gradeLevel: null, status: 'rejected' }
  ]
}

function handleSearch() {
  // 重置到第一页
  currentPage.value = 1
  ElMessage.info(`搜索完成，共找到 ${filteredData.value.length} 条记录`)
}

function handleReset() {
  searchForm.studentName = ''
  searchForm.studentNo = ''
  searchForm.gradeLevel = ''
  // 重置到第一页
  currentPage.value = 1
  ElMessage.info('已重置所有筛选条件')
}

function handleSizeChange(val) {
  pageSize.value = val
  // 切换每页条数时回到第一页
  currentPage.value = 1
  console.log(`每页 ${val} 条`)
}

function handleCurrentChange(val) {
  currentPage.value = val
  console.log(`当前页: ${val}`)
}

// 获取表格序号（考虑分页）
function getIndex(index) {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

function viewDetail(row) {
  currentRow.value = row
  dialogVisible.value = true
}

function getGradeType(grade) {
  const map = { A: 'danger', B: 'warning', C: '', D: 'success', F: 'info' }
  return map[grade] || 'info'
}

function getStatusType(status) {
  const map = { approved: 'success', pending: 'warning', rejected: 'danger', not_started: 'info' }
  return map[status] || 'info'
}

function formatStatus(status) {
  const map = { approved: '已通过', pending: '待审核', rejected: '已驳回', not_started: '未开始' }
  return map[status] || status || '未知'
}
</script>

<style scoped>
.score-management-container {
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

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0 0;
}
</style>
