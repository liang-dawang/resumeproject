<script setup>
import axios from 'axios'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const API_BASE = 'http://127.0.0.1:8000'
const USERS_STORAGE_KEY = 'app_users_v1'
const REPORT_HISTORY_API = `${API_BASE}/career/generated-development-reports`

const router = useRouter()

const currentTime = ref('')
const userMenuOpen = ref(false)
const activeMenu = ref('dashboard')
const expandedMenus = ref(['dashboard'])

const adminName = ref('系统管理员')
const adminAccount = ref('admin')

const dashboardLoading = ref(false)
const dashboardError = ref('')
const storedUsers = ref([])
const storedArchives = ref([])
const reportHistoryItems = ref([])
const reportHistoryTotal = ref(0)

const fallbackRecentUsers = [
  { name: '林晓晨', time: '2026-05-29 09:12', major: '软件工程' },
  { name: '周宇航', time: '2026-05-29 08:47', major: '计算机科学' },
  { name: '陈思琪', time: '2026-05-28 22:15', major: '信息管理' },
  { name: '黄子轩', time: '2026-05-28 19:03', major: '数据科学' }
]

const fallbackRecentConsults = [
  { user: '王五', topic: 'Java 后端职业规划', time: '10:32' },
  { user: '李四', topic: '前端转全栈路径咨询', time: '10:18' },
  { user: '张三', topic: '测试开发岗位匹配', time: '09:55' },
  { user: '赵六', topic: '实习经历优化建议', time: '09:41' }
]

const fallbackAnnouncements = [
  { title: 'v2.3 版本已上线', desc: '新增 AI 对话话术模板与报告导出优化', time: '05-28' },
  { title: '系统维护通知', desc: '本周六 02:00-04:00 进行数据库例行维护', time: '05-27' },
  { title: '知识库更新', desc: '新增 2026 春招岗位画像数据 320 条', time: '05-25' }
]

const fallbackMajorDistribution = [
  { name: '计算机类', percent: 38, color: '#00f2ff' },
  { name: '软件工程', percent: 24, color: '#3f8eff' },
  { name: '信息管理', percent: 18, color: '#7c5cff' },
  { name: '其他专业', percent: 20, color: '#d600ff' }
]

const fallbackJobIntentDistribution = [
  { name: 'Java 开发', value: 2860 },
  { name: '前端开发', value: 2140 },
  { name: '测试开发', value: 1520 },
  { name: '数据分析', value: 980 },
  { name: '产品经理', value: 640 }
]

const fallbackAgeDistribution = [
  { range: '18-20', percent: 22 },
  { range: '21-22', percent: 45 },
  { range: '23-24', percent: 28 },
  { range: '25+', percent: 5 }
]

const fallbackFeedbackList = [
  { user: '用户 A', content: '希望增加更多行业岗位画像', status: '待处理' },
  { user: '用户 B', content: '报告导出格式建议支持 PDF', status: '处理中' },
  { user: '用户 C', content: 'AI 回复偶尔偏慢', status: '已回复' }
]

const safeParseJson = (value, fallback = null) => {
  try {
    return value ? JSON.parse(value) : fallback
  } catch {
    return fallback
  }
}

const readStoredUsers = () => {
  if (typeof window === 'undefined') return []
  return safeParseJson(window.localStorage.getItem(USERS_STORAGE_KEY), [])
}

const readStoredArchives = () => {
  if (typeof window === 'undefined') return []
  const records = []
  for (let i = 0; i < window.localStorage.length; i += 1) {
    const key = window.localStorage.key(i)
    if (!key || !key.startsWith('student-archive:')) continue
    const studentId = key.slice('student-archive:'.length)
    const raw = window.localStorage.getItem(key)
    const record = safeParseJson(raw, null)
    if (!record) continue
    const profile = record.profile || {}
    records.push({
      studentId,
      name: profile.name || profile.studentName || `学生${studentId}`,
      major: profile.major || '未填写专业',
      targetJob: profile.targetJob || '未填写意向岗位',
      avatar: record.avatar || '',
      profileComplete: Number(profile.profileComplete || profile.profile_complete || 0) || 0,
      overallScore: Number(profile.overallScore || profile.overall_score || 0) || 0,
      updatedAt: Number(studentId) || 0
    })
  }
  return records.sort((a, b) => b.updatedAt - a.updatedAt)
}

const normalizeMajorCategory = (major) => {
  const text = String(major || '').trim()
  if (!text) return '其他专业'
  if (/计算机|软件|网络|信息|数据|人工智能|电子|通信/.test(text)) return '计算机类'
  if (/软件工程/.test(text)) return '软件工程'
  if (/信息管理/.test(text)) return '信息管理'
  return '其他专业'
}

const normalizeJobCategory = (job) => {
  const text = String(job || '').trim()
  if (!text) return '未填写'
  if (/Java/.test(text)) return 'Java 开发'
  if (/前端|Vue|React/.test(text)) return '前端开发'
  if (/测试|QA/.test(text)) return '测试开发'
  if (/数据/.test(text)) return '数据分析'
  if (/产品/.test(text)) return '产品经理'
  return text.split('（')[0]
}

