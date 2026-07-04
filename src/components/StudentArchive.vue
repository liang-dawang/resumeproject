<script setup>
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const API_BASE = 'http://127.0.0.1:8000'

const router = useRouter()
const route = useRoute()

const navItems = [
  { label: '首页', path: '/' },
  { label: '职业规划', path: '/career-planning' },
  { label: '数据中心', path: '/data-center' },
  { label: '报告管理', path: '/report-management' },
  { label: '个人中心', path: '/student-profile' }   
]
 
  
const tabs = [
  { key: 'overview', label: '总体概述' },
  { key: 'skills', label: '专业技能' },
  { key: 'internship', label: '实习经历' },
  { key: 'benchmark', label: '对标分析' }
]

const defaultAvatarUrl = '/avatar.jpg'
const avatarExportSize = 256

const mockStudentProfiles = [
  {
    id: 101,
    name: '王五',
    desired_job: 'Java 开发工程师（中级）',
    overall_score: 93,
    detail: {
      name: '王五',
      studentId: '20210001',
      school: '星海信息学院',
      major: '软件工程',
      education: '本科',
      grade: '大四',
      targetJob: 'Java 开发工程师（中级）',
      profileComplete: 96,
      competitiveness: 92,
      overallScore: 93,
      intents: ['Java 开发', '后端开发', '分布式系统'],
      phone: '13800000001',
      strengths: '基础扎实，具备较强的后端开发与接口联调能力',
      toImprove: '分布式系统设计、缓存优化、复杂业务抽象',
      professionalSkills: ['Java', 'Spring Boot', 'MySQL', 'Redis', 'Git'],
      certificates: ['软件设计师', '英语四级', 'Java 工程实践证书'],
      internshipAbility:
        '参与过校园 OA 系统与课程管理平台开发，负责登录认证、学生信息管理与接口联调，具备完整的项目开发闭环经验。',
      abilityMatch: {
        basic: 92,
        skill: 95,
        quality: 88,
        potential: 94
      },
      matchingRaw: {
        best_match: {
          soft_skill_scores: {
            沟通表达: 92,
            团队协作: 90,
            问题解决: 94,
            自我驱动: 89
          }
        }
      }
    }
  },
  {
    id: 102,
    name: '李四',
    desired_job: '前端开发工程师（初中级）',
    overall_score: 88,
    detail: {
      name: '李四',
      studentId: '20210002',
      school: '星海信息学院',
      major: '计算机科学与技术',
      education: '本科',
      grade: '大三',
      targetJob: '前端开发工程师（初中级）',
      profileComplete: 90,
      competitiveness: 86,
      overallScore: 88,
      intents: ['Vue 开发', '前端工程化', '数据可视化'],
      phone: '13800000002',
      strengths: '页面实现能力强，审美和交互细节把控较好',
      toImprove: '工程化规范、性能优化、跨端适配',
      professionalSkills: ['Vue', 'JavaScript', 'HTML/CSS', 'TypeScript', 'ECharts'],
      certificates: ['前端开发实践证书', '普通话二级甲等'],
      internshipAbility:
        '参与过就业服务平台前端开发，完成过个人中心、数据看板和图表展示模块，能独立承担业务页面开发。',
      abilityMatch: {
        basic: 87,
        skill: 90,
        quality: 84,
        potential: 88
      },
      matchingRaw: {
        best_match: {
          soft_skill_scores: {
            沟通表达: 86,
            团队协作: 88,
            问题解决: 84,
            自我驱动: 91
          }
        }
      }
    }
  },
  {
    id: 103,
    name: '张三',
    desired_job: '测试开发工程师（初级）',
    overall_score: 85,
    detail: {
      name: '张三',
      studentId: '20210003',
      school: '星海信息学院',
      major: '信息管理与信息系统',
      education: '本科',
      grade: '大四',
      targetJob: '测试开发工程师（初级）',
      profileComplete: 84,
      competitiveness: 83,
      overallScore: 85,
      intents: ['测试开发', '接口测试', '自动化测试'],
      phone: '13800000003',
      strengths: '逻辑清晰，测试意识较强，能够较快发现问题',
      toImprove: '自动化测试框架、接口测试和脚本能力',
      professionalSkills: ['测试用例设计', 'Python', '接口测试', 'SQL', 'JMeter'],
      certificates: ['软件测试基础证书'],
      internshipAbility:
        '在实训项目中承担测试执行与缺陷跟踪工作，熟悉用例编写、回归测试和缺陷管理流程。',
      abilityMatch: {
        basic: 82,
        skill: 80,
        quality: 86,
        potential: 87
      },
      matchingRaw: {
        best_match: {
          soft_skill_scores: {
            沟通表达: 82,
            团队协作: 87,
            问题解决: 83,
            自我驱动: 85
          }
        }
      }
    }
  }
]

const activeTab = ref('overview')

// ==================== 学生列表（最近三条） ====================
const studentList = ref([])
const listLoading = ref(false)
const listError = ref('')
const selectedStudentId = ref(null)

