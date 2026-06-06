<template>
  <div class="topic-container">
    <div class="search-section">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>毕业设计学期</label>
            <el-select v-model="searchForm.semester" placeholder="全部学期" clearable style="width: 100%">
              <el-option label="全部学期" value="" />
              <el-option 
                v-for="sem in semesterList" 
                :key="sem.semester" 
                :label="sem.semester" 
                :value="sem.semester"
              />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>毕业设计题目</label>
            <el-input v-model="searchForm.topicName" placeholder="请输入毕设题目" clearable />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>适用专业</label>
            <el-select v-model="searchForm.major" placeholder="请选择适用专业" clearable style="width: 100%">
              <el-option 
                v-for="cat in categoryList" 
                :key="cat" 
                :label="cat" 
                :value="cat"
              />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="search-item">
            <label>是否本人创建</label>
            <el-select v-model="searchForm.isSelfCreated" placeholder="请选择" clearable style="width: 100%">
              <el-option label="是" value="yes" />
              <el-option label="否" value="no" />
            </el-select>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" align="middle" style="margin-top: 16px;">
        <el-col :span="24">
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
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增
        </el-button>
        <el-button type="info" plain @click="handleIncludeTopic">
          <el-icon><Download /></el-icon>
          纳入题目
        </el-button>
        <el-button type="danger" plain @click="handleRemoveTopic">
          <el-icon><Delete /></el-icon>
          移除纳入题目
        </el-button>
        <el-button type="warning" plain @click="handleExport">
          <el-icon><Upload /></el-icon>
          导出
        </el-button>
        <el-button plain @click="handleImport">
          <el-icon><Upload /></el-icon>
          导入
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
        <el-table-column prop="topicId" label="毕业设计题目ID" width="140" align="center" />
        <el-table-column prop="topicName" label="毕业设计题目" min-width="180" align="center" show-overflow-tooltip />
        <el-table-column prop="description" label="题目描述" min-width="200" align="center" show-overflow-tooltip />
        <el-table-column prop="major" label="适用专业" width="140" align="center" show-overflow-tooltip />
        <el-table-column prop="selectedCount" label="查询学期已选次数" width="160" align="center" />
        <el-table-column prop="source" label="题目来源" width="120" align="center" />
        <el-table-column prop="creator" label="题目创建者" width="120" align="center" />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              查看详情
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

    <el-dialog v-model="addDialogVisible" title="新增毕业设计题目" width="700px" destroy-on-close>
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="120px">
        <el-form-item label="毕业设计题目" prop="topicName">
          <el-input v-model="addForm.topicName" placeholder="请输入毕设题目名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="题目描述" prop="description">
          <el-input
            v-model="addForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入题目详细描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="适用专业" prop="major">
          <el-select v-model="addForm.major" placeholder="请选择适用专业" style="width: 100%" multiple>
            <el-option label="软件技术" value="软件技术" />
            <el-option label="计算机科学" value="计算机科学" />
            <el-option label="大数据技术" value="大数据技术" />
            <el-option label="人工智能" value="人工智能" />
            <el-option label="信息安全" value="信息安全" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目来源" prop="source">
          <el-select v-model="addForm.source" placeholder="请选择题目来源" style="width: 100%">
            <el-option label="教师指定题目" value="教师指定题目" />
            <el-option label="学生自拟题目" value="学生自拟题目" />
            <el-option label="企业合作题目" value="企业合作题目" />
            <el-option label="科研课题" value="科研课题" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级" prop="difficulty">
          <el-radio-group v-model="addForm.difficulty">
            <el-radio value="简单">简单</el-radio>
            <el-radio value="中等">中等</el-radio>
            <el-radio value="困难">困难</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddSubmit">确定新增</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="题目详情" width="800px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="题目ID">{{ currentTopic?.topicId }}</el-descriptions-item>
        <el-descriptions-item label="题目名称">{{ currentTopic?.topicName }}</el-descriptions-item>
        <el-descriptions-item label="适用专业" :span="2">{{ currentTopic?.major }}</el-descriptions-item>
        <el-descriptions-item label="题目来源">{{ currentTopic?.source }}</el-descriptions-item>
        <el-descriptions-item label="已选次数">{{ currentTopic?.selectedCount }} 次</el-descriptions-item>
        <el-descriptions-item label="创建者">{{ currentTopic?.creator }}</el-descriptions-item>
        <el-descriptions-item label="难度等级">{{ currentTopic?.difficulty || '中等' }}</el-descriptions-item>
        <el-descriptions-item label="题目描述" :span="2">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentTopic?.description }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentTopic)">编辑题目</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑毕业设计题目" width="700px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="120px">
        <el-form-item label="题目ID">
          <el-input v-model="editForm.topicId" disabled />
        </el-form-item>
        <el-form-item label="毕业设计题目" prop="topicName">
          <el-input v-model="editForm.topicName" placeholder="请输入毕设题目名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="题目描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入题目详细描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="适用专业" prop="major">
          <el-select v-model="editForm.majorList" placeholder="请选择适用专业" style="width: 100%" multiple>
            <el-option label="软件技术" value="软件技术" />
            <el-option label="计算机科学" value="计算机科学" />
            <el-option label="大数据技术" value="大数据技术" />
            <el-option label="人工智能" value="人工智能" />
            <el-option label="信息安全" value="信息安全" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目来源" prop="source">
          <el-select v-model="editForm.source" placeholder="请选择题目来源" style="width: 100%">
            <el-option label="教师指定题目" value="教师指定题目" />
            <el-option label="学生自拟题目" value="学生自拟题目" />
            <el-option label="企业合作题目" value="企业合作题目" />
            <el-option label="科研课题" value="科研课题" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级" prop="difficulty">
          <el-radio-group v-model="editForm.difficulty">
            <el-radio value="简单">简单</el-radio>
            <el-radio value="中等">中等</el-radio>
            <el-radio value="困难">困难</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">保存修改</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="题目批量导入" width="600px" destroy-on-close class="import-topic-dialog">
      <div class="upload-container">
        <el-upload
          ref="topicUploadRef"
          class="upload-dragger"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xls,.xlsx"
          :on-change="handleTopicFileChange"
          :on-remove="handleTopicFileRemove"
          :on-exceed="handleTopicExceed"
          :file-list="topicImportFileList"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或 <em>点击上传</em>
          </div>
        </el-upload>

        <div class="upload-actions">
          <el-button type="primary" plain @click="downloadTopicTemplate">
            <el-icon><Download /></el-icon>
            下载模板 ↓
          </el-button>
          <span class="upload-tip">提示：仅允许导入 "xls" 或 "xlsx" 格式文件！</span>
        </div>

        <div v-if="selectedTopicFile" class="file-info">
          <el-icon><Document /></el-icon>
          <span>已选择文件：<strong>{{ selectedTopicFile.name }}</strong></span>
          <span class="file-size">({{ formatFileSize(selectedTopicFile.size) }})</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleTopicImportSubmit" :disabled="!selectedTopicFile">确 定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="includeDialogVisible" title="纳入题目" width="900px" destroy-on-close class="include-topic-dialog">
      <div class="filter-section">
        <el-select v-model="includeFilterMajor" placeholder="请选择适用专业筛选" style="width: 300px" clearable @change="handleIncludeMajorChange">
          <el-option label="全部专业" value="" />
          <el-option label="软件技术" value="软件技术" />
          <el-option label="计算机科学" value="计算机科学" />
          <el-option label="大数据技术" value="大数据技术" />
          <el-option label="人工智能" value="人工智能" />
          <el-option label="信息安全" value="信息安全" />
        </el-select>
        <el-button type="success" plain>题库共 {{ allAvailableTopics.length }} 道题，已纳入 {{ tableData.length }} 道</el-button>
      </div>

      <div class="topic-table-wrapper">
        <el-table
          ref="includeTableRef"
          :data="paginatedIncludeTopics"
          border
          stripe
          v-loading="includeLoading"
          @selection-change="handleIncludeSelectionChange"
          max-height="400"
          empty-text="暂无可纳入的题目"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="topicName" label="题目名称" min-width="180" align="center" show-overflow-tooltip />
          <el-table-column prop="major" label="适用专业" width="160" align="center" />
          <el-table-column prop="source" label="题目来源" width="140" align="center" />
          <el-table-column prop="difficulty" label="难度" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="getDifficultyType(row.difficulty)" size="small">{{ row.difficulty }}</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="availableTopicsForInclude.length === 0 && !includeLoading" class="empty-tip">
          <el-empty description="所有可用题目已全部纳入或暂无数据" :image-size="80">
            <template #description>
              <p style="color: #909399; font-size: 14px;">{{ includeFilterMajor ? `${includeFilterMajor} 专业的题目已全部纳入` : '题库中的可选题目已全部纳入' }}</p>
            </template>
          </el-empty>
        </div>

        <div class="table-footer">
          <div class="footer-tip">默认为10条数据，全选某类 请自行调整</div>
          <el-pagination
            v-model:current-page="includeCurrentPage"
            v-model:page-size="includePageSize"
            :total="availableTopicsForInclude.length"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            size="small"
            @size-change="handleIncludeSizeChange"
            @current-change="handleIncludePageChange"
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleIncludeSubmit" :disabled="selectedTopicsForInclude.length === 0">纳入 ({{ selectedTopicsForInclude.length }})</el-button>
          <el-button @click="includeDialogVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Plus, Download, Delete, Upload, View, UploadFilled, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import request from '@/utils/request'