const refreshDashboardData = async () => {
  dashboardLoading.value = true
  dashboardError.value = ''
  storedUsers.value = readStoredUsers()
  storedArchives.value = readStoredArchives()
  try {
    const { data } = await axios.get(REPORT_HISTORY_API, {
      params: { studentKey: 'latest' }
    })
    if (data?.code === 200 && data?.data?.items) {
      reportHistoryItems.value = data.data.items
      reportHistoryTotal.value = data.meta?.total ?? data.data.items.length
    } else {
      reportHistoryItems.value = []
      reportHistoryTotal.value = 0
    }
  } catch (error) {
    console.warn('加载报告历史失败，使用本地/空数据兜底', error)
    reportHistoryItems.value = []
    reportHistoryTotal.value = 0
  } finally {
    dashboardLoading.value = false
  }
}

const menuItems = [
  {
    key: 'dashboard',
    label: '首页控制台',
    icon: '🏠',
    children: []
  },
  {
    key: 'users',
    label: '用户管理',
    icon: '👥',
    children: [
      { key: 'users-all', label: '全部用户' },
      { key: 'users-audit', label: '账号审核' },
      { key: 'users-group', label: '用户分组' },
      { key: 'users-risk', label: '异常账号管控' }
    ]
  },
  {
    key: 'stats',
    label: '数据统计',
    icon: '📊',
    children: [
      { key: 'stats-visit', label: '访问数据' },
      { key: 'stats-duration', label: '使用时长' },
      { key: 'stats-report', label: '规划报告统计' },
      { key: 'stats-ai', label: 'AI 咨询量' }
    ]
  },
  {
    key: 'content',
    label: '内容管理',
    icon: '📚',
    children: [
      { key: 'content-kb', label: '职业知识库' },
      { key: 'content-quiz', label: '测评题库' },
      { key: 'content-template', label: '规划方案模板' },
      { key: 'content-news', label: '资讯公告' }
    ]
  },
  {
    key: 'agent',
    label: '智能体配置',
    icon: '🤖',
    children: [
      { key: 'agent-model', label: '模型参数设置' },
      { key: 'agent-script', label: '对话话术管理' },
      { key: 'agent-rule', label: '规则阈值配置' }
    ]
  },
  {
    key: 'system',
    label: '系统设置',
    icon: '⚙️',
    children: [
      { key: 'system-perm', label: '账号权限' },
      { key: 'system-log', label: '日志查询' },
      { key: 'system-base', label: '基础配置' }
    ]
  }
]

const totalUsers = computed(() => storedUsers.value.length)
const adminUsers = computed(() => storedUsers.value.filter((item) => String(item?.role || 'user') === 'admin').length)
const studentUsers = computed(() => storedUsers.value.filter((item) => String(item?.role || 'user') !== 'admin').length)
const archiveUsers = computed(() => storedArchives.value.length)

const statCards = computed(() => [
  {
    key: 'users',
    title: '总注册用户',
    value: String(totalUsers.value || 0),
    sub: `其中用户 ${studentUsers.value} 人，管理员 ${adminUsers.value} 人`,
    trend: '来自登录页',
    trendUp: true,
    icon: '👤',
    bars: [42, 55, 48, 62, 58, 71, 68]
  },
  {
    key: 'archive',
    title: '已建档学生',
    value: String(archiveUsers.value || 0),
    sub: '来自学生档案页 localStorage 缓存',
    trend: '与用户端同步',
    trendUp: true,
    icon: '📋',
    bars: [38, 45, 52, 49, 61, 58, 72]
  },
  {
    key: 'report',
    title: '职业报告生成数',
    value: String(reportHistoryTotal.value || 0),
    sub: '来自报告管理页历史记录接口',
    trend: '后端接口',
    trendUp: true,
    icon: '📄',
    bars: [30, 36, 34, 40, 38, 44, 46]
  },
  {
    key: 'active',
    title: '有效意向岗位数',
    value: String(jobIntentDistribution.value.length || 0),
    sub: '由学生档案中的意向岗位聚合得出',
    trend: '档案统计',
    trendUp: true,
    icon: '⚡',
    bars: [55, 52, 58, 54, 60, 57, 56]
  }
])

const quickActions = [
  { key: 'audit', label: '用户新增审核', icon: '✅', target: 'users-audit' },
  { key: 'kb', label: '知识库更新', icon: '📖', target: 'content-kb' },
  { key: 'log', label: '查看异常日志', icon: '🔍', target: 'system-log' },
  { key: 'notice', label: '发布平台公告', icon: '📢', target: 'content-news' }
]

const recentUsers = computed(() => {
  const archiveRows = storedArchives.value.slice(0, 4).map((item, index) => ({
    name: item.name,
    time: `档案 ${String(item.studentId || index + 1).padStart(3, '0')}`,
    major: item.major
  }))
  if (archiveRows.length) return archiveRows
  const userRows = storedUsers.value
    .filter((item) => String(item?.role || 'user') !== 'admin')
    .slice(0, 4)
    .map((item, index) => ({
      name: item.username || `用户${index + 1}`,
      time: '登录账号',
      major: '待补充档案'
    }))
  return userRows.length ? userRows : fallbackRecentUsers
})