// ==================== 选中学生详情 ====================
const student = ref(null)        // 来自 /career/overview 的 student 对象
const matchingRaw = ref(null)    // 来自 /career/overview 的 matchingRaw
const detailLoading = ref(false)
const detailError = ref('')
const avatarFileInput = ref(null)
const editDialogVisible = ref(false)
const profileForm = ref({
  name: '',
  education: '',
  phone: '',
  school: '',
  major: '',
  targetJob: ''
})

// ==================== 计算属性 ====================
const softSkills = computed(() => {
  const scores = matchingRaw.value?.best_match?.soft_skill_scores || {}
  return Object.entries(scores).map(([name, score]) => ({ name, score: Number(score) || 0 }))
})

const professionalSkills = computed(() => {
  const skills = student.value?.professionalSkills || []
  return skills.map((name) => ({ name, score: 75, experience: '' }))
})

const certificates = computed(() => student.value?.certificates || [])

const internshipText = computed(() => {
  return matchingRaw.value?.internship_ability || student.value?.internshipAbility || ''
})

const hasInternshipContent = computed(() => String(internshipText.value || '').trim().length > 0)

const benchmark = computed(() => {
  const am = student.value?.abilityMatch || {}
  return [
    { name: '基础要求', student: Math.round(am.basic || 0), target: 85, comment: '综合教育与基础能力评估' },
    { name: '专业技能', student: Math.round(am.skill || 0), target: 85, comment: '与目标岗位硬技能对比' },
    { name: '通用素质', student: Math.round(am.quality || 0), target: 80, comment: '软技能综合评估' },
    { name: '发展潜力', student: Math.round(am.potential || 0), target: 85, comment: '成长空间与学习能力' }
  ]
})

const gapGroups = computed(() => ({
  strongNeed: benchmark.value.filter((item) => item.target - item.student >= 20),
  basicNeed: benchmark.value.filter((item) => item.target - item.student >= 8 && item.target - item.student < 20),
  qualified: benchmark.value.filter((item) => item.target - item.student < 8)
}))

const completeRatio = computed(() => Math.round(student.value?.profileComplete || 0))
const competitiveRatio = computed(() => Math.round(student.value?.competitiveness || 0))
const overallRatio = computed(() => Math.round(student.value?.overallScore || 0))

const primarySkillCount = computed(() => professionalSkills.value.filter((s) => s.score >= 70).length)
const averageSkillDepth = computed(() =>
  professionalSkills.value.length
    ? Math.round(professionalSkills.value.reduce((sum, s) => sum + s.score, 0) / professionalSkills.value.length)
    : 0
)
const averageSoftSkill = computed(() =>
  softSkills.value.length
    ? Math.round(softSkills.value.reduce((sum, s) => sum + s.score, 0) / softSkills.value.length)
    : 0
)
const learningPotentialStar = computed(() => {
  const count = Math.max(1, Math.min(5, Math.round(averageSoftSkill.value / 20)))
  return '★'.repeat(count)
})

const profileAvatar = computed(() => student.value?.avatar || defaultAvatarUrl)

const displayName = computed(() =>
  editDialogVisible.value ? profileForm.value.name : (student.value?.name || '')
)
const displayMajor = computed(() =>
  editDialogVisible.value ? profileForm.value.major : (student.value?.major || '')
)
const displayEducation = computed(() =>
  editDialogVisible.value ? profileForm.value.education : (student.value?.education || '')
)
const displayTargetJob = computed(() =>
  editDialogVisible.value ? profileForm.value.targetJob : (student.value?.targetJob || '')
)

const archiveStorageKey = (studentId) => `student-archive:${studentId}`

const readArchiveRecord = (studentId) => {
  if (!studentId) return null
  try {
    const raw = window.localStorage.getItem(archiveStorageKey(studentId))
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

const writeArchiveRecord = (studentId, patch) => {
  if (!studentId) return
  try {
    const current = readArchiveRecord(studentId) || {}
    const next = {
      ...current,
      ...patch,
      profile: patch.profile ? { ...(current.profile || {}), ...patch.profile } : current.profile || undefined
    }
    if (next.avatar === '' || next.avatar == null) {
      delete next.avatar
    }
    if (next.profile && Object.keys(next.profile).length === 0) {
      delete next.profile
    }
    window.localStorage.setItem(archiveStorageKey(studentId), JSON.stringify(next))
  } catch (error) {
    console.warn('保存学生档案本地数据失败:', error)
  }
}

const syncFormFromStudent = () => {
  profileForm.value = {
    name: student.value?.name || '',
    education: student.value?.education || '',
    phone: student.value?.phone || '',
    school: student.value?.school || '',
    major: student.value?.major || '',
    targetJob: student.value?.targetJob || ''
  }
}

const applyStoredArchiveState = (studentId) => {
  if (!student.value) return
  const record = readArchiveRecord(studentId)
  if (!record) {
    student.value = {
      ...student.value,
      avatar: student.value.avatar || defaultAvatarUrl
    }
    return
  }
  student.value = {
    ...student.value,
    ...(record.profile || {}),
    avatar: record.avatar || student.value.avatar || defaultAvatarUrl
  }
}

const openAvatarPicker = () => {
  avatarFileInput.value?.click()
}

const cropImageToSquare = (source) =>
  new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => {
      try {
        const sourceWidth = image.naturalWidth || image.width
        const sourceHeight = image.naturalHeight || image.height
        const side = Math.min(sourceWidth, sourceHeight)
        const offsetX = Math.max(0, Math.floor((sourceWidth - side) / 2))
        const offsetY = Math.max(0, Math.floor((sourceHeight - side) / 2))
        const canvas = document.createElement('canvas')
        canvas.width = avatarExportSize
        canvas.height = avatarExportSize
        const context = canvas.getContext('2d')
        if (!context) {
          reject(new Error('无法创建头像画布'))
          return
        }
        context.drawImage(image, offsetX, offsetY, side, side, 0, 0, avatarExportSize, avatarExportSize)
        resolve(canvas.toDataURL('image/png'))
      } catch (error) {
        reject(error)
      }
    }
    image.onerror = () => reject(new Error('头像图片加载失败'))
    image.src = source
  })