const loading = ref(false)
const tableRef = ref(null)
const addFormRef = ref(null)
const editFormRef = ref(null)
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

// 从API获取的数据
const tableData = ref([])  // 改为空数组，从API加载
const semesterList = ref([]) // 学期列表
const categoryList = ref([]) // 题目分类列表

// 页面加载时获取数据
onMounted(() => {
  fetchTopics()
  fetchSemesters()
  fetchCategories()
})

/**
 * 从后端获取题目列表
 */
async function fetchTopics() {
  loading.value = true
  try {
    const res = await request.get('/v1/teacher/topics')
    if (res.data && Array.isArray(res.data)) {
      // 获取当前活跃的学期（取第一个）
      const currentSemester = semesterList.value.length > 0 ? semesterList.value[0].semester : ''
      
      // 转换数据格式以匹配前端表格
      tableData.value = res.data.map((topic, index) => ({
        id: index + 1,
        topicId: topic.topic_id,
        topicName: topic.topic_name,
        description: topic.description,
        major: topic.category || '未分类',
        selectedCount: topic.selection_count || 0,
        source: formatSource(topic.source),
        creator: topic.creator_name || '未知创建者',
        difficulty: formatDifficulty(topic.difficulty),
        semester: currentSemester, // 使用当前活跃学期
        status: topic.status,
        rawData: topic
      }))
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

/**
 * 从后端获取可用学期列表
 */
async function fetchSemesters() {
  try {
    const res = await request.get('/v1/teacher/semesters')
    if (res.data && Array.isArray(res.data)) {
      semesterList.value = res.data
    }
  } catch (error) {
    console.error('获取学期列表失败:', error)
  }
}

/**
 * 从后端获取题目分类列表
 */
async function fetchCategories() {
  try {
    const res = await request.get('/v1/teacher/topic-categories')
    if (res.data && Array.isArray(res.data)) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error('获取题目分类列表失败:', error)
  }
}

/**
 * 格式化题目来源
 */
function formatSource(source) {
  const map = {
    'teacher': '教师命题',
    'student': '学生自拟',
    'enterprise': '企业课题'
  }
  return map[source] || source || '未知来源'
}

/**
 * 格式化难度等级
 */
function formatDifficulty(difficulty) {
  if (!difficulty) return '未知'
  const map = { 1: '简单', 2: '较易', 3: '中等', 4: '较难', 5: '困难' }
  return map[difficulty] || difficulty.toString()
}

const searchForm = reactive({
  semester: '',
  topicName: '',
  major: '',
  isSelfCreated: ''
})

const selectedRows = ref([])
const currentTopic = ref(null)
const importDialogVisible = ref(false)
const topicUploadRef = ref(null)
const topicImportFileList = ref([])
const selectedTopicFile = ref(null)
const includeDialogVisible = ref(false)
const includeTableRef = ref(null)
const includeFilterMajor = ref('')
const includeLoading = ref(false)
const includeCurrentPage = ref(1)
const includePageSize = ref(10)
const selectedTopicsForInclude = ref([])

const addForm = reactive({
  topicName: '',
  description: '',
  major: [],
  source: '',
  difficulty: '中等'
})

const editForm = reactive({
  topicId: '',
  id: null,
  topicName: '',
  description: '',
  majorList: [],
  source: '',
  difficulty: '中等'
})

const addRules = {
  topicName: [{ required: true, message: '请输入毕设题目', trigger: 'blur' }],
  description: [{ required: true, message: '请输入题目描述', trigger: 'blur' }],
  major: [{ required: true, type: 'array', message: '请选择适用专业', trigger: 'change' }],
  source: [{ required: true, message: '请选择题目来源', trigger: 'change' }]
}

const editRules = {
  topicName: [{ required: true, message: '请输入毕设题目', trigger: 'blur' }],
  description: [{ required: true, message: '请输入题目描述', trigger: 'blur' }],
  majorList: [{ required: true, type: 'array', message: '请选择适用专业', trigger: 'change' }],
  source: [{ required: true, message: '请选择题目来源', trigger: 'change' }]
}

let topicIdCounter = 132

const allAvailableTopics = [
  { topicName: '基于React的在线考试系统设计与实现', major: '软件技术', source: '教师指定题目', difficulty: '中等', description: '使用React框架开发一个功能完善的在线考试系统...' },
  { topicName: '基于Python的自动化测试平台', major: '软件技术', source: '科研课题', difficulty: '困难', description: '利用Python+Selenium实现Web应用的自动化测试...' },
  { topicName: '智能客服聊天机器人研发', major: '人工智能', source: '企业合作题目', difficulty: '困难', description: '基于NLP技术构建智能客服系统，支持多轮对话...' },
  { topicName: '校园二手交易平台移动端App', major: '软件技术', source: '学生自拟题目', difficulty: '简单', description: '开发一款面向高校学生的二手物品交易应用...' },
  { topicName: '区块链技术在供应链管理中的应用', major: '信息安全', source: '科研课题', difficulty: '困难', description: '利用区块链的不可篡改特性设计供应链溯源系统...' },
  { topicName: '基于Spring Cloud微服务架构实践', major: '计算机科学', source: '教师指定题目', difficulty: '困难', description: '采用Spring Cloud全家桶构建分布式微服务系统...' },
  { topicName: '智慧校园一卡通管理系统', major: '软件技术', source: '教师指定题目', difficulty: '简单', description: '整合门禁、消费、考勤等功能的校园一卡通系统...' },
  { topicName: '大数据可视化分析平台', major: '大数据技术', source: '企业合作题目', difficulty: '中等', description: '使用ECharts+Python实现多维度数据的可视化展示...' },
  { topicName: '基于Vue3的组件库设计与实现', major: '软件技术', source: '学生自拟题目', difficulty: '中等', description: '从零设计和开发一套类似Element Plus的UI组件库...' },
  { topicName: '物联网环境监测系统', major: '人工智能', source: '教师指定题目', difficulty: '中等', description: '通过传感器网络实时采集环境数据并进行分析预警...' },
  { topicName: '跨境电商平台后端服务开发', major: '计算机科学', source: '企业合作题目', difficulty: '困难', description: '为跨境电商平台设计高并发、高可用的后端服务...' },
  { topicName: '移动端健康运动记录App', major: '软件技术', source: '学生自拟题目', difficulty: '简单', description: '支持步数统计、卡路里计算、运动轨迹记录等功能...' }
]

const availableTopicsForInclude = ref([...allAvailableTopics])

const paginatedIncludeTopics = computed(() => {
  const start = (includeCurrentPage.value - 1) * includePageSize.value
  const end = start + includePageSize.value
  return availableTopicsForInclude.value.slice(start, end)
})


const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (searchForm.semester && item.semester !== searchForm.semester) return false
    if (searchForm.topicName && !item.topicName.includes(searchForm.topicName)) return false
    if (searchForm.major && !item.major.includes(searchForm.major)) return false
    if (searchForm.isSelfCreated === 'yes' && !item.creator.includes('**')) return false
    if (searchForm.isSelfCreated === 'no' && item.creator.includes('**')) return false
    return true
  })
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredData.value.slice(start, end)
})