const recentConsults = computed(() => {
  const items = reportHistoryItems.value.slice(0, 4).map((item, index) => ({
    user: item.studentName || item.name || `学生${index + 1}`,
    topic: item.targetJob || item.jobTitle || '职业规划报告',
    time: item.createdAt || item.created_at || item.updatedAt || '后端记录'
  }))
  return items.length ? items : fallbackRecentConsults
})

const todos = computed(() => [
  { label: '待审核账号', count: Math.max(0, studentUsers.value - archiveUsers.value), level: 'high' },
  { label: '待完善档案', count: Math.max(0, studentUsers.value - archiveUsers.value), level: 'medium' }
])

const announcements = computed(() => fallbackAnnouncements)

const majorDistribution = computed(() => {
  const archives = storedArchives.value
  if (!archives.length) return fallbackMajorDistribution
  const counts = archives.reduce((acc, item) => {
    const key = normalizeMajorCategory(item.major)
    acc[key] = (acc[key] || 0) + 1
    return acc
  }, {})
  const total = Object.values(counts).reduce((sum, value) => sum + value, 0) || 1
  return Object.entries(counts)
    .sort((a, b) => b[1] - a[1])
    .map(([name, count], index) => ({
      name,
      percent: Math.round((count / total) * 100),
      color: ['#00f2ff', '#3f8eff', '#7c5cff', '#d600ff'][index % 4]
    }))
})

const jobIntentDistribution = computed(() => {
  const archives = storedArchives.value
  if (!archives.length) return fallbackJobIntentDistribution
  const counts = archives.reduce((acc, item) => {
    const key = normalizeJobCategory(item.targetJob)
    acc[key] = (acc[key] || 0) + 1
    return acc
  }, {})
  return Object.entries(counts)
    .sort((a, b) => b[1] - a[1])
    .map(([name, count]) => ({ name, value: count }))
})

const ageDistribution = computed(() => fallbackAgeDistribution)

const aiStatus = {
  online: true,
  responseMs: 420,
  load: 36,
  uptime: '99.97%'
}

const feedbackList = computed(() => fallbackFeedbackList)

const menuLabelMap = computed(() => {
  const map = { dashboard: '首页控制台' }
  menuItems.forEach((item) => {
    item.children.forEach((child) => {
      map[child.key] = `${item.label} / ${child.label}`
    })
  })
  return map
})

const activeViewTitle = computed(() => menuLabelMap.value[activeMenu.value] || '首页控制台')
const isDashboard = computed(() => activeMenu.value === 'dashboard')
const isUsersAll = computed(() => activeMenu.value === 'users-all')
const maxJobIntent = computed(() => Math.max(...jobIntentDistribution.value.map((item) => item.value), 1))

const userRows = computed(() => {
  const baseUsers = storedUsers.value.length ? storedUsers.value : readStoredUsers()
  return baseUsers.map((item, index) => ({
    index: index + 1,
    username: item.username || `用户${index + 1}`,
    role: String(item.role || 'user'),
    source: item.role === 'admin' ? '管理员账号' : '登录页账号'
  }))
})

const archiveRows = computed(() => {
  const archives = storedArchives.value
  if (archives.length) {
    return archives.map((item, index) => ({
      index: index + 1,
      name: item.name,
      major: item.major,
      targetJob: item.targetJob,
      profileComplete: item.profileComplete,
      overallScore: item.overallScore
    }))
  }
  return []
})

let timeTimer = null