const onAvatarSelected = async (event) => {
  const file = event.target.files?.[0]
  if (!file || !selectedStudentId.value) return
  const reader = new FileReader()
  reader.onload = async () => {
    try {
      const avatar = await cropImageToSquare(String(reader.result || defaultAvatarUrl))
      student.value = {
        ...student.value,
        avatar
      }
      writeArchiveRecord(selectedStudentId.value, { avatar })
    } catch (error) {
      console.warn('头像裁剪失败，使用原图显示:', error)
      const avatar = String(reader.result || defaultAvatarUrl)
      student.value = {
        ...student.value,
        avatar
      }
      writeArchiveRecord(selectedStudentId.value, { avatar })
    }
  }
  reader.readAsDataURL(file)
  event.target.value = ''
}

const openEditDialog = () => {
  if (!student.value) return
  syncFormFromStudent()
  editDialogVisible.value = true
}

const closeEditDialog = () => {
  editDialogVisible.value = false
}

const resetForm = () => {
  syncFormFromStudent()
}

const saveProfile = async () => {
  if (!selectedStudentId.value || !student.value) return
  const payload = {
    name: profileForm.value.name.trim(),
    education: profileForm.value.education.trim(),
    phone: profileForm.value.phone.trim(),
    school: profileForm.value.school.trim(),
    major: profileForm.value.major.trim(),
    targetJob: profileForm.value.targetJob.trim()
  }
  student.value = {
    ...student.value,
    ...payload,
    avatar: student.value.avatar || defaultAvatarUrl
  }
  writeArchiveRecord(selectedStudentId.value, { profile: payload })
  editDialogVisible.value = false
}

const logout = () => {
  try {
    localStorage.removeItem('authToken')
    localStorage.removeItem('isAuthed')
    localStorage.removeItem('currentUser')
    localStorage.removeItem('userRole')
  } catch (error) {
    console.warn('localStorage 不可用：', error)
  }
  router.push('/login')
}

// ==================== 方法 ====================
const go = (path) => {
  if (route.path !== path) router.push(path)
}

const openMatch = () => router.push('/career-planning?tab=job-match')
const editProfile = () => router.push('/career-planning?tab=student-input')

async function fetchStudentList() {
  listLoading.value = true
  listError.value = ''
  try {
    const { data } = await axios.get(`${API_BASE}/career/student/profiles/recent`, {
      params: { limit: 3 }
    })
    if (data?.code === 200) {
      const baseItems = Array.isArray(data.data?.items) ? data.data.items : []
      // 强兜底：逐条补齐姓名/意向/综合分，避免列表接口字段缺失
      const hydrated = await Promise.all(
        baseItems.map(async (item) => {
          const normalized = {
            ...item,
            name: item?.name || item?.student_name || '',
            desired_job: item?.desired_job || '',
            overall_score: Number(item?.overall_score) || 0
          }
          if (normalized.name && normalized.desired_job && normalized.overall_score > 0) {
            return normalized
          }
          try {
            const detailRes = await axios.get(`${API_BASE}/career/student/profiles/${item.id}`)
            const d = detailRes?.data?.data || {}
            return {
              ...normalized,
              name: normalized.name || d.name || d.student_name || item?.name || item?.student_name || '',
              desired_job: normalized.desired_job || d.targetJob || d.dynamicMatchedJob?.jobTitle || '未填写',
              overall_score: normalized.overall_score || Number(d.overallScore) || Number(d.competitiveness) || 0
            }
          } catch {
            return {
              ...normalized,
              name: normalized.name || item?.name || item?.student_name || '',
              desired_job: normalized.desired_job || '未填写',
              overall_score: normalized.overall_score || 0
            }
          }
        })
      )
      studentList.value = hydrated
      if (!selectedStudentId.value && studentList.value.length) {
        selectStudent(studentList.value[0])
      }
      if (!studentList.value.length) {
        applyMockData()
      }
    } else {
      applyMockData()
    }
  } catch (e) {
    console.error('获取学生档案失败:', e)
    applyMockData()
  } finally {
    listLoading.value = false
  }
}

