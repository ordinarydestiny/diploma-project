<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <el-row :gutter="20" align="middle">
        <el-col :span="24">
          <div class="welcome-content">
            <h1 class="welcome-title">👋 {{ greeting }}，{{ userInfo.realName || userInfo.name || '用户' }}</h1>
            <p class="welcome-subtitle">欢迎使用毕业设计管理系统 | 登录时间：{{ loginDateTime }} | 当前批次：{{ currentBatchName }}</p>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 系统公告 -->
    <div class="notice-section">
      <el-row :gutter="20">
        <el-col :span="24">
          <div class="notice-card">
            <div class="notice-header">
              <h3>📢 系统公告</h3>
              <el-button text type="primary" size="small" @click="viewAllNotices">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>

            <div class="notice-list">
              <div 
                v-for="(notice, index) in systemNotices" 
                :key="index"
                class="notice-item"
                @click="viewNotice(notice)"
              >
                <div class="notice-dot" :class="{ 'dot-new': notice.isNew }"></div>
                <div class="notice-content">
                  <div class="notice-title">
                    <span v-if="notice.isNew" class="new-badge">新</span>
                    {{ notice.title }}
                  </div>
                  <div class="notice-time">{{ notice.time }}</div>
                </div>
              </div>

              <el-empty v-if="systemNotices.length === 0" description="暂无系统公告" />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 主要内容区 -->
    <el-row :gutter="20" class="main-content">
      <!-- 左侧：待办事项 + 快捷入口 -->
      <el-col :xs="24" :lg="16">
        <!-- 待办事项 -->
        <div class="section-card todo-section">
          <div class="section-header">
            <h3>📋 待办事项</h3>
            <el-badge :value="todoCount" :max="99" class="todo-badge">
              <span></span>
            </el-badge>
          </div>
          
          <div class="todo-list" v-loading="loadingTodos">
            <div 
              v-for="(todo, index) in todoList" 
              :key="index" 
              class="todo-item"
              @click="handleTodoClick(todo)"
            >
              <div class="todo-icon" :class="'priority-' + todo.priority">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-desc">{{ todo.description }}</div>
                <div class="todo-meta">
                  <el-tag :type="getTodoType(todo.status)" size="small">
                    {{ todo.statusText }}
                  </el-tag>
                  <span class="todo-time">{{ todo.time }}</span>
                </div>
              </div>
              <div class="todo-action">
                <el-button type="primary" link size="small">
                  去处理
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>

            <el-empty v-if="!loadingTodos && todoList.length === 0" description="暂无待办事项" />
          </div>
        </div>

        <!-- 快捷入口 -->
        <div class="section-card quick-access-section">
          <div class="section-header">
            <h3>⚡ 常用功能</h3>
          </div>
          
          <div class="quick-grid">
            <div 
              v-for="(item, index) in quickAccessItems" 
              :key="index"
              class="quick-item"
              @click="handleQuickAccess(item)"
            >
              <div class="quick-icon" :style="{ background: item.color }">
                <el-icon :size="24"><component :is="item.icon" /></el-icon>
              </div>
              <div class="quick-label">{{ item.label }}</div>
              <div class="quick-count" v-if="item.count !== undefined">
                {{ item.count }}项
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：最新动态 + 进度概览 -->
      <el-col :xs="24" :lg="8">
        <!-- 最新动态 -->
        <div class="section-card activity-section">
          <div class="section-header">
            <h3>🕐 最新动态</h3>
            <el-button text type="primary" size="small" @click="viewAllActivities">
              查看全部
            </el-button>
          </div>
          
          <div class="activity-list" v-loading="loadingActivities">
            <div 
              v-for="(activity, index) in recentActivities" 
              :key="index"
              class="activity-item"
            >
              <div class="activity-dot" :class="'dot-' + activity.type"></div>
              <div class="activity-content">
                <div class="activity-text">{{ activity.text }}</div>
                <div class="activity-time">{{ activity.time }}</div>
              </div>
            </div>

            <el-empty v-if="!loadingActivities && recentActivities.length === 0" description="暂无动态" />
          </div>
        </div>

        <!-- 进度概览（仅教师和管理员可见） -->
        <div class="section-card progress-section" v-if="showProgressSection">
          <div class="section-header">
            <h3>📊 整体进度</h3>
          </div>
          
          <div class="progress-list">
            <div v-for="(progress, index) in progressList" :key="index" class="progress-item">
              <div class="progress-label">{{ progress.label }}</div>
              <el-progress 
                :percentage="progress.percentage" 
                :color="progress.color"
                :stroke-width="10"
              />
              <div class="progress-detail">
                <span>{{ progress.completed }}/{{ progress.total }}</span>
                <span class="progress-rate">{{ progress.rate }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User as UserIcon, Document as DocumentIcon, Refresh, Bell, ArrowRight, Top, Bottom, EditPen, Check, List, DataAnalysis, Setting, Files, Calendar, Trophy } from '@element-plus/icons-vue'