const formatTime = () => {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

const toggleMenu = (key) => {
  if (expandedMenus.value.includes(key)) {
    expandedMenus.value = expandedMenus.value.filter((item) => item !== key)
  } else {
    expandedMenus.value = [...expandedMenus.value, key]
  }
}

const selectMenu = (key, parentKey) => {
  activeMenu.value = key
  if (parentKey && !expandedMenus.value.includes(parentKey)) {
    expandedMenus.value = [...expandedMenus.value, parentKey]
  }
}

const goQuickAction = (target) => {
  selectMenu(target, target.split('-')[0])
}

const toggleUserMenu = () => {
  userMenuOpen.value = !userMenuOpen.value
}

const closeUserMenu = () => {
  userMenuOpen.value = false
}

const logout = () => {
  try {
    localStorage.removeItem('authToken')
    localStorage.removeItem('isAuthed')
    localStorage.removeItem('currentUser')
    localStorage.removeItem('userRole')
  } catch (error) {
    console.warn('退出登录失败:', error)
  }
  router.push('/login')
}

const handleStorageChange = () => {
  storedUsers.value = readStoredUsers()
  storedArchives.value = readStoredArchives()
}

onMounted(() => {
  try {
    adminAccount.value = localStorage.getItem('currentUser') || 'admin'
    adminName.value = adminAccount.value === 'admin' ? '系统管理员' : adminAccount.value
  } catch {
    adminAccount.value = 'admin'
  }
  formatTime()
  timeTimer = setInterval(formatTime, 1000)
  document.addEventListener('click', closeUserMenu)
  window.addEventListener('storage', handleStorageChange)
  refreshDashboardData()
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
  document.removeEventListener('click', closeUserMenu)
  window.removeEventListener('storage', handleStorageChange)
})
</script>

<template>
  <div class="admin-page">
    <header class="admin-topbar card-surface">
      <div class="topbar-left">
        <img class="brand-logo" src="/logo.png" alt="职绘智配" />
        <div class="brand-text">
          <strong>职绘智配</strong>
          <span>管理后台</span>
        </div>
      </div>

      <div class="topbar-right">
        <div class="admin-meta">
          <span class="meta-item">
            <i class="meta-icon">👤</i>
            {{ adminAccount }} · {{ adminName }}
          </span>
          <span class="meta-item">
            <i class="meta-icon">🕐</i>
            {{ currentTime }}
          </span>
        </div>
        <div class="user-menu-wrap" @click.stop>
          <button class="user-menu-btn" type="button" @click="toggleUserMenu">
            <span>管理员</span>
            <span class="caret" :class="{ open: userMenuOpen }">▾</span>
          </button>
          <div v-if="userMenuOpen" class="user-dropdown card-surface">
            <button type="button" class="dropdown-item">个人中心</button>
            <button type="button" class="dropdown-item">修改密码</button>
            <button type="button" class="dropdown-item danger" @click="logout">退出登录</button>
          </div>
        </div>
      </div>
    </header>

    <div class="admin-layout">
      <aside class="admin-sidebar card-surface">
        <nav class="side-nav">
          <div v-for="item in menuItems" :key="item.key" class="side-group">
            <button
              class="side-item"
              :class="{ active: activeMenu === item.key || item.children.some((c) => c.key === activeMenu) }"
              type="button"
              @click="item.children.length ? toggleMenu(item.key) : selectMenu(item.key)"
            >
              <span class="side-icon">{{ item.icon }}</span>
              <span class="side-label">{{ item.label }}</span>
              <span v-if="item.children.length" class="side-arrow" :class="{ open: expandedMenus.includes(item.key) }">▸</span>
            </button>
            <div v-if="item.children.length && expandedMenus.includes(item.key)" class="side-sub">
              <button
                v-for="child in item.children"
                :key="child.key"
                class="side-sub-item"
                :class="{ active: activeMenu === child.key }"
                type="button"
                @click="selectMenu(child.key, item.key)"
              >
                {{ child.label }}
              </button>
            </div>
          </div>
        </nav>
      </aside>

      <main class="admin-main">
        <div class="main-header">
          <h1>{{ activeViewTitle }}</h1>
          <p v-if="isDashboard" class="main-subtitle">实时监控平台核心指标与运营动态</p>
          <p v-else class="main-subtitle">功能模块演示页，可在此扩展具体业务操作</p>
        </div>

        <template v-if="isDashboard">
          <div v-if="dashboardLoading" class="state-box" style="margin-bottom: 16px;">
            <p class="state-text">正在同步用户端数据...</p>
          </div>
          <div v-else-if="dashboardError" class="state-box" style="margin-bottom: 16px;">
            <p class="state-text error">{{ dashboardError }}</p>
          </div>

          <section class="stat-grid">
            <article v-for="card in statCards" :key="card.key" class="stat-card card-surface">
              <div class="stat-head">
                <span class="stat-icon">{{ card.icon }}</span>
                <span class="stat-title">{{ card.title }}</span>
              </div>
              <strong class="stat-value">{{ card.value }}</strong>
              <p class="stat-sub">{{ card.sub }}</p>
              <div class="stat-foot">
                <span class="stat-trend" :class="{ up: card.trendUp, down: !card.trendUp }">
                  {{ card.trendUp ? '↑' : '↓' }} {{ card.trend }}
                </span>
                <div class="mini-bars" aria-hidden="true">
                  <i
                    v-for="(h, idx) in card.bars"
                    :key="idx"
                    :style="{ height: `${h}%` }"
                  ></i>
                </div>
              </div>
            </article>
          </section>

          <section class="quick-grid">
            <button
              v-for="action in quickActions"
              :key="action.key"
              class="quick-btn card-surface"
              type="button"
              @click="goQuickAction(action.target)"
            >
              <span class="quick-icon">{{ action.icon }}</span>
              <span>{{ action.label }}</span>
            </button>
          </section>

          <section class="detail-grid">
            <article class="panel card-surface">
              <h3>最近动态</h3>
              <div class="panel-block">
                <h4>最新注册用户</h4>
                <ul class="data-list">
                  <li v-for="user in recentUsers" :key="user.name + user.time" class="data-row">
                    <div>
                      <strong>{{ user.name }}</strong>
                      <span>{{ user.major }} · {{ user.time }}</span>
                    </div>
                    <button class="link-btn" type="button" @click="selectMenu('users-all', 'users')">查看详情</button>
                  </li>
                </ul>
              </div>
              <div class="panel-block">
                <h4>最近规划记录</h4>
                <ul class="data-list">
                  <li v-for="item in recentConsults" :key="item.user + item.time" class="data-row">
                    <div>
                      <strong>{{ item.user }}</strong>
                      <span>{{ item.topic }} · {{ item.time }}</span>
                    </div>
                  </li>
                </ul>
              </div>
            </article>

            <article class="panel card-surface">
              <h3>待办提醒 & 系统公告</h3>
              <div class="panel-block">
                <h4>待办事项</h4>
                <ul class="todo-list">
                  <li v-for="todo in todos" :key="todo.label" class="todo-item">
                    <span>{{ todo.label }}</span>
                    <span class="todo-badge" :class="todo.level">{{ todo.count }}</span>
                  </li>
                </ul>
              </div>
              <div class="panel-block">
                <h4>平台公告</h4>
                <ul class="notice-list">
                  <li v-for="notice in announcements" :key="notice.title" class="notice-item">
                    <strong>{{ notice.title }}</strong>
                    <p>{{ notice.desc }}</p>
                    <span class="notice-time">{{ notice.time }}</span>
                  </li>
                </ul>
              </div>
            </article>
          </section>

          <section class="extra-grid">
            <article class="panel card-surface">
              <h3>用户画像分析</h3>
              <div class="chart-row">
                <div class="chart-block">
                  <h4>专业分布</h4>
                  <div class="pie-chart">
                    <svg viewBox="0 0 120 120" class="pie-svg">
                      <circle cx="60" cy="60" r="48" fill="none" stroke="rgba(255,255,255,0.08)" stroke-width="18" />
                      <circle
                        cx="60" cy="60" r="48" fill="none"
                        stroke="#00f2ff" stroke-width="18"
                        stroke-dasharray="115 187"
                        stroke-dashoffset="0"
                        transform="rotate(-90 60 60)"
                      />
                      <circle
                        cx="60" cy="60" r="48" fill="none"
                        stroke="#3f8eff" stroke-width="18"
                        stroke-dasharray="72 230"
                        stroke-dashoffset="-115"
                        transform="rotate(-90 60 60)"
                      />
                      <circle
                        cx="60" cy="60" r="48" fill="none"
                        stroke="#7c5cff" stroke-width="18"
                        stroke-dasharray="54 248"
                        stroke-dashoffset="-187"
                        transform="rotate(-90 60 60)"
                      />
                      <circle
                        cx="60" cy="60" r="48" fill="none"
                        stroke="#d600ff" stroke-width="18"
                        stroke-dasharray="61 241"
                        stroke-dashoffset="-241"
                        transform="rotate(-90 60 60)"
                      />
                    </svg>
                    <ul class="legend">
                      <li v-for="item in majorDistribution" :key="item.name">
                        <i :style="{ background: item.color }"></i>{{ item.name }} {{ item.percent }}%
                      </li>
                    </ul>
                  </div>
                </div>
                <div class="chart-block">
                  <h4>意向职业 TOP5</h4>
                  <div class="bar-chart">
                    <div v-for="item in jobIntentDistribution" :key="item.name" class="bar-row">
                      <span class="bar-label">{{ item.name }}</span>
                      <div class="bar-track">
                        <i :style="{ width: `${(item.value / maxJobIntent) * 100}%` }"></i>
                      </div>
                      <span class="bar-value">{{ item.value }}</span>
                    </div>
                  </div>
                </div>
                <div class="chart-block">
                  <h4>年龄段分布</h4>
                  <div class="age-bars">
                    <div v-for="item in ageDistribution" :key="item.range" class="age-col">
                      <div class="age-bar-wrap">
                        <i :style="{ height: `${item.percent * 2}px` }"></i>
                      </div>
                      <span>{{ item.range }}</span>
                      <em>{{ item.percent }}%</em>
                    </div>
                  </div>
                </div>
              </div>
            </article>

            <article class="panel card-surface">
              <h3>AI 模型运行状态</h3>
              <div class="ai-status-grid">
                <div class="ai-status-item">
                  <span>在线状态</span>
                  <strong :class="{ online: aiStatus.online }">{{ aiStatus.online ? '运行中' : '离线' }}</strong>
                </div>
                <div class="ai-status-item">
                  <span>平均响应</span>
                  <strong>{{ aiStatus.responseMs }} ms</strong>
                </div>
                <div class="ai-status-item">
                  <span>当前负载</span>
                  <strong>{{ aiStatus.load }}%</strong>
                  <div class="load-track"><i :style="{ width: `${aiStatus.load}%` }"></i></div>
                </div>
                <div class="ai-status-item">
                  <span>服务可用率</span>
                  <strong>{{ aiStatus.uptime }}</strong>
                </div>
              </div>
              <h4 class="sub-heading">反馈管理</h4>
              <ul class="feedback-list">
                <li v-for="(item, idx) in feedbackList" :key="idx" class="feedback-item">
                  <div>
                    <strong>{{ item.user }}</strong>
                    <p>{{ item.content }}</p>
                  </div>
                  <span class="feedback-status" :class="item.status">{{ item.status }}</span>
                </li>
              </ul>
            </article>
          </section>
        </template>

        <template v-else-if="isUsersAll">
          <section class="panel card-surface">
            <h3>全部用户</h3>
            <p class="main-subtitle">数据来源：登录页的 app_users_v1，以及学生档案页的 student-archive:* 本地缓存</p>

            <div class="panel-block">
              <h4>账号概览</h4>
              <ul class="todo-list">
                <li class="todo-item">
                  <span>账号总数</span>
                  <span class="todo-badge high">{{ totalUsers }}</span>
                </li>
                <li class="todo-item">
                  <span>学生账号</span>
                  <span class="todo-badge medium">{{ studentUsers }}</span>
                </li>
                <li class="todo-item">
                  <span>管理员账号</span>
                  <span class="todo-badge medium">{{ adminUsers }}</span>
                </li>
                <li class="todo-item">
                  <span>已建档人数</span>
                  <span class="todo-badge high">{{ archiveUsers }}</span>
                </li>
              </ul>
            </div>

            <div class="panel-block">
              <h4>账号列表</h4>
              <ul class="data-list">
                <li v-for="user in userRows" :key="user.username" class="data-row">
                  <div>
                    <strong>{{ user.username }}</strong>
                    <span>{{ user.role }} · {{ user.source }}</span>
                  </div>
                </li>
              </ul>
            </div>

            <div class="panel-block">
              <h4>学生档案</h4>
              <ul class="data-list">
                <li v-for="item in archiveRows" :key="item.name + item.index" class="data-row">
                  <div>
                    <strong>{{ item.name }}</strong>
                    <span>{{ item.major }}</span>
                  </div>
                  <div style="text-align: right;">
                    <strong>{{ item.targetJob }}</strong>
                    <span>完整度 {{ item.profileComplete }} · 竞争力 {{ item.overallScore }}</span>
                  </div>
                </li>
              </ul>
            </div>
          </section>
        </template>

        <section v-else class="placeholder-panel card-surface">
          <div class="placeholder-icon">🛠️</div>
          <h2>{{ activeViewTitle }}</h2>
          <p>该功能模块为管理端演示入口，后续可在此接入真实 API 与业务表单。</p>
          <button class="btn" type="button" @click="selectMenu('dashboard')">返回首页控制台</button>
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-page {
  --sidebar-width: 240px;
  --topbar-height: 64px;
  min-height: 100vh;
  color: var(--cyber-text-main, #e8f8ff);
  background:
    radial-gradient(ellipse 120% 80% at 18% 40%, rgba(0, 242, 255, 0.14) 0%, transparent 55%),
    radial-gradient(ellipse 100% 90% at 88% 62%, rgba(255, 0, 255, 0.12) 0%, transparent 52%),
    radial-gradient(circle at 50% 120%, rgba(214, 0, 255, 0.1) 0%, transparent 45%),
    linear-gradient(150deg, rgba(9, 16, 38, 0.96) 0%, rgba(11, 16, 32, 0.9) 50%, rgba(18, 12, 36, 0.92) 100%);
  position: relative;
}

.admin-page::before,
.admin-page::after {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
}

.admin-page::before {
  background-image:
    linear-gradient(rgba(0, 242, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 0, 255, 0.06) 1px, transparent 1px);
  background-size: 44px 44px;
  opacity: 0.36;
}

.admin-page::after {
  background:
    linear-gradient(115deg, transparent 30%, rgba(0, 242, 255, 0.16) 46%, transparent 62%),
    linear-gradient(295deg, transparent 36%, rgba(255, 0, 255, 0.1) 50%, transparent 64%);
  opacity: 0.44;
}

.admin-topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  height: var(--topbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-radius: 0;
  border-left: none;
  border-right: none;
  border-top: none;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 42px;
  height: 42px;
  object-fit: contain;
  filter: drop-shadow(0 0 10px rgba(0, 242, 255, 0.45));
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.brand-text strong {
  font-size: 18px;
  color: #f2fdff;
  text-shadow: 0 0 12px rgba(0, 242, 255, 0.4);
}

.brand-text span {
  font-size: 12px;
  color: rgba(184, 233, 255, 0.85);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-meta {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(232, 248, 255, 0.9);
}

.meta-icon {
  font-style: normal;
}

.user-menu-wrap {
  position: relative;
}

.user-menu-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid rgba(0, 242, 255, 0.35);
  border-radius: 999px;
  background: rgba(6, 18, 48, 0.65);
  color: #b8f8ff;
  font-size: 14px;
  font-weight: 600;
  padding: 8px 14px;
  cursor: pointer;
}

.user-menu-btn:hover,
.caret.open {
  color: #fff;
  border-color: rgba(255, 0, 255, 0.5);
}

.caret {
  transition: transform 0.2s ease;
}

.caret.open {
  transform: rotate(180deg);
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 140px;
  padding: 8px;
  border-radius: 12px;
  z-index: 40;
}

.dropdown-item {
  display: block;
  width: 100%;
  text-align: left;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #e8f8ff;
  font-size: 14px;
  padding: 10px 12px;
  cursor: pointer;
}

.dropdown-item:hover {
  background: rgba(214, 0, 255, 0.18);
  color: #fff;
}

.dropdown-item.danger {
  color: #fca5a5;
}

.dropdown-item.danger:hover {
  background: rgba(220, 38, 38, 0.2);
  color: #fecaca;
}

.admin-layout {
  display: flex;
  padding-top: var(--topbar-height);
  min-height: 100vh;
}

.admin-sidebar {
  position: fixed;
  top: var(--topbar-height);
  left: 0;
  bottom: 0;
  width: var(--sidebar-width);
  border-radius: 0;
  border-left: none;
  border-bottom: none;
  overflow-y: auto;
  padding: 16px 12px;
  z-index: 20;
}

.side-nav {
  display: grid;
  gap: 6px;
}

.side-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid transparent;
  border-radius: 10px;
  background: transparent;
  color: #b8f8ff;
  font-size: 14px;
  font-weight: 600;
  padding: 10px 12px;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s ease;
}

.side-item:hover,
.side-item.active {
  color: #fff;
  background: rgba(214, 0, 255, 0.18);
  border-color: rgba(0, 242, 255, 0.35);
  box-shadow: 0 0 14px rgba(0, 242, 255, 0.12);
}

.side-icon {
  width: 22px;
  text-align: center;
}

.side-label {
  flex: 1;
}

.side-arrow {
  font-size: 12px;
  transition: transform 0.2s ease;
}

.side-arrow.open {
  transform: rotate(90deg);
}

.side-sub {
  display: grid;
  gap: 4px;
  padding: 4px 0 8px 34px;
}

.side-sub-item {
  border: none;
  border-radius: 8px;
  background: transparent;
  color: rgba(184, 248, 255, 0.82);
  font-size: 13px;
  text-align: left;
  padding: 8px 10px;
  cursor: pointer;
}

.side-sub-item:hover,
.side-sub-item.active {
  color: #fff;
  background: rgba(56, 121, 201, 0.28);
}

.admin-main {
  flex: 1;
  margin-left: var(--sidebar-width);
  width: calc(100% - var(--sidebar-width));
  padding: 24px 28px 32px;
  min-width: 0;
}

.main-header {
  margin-bottom: 20px;
}

.main-header h1 {
  margin: 0;
  font-size: 24px;
  color: #fff;
}

.main-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: rgba(184, 233, 255, 0.85);
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.stat-card {
  padding: 16px 18px;
}

.stat-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.stat-icon {
  font-size: 20px;
}

.stat-title {
  font-size: 14px;
  color: rgba(202, 239, 255, 0.9);
}

.stat-value {
  display: block;
  font-size: 28px;
  line-height: 1.1;
  color: #fff;
  margin-bottom: 6px;
}

.stat-sub {
  margin: 0 0 12px;
  font-size: 12px;
  color: rgba(184, 233, 255, 0.78);
}

.stat-foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
}

.stat-trend {
  font-size: 12px;
  font-weight: 700;
}

.stat-trend.up {
  color: #4ade80;
}

.stat-trend.down {
  color: #f87171;
}

.mini-bars {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 32px;
}

.mini-bars i {
  display: block;
  width: 5px;
  border-radius: 3px 3px 0 0;
  background: linear-gradient(180deg, #00f2ff, #3f8eff);
  min-height: 4px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.quick-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 88px;
  border: 1px solid rgba(0, 242, 255, 0.28);
  border-radius: 16px;
  background: rgba(8, 27, 48, 0.55);
  color: #e8f8ff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-btn:hover {
  transform: translateY(-2px);
  border-color: rgba(255, 0, 255, 0.45);
  box-shadow: 0 0 18px rgba(0, 242, 255, 0.18);
}

.quick-icon {
  font-size: 24px;
}

.detail-grid,
.extra-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 18px;
}

.panel {
  padding: 18px 20px;
}

.panel h3 {
  margin: 0 0 14px;
  font-size: 18px;
  color: #fff;
}

.panel-block + .panel-block {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(106, 210, 255, 0.18);
}

.panel-block h4,
.sub-heading {
  margin: 0 0 10px;
  font-size: 14px;
  color: rgba(202, 239, 255, 0.92);
}

.data-list,
.todo-list,
.notice-list,
.feedback-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 10px;
}