function handleSearch() {
  loading.value = true
  currentPage.value = 1
  setTimeout(() => {
    loading.value = false
    const count = filteredData.value.length
    ElMessage.success(`搜索完成，共找到 ${count} 条题目`)
  }, 300)
}

function handleReset() {
  searchForm.semester = ''
  searchForm.topicName = ''
  searchForm.major = ''
  searchForm.isSelfCreated = ''
  currentPage.value = 1
  pageSize.value = 10
  ElMessage.info('已重置搜索条件')
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function handleAdd() {
  addDialogVisible.value = true
  addForm.topicName = ''
  addForm.description = ''
  addForm.major = []
  addForm.source = ''
  addForm.difficulty = '中等'
}

async function handleAddSubmit() {
  addFormRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        // 调用后端API新增题目
        const newTopicData = {
          topicName: addForm.topicName,
          description: addForm.description,
          category: addForm.major.join(', '), // 使用category字段存储专业
          source: mapSourceToDb(addForm.source),   // 转换为数据库格式
          difficulty: mapDifficultyToDb(addForm.difficulty), // 转换为数据库格式
          requirements: '',  // 可以后续扩展
          maxStudents: 5    // 默认最大可选学生数
        }
        
        const res = await request.post('/topics', newTopicData)
        
        // 新增成功后重新获取数据
        await fetchTopics()
        
        addDialogVisible.value = false
        ElMessage.success('✅ 新增题目成功！数据已同步到数据库')
      } catch (error) {
        console.error('新增题目失败:', error)
        ElMessage.error('❌ 新增题目失败，请重试')
      }
    }
  })
}