const router = useRouter()

const userInfo = reactive({
  name: '',
  realName: '',
  role: '',
  roleCode: ''
})

onMounted(() => {
  const userStr = localStorage.getItem('userInfo')
  if (userStr && userStr !== '{}' && userStr !== 'null') {
    try {
      const user = JSON.parse(userStr)
      console.log('【首页】从localStorage获取到用户信息:', user)
      
      userInfo.username = user.username || ''
      userInfo.realName = user.realName || user.name || user.username || '用户'
      userInfo.role = user.role ? (roleLabels[user.role] || user.role) : ''
      userInfo.roleCode = user.role || ''
      
      console.log('【首页】设置后的userInfo:', { 
        realName: userInfo.realName, 
        username: userInfo.username,
        role: userInfo.role,
        roleCode: userInfo.roleCode
      })
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  } else {
    console.warn('【首页】未找到用户信息或信息为空')
  }
  
  fetchDashboardData()
})

const roleLabels = {
  college_admin: '院级管理员',
  major_admin: '专业负责人',
  teacher: '指导教师',
  student: '学生'
}

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const now = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  return now.toLocaleDateString('zh-CN', options)
})

const loginDateTime = ref('')

let timer = null

function updateCurrentTime() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekDay = weekDays[now.getDay()]
  
  loginDateTime.value = `${year}年${month}月${day}日 ${weekDay} ${hours}:${minutes}:${seconds}`
}

const currentBatchName = ref('2025-2026学年 第2学期')

const loadingTodos = ref(false)
const todoList = ref([])
const todoCount = computed(() => todoList.value.length)

const getTodoType = (status) => {
  const types = {
    'urgent': 'danger',
    'pending': 'warning',
    'normal': 'info',
    'completed': 'success'
  }
  return types[status] || 'info'
}

const handleTodoClick = (todo) => {
  if (todo.route) {
    router.push(todo.route)
  }
}

const quickAccessItems = computed(() => {
  const role = userInfo.roleCode
  
  // 所有可用功能的完整配置（与侧边栏菜单完全一致）
  const allFunctionItems = [
    // 通用功能 - 学生专用
    { 
      label: '我的毕设', 
      icon: markRaw(UserIcon), 
      color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      route: '/my-graduation',
      roles: ['student']  // 只有学生显示
    },
    // 管理功能 - 院管/专业负责人/教师共用
    { 
      label: '毕设通知', 
      icon: markRaw(Bell), 
      color: 'linear-gradient(135deg, #f5af19 0%, #f12711 100%)',
      route: '/graduation/notice',
      roles: ['college_admin', 'major_admin']
    },
    { 
      label: '毕设批次', 
      icon: markRaw(Calendar), 
      color: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
      route: '/graduation/batch',
      roles: ['college_admin', 'major_admin']
    },
    // 教学核心功能
    { 
      label: '题库管理', 
      icon: markRaw(Files), 
      color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
      route: '/graduation/topic-management',
      roles: ['college_admin', 'major_admin', 'teacher']
    },
    { 
      label: '学生选题', 
      icon: markRaw(List), 
      color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
      route: '/graduation/student-selection',
      roles: ['college_admin', 'teacher']
    },
    { 
      label: '下达任务书', 
      icon: markRaw(EditPen), 
      color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
      route: '/graduation/task-book',
      roles: ['college_admin', 'teacher']
    },
    // 审核检查功能 - 教师专用
    { 
      label: '中期检查', 
      icon: markRaw(Check), 
      color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
      route: '/graduation/midterm-check',
      roles: ['teacher']
    },
    { 
      label: '最终检查', 
      icon: markRaw(DataAnalysis), 
      color: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
      route: '/graduation/final-check',
      roles: ['teacher']
    },
    // 答辩与签到
    { 
      label: '答辩管理', 
      icon: markRaw(Trophy), 
      color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
      route: '/graduation/defense',
      roles: ['college_admin', 'teacher']
    },
    { 
      label: '签到情况', 
      icon: markRaw(Calendar), 
      color: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
      route: '/graduation/check-in',
      roles: ['college_admin', 'teacher']
    },
    // 成绩管理
    { 
      label: '成绩管理', 
      icon: markRaw(Trophy), 
      color: 'linear-gradient(135deg, #ff6a88 0%, #ff99ac 100%)',
      route: '/graduation/score-management',
      roles: ['college_admin', 'teacher']
    }
  ]
  
  // 根据当前用户角色过滤可用的功能
  const filteredItems = allFunctionItems.filter(item => 
    item.roles && item.roles.includes(role)
  )
  
  return filteredItems
})