function applyMockData() {
  studentList.value = mockStudentProfiles.map((item) => ({
    id: item.id,
    name: item.name,
    desired_job: item.desired_job,
    overall_score: item.overall_score
  }))
  listError.value = ''
  if (!selectedStudentId.value && studentList.value.length) {
    selectStudent(studentList.value[0])
  }
}

async function fetchStudentDetail(studentId) {
  detailLoading.value = true
  detailError.value = ''
  student.value = null
  matchingRaw.value = null
  try {
    // 必须传 student_id，否则后端始终返回「最新一条」画像
    const { data } = await axios.get(`${API_BASE}/career/overview`, {
      params: { student_id: studentId }
    })
    if (data?.code === 200 && data?.data?.student) {
      student.value = data.data.student
      matchingRaw.value = data.data.matchingRaw
      applyStoredArchiveState(studentId)
    } else {
      applyMockDetail(studentId)
    }
  } catch (e) {
    console.error('获取学生详情失败:', e)
    applyMockDetail(studentId)
  } finally {
    detailLoading.value = false
  }
}

function applyMockDetail(studentId) {
  const mockItem = mockStudentProfiles.find((item) => item.id === studentId) || mockStudentProfiles[0]
  if (!mockItem) {
    detailError.value = '获取详情失败'
    return
  }
  student.value = {
    ...mockItem.detail,
    avatar: mockItem.detail.avatar || defaultAvatarUrl
  }
  matchingRaw.value = mockItem.detail.matchingRaw
  applyStoredArchiveState(studentId)
  detailError.value = ''
}

function selectStudent(item) {
  selectedStudentId.value = item.id
  fetchStudentDetail(item.id)
}

onMounted(() => {
  fetchStudentList()
})
</script>

