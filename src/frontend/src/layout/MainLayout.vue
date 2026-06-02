<!--
  主布局组件 - 包含侧边栏导航、顶部栏和内容区域
  支持角色切换功能，多角色用户可在顶部下拉菜单中切换当前角色
-->
<template>
  <el-container class="main-layout">
    <el-aside
      :width="isCollapse ? '64px' : '220px'"
      class="sidebar"
    >
      <div class="logo-container">
        <img
          src="/vite.svg"
          alt="logo"
          class="logo-icon"
        >
        <span
          v-show="!isCollapse"
          class="logo-text"
        >实习与毕设系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        class="sidebar-menu"
      >
        <template
          v-for="route in filteredRoutes"
          :key="route.path"
        >
          <el-sub-menu
            v-if="route.children && route.children.length > 0"
            :index="route.path"
          >
            <template #title>
              <el-icon><component :is="route.meta.icon" /></el-icon>
              <span>{{ getMenuTitle(route) }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="child.path"
            >
              <el-icon><component :is="child.meta.icon" /></el-icon>
              <span>{{ getMenuTitle(child) }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item
            v-else
            :index="route.path"
          >
            <el-icon><component :is="route.meta.icon" /></el-icon>
            <span>{{ getMenuTitle(route) }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            @click="toggleCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">
              首页
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta?.title && currentRoute.name !== 'Home'">
              {{ getMenuTitle(currentRoute) }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              <span class="username">{{ userStore.userInfo.realName || userStore.userInfo.username || '用户' }}</span>
              <span class="role-tag">{{ userStore.roleLabel }}</span>
              <el-icon v-if="userStore.hasMultipleRoles"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="role in userStore.roles"
                  :key="role.roleId"
                  :command="'switch:' + role.roleId"
                  :disabled="role.roleCode === userStore.roleCode"
                >
                  <el-icon v-if="role.roleCode === userStore.roleCode">
                    <Check />
                  </el-icon>
                  {{ role.roleName }}
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="userStore.hasMultipleRoles"
                  divided
                  command="switch-divider"
                  disabled
                >
                  <span class="switch-hint">点击切换角色</span>
                </el-dropdown-item>
                <el-dropdown-item
                  divided
                  command="profile"
                >
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item
                  divided
                  command="logout"
                >
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'

/** 当前路由信息 */
const route = useRoute()
/** 路由实例 */
const router = useRouter()
/** 用户状态Store */
const userStore = useUserStore()
/** 侧边栏折叠状态 */
const isCollapse = ref(false)

/**
 * 当前激活的菜单路径
 * @returns {string} 当前路由路径
 */
const activeMenu = computed(() => route.path)
/**
 * 当前路由对象
 * @returns {import('vue-router').RouteLocationNormalized} 当前路由信息
 */
const currentRoute = computed(() => route)

/** 全部菜单路由配置 */
const allMenuRoutes = [
  {
    path: '/home',
    meta: { title: '首页', icon: 'HomeFilled', roles: ['STUDENT', 'TEACHER', 'MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] }
  },
  {
    path: '/internship',
    meta: { title: '实习前', icon: 'Briefcase', roles: ['STUDENT', 'TEACHER', 'MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] },
    children: [
      { path: '/internship/application', meta: { title: '实习申请', icon: 'Document', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship/my-plan', meta: { title: '实习计划', icon: 'List', roles: ['STUDENT'] } },
      { path: '/internship/plan', meta: { title: '实习计划管理', icon: 'List', roles: ['TEACHER', 'MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship/record', meta: { title: '实习记录', icon: 'Folder', roles: ['STUDENT'] } }
    ]
  },
  {
    path: '/internship-mid',
    meta: { title: '实习中', icon: 'DataBoard', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] },
    children: [
      { path: '/internship-mid/overview', meta: { title: '实习生一览表', icon: 'UserFilled', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-mid/checkin', meta: { title: '签到管理', icon: 'Location', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-mid/my-checkin', meta: { title: '签到', icon: 'Location', roles: ['STUDENT'] } },
      { path: '/internship-mid/checkin-remind', meta: { title: '签到提醒记录', icon: 'Bell', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-mid/my-checkin-remind', meta: { title: '签到提醒', icon: 'Bell', roles: ['STUDENT'] } },
      { path: '/internship-mid/daily-report', meta: { title: '日报', icon: 'Notebook', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-mid/weekly-report', meta: { title: '周报', icon: 'Notebook', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-mid/monthly-report', meta: { title: '月报', icon: 'Notebook', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-mid/guidance-record', meta: { title: '实习指导记录', icon: 'ChatLineSquare', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-mid/visit-record', meta: { title: '实习巡访记录', icon: 'Promotion', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-mid/student-change', meta: { title: '学生异动', icon: 'Switch', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-mid/process-statistics', meta: { title: '实习生过程统计', icon: 'DataAnalysis', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] } }
    ]
  },
  {
    path: '/internship-post',
    meta: { title: '实习后', icon: 'Finished', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] },
    children: [
      { path: '/internship-post/assessment-inner', meta: { title: '校内考核表', icon: 'EditPen', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-post/assessment-outer', meta: { title: '校外考核表', icon: 'EditPen', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-post/report', meta: { title: '实习报告', icon: 'Document', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] } },
      { path: '/internship-post/score', meta: { title: '实习成绩管理', icon: 'TrendCharts', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] } }
    ]
  },
  {
    path: '/admin',
    meta: { title: '基础资料', icon: 'Setting', roles: ['DEPT_ADMIN', 'ADMIN'] },
    children: [
      { path: '/admin/dept', meta: { title: '院系管理', icon: 'OfficeBuilding', roles: ['DEPT_ADMIN', 'ADMIN'] } },
      { path: '/admin/semester', meta: { title: '学期管理', icon: 'Calendar', roles: ['ADMIN'] } },
      { path: '/admin/grade', meta: { title: '年级管理', icon: 'Histogram', roles: ['ADMIN'] } },
      { path: '/admin/major', meta: { title: '专业管理', icon: 'School', roles: ['DEPT_ADMIN', 'ADMIN'] } },
      { path: '/admin/class', meta: { title: '班级管理', icon: 'UserFilled', roles: ['DEPT_ADMIN', 'ADMIN'] } },
      { path: '/admin/teacher', meta: { title: '老师管理', icon: 'Avatar', roles: ['DEPT_ADMIN', 'ADMIN'] } },
      { path: '/admin/student', meta: { title: '学生管理', icon: 'User', roles: ['DEPT_ADMIN', 'ADMIN'] } },
      { path: '/admin/calendar', meta: { title: '校历管理', icon: 'Calendar', roles: ['ADMIN'] } }
    ]
  },
  {
    path: '/manual',
    meta: { title: '用户手册', icon: 'QuestionFilled', roles: ['STUDENT', 'TEACHER', 'MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] }
  }
]

/**
 * 根据当前角色过滤菜单路由
 * @returns {Array<object>} 过滤后的菜单路由数组
 */
/** 根据路径和角色动态获取菜单标题 */
function getMenuTitle(item) {
  if (item.path === '/internship/application' && userStore.roleCode !== 'STUDENT') {
    return '实习申请管理'
  }
  return item.meta?.title || ''
}

const filteredRoutes = computed(() => {
  /** 当前用户角色编码 */
  const roleCode = userStore.roleCode
  return allMenuRoutes.filter(item => {
    if (!item.meta?.roles) return false
    if (!item.meta.roles.includes(roleCode)) return false
    return true
  }).map(item => {
    if (item.children) {
      /** 过滤后的子路由列表 */
      const filteredChildren = item.children.filter(child => child.meta?.roles?.includes(roleCode))
      return { ...item, children: filteredChildren.length > 0 ? filteredChildren : null }
    }
    return item
  }).filter(item => {
    if (item.children !== undefined && item.children === null) return false
    return true
  })
})

/** 切换侧边栏折叠状态 */
function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

/**
 * 处理下拉菜单命令
 * @param {string} command - 下拉菜单命令，支持'logout'和'switch:roleId'格式
 */
async function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await import('@/utils/request.js').then(m => m.default.post('/auth/logout'))
      } catch (e) {
        console.warn('登出接口调用失败', e.message)
      }
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  } else if (command.startsWith('switch:')) {
    const roleId = Number(command.split(':')[1])
    try {
      await userStore.switchRole(roleId)
      ElMessage.success('角色切换成功')
      router.push('/home')
    } catch (e) {
      console.warn('角色切换失败', e.message)
    }
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background-color: #263445;
}

.logo-icon {
  width: 32px;
  height: 32px;
}

.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  margin-left: 10px;
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.main-container {
  flex-direction: column;
}

.header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
}

.username {
  font-size: 14px;
}

.role-tag {
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.switch-hint {
  font-size: 12px;
  color: #909399;
}

.main-content {
  background: #f0f2f5;
  overflow-y: auto;
}
</style>