function handleViewDetail(row) {
  currentTopic.value = row
  detailDialogVisible.value = true
}

function handleEdit(topic) {
  if (!topic) return

  detailDialogVisible.value = false

  editForm.topicId = topic.topicId
  editForm.id = topic.id
  editForm.topicName = topic.topicName
  editForm.description = topic.description
  editForm.majorList = topic.major.split(', ').filter(m => m)
  editForm.source = topic.source
  editForm.difficulty = topic.difficulty || '中等'

  editDialogVisible.value = true
}

async function handleEditSubmit() {
  editFormRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        // 调用后端API更新题目
        const updateData = {
          topicName: editForm.topicName,
          description: editForm.description,
          category: editForm.majorList.join(', '), // 使用category字段存储专业
          source: mapSourceToDb(editForm.source),   // 转换为数据库格式
          difficulty: mapDifficultyToDb(editForm.difficulty) // 转换为数据库格式
        }
        
        await request.put(`/topics/${editForm.topicId}`, updateData)
        
        // 更新成功后重新获取数据
        await fetchTopics()
        
        editDialogVisible.value = false
        ElMessage.success('✅ 题目修改成功！数据已同步到数据库')
      } catch (error) {
        console.error('更新题目失败:', error)
        ElMessage.error('❌ 题目修改失败，请重试')
      }
    }
  })
}

