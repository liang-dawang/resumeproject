import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../components/HomePage.vue'
import CareerPlanning from '../components/CareerPlanning.vue'
import DataCenter from '../components/DataCenter.vue'
import StudentArchive from '../components/StudentArchive.vue'
import ReportManagement from '../components/ReportManagement.vue'
import ResumeForm from '../components/ResumeForm.vue'
import OptimizedResumeView from '../components/OptimizedResumeView.vue'
import RoutePlaceholder from '../components/RoutePlaceholder.vue'
import LoginPage from '../components/LoginPage.vue'
import AdminPage from '../components/AdminPage.vue'

const placeholder = (title, description) => ({
  component: RoutePlaceholder,
  props: {
    title,
    description
  }
})

const routes = [
  // 👇 登录页（新加）
  {
    path: '/login',
    component: LoginPage
  },
  {
    path: '/admin',
    component: AdminPage
  },
  {
    path: '/',
    component: HomePage
  },
  {
    path: '/career-planning',
    component: CareerPlanning
  },
  {
    path: '/data-center',
    component: DataCenter
  },
  {
    path: '/student-profile',
    component: StudentArchive
  },
  {
    path: '/report-management',
    component: ReportManagement
  },
  {
    path: '/data-center/job-data',
    component: DataCenter
  },
  {
    path: '/data-center/job-portrait',
    component: DataCenter
  },
  {
    path: '/data-center/job-graph',
    component: DataCenter
  },
  {
    path: '/student-profile/input',
    component: ResumeForm
  },
  {
    path: '/student-profile/optimized-resume',
    component: OptimizedResumeView
  },
  {
    path: '/student-profile/ability',
    ...placeholder('学生能力画像', '基于学习与实践表现构建能力雷达，定位优势与待提升维度。')
  },
  {
    path: '/career-planning/match',
    ...placeholder('人岗匹配', '智能计算人岗匹配度并给出差距分析，支持目标岗位优先级排序。')
  },
  {
    path: '/career-planning/report',
    ...placeholder('职业规划报告', '自动整合能力评估与岗位建议，生成可执行的职业规划报告。')
  },
  {
    path: '/career-planning/path',
    ...placeholder('职业发展路径', '生成分阶段成长路线，明确学习任务、实践方向与求职节点。')
  },
  {
    path: '/career-planning/action-plan',
    ...placeholder('行动计划', '将发展目标拆解为周月行动项，帮助持续追踪与复盘优化。')
  },
  {
    path: '/report-management/edit-export',
    ...placeholder('报告编辑导出', '支持在线编辑职业报告并按需导出，便于汇报与归档管理。')
  },
  {
    path: '/report-management/history',
    ...placeholder('历史报告', '历史报告模块将展示过往报告记录并支持版本对比。')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 👇 路由守卫：未登录自动跳转到登录页（核心！）
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('authToken')
  const isAuthed = localStorage.getItem('isAuthed') === '1'

  // 如果没有登录，且不是去登录页 → 跳登录
  if (to.path !== '/login' && !token && !isAuthed) {
    next('/login')
  } 
  // 如果已经登录，访问登录页 → 跳首页
  else if (to.path === '/login' && token && isAuthed) {
    next('/')
  } 
  // 正常放行
  else {
    next()
  }
})

export default router