<template>
  <div class="archive-page">
    <header class="top-nav card-surface">
      <div class="brand">
        <span>职途智链</span>
        <img class="brand-logo" src="/logo.png" alt="职途智链logo" />
      </div>
      <nav class="nav-list">
        <button
          v-for="item in navItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: route.path === item.path }"
          @click="go(item.path)"
        >
          {{ item.label }}
        </button>
      </nav>
    </header>

    <main class="archive-content">

      <section v-if="listLoading" class="state-box">
        <p class="state-text">正在加载学生档案...</p>
      </section>
      <section v-else-if="listError && !studentList.length" class="state-box">
        <p class="state-text error">{{ listError }}</p>
        <button class="btn btn-primary" @click="fetchStudentList">重试</button>
      </section>

      <!-- 选中学生详情 -->
      <template v-if="selectedStudentId">
        <section v-if="detailLoading" class="state-box">
          <p class="state-text">正在加载学生画像...</p>
        </section>
        <section v-else-if="detailError" class="state-box">
          <p class="state-text error">{{ detailError }}</p>
        </section>
        <template v-else-if="student">
          <!-- 基本信息 + 评分 -->
          <section class="profile-top card-surface">
            <div class="student-info">
              <div class="avatar-wrap">
                <button class="avatar-button" type="button" @click="openAvatarPicker" aria-label="点击上传头像">
                  <img :src="profileAvatar" class="avatar-img" alt="用户头像" />
                </button>
                <input ref="avatarFileInput" class="hidden-file-input" type="file" accept="image/*" @change="onAvatarSelected" />
              </div>
              <div class="basic">
                <h2>{{ displayName }}</h2>
                <p>{{ displayMajor }} | {{ displayEducation }}</p>
                <p class="profile-intent">意向岗位：{{ displayTargetJob }}</p>
              </div>
            </div>

            <div class="score-section">
              <button class="edit-entry-btn btn btn-primary hero-cta" type="button" @click="openEditDialog">
                <span class="hero-cta-text">编辑信息</span>
                <span class="hero-cta-arrow" aria-hidden="true"></span>
              </button>
              <div class="score-grid">
                <article class="score-card">
                  <strong>{{ completeRatio }}分</strong>
                  <span>完整度评分</span>
                  <div class="score-track"><i :style="{ width: `${Math.min(completeRatio, 100)}%` }"></i></div>
                </article>
                <article class="score-card">
                  <strong>{{ competitiveRatio }}分</strong>
                  <span>竞争力评分</span>
                  <div class="score-track"><i :style="{ width: `${Math.min(competitiveRatio, 100)}%` }"></i></div>
                </article>
                <article class="score-card">
                  <strong>{{ overallRatio }}分</strong>
                  <span>综合评分</span>
                  <div class="score-track"><i :style="{ width: `${Math.min(overallRatio, 100)}%` }"></i></div>
                </article>
              </div>
            </div>
          </section>

          <section v-if="editDialogVisible" class="profile-edit-panel card-surface">
            <button class="dialog-close" type="button" aria-label="关闭编辑" @click="closeEditDialog">×</button>
            <div class="module-title">个人信息编辑</div>
            <div class="form-grid">
              <label class="field-item">
                <span class="field-label">姓名</span>
                <input v-model="profileForm.name" class="field-input" type="text" />
              </label>
              <label class="field-item">
                <span class="field-label">学历</span>
                <input v-model="profileForm.education" class="field-input" type="text" />
              </label>
              <label class="field-item">
                <span class="field-label">电话</span>
                <input v-model="profileForm.phone" class="field-input" type="text" />
              </label>
              <label class="field-item">
                <span class="field-label">院校</span>
                <input v-model="profileForm.school" class="field-input" type="text" />
              </label>
              <label class="field-item">
                <span class="field-label">专业</span>
                <input v-model="profileForm.major" class="field-input" type="text" />
              </label>
              <label class="field-item">
                <span class="field-label">意向岗位</span>
                <input v-model="profileForm.targetJob" class="field-input" type="text" />
              </label>
            </div>
            <div class="form-footer">
              <div class="form-actions">
                <button class="form-btn form-btn-primary" type="button" @click="saveProfile">保存修改</button>
                <button class="form-btn" type="button" @click="resetForm">重置</button>
              </div>
              <button class="form-btn form-btn-logout" type="button" @click="logout">退出登录</button>
            </div>
          </section>

          <!-- Tab 切换 -->
          <section class="tab-row">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              class="tab-item"
              :class="{ active: activeTab === tab.key }"
              @click="activeTab = tab.key"
            >
              {{ tab.label }}
            </button>
          </section>

          <!-- 总体概述 -->
          <section v-if="activeTab === 'overview'" class="panel card-surface">
            <h3>软技能综合评分</h3>
            <div v-if="softSkills.length" class="ring-grid">
              <article v-for="item in softSkills" :key="item.name" class="ring-card">
                <h4>{{ item.name }}</h4>
                <div class="ring" :style="{ '--value': `${Math.min(item.score, 100)}%` }">
                  <div class="ring-inner">{{ item.score }}分</div>
                </div>
              </article>
            </div>
            <p v-else class="state-text">暂无软技能数据，请先完成人岗匹配</p>

            <div class="cert-block card-surface">
              <h3>证书与认证</h3>
              <div v-if="certificates.length" class="chip-row">
                <span v-for="cert in certificates" :key="cert" class="chip">🏅 {{ cert }}</span>
              </div>
              <p v-else class="cert-empty">暂无证书记录</p>
            </div>

            <div class="summary-grid">
              <article class="summary-card">
                <span>目标岗位</span>
                <strong>{{ student.targetJob || '未设定' }}</strong>
              </article>
              <article class="summary-card">
                <span>核心优势</span>
                <strong>{{ student.strengths || '暂无' }}</strong>
              </article>
              <article class="summary-card">
                <span>待提升方向</span>
                <strong>{{ student.toImprove || '暂无' }}</strong>
              </article>
            </div>
          </section>

          <!-- 专业技能 -->
          <section v-if="activeTab === 'skills'" class="panel card-surface">
            <div class="skill-stats">
              <div class="stat-item">
                <strong>{{ primarySkillCount }}</strong>
                <span>核心技能数</span>
              </div>
              <div class="stat-item">
                <strong>{{ averageSkillDepth }}</strong>
                <span>平均熟练度</span>
              </div>
              <div class="stat-item">
                <strong>{{ learningPotentialStar }}</strong>
                <span>学习潜力</span>
              </div>
            </div>

            <div v-if="professionalSkills.length" class="skill-list">
              <div v-for="skill in professionalSkills" :key="skill.name" class="skill-item">
                <div class="skill-header">
                  <span>{{ skill.name }}</span>
                  <strong>{{ skill.score }}</strong>
                </div>
                <div class="skill-track">
                  <i :style="{ width: `${Math.min(skill.score, 100)}%` }"></i>
                </div>
              </div>
            </div>
            <p v-else class="state-text">暂无专业技能数据</p>
          </section>

          <!-- 实习经历 -->
          <section v-if="activeTab === 'internship'" class="panel card-surface panel-internship">
            <div v-if="hasInternshipContent" class="internship-text internship-text--filled">
              <h4>实习/项目经历描述</h4>
              <p>{{ internshipText }}</p>
            </div>
            <div v-else class="internship-empty-state">
              <svg
                class="internship-empty-icon"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.35"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
              >
                <rect x="3" y="7" width="18" height="13" rx="2" />
                <path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                <path d="M3 12h18" />
                <path d="M12 12v5" />
                <path d="M9 15h6" />
              </svg>
              <p class="internship-empty-title">暂未填写实习经历</p>
              <p class="internship-empty-sub">
                目前暂无实习履历，后续可补充个人实践、岗位实习相关经历哦
              </p>
            </div>
            <div class="action-row">
              <button class="btn btn-primary" @click="editProfile">补充实习经历</button>
            </div>
          </section>

          <!-- 对标分析 -->
          <section v-if="activeTab === 'benchmark'" class="panel card-surface">
            <div class="gap-grid">
              <div class="gap-col">
                <h4>急需提升</h4>
                <ul>
                  <li v-for="item in gapGroups.strongNeed" :key="item.name">
                    {{ item.name }}（差距 {{ item.target - item.student }} 分）：{{ item.comment }}
                  </li>
                  <li v-if="!gapGroups.strongNeed.length">无</li>
                </ul>
              </div>
              <div class="gap-col">
                <h4>有待提升</h4>
                <ul>
                  <li v-for="item in gapGroups.basicNeed" :key="item.name">
                    {{ item.name }}（差距 {{ item.target - item.student }} 分）：{{ item.comment }}
                  </li>
                  <li v-if="!gapGroups.basicNeed.length">无</li>
                </ul>
              </div>
              <div class="gap-col">
                <h4>已达标</h4>
                <ul>
                  <li v-for="item in gapGroups.qualified" :key="item.name">
                    {{ item.name }}（当前 {{ item.student }} / 目标 {{ item.target }} 分）
                  </li>
                  <li v-if="!gapGroups.qualified.length">无</li>
                </ul>
              </div>
            </div>

            <div class="benchmark-list">
              <div v-for="item in benchmark" :key="item.name" class="bench-item">
                <div class="bench-header">
                  <span>{{ item.name }}</span>
                  <span>{{ item.student }} / {{ item.target }}</span>
                </div>
                <div class="dual-track">
                  <i class="bar-student" :style="{ width: `${Math.min(item.student, 100)}%` }"></i>
                  <i class="bar-target" :style="{ width: `${Math.min(item.target, 100)}%` }"></i>
                </div>
                <p class="bench-comment">{{ item.comment }}</p>
              </div>
            </div>
          </section>

          <!-- 操作按钮 -->
          <div class="action-row">
            <button class="btn btn-primary" @click="openMatch">进行人岗匹配</button>
            <button class="btn btn-secondary" @click="editProfile">编辑简历信息</button>
            <button class="btn btn-primary" @click="go('/report-management')">查看职业报告</button>
          </div>
        </template>
      </template>

    </main>
  </div>
