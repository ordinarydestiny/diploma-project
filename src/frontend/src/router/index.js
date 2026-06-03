import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { computed } from 'vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', requiresAuth: false }
      },
      {
        path: 'graduation/notice',
        name: 'GraduationNotice',
        component: () => import('@/views/graduation/notice.vue'),
        meta: { title: '毕设通知', icon: 'Bell' }
      },
      {
        path: 'graduation/batch',
        name: 'GraduationBatch',
        component: () => import('@/views/graduation/batch.vue'),
        meta: { title: '毕设批次', icon: 'Calendar' }
      },
      {
        path: 'graduation/topic-manage',
        name: 'TopicManage',
        component: () => import('@/views/graduation/topic-manage.vue'),
        meta: { title: '题目管理', icon: 'Document' }
      },
      {
        path: 'graduation/student-selection',
        name: 'StudentSelection',
        component: () => import('@/views/graduation/student-selection.vue'),
        meta: { title: '学生选题', icon: 'UserFilled' }
      },
      {
        path: 'graduation/task-book',
        name: 'TaskBook',
        component: () => import('@/views/graduation/task-book.vue'),
        meta: { title: '下达任务书', icon: 'EditPen' }
      },
      {
        path: 'graduation/defense',
        name: 'Defense',
        component: () => import('@/views/graduation/defense.vue'),
        meta: { title: '答辩管理', icon: 'DataAnalysis' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const menuRoutes = computed(() => {
  return []
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 顶岗实习系统` : '顶岗实习系统'
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth === false) {
    next()
    return
  }
  if (!token) {
    next('/login')
    return
  }
  if (to.meta.roles) {
    const roleCode = localStorage.getItem('roleCode')
    if (!to.meta.roles.includes(roleCode)) {
      next('/home')
      return
    }
  }
  next()
})

export { menuRoutes }
export default router
