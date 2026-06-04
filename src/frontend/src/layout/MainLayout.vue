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
        >顶岗实习系统</span>
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
          v-for="(route, index) in filteredRoutes"
          :key="route.path"
        >
          <el-sub-menu
            v-if="route.children && route.children.length > 0"
            :index="route.path"
          >
            <template #title>
              <el-icon><component :is="route.meta.icon" /></el-icon>
              <span>{{ route.meta.title }}</span>
            </template>
            <el-menu-item
              v-for="(child, childIndex) in route.children"
              :key="childIndex"
              :index="child.path"
            >
              <el-icon><component :is="child.meta.icon" /></el-icon>
              <span>{{ child.meta.title }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item
            v-else
            :index="route.path"
          >
            <el-icon><component :is="route.meta.icon" /></el-icon>
            <span>{{ route.meta.title }}</span>
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
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              <span class="username">{{ userStore.userInfo.realName || userStore.userInfo.username || '访客' }}</span>
              <span v-if="userStore.token && userStore.roleCode" class="role-tag">{{ userStore.roleLabel }}</span>
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

/**
 *
 */
const route = useRoute()
/**
 *
 */
const router = useRouter()
/**
 *
 */
const userStore = useUserStore()
/**
 *
 */
const isCollapse = ref(false)

/**
 *
 */
const activeMenu = computed(() => {
  const path = route.path
  if (route.meta?.parentPath) {
    return route.meta.parentPath
  }
  return path
})
/**
 *
 */
const currentRoute = computed(() => route)

/**
 *
 */
const allMenuRoutes = [
  {
    path: '/home',
    meta: { title: '首页', icon: 'HomeFilled' }
  },
  {
    path: '/graduation',
    meta: { title: '毕业设计', icon: 'Reading' },
    children: [
      { path: '/graduation/notice', meta: { title: '毕设通知', icon: 'Bell' } },
      { path: '/graduation/batch', meta: { title: '毕设批次', icon: 'Calendar' } },
      { path: '/graduation/topic-management', meta: { title: '题库管理', icon: 'Document' } },
      { path: '/graduation/student-selection', meta: { title: '学生选题', icon: 'UserFilled' } },
      { path: '/graduation/task-book', meta: { title: '下达任务书', icon: 'EditPen' } },
      { path: '/graduation/defense', meta: { title: '答辩管理', icon: 'DataAnalysis' } },
      { path: '/graduation/check-in', meta: { title: '签到情况', icon: 'Clock' } },
      { path: '/graduation/midterm-check', meta: { title: '中期检查', icon: 'Check' } },
      { path: '/graduation/final-check', meta: { title: '最终检查', icon: 'Finished' } }
    ]
  }
]

/**
 *
 */
const filteredRoutes = computed(() => {
  return allMenuRoutes
})

/**
 *
 */
function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

/**
 *
 * @param command
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
    /**
     *
     */
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