const handleQuickAccess = (item) => {
  if (item.route) {
    router.push(item.route)
  }
}

const loadingActivities = ref(false)
const recentActivities = ref([
  {
    type: 'info',
    text: '系统于 2026-06-11 14:30 进行了维护升级',
    time: '刚刚'
  },
  {
    type: 'success',
    text: '钱七同学提交了中期检查报告 V2.0',
    time: '10分钟前'
  },
  {
    type: 'warning',
    text: '张三同学的最终检查报告被驳回修改',
    time: '1小时前'
  },
  {
    type: 'success',
    text: '王指导老师录入了李四的答辩成绩：85分（良好）',
    time: '2小时前'
  },
  {
    type: 'info',
    text: '新增了5个毕业设计题目到题库',
    time: '3小时前'
  }
])

const showProgressSection = computed(() => {
  const role = userInfo.roleCode
  return role === 'teacher' || role === 'college_admin' || role === 'major_admin'
})

const progressList = ref([
  {
    label: '选题完成率',
    percentage: 75,
    color: '#409EFF',
    completed: 75,
    total: 100,
    rate: '75%'
  },
  {
    label: '任务书下达率',
    percentage: 60,
    color: '#67C23A',
    completed: 60,
    total: 100,
    rate: '60%'
  },
  {
    label: '中期检查完成率',
    percentage: 45,
    color: '#E6A23C',
    completed: 45,
    total: 100,
    rate: '45%'
  },
  {
    label: '最终检查定稿率',
    percentage: 30,
    color: '#F56C6C',
    completed: 30,
    total: 100,
    rate: '30%'
  },
  {
    label: '答辩完成率',
    percentage: 20,
    color: '#909399',
    completed: 20,
    total: 100,
    rate: '20%'
  }
])

const systemNotices = ref([
  {
    isNew: true,
    title: '关于2026届毕业设计答辩安排的通知',
    time: '2026-06-10'
  },
  {
    isNew: true,
    title: '毕业设计管理系统使用指南（更新版）',
    time: '2026-06-08'
  },
  {
    isNew: false,
    title: '关于提交最终检查报告截止时间的提醒',
    time: '2026-06-05'
  },
  {
    isNew: false,
    title: '系统维护通知：2026年6月11日停机维护',
    time: '2026-06-09'
  }
])