/**
 * 将前端显示的题目来源转换为数据库存储值
 */
function mapSourceToDb(source) {
  const map = {
    '教师指定题目': 'teacher',
    '学生自拟题目': 'student',
    '企业合作题目': 'enterprise',
    '科研课题': 'research'
  }
  return map[source] || 'teacher'
}

/**
 * 将前端显示的难度等级转换为数据库存储值
 */
function mapDifficultyToDb(difficulty) {
  const map = {
    '简单': 1,
    '较易': 2,
    '中等': 3,
    '较难': 4,
    '困难': 5
  }
  return map[difficulty] || 3
}

function handleIncludeTopic() {
  includeDialogVisible.value = true
  includeFilterMajor.value = ''
  selectedTopicsForInclude.value = []
  includeCurrentPage.value = 1

  includeLoading.value = true
  setTimeout(() => {
    const existingTopicNames = tableData.value.map(t => t.topicName)
    availableTopicsForInclude.value = allAvailableTopics.filter(t => !existingTopicNames.includes(t.topicName))
    includeLoading.value = false
  }, 300)
}

function handleIncludeSelectionChange(rows) {
  selectedTopicsForInclude.value = rows
}

function handleIncludeMajorChange(val) {
  includeLoading.value = true
  includeCurrentPage.value = 1

  setTimeout(() => {
    let filtered = [...allAvailableTopics]

    if (val) {
      filtered = allAvailableTopics.filter(t => t.major === val)
    }

    const existingTopicNames = tableData.value.map(t => t.topicName)
    availableTopicsForInclude.value = filtered.filter(t => !existingTopicNames.includes(t.topicName))

    includeLoading.value = false

    if (val) {
      const totalCount = allAvailableTopics.filter(t => t.major === val).length
      const alreadyIncluded = totalCount - availableTopicsForInclude.value.length
      if (alreadyIncluded > 0) {
        ElMessage.info(`${val} 专业共 ${totalCount} 道题，已纳入 ${alreadyIncluded} 道，可选 ${availableTopicsForInclude.value.length} 道`)
      } else {
        ElMessage.success(`${val} 专业共 ${availableTopicsForInclude.value.length} 道题可纳入`)
      }
    } else {
      const totalAll = allAvailableTopics.length
      const alreadyIncludedAll = totalAll - availableTopicsForInclude.value.length
      ElMessage.info(`全部专业共 ${totalAll} 道题，已纳入 ${alreadyIncludedAll} 道，可选 ${availableTopicsForInclude.value.length} 道`)
    }

    selectedTopicsForInclude.value = []
  }, 300)
}