.data-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(8, 27, 48, 0.45);
  border: 1px solid rgba(106, 210, 255, 0.2);
}

.data-row strong {
  display: block;
  font-size: 14px;
  color: #fff;
}

.data-row span {
  font-size: 12px;
  color: rgba(184, 233, 255, 0.78);
}

.link-btn {
  border: none;
  background: transparent;
  color: #00f2ff;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
}

.link-btn:hover {
  color: #fff;
  text-decoration: underline;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(8, 27, 48, 0.45);
  border: 1px solid rgba(106, 210, 255, 0.2);
  font-size: 14px;
}

.todo-badge {
  min-width: 28px;
  height: 28px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
}

.todo-badge.high {
  background: #dc2626;
  box-shadow: 0 0 12px rgba(220, 38, 38, 0.45);
}

.todo-badge.medium {
  background: #f59e0b;
}

.notice-item {
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(8, 27, 48, 0.45);
  border: 1px solid rgba(106, 210, 255, 0.2);
}

.notice-item strong {
  display: block;
  font-size: 14px;
  color: #fff;
  margin-bottom: 4px;
}

.notice-item p {
  margin: 0 0 6px;
  font-size: 13px;
  color: rgba(202, 239, 255, 0.85);
  line-height: 1.5;
}

.notice-time {
  font-size: 12px;
  color: rgba(184, 233, 255, 0.65);
}