</template>

<style scoped>
.archive-page {
  --page-padding: 24px;
  --nav-block-height: 92px;
  min-height: 100vh;
  padding: calc(var(--nav-block-height) + 28px) var(--page-padding) var(--page-padding);
  color: #12294b;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Segoe UI', sans-serif;
  background-color: #e7f1f8;
  background-image: url('/background.jpg');
  background-repeat: no-repeat;
  background-position: center;
  background-size: cover;
}

.card-surface {
  border-radius: 12px;
  box-shadow: 0 10px 24px rgba(18, 41, 75, 0.1);
}

.top-nav {
  position: fixed;
  top: 0;
  left: var(--page-padding);
  right: var(--page-padding);
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.24) 0%, rgba(255, 255, 255, 0.06) 42%, rgba(255, 255, 255, 0) 100%),
    linear-gradient(90deg, #1f4d9a 0%, #3879c9 52%, #32c9fd 100%);
  color: #fff;
  padding: 16px 30px;
  min-height: 60px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 1px;
}

.brand-logo {
  width: 60px;
  height: 60px;
  object-fit: contain;
}

.nav-list { display: flex; gap: 8px; flex-wrap: wrap; }

.nav-item {
  border: none; border-radius: 8px; background: transparent;
  color: #fff; font-size: 18px; font-weight: 700; line-height: 1; padding: 16px 18px; cursor: pointer;
  transition: background-color 0.2s ease;
}
.nav-item:hover, .nav-item.active { background: #3879c9; }

.archive-content {
  max-width: 1200px;
  margin: 0 auto;
}

.archive-content h1 {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 10px;
  color: #f2fdff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.45);
}

/* 状态框 */
.state-box {
  min-height: 120px;
  display: grid;
  place-items: center;
  gap: 10px;
  text-align: center;
}

.state-text {
  margin: 0;
  font-size: 16px;
  color: #e0f4ff;
}

.state-text.error {
  color: #fca5a5;
}

/* 基本信息 + 分数 */
.profile-top {
  background: rgba(8, 27, 48, 0.85);
  border: 1px solid rgba(106, 210, 255, 0.34);
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
  position: relative;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.avatar-wrap {
  display: grid;
  justify-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.avatar-button {
  width: 88px;
  height: 88px;
  padding: 0;
  border: 2px solid rgba(147, 211, 255, 0.45);
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(145deg, rgba(56, 121, 201, 0.85), rgba(8, 27, 48, 0.95));
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.18);
  cursor: pointer;
  flex-shrink: 0;
}

.avatar-button:hover {
  border-color: rgba(0, 242, 255, 0.8);
  box-shadow: 0 12px 24px rgba(0, 242, 255, 0.12);
}

.avatar-button:focus-visible {
  outline: 2px solid rgba(0, 242, 255, 0.9);
  outline-offset: 3px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  object-position: center center;
}

.hidden-file-input {
  display: none;
}

.basic h2 {
  margin: 0;
  font-size: 24px;
  color: #ffffff;
}

.basic p {
  margin: 4px 0 0;
  font-size: 14px;
  color: rgba(202, 239, 255, 0.88);
}

.basic p.profile-intent {
  color: #ffffff;
  font-weight: 600;
}

.score-section {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  margin-left: auto;
}

.edit-entry-btn {
  flex-shrink: 0;
  margin-right: 100px;
}

.score-grid {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.score-card {
  min-width: 128px;
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.34);
  border-radius: 12px;
  padding: 14px 18px;
  display: grid;
  gap: 6px;
  text-align: center;
}

.score-card strong {
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
}

.score-card span {
  font-size: 13px;
  color: rgba(232, 248, 255, 0.9);
}

.score-track {
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  overflow: hidden;
}

.score-track i {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: #3879c9;
}

/* Tabs */
.tab-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.tab-item {
  border: 1px solid rgba(0, 242, 255, 0.35);
  border-radius: 999px;
  background: rgba(8, 20, 48, 0.65);
  color: #b8f8ff;
  font-size: 16px;
  font-weight: 600;
  padding: 10px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-item:hover {
  background: rgba(214, 0, 255, 0.15);
  color: #ffffff;
  border-color: rgba(255, 0, 255, 0.45);
}

.tab-item.active {
  background: #3879c9;
  color: #ffffff;
  border-color: rgba(147, 211, 255, 0.55);
}

/* Panel */
.panel {
  background: rgba(8, 27, 48, 0.72);
  border: 1px solid rgba(106, 210, 255, 0.34);
  padding: 20px;
  margin-bottom: 14px;
  color: #e8f4ff;
}

.panel h3 {
  margin: 0 0 14px;
  font-size: 20px;
  color: #ffffff;
}

.panel .state-text {
  color: rgba(202, 239, 255, 0.85);
}

/* 软技能 rings：纯色环 + 深色内盘，避免炫彩渐变 */
.ring-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.ring-card {
  text-align: center;
}

.ring-card h4 {
  margin: 0 0 10px;
  font-size: 14px;
  color: #ffffff;
  font-weight: 700;
}

.ring {
  --value: 0%;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  margin: 0 auto;
  background: conic-gradient(#3879c9 var(--value), rgba(30, 55, 88, 0.95) 0);
  display: grid;
  place-items: center;
}

.ring-inner {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: #0c1828;
  border: 1px solid rgba(106, 210, 255, 0.35);
  display: grid;
  place-items: center;
  font-size: 15px;
  font-weight: 800;
  color: #ffffff;
}

/* 证书 */
.cert-block {
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.34);
  padding: 16px;
  margin-top: 14px;
}

.cert-block h3 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #ffffff;
}

.cert-block p,
.cert-empty {
  color: rgba(202, 239, 255, 0.75);
  margin: 0;
}

.chip-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  background: rgba(12, 40, 72, 0.92);
  border: 1px solid rgba(106, 210, 255, 0.35);
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 13px;
  color: #e0f4ff;
}

/* 摘要卡片（岗位职责条） */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 16px;
}