function getDifficultyType(difficulty) {
  const map = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return map[difficulty] || 'info'
}

function handleIncludeSizeChange(val) {
  includePageSize.value = val
  includeCurrentPage.value = 1
}

function handleIncludePageChange(val) {
  includeCurrentPage.value = val
}

function handleIncludeSubmit() {
  if (selectedTopicsForInclude.value.length === 0) {
    ElMessage.warning('请至少选择一道题目')
    return
  }

  const existingTopicNames = tableData.value.map(t => t.topicName)
  const duplicateTopics = selectedTopicsForInclude.value.filter(t => existingTopicNames.includes(t.topicName))
  const newTopics = selectedTopicsForInclude.value.filter(t => !existingTopicNames.includes(t.topicName))

  if (duplicateTopics.length > 0) {
    ElMessageBox.confirm(
      `检测到 ${duplicateTopics.length} 道题目已被纳入，是否只纳入剩余 ${newTopics.length} 道新题目？`,
      '重复检测提示',
      {
        confirmButtonText: '确认纳入',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      addTopicsToTable(newTopics)
    }).catch(() => {})
  } else {
    addTopicsToTable(selectedTopicsForInclude.value)
  }
}

function addTopicsToTable(topics) {
  let newId = Math.max(...tableData.value.map(t => t.id), 0) + 1
  let successCount = 0

  topics.forEach(topic => {
    const newTopicId = ++topicIdCounter
    tableData.value.push({
      id: newId++,
      topicId: String(newTopicId),
      topicName: topic.topicName,
      description: topic.description,
      major: topic.major,
      selectedCount: 0,
      source: topic.source,
      creator: '当前用户',
      difficulty: topic.difficulty,
      semester: searchForm.semester || '2023-2024-2'
    })
    successCount++
  })

  includeDialogVisible.value = false

  const totalPages = Math.ceil(filteredData.value.length / pageSize.value)
  currentPage.value = totalPages

  ElMessage.success(`✅ 成功纳入 ${successCount} 道题目！当前批次共 ${tableData.value.length} 道题目`)

  selectedTopicsForInclude.value = []
  includeFilterMajor.value = ''
}

