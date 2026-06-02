/**
 * 路由配置
 * 定义所有页面路由、路由守卫和菜单过滤逻辑
 * 路由守卫负责：页面标题设置、登录态校验、角色权限校验
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { computed } from 'vue'

/** 路由表定义 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    /**
     * 懒加载组件
     * @returns {Promise<object>} 组件模块
     */
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    /**
     * 懒加载组件
     * @returns {Promise<object>} 组件模块
     */
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', roles: ['STUDENT', 'TEACHER', 'MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User', roles: ['STUDENT', 'TEACHER', 'MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] }
      }
    ]
  },
  {
    path: '/internship',
    /**
     * 懒加载组件
     * @returns {Promise<object>} 组件模块
     */
    component: () => import('@/layout/MainLayout.vue'),
    meta: { title: '实习前', icon: 'Briefcase', roles: ['STUDENT', 'TEACHER', 'MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] },
    children: [
      {
        path: 'application',
        name: 'InternshipApplication',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/application.vue'),
        meta: { title: '实习申请', icon: 'Document', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'my-plan',
        name: 'MyInternshipPlan',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/my-plan.vue'),
        meta: { title: '实习计划', icon: 'List', roles: ['STUDENT'] }
      },
      {
        path: 'plan',
        name: 'InternshipPlan',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/plan.vue'),
        meta: { title: '实习计划管理', icon: 'List', roles: ['TEACHER', 'MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'plan-assign/:id',
        name: 'PlanAssign',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/plan-assign.vue'),
        meta: { title: '师生分配', roles: ['MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'record',
        name: 'InternshipRecord',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/record.vue'),
        meta: { title: '实习记录', icon: 'Folder', roles: ['STUDENT'] }
      }
    ]
  },
  {
    path: '/internship-mid',
    /**
     * 懒加载组件
     * @returns {Promise<object>} 组件模块
     */
    component: () => import('@/layout/MainLayout.vue'),
    meta: { title: '实习中', icon: 'DataBoard', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] },
    children: [
      {
        path: 'overview',
        name: 'InternshipOverview',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/record.vue'),
        meta: { title: '实习生一览表', icon: 'UserFilled', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'checkin',
        name: 'InternshipCheckin',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/checkin.vue'),
        meta: { title: '签到管理', icon: 'Location', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'my-checkin',
        name: 'MyCheckin',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/checkin.vue'),
        meta: { title: '签到', icon: 'Location', roles: ['STUDENT'] }
      },
      {
        path: 'checkin-remind',
        name: 'CheckinRemind',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/checkin-remind.vue'),
        meta: { title: '签到提醒记录', icon: 'Bell', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'my-checkin-remind',
        name: 'MyCheckinRemind',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/my-checkin-remind.vue'),
        meta: { title: '签到提醒', icon: 'Bell', roles: ['STUDENT'] }
      },
      {
        path: 'daily-report',
        name: 'DailyReport',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/daily-report.vue'),
        meta: { title: '日报', icon: 'Notebook', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'weekly-report',
        name: 'WeeklyReport',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/weekly-report.vue'),
        meta: { title: '周报', icon: 'Notebook', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'monthly-report',
        name: 'MonthlyReport',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/monthly-report.vue'),
        meta: { title: '月报', icon: 'Notebook', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'guidance-record',
        name: 'GuidanceRecord',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/guidance-record.vue'),
        meta: { title: '实习指导记录', icon: 'ChatLineSquare', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'visit-record',
        name: 'VisitRecord',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/visit-record.vue'),
        meta: { title: '实习巡访记录', icon: 'Promotion', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'student-change',
        name: 'StudentChange',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/student-change.vue'),
        meta: { title: '学生异动', icon: 'Switch', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'process-statistics',
        name: 'ProcessStatistics',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/internship/process-statistics.vue'),
        meta: { title: '流程统计', icon: 'DataAnalysis', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      }
    ]
  },
  {
    path: '/internship-post',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { title: '实习后', icon: 'Finished', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] },
    children: [
      {
        path: 'assessment-inner',
        name: 'AssessmentInner',
        component: () => import('@/views/internship/assessment-inner.vue'),
        meta: { title: '校内考核表', icon: 'EditPen', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'assessment-outer',
        name: 'AssessmentOuter',
        component: () => import('@/views/internship/assessment-outer.vue'),
        meta: { title: '校外考核表', icon: 'EditPen', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'report',
        name: 'InternshipReport',
        component: () => import('@/views/internship/report.vue'),
        meta: { title: '实习报告', icon: 'Document', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'score',
        name: 'InternshipScore',
        component: () => import('@/views/internship/score.vue'),
        meta: { title: '实习成绩管理', icon: 'TrendCharts', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      }
    ]
  },
  {
    path: '/graduation',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { title: '毕业设计', icon: 'Reading', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'], hidden: true },
    children: [
      {
        path: 'notice',
        name: 'GraduationNotice',
        component: () => import('@/views/graduation/notice.vue'),
        meta: { title: '毕业设计通知', icon: 'Bell', roles: ['STUDENT', 'TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'my-project',
        name: 'MyProject',
        component: () => import('@/views/graduation/my-project.vue'),
        meta: { title: '我的毕业设计', icon: 'UserFilled', roles: ['STUDENT'] }
      },
      {
        path: 'project-manage',
        name: 'ProjectManage',
        component: () => import('@/views/graduation/project-manage.vue'),
        meta: { title: '毕业设计管理', icon: 'Management', roles: ['TEACHER', 'DEPT_ADMIN', 'ADMIN'] }
      }
    ]
  },
  {
    path: '/admin',
    /**
     * 懒加载组件
     * @returns {Promise<object>} 组件模块
     */
    component: () => import('@/layout/MainLayout.vue'),
    meta: { title: '基础资料', icon: 'Setting', roles: ['DEPT_ADMIN', 'ADMIN'] },
    children: [
      {
        path: 'dept',
        name: 'DeptManage',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/admin/dept.vue'),
        meta: { title: '院系管理', icon: 'OfficeBuilding', roles: ['DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'semester',
        name: 'SemesterManage',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/admin/semester.vue'),
        meta: { title: '学期管理', icon: 'Calendar', roles: ['ADMIN'] }
      },
      {
        path: 'grade',
        name: 'GradeManage',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/admin/grade.vue'),
        meta: { title: '年级管理', icon: 'Histogram', roles: ['ADMIN'] }
      },
      {
        path: 'major',
        name: 'MajorManage',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/admin/major.vue'),
        meta: { title: '专业管理', icon: 'School', roles: ['DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'class',
        name: 'ClassManage',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/admin/class.vue'),
        meta: { title: '班级管理', icon: 'UserFilled', roles: ['DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'teacher',
        name: 'TeacherManage',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/admin/teacher.vue'),
        meta: { title: '老师管理', icon: 'Avatar', roles: ['DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'student',
        name: 'StudentManage',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/admin/student.vue'),
        meta: { title: '学生管理', icon: 'User', roles: ['DEPT_ADMIN', 'ADMIN'] }
      },
      {
        path: 'calendar',
        name: 'SchoolCalendar',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/admin/calendar.vue'),
        meta: { title: '校历管理', icon: 'Calendar', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/manual',
    /**
     * 懒加载组件
     * @returns {Promise<object>} 组件模块
     */
    component: () => import('@/layout/MainLayout.vue'),
    meta: { title: '用户手册', icon: 'QuestionFilled', roles: ['STUDENT', 'TEACHER', 'MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] },
    children: [
      {
        path: '',
        name: 'UserManual',
        /**
         * 懒加载组件
         * @returns {Promise<object>} 组件模块
         */
        component: () => import('@/views/manual/index.vue'),
        meta: { title: '用户手册', icon: 'QuestionFilled', roles: ['STUDENT', 'TEACHER', 'MAJOR_DIRECTOR', 'DEPT_ADMIN', 'ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    /**
     * 懒加载组件
     * @returns {Promise<object>} 组件模块
     */
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', requiresAuth: false }
  }
]

/** Vue Router实例 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 根据当前用户角色过滤菜单路由
 * 仅返回当前角色有权访问的路由项
 * @returns {Array<object>} 过滤后的菜单路由数组
 */
const menuRoutes = computed(() => {
  const userStore = useUserStore()
  const roleCode = userStore.roleCode
  return routes.filter(route => {
    if (!route.meta?.roles) return false
    if (route.meta.hidden) return false
    return route.meta.roles.includes(roleCode)
  })
})

/**
 * 全局路由守卫
 * 1. 设置页面标题
 * 2. 不需要认证的页面直接放行
 * 3. 未登录用户重定向到登录页
 * 4. 角色权限校验：无权限则重定向到首页
 * @param {import('vue-router').RouteLocationNormalized} to - 目标路由
 * @param {import('vue-router').RouteLocationNormalized} from - 来源路由
 * @param {import('vue-router').NavigationGuardNext} next - 路由放行函数
 */
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 实习与毕设管理系统` : '实习与毕设管理系统'

  /** 从localStorage获取Token */
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
    /** 从localStorage获取角色编码 */
    const roleCode = localStorage.getItem('roleCode')
    if (!to.meta.roles.includes(roleCode)) {
      next('/home')
      return
    }
  }

  next()
})

/** 导出菜单路由计算属性 */
export { menuRoutes }
/** 导出Vue Router实例 */
export default router