.summary-card {
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.34);
  border-radius: 10px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-card span {
  font-size: 13px;
  color: rgba(232, 248, 255, 0.88);
  font-weight: 600;
}

.summary-card strong {
  font-size: 15px;
  color: #ffffff;
  line-height: 1.55;
  font-weight: 700;
}

/* 技能统计 */
.skill-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.34);
  padding: 14px 20px;
  border-radius: 10px;
}

.stat-item strong {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
}

.stat-item span {
  font-size: 13px;
  color: rgba(202, 239, 255, 0.88);
}

.skill-list {
  display: grid;
  gap: 12px;
}

.skill-item {
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.34);
  border-radius: 10px;
  padding: 12px 14px;
}

.skill-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #ffffff;
  font-weight: 600;
}

.skill-header strong {
  color: #ffffff;
  font-size: 15px;
}

.skill-track {
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  overflow: hidden;
}

.skill-track i {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: #38bdf8;
}

/* 实习（有内容时对齐首页岗位预览玻璃卡） */
.internship-text--filled {
  border-radius: 20px;
  padding: 18px 20px;
  margin-bottom: 16px;
  color: #b8f8ff;
  background: rgba(6, 14, 36, 0.72);
  border: 1px solid rgba(0, 242, 255, 0.32);
  box-shadow:
    inset 0 0 22px rgba(0, 242, 255, 0.06),
    0 8px 24px rgba(0, 0, 0, 0.25);
}

.internship-text--filled h4 {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 800;
  color: #00f2ff;
  text-shadow: 0 0 10px rgba(0, 242, 255, 0.35);
}

.internship-text--filled p {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: #b8f8ff;
}

.internship-empty-state {
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 32px 20px;
  margin-bottom: 16px;
  border-radius: 20px;
  border: 1px dashed rgba(106, 210, 255, 0.28);
  background: rgba(6, 14, 36, 0.45);
}

.internship-empty-icon {
  width: 56px;
  height: 56px;
  color: rgba(180, 200, 220, 0.55);
  margin-bottom: 16px;
}

.internship-empty-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 800;
  color: #ffffff;
}

.internship-empty-sub {
  margin: 0;
  max-width: 420px;
  font-size: 14px;
  line-height: 1.65;
  color: rgba(202, 239, 255, 0.78);
}

/* 对标分析 */
.gap-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.gap-col {
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.34);
  border-radius: 10px;
  padding: 12px 14px;
}