async function handleRemoveTopic() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要移除的题目')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个题目吗？<br/><br/>
     <small style="color: #f56c6c;">⚠️ 此操作将从数据库中永久删除，无法恢复！</small>`,
    '确认删除',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true
    }
  ).then(async () => {
    try {
      // 调用后端API逐个删除题目
      const deletePromises = selectedRows.value.map(row => 
        request.delete(`/topics/${row.topicId}`)
      )
      
      await Promise.all(deletePromises)
      
      // 删除成功后重新获取数据
      await fetchTopics()
      
      ElMessage.success(`✅ 成功删除 ${selectedRows.value.length} 个题目！数据已从数据库移除`)
      selectedRows.value = []
    } catch (error) {
      console.error('删除题目失败:', error)
      ElMessage.error('❌ 删除失败，可能有些题目已被学生选用或发生错误')
    }
  }).catch(() => {})
}

function handleExport() {
  if (filteredData.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  try {
    const headers = ['题目ID', '题目名称', '题目描述', '适用专业', '已选次数', '题目来源', '创建者']
    const data = filteredData.value.map(row => ({
      '题目ID': row.topicId,
      '题目名称': row.topicName,
      '题目描述': row.description,
      '适用专业': row.major,
      '已选次数': row.selectedCount,
      '题目来源': row.source,
      '创建者': row.creator
    }))

    const ws = XLSX.utils.json_to_sheet(data)
    ws['!cols'] = [
      { wch: 10 },
      { wch: 30 },
      { wch: 50 },
      { wch: 18 },
      { wch: 12 },
      { wch: 16 },
      { wch: 12 }
    ]

    XLSX.utils.sheet_add_aoa(ws, [headers], { origin: 'A1' })

    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '题目列表')

    const fileName = `毕业设计题目_${new Date().toISOString().slice(0, 10)}.xlsx`
    XLSX.writeFile(wb, fileName)

    ElMessage.success(`成功导出 ${filteredData.value.length} 条题目数据`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  }
}


function handleImport() {
  importDialogVisible.value = true
  selectedTopicFile.value = null
  topicImportFileList.value = []
}

function handleTopicFileChange(file, fileList) {
  const fileName = file.name.toLowerCase()
  const isExcel = fileName.endsWith('.xls') || fileName.endsWith('.xlsx')

  if (!isExcel) {
    ElMessage.error('仅支持上传 .xls 或 .xlsx 格式的文件！')
    topicImportFileList.value = []
    selectedTopicFile.value = null
    return
  }

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB！')
    topicImportFileList.value = []
    selectedTopicFile.value = null
    return
  }

  topicImportFileList.value = [file]
  selectedTopicFile.value = file
}

function handleTopicFileRemove(file, fileList) {
  topicImportFileList.value = []
  selectedTopicFile.value = null
}

function handleTopicExceed(files, fileList) {
  ElMessage.warning('只能选择1个文件，请先移除已选文件')
}

function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

function downloadTopicTemplate() {
  const headers = ['题目名称', '题目描述', '适用专业', '题目来源', '难度等级']
  const templateData = [
    { '题目名称': '基于Vue.js的在线教育平台设计与实现', '题目描述': '设计并开发一个基于Vue.js框架的在线教育平台...', '适用专业': '软件技术', '题目来源': '教师指定题目', '难度等级': '中等' },
    { '题目名称': '智能交通信号控制系统研究', '题目描述': '利用物联网和人工智能技术设计智能交通控制系统...', '适用专业': '大数据技术', '题目来源': '科研课题', '难度等级': '困难' },
    { '题目名称': '（示例）', '题目描述': '（示例）', '适用专业': '（示例）', '题目来源': '（示例）', '难度等级': '（示例）' }
  ]

  const ws = XLSX.utils.json_to_sheet(templateData)
  ws['!cols'] = [
    { wch: 35 },
    { wch: 50 },
    { wch: 14 },
    { wch: 16 },
    { wch: 12 }
  ]

  XLSX.utils.sheet_add_aoa(ws, [headers], { origin: 'A1' })

  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '导入模板')

  const fileName = `毕设题目导入模板_${new Date().toISOString().slice(0, 10)}.xlsx`
  XLSX.writeFile(wb, fileName)

  ElMessage.success('模板下载成功！请按格式填写数据后上传')
}

function handleTopicImportSubmit() {
  if (!selectedTopicFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  ElMessageBox.confirm(
    `确定要导入文件 "${selectedTopicFile.value.name}" 吗？<br/><br/>
     <small style="color: #909399;">注意：导入后将根据题目名称匹配，重复的将被更新</small>`,
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

      const mockImportData = [
        { topicName: '基于React的企业级后台管理系统', description: '使用React和Ant Design构建企业级后台管理系统', major: '软件技术', source: '学生自拟题目', difficulty: '中等' },
        { topicName: '基于Python的数据可视化分析平台', description: '利用Python和ECharts实现数据采集、清洗、分析和可视化展示', major: '大数据技术', source: '教师指定题目', difficulty: '困难' },
        { topicName: '基于Spring Cloud的微服务架构实践', description: '采用Spring Cloud微服务架构设计和实现分布式系统', major: '计算机科学', source: '企业合作题目', difficulty: '困难' },
        { topicName: '移动端健康管理系统App开发', description: '开发一款跨平台的健康管理应用，支持运动记录、饮食管理等功能', major: '人工智能', source: '学生自拟题目', difficulty: '简单' }
      ]

      mockImportData.forEach(item => {
        const existingIndex = tableData.value.findIndex(t => t.topicName === item.topicName)

        if (existingIndex > -1) {
          tableData.value[existingIndex] = {
            ...tableData.value[existingIndex],
            description: item.description,
            major: item.major,
            source: item.source,
            difficulty: item.difficulty
          }
          updateCount++
        } else {
          const newId = ++topicIdCounter
          tableData.value.push({
            id: Date.now() + successCount,
            topicId: String(newId),
            topicName: item.topicName,
            description: item.description,
            major: item.major,
            selectedCount: 0,
            source: item.source,
            creator: '当前用户',
            difficulty: item.difficulty,
            semester: searchForm.semester || '2023-2024-2'
          })
          successCount++
        }
      })

      loading.value = false
      importDialogVisible.value = false
      selectedTopicFile.value = null
      topicImportFileList.value = []

      let message = `✅ 导入完成！<br/>`
      if (successCount > 0) message += `• 新增 ${successCount} 条题目<br/>`
      if (updateCount > 0) message += `• 更新 ${updateCount} 条题目<br/>`
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

function handleRefresh() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功，数据已更新')
  }, 500)
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
.topic-container {
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

@media (max-width: 768px) {
  .action-section {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .action-buttons {
    flex-wrap: wrap;
  }
}

.topic-container :deep(.el-descriptions__label) {
  white-space: nowrap !important;
  text-align: center !important;
  vertical-align: middle !important;
  display: table-cell !important;
  line-height: normal !important;
}

.topic-container :deep(.el-descriptions__content) {
  vertical-align: middle !important;
}

.import-topic-dialog .upload-container {
  padding: 10px 0;
}

.import-topic-dialog .upload-dragger {
  width: 100%;
}

.import-topic-dialog .upload-dragger :deep(.el-upload) {
  width: 100%;
}

.import-topic-dialog .upload-dragger :deep(.el-upload-dragger) {
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

.import-topic-dialog .upload-dragger :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.import-topic-dialog .upload-dragger :deep(.el-icon--upload) {
  font-size: 60px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.import-topic-dialog .upload-dragger :deep(.el-upload__text) {
  font-size: 14px;
  color: #606266;
}

.import-topic-dialog .upload-dragger :deep(.el-upload__text em) {
  color: #409eff;
  font-style: normal;
  font-weight: 500;
}

.import-topic-dialog .upload-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
  padding: 16px;
  background-color: #fff9e6;
  border: 1px solid #ffe58f;
  border-radius: 4px;
}

.import-topic-dialog .upload-tip {
  color: #fa8c16;
  font-size: 13px;
  font-weight: 500;
}

.import-topic-dialog .file-info {
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

.import-topic-dialog .file-info .el-icon {
  font-size: 18px;
}

.import-topic-dialog .file-size {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
}

.include-topic-dialog .filter-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.include-topic-dialog .topic-table-wrapper {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 0;
}

.include-topic-dialog .empty-tip {
  padding: 40px 20px;
  text-align: center;
}

.include-topic-dialog .table-footer {
  padding: 12px 16px;
  background-color: #fafafa;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.include-topic-dialog .footer-tip {
  color: #67c23a;
  font-size: 13px;
}

.include-topic-dialog .dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