onMounted(() => {
  // 初始化当前时间
  updateCurrentTime()
  
  // 每秒更新时间
  timer = setInterval(updateCurrentTime, 1000)
  
  const userStr = localStorage.getItem('userInfo')
  if (userStr && userStr !== '{}' && userStr !== 'null') {
    try {
      const user = JSON.parse(userStr)
      console.log('【首页】从localStorage获取到用户信息:', user)
      
      userInfo.username = user.username || ''
      userInfo.realName = user.realName || user.name || user.username || '用户'
      userInfo.role = user.role ? (roleLabels[user.role] || user.role) : ''
      userInfo.roleCode = user.role || ''
      
      console.log('【首页】设置后的userInfo:', { 
        realName: userInfo.realName, 
        username: userInfo.username,
        role: userInfo.role,
        roleCode: userInfo.roleCode
      })
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  } else {
    console.warn('【首页】未找到用户信息或信息为空')
  }
  
  fetchDashboardData()
})

onUnmounted(() => {
  // 清理定时器，防止内存泄漏
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})

const viewAllActivities = () => {
  ElMessage.info('查看全部动态功能开发中...')
}

const viewAllNotices = () => {
  ElMessage.info('查看全部公告功能开发中...')
}

const viewNotice = (notice) => {
  ElMessage.info('查看公告：' + notice.title)
}

async function fetchDashboardData() {
  try {
    loadingTodos.value = true
    loadingActivities.value = true
    
    todoList.value = [
      {
        id: 1,
        title: '审批钱七的中期检查报告',
        description: '该生提交了V2.0版本的中期检查报告，请及时审核',
        status: 'urgent',
        statusText: '紧急',
        priority: 'high',
        time: '刚刚',
        route: '/midterm-check'
      },
      {
        id: 2,
        title: '下达张三的任务书',
        description: '张三同学已完成选题，请下达毕业设计任务书',
        status: 'pending',
        statusText: '待处理',
        priority: 'medium',
        time: '1小时前',
        route: '/task-book'
      },
      {
        id: 3,
        title: '审核李四的最终检查报告',
        description: '李四提交了最终版本的毕设报告，等待您定稿',
        status: 'pending',
        statusText: '待处理',
        priority: 'medium',
        time: '2小时前',
        route: '/final-check'
      },
      {
        id: 4,
        title: '录入王五的答辩成绩',
        description: '王五同学已完成答辩，请录入答辩成绩和评语',
        status: 'normal',
        statusText: '进行中',
        priority: 'low',
        time: '昨天',
        route: '/defense'
      },
      {
        id: 5,
        title: '确认赵六的选题申请',
        description: '赵六同学选择了《基于Vue的移动端应用开发》题目',
        status: 'normal',
        statusText: '待确认',
        priority: 'low',
        time: '2天前',
        route: '/student-selection'
      }
    ]

  } catch (error) {
    console.error('加载Dashboard数据失败:', error)
    todoList.value = []
  } finally {
    loadingTodos.value = false
    loadingActivities.value = false
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 20px;
  color: white;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.welcome-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  color: white;
}

.welcome-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.notice-section {
  margin-bottom: 20px;
}

.notice-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notice-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.notice-list {
  max-height: 200px;
  overflow-y: auto;
  overflow-x: hidden;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 10px;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.3s ease;
}

.notice-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #C0C4CC;
  margin-top: 7px;
  flex-shrink: 0;
}

.dot-new {
  background: #F56C6C;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.notice-content {
  flex: 1;
  min-width: 0;
}

.notice-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
  margin-bottom: 6px;
  word-break: break-all;
}

.new-badge {
  display: inline-block;
  padding: 2px 6px;
  font-size: 11px;
  font-weight: 600;
  color: white;
  background: #F56C6C;
  border-radius: 4px;
  margin-right: 6px;
  vertical-align: middle;
}

.notice-time {
  font-size: 13px;
  color: #909399;
}

.main-content {
  margin-bottom: 20px;
}

.section-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.todo-badge {
  margin-left: 8px;
}

.todo-list {
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;  /* 防止横向滚动条 */
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #fafafa;
  transition: all 0.3s ease;
  cursor: pointer;
}

.todo-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.todo-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.priority-high {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.priority-medium {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.priority-low {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.todo-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.todo-time {
  font-size: 12px;
  color: #C0C4CC;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.quick-item:hover {
  background: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.quick-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.quick-label {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  text-align: center;
  margin-bottom: 4px;
}

.quick-count {
  font-size: 11px;
  color: #909399;
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.dot-info { background: #409EFF; }
.dot-success { background: #67C23A; }
.dot-warning { background: #E6A23C; }
.dot-danger { background: #F56C6C; }

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 13px;
  color: #303133;
  line-height: 1.5;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #C0C4CC;
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.progress-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.progress-detail {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.progress-rate {
  font-weight: 600;
  color: #409EFF;
}

.notice-list {
  max-height: 250px;
  overflow-y: auto;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.notice-item:hover {
  background: #fafafa;
  padding-left: 8px;
  padding-right: 8px;
  border-radius: 6px;
}

.notice-text {
  flex: 1;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  font-size: 12px;
  color: #C0C4CC;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;
  }
  
  .welcome-title {
    font-size: 20px;
  }
  
  .welcome-subtitle {
    font-size: 12px;
  }
  
  .welcome-actions {
    margin-top: 12px;
    justify-content: flex-start;
  }
  
  .stat-value {
    font-size: 26px;
  }
  
  .quick-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
  
  .quick-item {
    padding: 16px 8px;
  }
  
  .quick-icon {
    width: 48px;
    height: 48px;
  }
  
  .section-card {
    padding: 16px;
  }
  
  .todo-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