.gap-col h4 {
  margin: 0 0 10px;
  font-size: 15px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(12, 40, 72, 0.95);
  border: 1px solid rgba(106, 210, 255, 0.35);
  display: inline-block;
  color: #ffffff;
  font-weight: 800;
}

.gap-col ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #ffffff;
}

.benchmark-list {
  display: grid;
  gap: 14px;
}

.bench-item {
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.34);
  border-radius: 10px;
  padding: 12px 14px;
}

.bench-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #ffffff;
}

.dual-track {
  position: relative;
  height: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  overflow: hidden;
  margin-bottom: 8px;
}

.bar-student {
  position: absolute;
  height: 100%;
  border-radius: 999px;
  background: #3879c9;
  top: 0;
  left: 0;
  display: block;
}

.bar-target {
  position: absolute;
  height: 3px;
  border-radius: 999px;
  background: #f87171;
  bottom: 0;
  left: 0;
  display: block;
  opacity: 0.95;
}

.bench-comment {
  margin: 0;
  font-size: 13px;
  color: rgba(224, 244, 255, 0.92);
  line-height: 1.55;
}

/* 操作行 */
.action-row { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 14px; }

/* 个人信息编辑面板 */
.profile-edit-panel {
  position: relative;
  background: rgba(8, 27, 48, 0.85);
  border: 1px solid rgba(106, 210, 255, 0.34);
  padding: 28px 20px 16px;
  margin-bottom: 16px;
  color: #e8f4ff;
}

.dialog-close {
  position: absolute;
  top: 10px;
  left: 12px;
  width: 28px;
  height: 28px;
  border: 1px solid rgba(106, 210, 255, 0.35);
  border-radius: 8px;
  background: rgba(8, 27, 48, 0.7);
  color: rgba(232, 248, 255, 0.9);
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  display: grid;
  place-items: center;
  padding: 0;
  transition: all 0.2s ease;
}

.dialog-close:hover {
  color: #ffffff;
  border-color: rgba(0, 242, 255, 0.65);
  background: rgba(56, 121, 201, 0.35);
}

.module-title {
  margin: 0 0 12px;
  padding-left: 36px;
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px 14px;
  margin-bottom: 12px;
}

.field-item {
  display: grid;
  gap: 6px;
}

.field-label {
  font-size: 13px;
  color: rgba(202, 239, 255, 0.88);
}

.field-input {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid rgba(106, 210, 255, 0.34);
  border-radius: 8px;
  background: rgba(8, 27, 48, 0.58);
  color: #ffffff;
  font-size: 14px;
  padding: 8px 10px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field-input:focus {
  border-color: rgba(0, 242, 255, 0.65);
  box-shadow: 0 0 0 2px rgba(0, 242, 255, 0.12);
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.form-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.form-btn {
  border: 1px solid rgba(106, 210, 255, 0.34);
  border-radius: 8px;
  background: rgba(8, 27, 48, 0.58);
  color: #e8f4ff;
  font-size: 14px;
  padding: 10px 18px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-btn:hover {
  border-color: rgba(0, 242, 255, 0.55);
  background: rgba(56, 121, 201, 0.35);
}

.form-btn-primary {
  background: #1f4d9a;
  border-color: rgba(147, 211, 255, 0.45);
  color: #ffffff;
}

.form-btn-primary:hover {
  background: #3879c9;
}

.form-btn-logout {
  margin-left: auto;
  background: #1f4d9a;
  border-color: rgba(147, 211, 255, 0.45);
  color: #ffffff;
}

.form-btn-logout:hover {
  background: #3879c9;
  border-color: rgba(147, 211, 255, 0.45);
  color: #ffffff;
}

/* 按钮 */
.btn { border: none; cursor: pointer; border-radius: 8px; font-size: 15px; padding: 11px 20px; transition: all 0.2s ease; }
.btn-primary { background: #1f4d9a; color: #fff; }
.btn-primary:hover { background: #3879c9; }
.btn-primary:disabled { background: #b7d2e8; cursor: not-allowed; }
.btn-secondary { background: #e7f1f8; color: #12294b; }
.btn-secondary:hover { background: #b7d2e8; }
.btn-secondary:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 860px) {
  .archive-page { --page-padding: 14px; --nav-block-height: 132px; }
  .top-nav { flex-direction: column; align-items: flex-start; gap: 10px; }
  .profile-top { flex-direction: column; }
  .student-info { width: 100%; flex-direction: column; align-items: center; text-align: center; }
  .score-section {
    width: 100%;
    margin-left: 0;
    flex-direction: column;
    align-items: stretch;
  }
  .edit-entry-btn {
    width: 100%;
    min-width: 0;
  }
  .form-grid,
  .summary-grid,
  .gap-grid { grid-template-columns: 1fr; }
  .profile-edit-panel { padding: 28px 14px 14px; margin-bottom: 14px; }
  .form-footer { flex-direction: column; align-items: stretch; }
  .form-btn-logout { margin-left: 0; width: 100%; }
  .summary-grid, .gap-grid { grid-template-columns: 1fr; }
}
</style>