.chart-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.chart-block h4 {
  margin: 0 0 12px;
  font-size: 13px;
  color: rgba(202, 239, 255, 0.9);
}

.pie-chart {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.pie-svg {
  width: 110px;
  height: 110px;
}

.legend {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 6px;
  font-size: 12px;
  color: rgba(202, 239, 255, 0.88);
}

.legend li {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend i {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  font-style: normal;
}

.bar-chart {
  display: grid;
  gap: 8px;
}

.bar-row {
  display: grid;
  grid-template-columns: 72px 1fr 42px;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.bar-label {
  color: rgba(202, 239, 255, 0.88);
}

.bar-track {
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.bar-track i {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #00f2ff, #d600ff);
}

.bar-value {
  text-align: right;
  color: #fff;
}

.age-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  gap: 8px;
  min-height: 120px;
}

.age-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: rgba(202, 239, 255, 0.88);
}

.age-bar-wrap {
  width: 28px;
  height: 90px;
  display: flex;
  align-items: flex-end;
  border-radius: 8px 8px 0 0;
  background: rgba(255, 255, 255, 0.08);
}

.age-bar-wrap i {
  display: block;
  width: 100%;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #00f2ff, #3879c9);
  min-height: 8px;
}

.age-col em {
  font-style: normal;
  color: #fff;
  font-weight: 700;
}

.ai-status-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.ai-status-item {
  padding: 12px;
  border-radius: 12px;
  background: rgba(8, 27, 48, 0.45);
  border: 1px solid rgba(106, 210, 255, 0.2);
}

.ai-status-item span {
  display: block;
  font-size: 12px;
  color: rgba(184, 233, 255, 0.78);
  margin-bottom: 6px;
}

.ai-status-item strong {
  font-size: 18px;
  color: #fff;
}

.ai-status-item strong.online {
  color: #4ade80;
}

.load-track {
  margin-top: 8px;
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.load-track i {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #4ade80, #00f2ff);
}

.feedback-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(8, 27, 48, 0.45);
  border: 1px solid rgba(106, 210, 255, 0.2);
}

.feedback-item strong {
  display: block;
  font-size: 14px;
  color: #fff;
  margin-bottom: 4px;
}

.feedback-item p {
  margin: 0;
  font-size: 13px;
  color: rgba(202, 239, 255, 0.85);
  line-height: 1.5;
}

.feedback-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
  white-space: nowrap;
  background: rgba(56, 121, 201, 0.35);
  color: #e8f8ff;
}

.feedback-status.待处理 {
  background: rgba(220, 38, 38, 0.25);
  color: #fecaca;
}

.feedback-status.处理中 {
  background: rgba(245, 158, 11, 0.25);
  color: #fde68a;
}

.feedback-status.已回复 {
  background: rgba(74, 222, 128, 0.2);
  color: #bbf7d0;
}

.placeholder-panel {
  padding: 48px 32px;
  text-align: center;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.placeholder-panel h2 {
  margin: 0 0 10px;
  font-size: 22px;
  color: #fff;
}

.placeholder-panel p {
  margin: 0 0 20px;
  color: rgba(184, 233, 255, 0.85);
  line-height: 1.6;
}

.btn {
  border: 1px solid rgba(0, 242, 255, 0.55);
  border-radius: 8px;
  color: #fff;
  background: linear-gradient(105deg, rgba(0, 242, 255, 0.35) 0%, rgba(30, 20, 70, 0.85) 48%, rgba(214, 0, 255, 0.4) 100%);
  font-size: 14px;
  font-weight: 600;
  padding: 10px 18px;
  cursor: pointer;
}

.btn:hover {
  filter: brightness(1.08);
  transform: translateY(-1px);
}

@media (max-width: 1200px) {
  .stat-grid,
  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .admin-sidebar {
    position: static;
    width: 100%;
    height: auto;
  }

  .admin-layout {
    flex-direction: column;
  }

  .admin-main {
    margin-left: 0;
    width: 100%;
    padding: 16px;
  }

  .detail-grid,
  .extra-grid {
    grid-template-columns: 1fr;
  }

  .admin-meta {
    display: none;
  }
}

@media (max-width: 600px) {
  .stat-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
