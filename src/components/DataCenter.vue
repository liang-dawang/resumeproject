<script setup>
import axios from 'axios'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const API_BASE = 'http://127.0.0.1:8080'
const CAREER_LIST_API = 'http://localhost:8080/api/career/list'

const router = useRouter()
const route = useRoute()

const navItems = [
  { label: '首页', path: '/' },
  { label: '职业规划', path: '/career-planning' },
  { label: '数据中心', path: '/data-center' },
  { label: '报告管理', path: '/report-management' },
  { label: '个人中心', path: '/student-profile' },
  
]

const sidebarMenus = [
  { key: 'job-data', label: '岗位介绍' },
  { key: 'job-portrait', label: '岗位能力需求' },
  { key: 'job-graph', label: '职业发展路径' }
]

const categoryOptions = ['C/C++', 'Java', '前端开发', '实施工程师', '技术支持与运维', '测试工程师', '硬件测试', '科研人员', '软件测试', '项目经理/主管']
const companyScaleOptions = ['20人以下', '20-99人', '100-299人', '300-499人', '500-999人', '1000-9999人', '10000人以上']
const sortOptions = ['薪资']

const jobListData = ref([])
const jobListTotal = ref(0)
const jobListLoading = ref(false)
const jobListError = ref('')

const portraitData = {
  'C/C++': {
    role: 'C/C++开发工程师',
    duty: '负责高性能核心模块开发、底层优化与跨平台功能实现。',
    skills: [
      { name: 'C/C++', value: 92 },
      { name: 'Linux', value: 78 },
      { name: '数据结构与算法', value: 86 },
      { name: '网络编程', value: 74 }
    ],
    abilities: [
      { name: '编码能力', stars: 5, score: 91 },
      { name: '问题解决', stars: 4, score: 86 },
      { name: '沟通能力', stars: 4, score: 76 },
      { name: '学习能力', stars: 5, score: 88 },
      { name: '团队合作', stars: 4, score: 80 }
    ],
    certificates: ['软考中级', '嵌入式系统工程师'],
    salary: '20k-35k',
    prospect: '可向高性能系统开发、基础架构与技术专家方向发展。'
  },
  Java: {
    role: 'Java工程师',
    duty: '负责企业级后端服务开发、接口设计与系统稳定性保障。',
    skills: [
      { name: 'Java', value: 90 },
      { name: 'Spring/SpringBoot', value: 85 },
      { name: 'MySQL', value: 80 },
      { name: 'Redis', value: 75 },
      { name: 'Linux', value: 70 }
    ],
    abilities: [
      { name: '编码能力', stars: 5, score: 90 },
      { name: '问题解决', stars: 5, score: 85 },
      { name: '沟通能力', stars: 4, score: 75 },
      { name: '学习能力', stars: 5, score: 85 },
      { name: '团队合作', stars: 4, score: 80 }
    ],
    certificates: ['软考中级', '阿里云ACP可选'],
    salary: '19k-32k',
    prospect: '可向架构师、技术负责人或分布式平台方向持续成长。'
  },
  前端开发: {
    role: '前端开发工程师',
    duty: '负责Web端页面开发、交互体验优化与前后端协作交付。',
    skills: [
      { name: 'JavaScript/TypeScript', value: 88 },
      { name: 'Vue/React', value: 84 },
      { name: '工程化构建', value: 76 },
      { name: '性能优化', value: 72 }
    ],
    abilities: [
      { name: '编码能力', stars: 5, score: 88 },
      { name: '问题解决', stars: 4, score: 82 },
      { name: '沟通能力', stars: 4, score: 78 },
      { name: '学习能力', stars: 5, score: 86 },
      { name: '团队合作', stars: 4, score: 82 }
    ],
    certificates: ['前端工程化证书', '软考中级'],
    salary: '16k-30k',
    prospect: '可向前端架构、可视化、全栈方向拓展。'
  },
  实施工程师: {
    role: '实施工程师',
    duty: '负责系统部署实施、需求落地与客户培训支持。',
    skills: [
      { name: '系统部署', value: 80 },
      { name: '需求理解', value: 76 },
      { name: 'SQL基础', value: 68 },
      { name: '项目协同', value: 74 }
    ],
    abilities: [
      { name: '执行能力', stars: 4, score: 82 },
      { name: '问题解决', stars: 4, score: 78 },
      { name: '沟通能力', stars: 5, score: 88 },
      { name: '学习能力', stars: 4, score: 80 },
      { name: '团队合作', stars: 4, score: 84 }
    ],
    certificates: ['PMP基础证书', '信息系统项目管理师'],
    salary: '10k-18k',
    prospect: '可向项目经理、解决方案顾问方向发展。'
  },
  '技术支持与运维': {
    role: '技术支持与运维工程师',
    duty: '负责生产环境稳定运行、故障响应与自动化运维优化。',
    skills: [
      { name: 'Linux运维', value: 82 },
      { name: '脚本开发', value: 72 },
      { name: '监控体系', value: 78 },
      { name: '容器化', value: 70 }
    ],
    abilities: [
      { name: '执行能力', stars: 5, score: 86 },
      { name: '问题解决', stars: 5, score: 84 },
      { name: '沟通能力', stars: 4, score: 76 },
      { name: '学习能力', stars: 4, score: 79 },
      { name: '团队合作', stars: 4, score: 81 }
    ],
    certificates: ['RHCE', '云平台运维认证'],
    salary: '11k-20k',
    prospect: '可向SRE、云平台运维专家方向成长。'
  },
  测试工程师: {
    role: '测试工程师',
    duty: '负责测试方案设计、缺陷管理与质量保障闭环。',
    skills: [
      { name: '测试设计', value: 84 },
      { name: '接口测试', value: 78 },
      { name: '自动化测试', value: 72 },
      { name: '质量分析', value: 74 }
    ],
    abilities: [
      { name: '细节把控', stars: 5, score: 88 },
      { name: '问题解决', stars: 4, score: 80 },
      { name: '沟通能力', stars: 4, score: 78 },
      { name: '学习能力', stars: 4, score: 79 },
      { name: '团队合作', stars: 4, score: 82 }
    ],
    certificates: ['ISTQB', '软考中级'],
    salary: '12k-22k',
    prospect: '可向测试开发、质量负责人方向发展。'
  },
  硬件测试: {
    role: '硬件测试工程师',
    duty: '负责硬件功能验证、稳定性测试与实验数据分析。',
    skills: [
      { name: '硬件原理', value: 80 },
      { name: '测试仪器使用', value: 84 },
      { name: '故障分析', value: 76 },
      { name: '数据处理', value: 70 }
    ],
    abilities: [
      { name: '执行能力', stars: 4, score: 82 },
      { name: '问题解决', stars: 4, score: 80 },
      { name: '沟通能力', stars: 4, score: 74 },
      { name: '学习能力', stars: 4, score: 78 },
      { name: '团队合作', stars: 4, score: 79 }
    ],
    certificates: ['电子工程师证书', '硬件测试认证'],
    salary: '13k-23k',
    prospect: '可向硬件研发验证专家或测试主管方向发展。'
  },
  科研人员: {
    role: '科研人员',
    duty: '负责前沿技术研究、实验验证与成果转化。',
    skills: [
      { name: '研究方法', value: 88 },
      { name: '论文写作', value: 82 },
      { name: '编程与数据分析', value: 80 },
      { name: '学术表达', value: 76 }
    ],
    abilities: [
      { name: '创新能力', stars: 5, score: 90 },
      { name: '问题解决', stars: 5, score: 88 },
      { name: '沟通能力', stars: 4, score: 74 },
      { name: '学习能力', stars: 5, score: 92 },
      { name: '团队合作', stars: 4, score: 78 }
    ],
    certificates: ['科研项目管理证书', '数据分析认证'],
    salary: '17k-32k',
    prospect: '可向高级研究员、技术专家、产业转化方向发展。'
  },
  软件测试: {
    role: '软件测试工程师',
    duty: '负责功能、性能与自动化测试，推动研发质量持续提升。',
    skills: [
      { name: '功能测试', value: 85 },
      { name: '性能测试', value: 76 },
      { name: '自动化框架', value: 72 },
      { name: '质量工具链', value: 74 }
    ],
    abilities: [
      { name: '细节把控', stars: 5, score: 87 },
      { name: '问题解决', stars: 4, score: 82 },
      { name: '沟通能力', stars: 4, score: 76 },
      { name: '学习能力', stars: 4, score: 80 },
      { name: '团队合作', stars: 4, score: 81 }
    ],
    certificates: ['ISTQB', '软考中级'],
    salary: '14k-24k',
    prospect: '可向测试架构师、质量平台方向发展。'
  },
  '项目经理/主管': {
    role: '项目经理/主管',
    duty: '负责项目全周期管理、资源协调与交付目标达成。',
    skills: [
      { name: '项目管理', value: 90 },
      { name: '需求管理', value: 84 },
      { name: '风险控制', value: 82 },
      { name: '跨团队协同', value: 88 }
    ],
    abilities: [
      { name: '组织协调', stars: 5, score: 92 },
      { name: '问题解决', stars: 5, score: 88 },
      { name: '沟通能力', stars: 5, score: 93 },
      { name: '学习能力', stars: 4, score: 80 },
      { name: '团队合作', stars: 5, score: 90 }
    ],
    certificates: ['PMP', '信息系统项目管理师'],
    salary: '24k-43k',
    prospect: '可向高级项目总监、产品负责人与管理层方向发展。'
  }
}

const normalizeJobKey = (jobName) => {
  const text = String(jobName || '').trim()
  if (!text) return ''
  if (text.includes('_')) return text
  const matched = text.match(/^(.*?)(初级|中级|高级|顶级)$/)
  if (matched) {
    return `${matched[1]}_${matched[2]}`
  }
  return text
}

const parseTechnicalSkills = (value) => {
  if (Array.isArray(value)) {
    return value
  }
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }
  return []
}

const sortSkillsByScoreDesc = (skills) => {
  return [...(Array.isArray(skills) ? skills : [])].sort((a, b) => (Number(b?.score) || 0) - (Number(a?.score) || 0))
}

const transferColumns = [
  'C/C++_中级',
  'C/C++_初级',
  'C/C++_高级',
  'Java_中级',
  'Java_初级',
  'Java_高级',
  '前端开发_中级',
  '前端开发_初级',
  '前端开发_高级',
  '实施工程师_中级',
  '实施工程师_初级',
  '实施工程师_高级',
  '技术支持工程师_中级',
  '技术支持工程师_初级',
  '技术支持工程师_高级',
  '测试工程师_中级',
  '测试工程师_初级',
  '测试工程师_高级',
  '硬件测试_中级',
  '硬件测试_初级',
  '硬件测试_高级',
  '科研人员_顶级',
  '软件测试_中级',
  '软件测试_初级',
  '软件测试_高级',
  '项目经理/主管_顶级'
]

const PORTRAIT_JOB_API = 'http://localhost:8080/api/portrait/job_name'

const normalizePortraitJobOptions = (list) => {
  return [...new Set(
    (Array.isArray(list) ? list : [])
      .map((item) => String(item || '').trim())
      .filter(Boolean)
  )]
}

const portraitJobOptions = ref([...transferColumns])
const portraitJobLoading = ref(false)
const portraitJobError = ref('')

const allJobsForPortrait = computed(() => {
  return portraitJobOptions.value.length ? portraitJobOptions.value : transferColumns
})

const portraitApiData = ref(null)
const loadingKey = ref('')

const jobGraphLoading = ref(false)
const jobGraphError = ref('')
const jobGraphDebug = ref({ focusJob: '', nodeCount: 0, edgeCount: 0 })
/** 与接口同步，用于垂直晋升 / 换岗路径面板（不含 vis 坐标） */
const pathGraphSnapshot = ref(null)

const levelRankFromJobName = (name) => {
  const s = String(name || '')
  if (s.includes('顶级')) return 4
  if (s.includes('高级')) return 3
  if (s.includes('中级')) return 2
  if (s.includes('初级')) return 1
  return 0
}

const skillsFromGraphEdge = (edge) => {
  if (!edge) return []
  const g = edge.gap_skills ?? edge.skills_gap ?? edge.skill_gap ?? edge.skills ?? edge.gapSkills
  if (Array.isArray(g)) return g.map(String).filter(Boolean).slice(0, 14)
  if (typeof g === 'string' && g.trim()) {
    try {
      const p = JSON.parse(g)
      if (Array.isArray(p)) return p.map(String).filter(Boolean).slice(0, 14)
    } catch {
      /* ignore */
    }
    return g
      .split(/[,，、;/]/)
      .map((x) => x.trim())
      .filter(Boolean)
      .slice(0, 14)
  }
  return []
}

const buildVerticalPromoteSteps = (rawNodes, rawEdges, focusName) => {
  const focusNode = rawNodes.find((n) => n.name === focusName)
  if (!focusNode) return []
  const promoteEdges = rawEdges.filter((e) => e.type === 'PROMOTE_TO')
  const idToName = (id) => rawNodes.find((n) => n.id === id)?.name || ''
  const byFrom = new Map()
  for (const e of promoteEdges) {
    const k = String(e.from)
    if (!byFrom.has(k)) byFrom.set(k, [])
    byFrom.get(k).push(e)
  }
  for (const list of byFrom.values()) {
    list.sort(
      (a, b) =>
        levelRankFromJobName(idToName(a.to)) - levelRankFromJobName(idToName(b.to)) ||
        String(idToName(a.to)).localeCompare(String(idToName(b.to)), 'zh-CN')
    )
  }
  const chain = [focusName]
  let cur = focusNode.id
  const visited = new Set([String(cur)])
  while (true) {
    const outs = byFrom.get(String(cur))
    if (!outs?.length) break
    const picked = outs.find((e) => !visited.has(String(e.to)))
    if (!picked) break
    visited.add(String(picked.to))
    chain.push(idToName(picked.to))
    cur = picked.to
  }
  const steps = []
  for (let i = 0; i < chain.length - 1; i++) {
    const from = chain[i]
    const to = chain[i + 1]
    const fromId = rawNodes.find((n) => n.name === from)?.id
    const edge = promoteEdges.find(
      (e) => String(e.from) === String(fromId) && idToName(e.to) === to
    )
    steps.push({ from, to, skills: skillsFromGraphEdge(edge) })
  }
  return steps
}

const buildTransferBranchesFromGraph = (rawNodes, rawEdges, focusName) => {
  const focusNode = rawNodes.find((n) => n.name === focusName)
  if (!focusNode) return []
  const idToName = (id) => rawNodes.find((n) => n.id === id)?.name || ''
  return rawEdges
    .filter((e) => e.type === 'CAN_TRANSFER_TO' && String(e.from) === String(focusNode.id))
    .map((e) => ({
      target: idToName(e.to),
      skills: skillsFromGraphEdge(e)
    }))
    .filter((b) => b.target)
}

const pathGraphVerticalSteps = computed(() => {
  const portrait = portraitApiData.value
  if (Array.isArray(portrait?.steps) && portrait.steps.length) {
    return portrait.steps.map((step) => ({
      from: String(step?.from || ''),
      to: String(step?.to || ''),
      skills: Array.isArray(step?.need_skills) ? step.need_skills.map(String).filter(Boolean) : []
    }))
  }
  const snap = pathGraphSnapshot.value
  if (!snap?.focusJob || !snap.nodes?.length) return []
  return buildVerticalPromoteSteps(snap.nodes, snap.edges, snap.focusJob)
})

const pathGraphTransferBranches = computed(() => {
  const portrait = portraitApiData.value
  if (Array.isArray(portrait?.transferPaths) && portrait.transferPaths.length) {
    return portrait.transferPaths.map((item) => ({
      target: String(item?.target_job || ''),
      skills: Array.isArray(item?.need_skills) ? item.need_skills.map(String).filter(Boolean) : []
    })).filter((branch) => branch.target)
  }
  const snap = pathGraphSnapshot.value
  if (!snap?.focusJob || !snap.nodes?.length) return []
  return buildTransferBranchesFromGraph(snap.nodes, snap.edges, snap.focusJob)
})

const pathGraphFocusShortLabel = computed(() => {
  const snap = pathGraphSnapshot.value
  const key = snap?.focusJob || selectedGraphJobKey.value
  return formatGraphRole(key)
})

const normalizeGraphRole = (role) => {
  const text = String(role || '').trim()
  if (!text) return ''
  if (text.includes('_')) return text
  const matched = text.match(/^(.*?)(初级|中级|高级|顶级)$/)
  if (matched) {
    return `${matched[1]}_${matched[2]}`
  }
  return text
}

const formatGraphRole = (role) => normalizeGraphRole(role).replace('_', '')

const activeMenu = ref('job-data')
const keyword = ref('')
const selectedCategory = ref('')
const selectedScale = ref('')
const selectedSort = ref('')
const currentPage = ref(1)
const pageSize = 8
const selectedPortraitJob = ref('Java_初级')
const selectedJobDetail = ref(null)
const showPortraitSelector = ref(false)
const jobDetailSectionRef = ref(null)

const resolvePortraitJobKey = (value) => {
  const normalized = normalizeJobKey(value)
  const availableJobs = allJobsForPortrait.value
  return availableJobs.includes(normalized) ? normalized : (availableJobs[0] || 'Java_初级')
}

const resolveCategoryFromQuery = (value) => {
  const queryValue = Array.isArray(value) ? value[0] : value
  const normalized = String(queryValue || '').trim()
  return categoryOptions.includes(normalized) ? normalized : ''
}

const selectedGraphJobKey = computed(() => normalizeJobKey(selectedPortraitJob.value))

const selectedGraphJobBase = computed(() => {
  const key = String(selectedGraphJobKey.value || '')
  return key.split('_')[0] || key
})

const selectedGraphJobLabel = computed(() => formatGraphRole(selectedGraphJobKey.value))

const activePortrait = computed(() => {
  const apiData = portraitApiData.value
  if (apiData) {
    const technicalSkills = parseTechnicalSkills(apiData.technical_skills)
    const sortedTechnicalSkills = sortSkillsByScoreDesc(technicalSkills)
    const scoreBase = sortedTechnicalSkills.reduce((max, item) => Math.max(max, Number(item?.score) || 0), 1)
    const skills = sortedTechnicalSkills.map((item) => {
      const score = Number(item?.score) || 0
      return {
        name: item?.name || '未命名技能',
        score,
        value: Math.min(100, Math.max(0, Math.round((score / scoreBase) * 100)))
      }
    })
    const roleName = normalizeJobKey(`${apiData.job_name || ''}_${apiData.level || ''}`.replace(/^_+|_+$/g, ''))
    const certText = String(apiData.certificate_requirement || '').trim()
    const certificates = certText && certText !== '无' ? certText.split(/[、,，/]/).map((item) => item.trim()).filter(Boolean) : []

    return {
      role: roleName || selectedPortraitJob.value,
      duty: String(apiData.internship_ability || apiData.communication_ability || '暂无岗位职责说明'),
      skills,
      certificates,
      salary: String(apiData.experience_requirement || '经验要求待完善'),
      prospect: String(apiData.learning_ability || '暂无发展前景描述')
    }
  }

  const fallbackKey = String(selectedPortraitJob.value || '').split('_')[0]
  return portraitData[fallbackKey] || portraitData.Java
})

const portraitAbilityScores = computed(() => {
  const apiData = portraitApiData.value
  if (!apiData) return null

  const abilityTextMap = {
    创新能力: String(apiData.innovation_ability || ''),
    学习能力: String(apiData.learning_ability || ''),
    抗压能力: String(apiData.stress_resistance || ''),
    沟通能力: String(apiData.communication_ability || ''),
    实习能力: String(apiData.internship_ability || '')
  }

  const scoreByTextLength = (text) => {
    const len = text.trim().length
    if (!len) return 60
    return Math.min(98, Math.max(65, 60 + Math.round(len / 3)))
  }

  const scoreMap = {}
  abilityNames.forEach((name) => {
    scoreMap[name] = scoreByTextLength(abilityTextMap[name] || '')
  })
  return scoreMap
})

const fetchPortraitData = async (jobKey) => {
  const response = await axios.post('http://127.0.0.1:8080/api/portrait/find', {
    job_name: encodeURIComponent(jobKey) 
  })
  if (response?.data?.code === 200 && response?.data?.data) {
    return response.data.data
  }
  return null
}

const fetchPortraitJobOptions = async () => {
  portraitJobLoading.value = true
  portraitJobError.value = ''
  try {
    const { data } = await axios.post(PORTRAIT_JOB_API)
    const remoteJobs = normalizePortraitJobOptions(data?.data)
    if (remoteJobs.length) {
      portraitJobOptions.value = remoteJobs
    } else {
      portraitJobOptions.value = [...transferColumns]
      portraitJobError.value = '岗位列表为空，已回退到本地默认列表。'
    }

    const availableJobs = allJobsForPortrait.value
    if (!availableJobs.includes(selectedPortraitJob.value) && availableJobs.length) {
      selectedPortraitJob.value = availableJobs[0]
    }
  } catch (error) {
    console.warn('加载岗位名称列表失败，已使用本地默认列表', error)
    portraitJobOptions.value = [...transferColumns]
    portraitJobError.value = '岗位列表加载失败，已使用本地默认列表。'

    const availableJobs = allJobsForPortrait.value
    if (!availableJobs.includes(selectedPortraitJob.value) && availableJobs.length) {
      selectedPortraitJob.value = availableJobs[0]
    }
  } finally {
    portraitJobLoading.value = false
  }
}

const refreshPortrait = async (jobKey) => {
  const safeKey = String(jobKey || '').trim()
  if (!safeKey) return
  loadingKey.value = safeKey
  try {
    const portrait = await fetchPortraitData(safeKey)

    if (loadingKey.value !== safeKey) {
      return
    }
    portraitApiData.value = portrait
  } catch (error) {
    if (loadingKey.value !== safeKey) {
      return
    }
    console.error('请求岗位画像失败:', error)
    portraitApiData.value = null
  }
}

const renderJobGraphNetwork = async () => {
  if (!selectedGraphJobKey.value) return
  jobGraphLoading.value = true
  jobGraphError.value = ''
  try {
    const { data } = await axios.get(`${API_BASE}/career/path/graph`, { params: { jobKey: selectedGraphJobKey.value } })

    if (!data?.data?.focusJob) {
      jobGraphError.value = '图谱数据为空'
      jobGraphDebug.value = { focusJob: '', nodeCount: 0, edgeCount: 0 }
      pathGraphSnapshot.value = null
      return
    }

    const graphData = data.data
    const rawNodes = graphData.nodes || []
    const rawEdges = graphData.edges || []
    pathGraphSnapshot.value = {
      focusJob: graphData.focusJob || '',
      nodes: rawNodes,
      edges: rawEdges
    }
    jobGraphDebug.value = {
      focusJob: graphData.focusJob || '',
      nodeCount: rawNodes.length,
      edgeCount: rawEdges.length
    }

    // 数据中心仅保留「垂直岗位图谱 / 换岗路径图谱」，不再渲染 Neo4j 画布
  } catch (e) {
    console.error('岗位图谱渲染失败:', e)
    jobGraphError.value = '图谱渲染失败，请确认后端 /career/path/graph 可用且 Neo4j 已连接。'
    jobGraphDebug.value = { focusJob: '', nodeCount: 0, edgeCount: 0 }
    pathGraphSnapshot.value = null
  } finally {
    jobGraphLoading.value = false
  }
}

const fetchJobListData = async () => {
  jobListLoading.value = true
  jobListError.value = ''
  try {
    const { data } = await axios.get(CAREER_LIST_API, {
      params: {
        jobName: keyword.value || undefined,
        workArea: undefined,
        lowSalary: undefined,
        highSalary: undefined,
        page: currentPage.value,
        size: pageSize
      }
    })

    // 契约格式：{ code, msg, data: [...] }
    const sourceList = Array.isArray(data?.data) ? data.data : []
    jobListData.value = sourceList.map((item) => ({
      id: item?.id,
      name: String(item?.jobName || item?.name || ''),
      category: String(item?.jobCategory || item?.category || ''),
      salaryMin: Number(item?.lowSalary ?? item?.salaryMin ?? 0),
      salaryMax: Number(item?.highSalary ?? item?.salaryMax ?? 0),
      companyScale: String(item?.companyScale || ''),
      companyName: String(item?.companyName || ''),
      address: String(item?.workArea || item?.address || ''),
      jobDetail: String(item?.jobDuty || item?.jobDetail || ''),
      sourceUrl: String(item?.sourceUrl || '')
    }))
    jobListTotal.value = Number(data?.total ?? data?.meta?.total ?? sourceList.length)
  } catch (error) {
    console.error('请求岗位数据失败:', error)
    jobListData.value = []
    jobListTotal.value = 0
    jobListError.value = '岗位数据加载失败，请检查 /api/career/list 服务与登录 token。'
  } finally {
    jobListLoading.value = false
  }
}

watch(
  selectedPortraitJob,
  (jobKey) => {
    refreshPortrait(jobKey)
  },
  { immediate: true }
)

watch(
  () => [route.path, route.query.jobKey],
  ([path, queryJobKey]) => {
    if (path !== '/data-center/job-portrait') return
    const resolvedKey = resolvePortraitJobKey(queryJobKey)
    if (selectedPortraitJob.value !== resolvedKey) {
      selectedPortraitJob.value = resolvedKey
    }
  },
  { immediate: true }
)

watch(
  () => [route.path, route.query.category],
  ([path, queryCategory]) => {
    if (path !== '/data-center/job-data') return
    const resolvedCategory = resolveCategoryFromQuery(queryCategory)
    if (selectedCategory.value !== resolvedCategory) {
      selectedCategory.value = resolvedCategory
      currentPage.value = 1
    }
  },
  { immediate: true }
)

const filteredJobList = computed(() => {
  const withCategory = selectedCategory.value && selectedCategory.value !== '全部'
    ? jobListData.value.filter((item) => String(item.category || '') === selectedCategory.value)
    : jobListData.value
  const withScale = selectedScale.value && selectedScale.value !== '全部'
    ? withCategory.filter((item) => String(item.companyScale || '') === selectedScale.value)
    : withCategory
  return [...withScale].sort((a, b) => b.salaryMax - a.salaryMax)
})

// 服务端分页：total 来自后端，pagedJobs 直接用当前页数据（后端已分好页）
const totalPages = computed(() => Math.max(1, Math.ceil(jobListTotal.value / pageSize)))

const pagedJobs = computed(() => filteredJobList.value)

const resolveGraphRoleCategory = (role) => {
  if (/C\/C\+\+|C\+\+/.test(role)) return 'C/C++'
  if (/Java/.test(role)) return 'Java'
  if (/前端/.test(role)) return '前端开发'
  if (/实施/.test(role)) return '实施工程师'
  if (/运维|SRE|技术支持/.test(role)) return '技术支持与运维'
  if (/硬件/.test(role)) return '硬件测试'
  if (/科研|研究员|首席/.test(role)) return '科研人员'
  if (/软件测试|自动化测试|质量/.test(role)) return '软件测试'
  if (/测试/.test(role)) return '测试工程师'
  if (/项目经理|总监|主管/.test(role)) return '项目经理/主管'
  return 'Java'
}

const skillMetaByRole = {
  'C/C++': {
    'C/C++': { desc: '用于构建高性能核心模块与底层能力。', focus: '内存管理、指针安全、工程规范。', scene: '核心引擎、性能敏感服务开发。' },
    Linux: { desc: '作为开发与调试运行环境支撑日常工作。', focus: '系统命令、进程网络、日志定位。', scene: '服务器部署、故障排查与调优。' },
    '数据结构与算法': { desc: '支撑复杂问题建模与高效实现。', focus: '时间空间复杂度与常见结构。', scene: '性能优化、算法设计与重构。' },
    '网络编程': { desc: '支撑分布式通信与服务互联。', focus: 'TCP/IP、并发连接、协议处理。', scene: '网关通信、实时交互系统。' }
  },
  Java: {
    Java: { desc: '承担后端主业务逻辑与服务能力实现。', focus: '并发编程、JVM、工程编码规范。', scene: '业务接口、服务编排与性能优化。' },
    'Spring/SpringBoot': { desc: '用于快速构建稳定的企业级后端服务。', focus: 'IOC/AOP、自动配置、分层设计。', scene: '微服务开发、接口治理与交付。' },
    MySQL: { desc: '用于核心业务数据存储与事务处理。', focus: '索引设计、SQL 调优、事务隔离。', scene: '订单、用户、报表等数据场景。' },
    Redis: { desc: '用于缓存加速和高并发热点处理。', focus: '数据结构、缓存策略、持久化。', scene: '缓存层、分布式锁、会话管理。' },
    Linux: { desc: '支撑服务部署、运行维护与故障定位。', focus: '系统命令、网络排查、进程管理。', scene: '线上发布、日志分析与巡检。' }
  },
  前端开发: {
    'JavaScript/TypeScript': { desc: '负责前端交互逻辑与工程代码实现。', focus: '语法类型、异步流程、模块化。', scene: '页面交互、状态管理、数据渲染。' },
    'Vue/React': { desc: '用于组件化开发与页面结构组织。', focus: '组件设计、生命周期、路由状态。', scene: '业务页面搭建与体验优化。' },
    工程化构建: { desc: '提升前端项目可维护性与发布效率。', focus: '构建工具、包管理、规范流程。', scene: '多环境构建、持续集成发布。' },
    性能优化: { desc: '用于提升加载速度与运行流畅度。', focus: '首屏优化、资源压缩、缓存策略。', scene: '高访问页面与复杂交互优化。' }
  },
  实施工程师: {
    系统部署: { desc: '负责客户现场系统部署与上线配置。', focus: '安装流程、环境配置、参数校验。', scene: '项目交付实施与验收。' },
    需求理解: { desc: '用于将业务诉求转化为可执行方案。', focus: '需求澄清、流程梳理、边界确认。', scene: '项目启动与方案评审。' },
    SQL基础: { desc: '支撑数据初始化与问题排查处理。', focus: '基础查询、更新脚本、数据校验。', scene: '实施过程中的数据处理。' },
    项目协同: { desc: '保障跨角色协作与交付进度稳定。', focus: '任务推进、沟通反馈、风险跟踪。', scene: '客户沟通、团队协同交付。' }
  },
  '技术支持与运维': {
    'Linux运维': { desc: '支撑生产环境服务稳定运行。', focus: '系统管理、服务巡检、故障定位。', scene: '线上服务保障与值班响应。' },
    脚本开发: { desc: '用于自动化运维与批量任务处理。', focus: 'Shell/Python 脚本编写规范。', scene: '巡检自动化与批处理任务。' },
    监控体系: { desc: '构建服务可观测与告警机制。', focus: '指标采集、阈值配置、告警闭环。', scene: '故障预警与稳定性治理。' },
    容器化: { desc: '提升环境一致性与交付效率。', focus: '镜像管理、编排基础、资源隔离。', scene: '应用部署与弹性扩缩容。' }
  },
  测试工程师: {
    测试设计: { desc: '保障测试覆盖与用例有效性。', focus: '等价类、边界值、场景拆解。', scene: '需求评审到测试执行全流程。' },
    接口测试: { desc: '验证服务接口正确性与稳定性。', focus: '接口协议、参数校验、断言设计。', scene: '后端联调与回归测试。' },
    自动化测试: { desc: '提升回归效率并降低人工成本。', focus: '脚本框架、用例维护、持续集成。', scene: '版本迭代回归与稳定性验证。' },
    质量分析: { desc: '用于缺陷复盘与质量改进决策。', focus: '指标统计、缺陷归因、风险评估。', scene: '测试总结与质量改进会议。' }
  },
  硬件测试: {
    硬件原理: { desc: '支撑硬件功能分析与问题定位。', focus: '电路基础、信号流程、模块原理。', scene: '板卡测试与故障分析。' },
    测试仪器使用: { desc: '用于精确采集硬件测试数据。', focus: '示波器等仪器操作与校准。', scene: '性能测试与稳定性验证。' },
    故障分析: { desc: '快速定位硬件异常并反馈改进。', focus: '异常复现、日志比对、定位路径。', scene: '异常排障与质量改进闭环。' },
    数据处理: { desc: '用于测试数据整理与报告输出。', focus: '数据清洗、趋势分析、报告呈现。', scene: '测试总结与评审汇报。' }
  },
  科研人员: {
    研究方法: { desc: '用于课题设计与研究路径规划。', focus: '研究设计、实验对照、结论验证。', scene: '课题立项与研究推进。' },
    论文写作: { desc: '用于技术成果沉淀与学术表达。', focus: '论文结构、论证逻辑、图表表达。', scene: '论文发表与项目结题。' },
    '编程与数据分析': { desc: '用于实验数据处理与模型验证。', focus: '数据分析流程与脚本实现。', scene: '实验分析与结果复现。' },
    学术表达: { desc: '用于研究成果展示与学术沟通。', focus: '汇报结构、答辩逻辑、观点表达。', scene: '组会汇报、学术交流与答辩。' }
  },
  软件测试: {
    功能测试: { desc: '保障功能需求完整实现与可用性。', focus: '场景覆盖、异常分支、回归验证。', scene: '版本测试与上线前验证。' },
    性能测试: { desc: '评估系统在压力下的稳定表现。', focus: '压测模型、瓶颈定位、结果分析。', scene: '高并发场景性能评估。' },
    自动化框架: { desc: '用于搭建可复用测试能力体系。', focus: '框架选型、脚本组织、执行策略。', scene: '持续回归与质量门禁建设。' },
    质量工具链: { desc: '支撑测试全流程管理与协同。', focus: '缺陷系统、测试平台、指标追踪。', scene: '测试过程治理与质量看板。' }
  },
  '项目经理/主管': {
    项目管理: { desc: '用于项目全生命周期统筹推进。', focus: '计划拆解、里程碑、风险管理。', scene: '跨团队项目交付管理。' },
    需求管理: { desc: '用于需求澄清、优先级排序与变更控制。', focus: '需求分析、范围界定、版本规划。', scene: '产品与研发协同推进。' },
    风险控制: { desc: '提前识别并降低项目交付风险。', focus: '风险识别、预案制定、跟踪闭环。', scene: '关键节点质量与进度保障。' },
    跨团队协同: { desc: '打通多角色协作链路提升执行效率。', focus: '沟通机制、冲突处理、决策同步。', scene: '多部门协同与资源协调。' }
  }
}

const flatPortraitSkills = computed(() => {
  const roleKey = resolveGraphRoleCategory(activePortrait.value.role)
  const roleMeta = skillMetaByRole[roleKey] || {}
  const normalizedSkills = (Array.isArray(activePortrait.value.skills) ? activePortrait.value.skills : []).map((skill) => {
    const score = Number(skill?.score ?? skill?.rawScore ?? skill?.value) || 0
    return { ...skill, score }
  })

  return sortSkillsByScoreDesc(normalizedSkills).map((skill) => {
    const meta = roleMeta[skill.name] || {
      desc: `${activePortrait.value.role}中该技能用于补齐关键能力。`,
      focus: '结合项目实战持续迭代技能深度。',
      scene: '在真实业务场景中完成能力迁移。'
    }
    return { ...skill, ...meta }
  })
})

const abilityAdviceMap = {
  创新能力: { insight: '能够在常规方案之外提出可落地优化点。', advice: '每周做一次方案复盘，沉淀可复用创新思路。' },
  学习能力: { insight: '具备持续吸收新技术并转化为实践的能力。', advice: '按周制定学习计划并输出项目化学习笔记。' },
  抗压能力: { insight: '面对高强度交付场景具备稳定执行能力。', advice: '通过真实项目节奏训练任务分解与优先级管理。' },
  沟通能力: { insight: '可以有效进行需求澄清、进度同步和问题对齐。', advice: '强化结构化表达，固定进行会议结论回传。' },
  实习能力: { insight: '能在真实团队流程中快速融入并承担任务。', advice: '主动参与联调、提测、复盘，提升端到端实战经验。' }
}

const roleAbilityMap = {
  'C/C++': [84, 80, 78, 75, 73],
  Java: [82, 86, 79, 77, 74],
  前端开发: [88, 84, 76, 82, 78],
  实施工程师: [72, 78, 85, 84, 83],
  '技术支持与运维': [76, 79, 88, 81, 82],
  测试工程师: [74, 82, 81, 79, 80],
  硬件测试: [73, 77, 84, 76, 79],
  科研人员: [91, 93, 72, 74, 70],
  软件测试: [75, 80, 82, 77, 81],
  '项目经理/主管': [86, 81, 83, 92, 88]
}

const abilityNames = ['创新能力', '学习能力', '抗压能力', '沟通能力', '实习能力']

const currentAbilityItems = computed(() => {
  const key = resolveGraphRoleCategory(activePortrait.value.role)
  const apiScores = portraitAbilityScores.value
  const fallbackScores = roleAbilityMap[key] || roleAbilityMap.Java
  return abilityNames.map((name, index) => {
    const score = apiScores?.[name] ?? fallbackScores[index]
    return {
      name,
      score,
      stars: Math.max(1, Math.min(5, Math.round(score / 20)))
    }
  })
})

const radarSize = 240
const radarCenter = radarSize / 2
const radarRadius = 84

const radarAxisPoints = computed(() => {
  const total = currentAbilityItems.value.length || 1
  return currentAbilityItems.value.map((_, index) => {
    const angle = (-Math.PI / 2) + (2 * Math.PI * index) / total
    return {
      x: radarCenter + radarRadius * Math.cos(angle),
      y: radarCenter + radarRadius * Math.sin(angle)
    }
  })
})

const radarPolygonPoints = computed(() => {
  const total = currentAbilityItems.value.length || 1
  return currentAbilityItems.value.map((ability, index) => {
    const angle = (-Math.PI / 2) + (2 * Math.PI * index) / total
    const ratio = Math.min(100, Math.max(0, ability.score)) / 100
    const radius = radarRadius * ratio
    const x = radarCenter + radius * Math.cos(angle)
    const y = radarCenter + radius * Math.sin(angle)
    return `${x},${y}`
  }).join(' ')
})

const radarRingLayerCount = 5

const radarGridRings = computed(() => {
  const total = currentAbilityItems.value.length || 1
  const rings = []
  for (let step = 1; step <= radarRingLayerCount; step++) {
    const r = (radarRadius * step) / radarRingLayerCount
    const pts = []
    for (let index = 0; index < total; index++) {
      const angle = (-Math.PI / 2) + (2 * Math.PI * index) / total
      pts.push(`${radarCenter + r * Math.cos(angle)},${radarCenter + r * Math.sin(angle)}`)
    }
    rings.push(pts.join(' '))
  }
  return rings
})

const radarAnimProgress = ref(0)
let radarIntroRaf = 0

const runRadarIntro = () => {
  if (radarIntroRaf) {
    cancelAnimationFrame(radarIntroRaf)
    radarIntroRaf = 0
  }
  radarAnimProgress.value = 0
  const start = performance.now()
  const duration = 1250
  const easeOutCubic = (x) => 1 - (1 - x) ** 3
  const tick = (now) => {
    const raw = Math.min(1, (now - start) / duration)
    radarAnimProgress.value = easeOutCubic(raw)
    if (raw < 1) {
      radarIntroRaf = requestAnimationFrame(tick)
    } else {
      radarIntroRaf = 0
    }
  }
  radarIntroRaf = requestAnimationFrame(tick)
}

const radarPolygonPointsAnimated = computed(() => {
  const t = radarAnimProgress.value
  const total = currentAbilityItems.value.length || 1
  return currentAbilityItems.value
    .map((ability, index) => {
      const angle = (-Math.PI / 2) + (2 * Math.PI * index) / total
      const ratio = (Math.min(100, Math.max(0, ability.score)) / 100) * t
      const radius = radarRadius * ratio
      const x = radarCenter + radius * Math.cos(angle)
      const y = radarCenter + radius * Math.sin(angle)
      return `${x},${y}`
    })
    .join(' ')
})

const radarVertexPointsAnimated = computed(() => {
  const t = radarAnimProgress.value
  const total = currentAbilityItems.value.length || 1
  return currentAbilityItems.value.map((ability, index) => {
    const angle = (-Math.PI / 2) + (2 * Math.PI * index) / total
    const ratio = (Math.min(100, Math.max(0, ability.score)) / 100) * t
    const radius = radarRadius * ratio
    return {
      x: radarCenter + radius * Math.cos(angle),
      y: radarCenter + radius * Math.sin(angle)
    }
  })
})

const radarSectorHitPolygons = computed(() => {
  const verts = radarVertexPointsAnimated.value
  const n = verts.length
  if (!n) return []
  return verts.map((_, i) => {
    const a = verts[i]
    const b = verts[(i + 1) % n]
    return `${radarCenter},${radarCenter} ${a.x},${a.y} ${b.x},${b.y}`
  })
})

const radarHoveredIndex = ref(null)

const setRadarHover = (index) => {
  radarHoveredIndex.value = index
}

const clearRadarHover = () => {
  radarHoveredIndex.value = null
}

const radarWedgePoints = computed(() => {
  const i = radarHoveredIndex.value
  if (i == null) return ''
  const n = currentAbilityItems.value.length || 1
  const half = Math.PI / n
  const angle = (-Math.PI / 2) + (2 * Math.PI * i) / n
  const r = radarRadius * 1.02
  const x1 = radarCenter + r * Math.cos(angle - half)
  const y1 = radarCenter + r * Math.sin(angle - half)
  const x2 = radarCenter + r * Math.cos(angle + half)
  const y2 = radarCenter + r * Math.sin(angle + half)
  return `${radarCenter},${radarCenter} ${x1},${y1} ${x2},${y2}`
})

const radarAxisTickLines = computed(() => {
  const total = currentAbilityItems.value.length || 1
  const ticks = []
  const steps = [0.2, 0.4, 0.6, 0.8, 1]
  for (let ai = 0; ai < total; ai++) {
    const angle = (-Math.PI / 2) + (2 * Math.PI * ai) / total
    const cos = Math.cos(angle)
    const sin = Math.sin(angle)
    for (const s of steps) {
      const len = 5
      const r0 = radarRadius * s
      const r1 = r0 + len
      ticks.push({
        x1: radarCenter + r0 * cos,
        y1: radarCenter + r0 * sin,
        x2: radarCenter + r1 * cos,
        y2: radarCenter + r1 * sin
      })
    }
  }
  return ticks
})

watch(
  () => currentAbilityItems.value.map((a) => a.score).join(','),
  () => {
    nextTick(() => runRadarIntro())
  }
)

const abilityHoverSummaryMap = {
  创新能力: '方案创新与落地',
  学习能力: '新知吸收与转化',
  抗压能力: '高压执行与稳定',
  沟通能力: '协同表达与对齐',
  实习能力: '实战融入与交付'
}

const getAbilityHoverSummary = (name) => abilityHoverSummaryMap[name] || '能力表现'

const getRadarTipWidth = (ability) => {
  const title = `${ability.name} ${ability.score}分`
  const sub = getAbilityHoverSummary(ability.name)
  const maxLen = Math.max(title.length, sub.length)
  return Math.min(170, Math.max(106, maxLen * 12 + 18))
}

const getRadarTipTransform = (point, ability) => {
  const tipWidth = getRadarTipWidth(ability)
  const offsetX = point.x >= radarCenter ? 12 : -(tipWidth + 12)
  const offsetY = point.y < radarCenter ? -52 : 10
  return `translate(${offsetX}, ${offsetY})`
}

const radarFloatingTipTransform = computed(() => {
  const i = radarHoveredIndex.value
  if (i == null) return ''
  const pt = radarVertexPointsAnimated.value[i]
  const ability = currentAbilityItems.value[i]
  if (!pt || !ability) return ''
  return `translate(${pt.x}, ${pt.y}) ${getRadarTipTransform(pt, ability)}`
})

const certificateMetaMap = {
  '软考中级': { difficulty: '中等', requirement: '大专及以上可报考', value: '高', target: '应届生/初中级工程师', priority: '必考', material: '官方教程+真题', cycle: '2-3个月' },
  '阿里云ACP可选': { difficulty: '中等', requirement: '具备云服务基础', value: '中高', target: '云上开发岗位', priority: '选考', material: '阿里云认证课程', cycle: '1-2个月' },
  ISTQB: { difficulty: '中等', requirement: '测试基础知识', value: '中高', target: '测试/测开岗位', priority: '选考', material: 'ISTQB 大纲+题库', cycle: '1-2个月' },
  PMP: { difficulty: '较高', requirement: '项目经验要求', value: '高', target: '项目管理方向', priority: '选考', material: 'PMBOK+训练营', cycle: '3-4个月' }
}

const detailedCertificates = computed(() => {
  return activePortrait.value.certificates.map((name) => {
    const meta = certificateMetaMap[name] || {
      difficulty: '中等',
      requirement: '满足官方报名条件',
      value: '中等',
      target: '岗位相关方向',
      priority: '选考',
      material: '官方教材+实战资料',
      cycle: '1-3个月'
    }
    return { name, ...meta }
  })
})

const salaryInsightByCategory = {
  Java: {
    city: [
      { name: '一线城市', value: 28 },
      { name: '新一线', value: 22 },
      { name: '二线城市', value: 16 }
    ],
    exp: [
      { name: '0-1年', value: 14 },
      { name: '1-3年', value: 22 },
      { name: '3-5年', value: 30 }
    ],
    composition: ['基本工资 70%', '绩效奖金 20%', '年终奖 10%'],
    benefits: ['五险一金', '年度体检', '技术培训补贴'],
    level: '处于行业中上水平'
  }
}

const currentSalaryInsight = computed(() => {
  const key = resolveGraphRoleCategory(activePortrait.value.role)
  return salaryInsightByCategory[key] || salaryInsightByCategory.Java
})

const roleGrowthMap = {
  'C/C++': [
    { title: 'C/C++工程师', exp: '0-1年', skills: 'C/C++基础 / 数据结构 / Linux', salary: '10k-16k' },
    { title: '中级C++工程师', exp: '1-3年', skills: '网络编程 / 性能优化 / 工程规范', salary: '16k-24k' },
    { title: '高级C++工程师', exp: '3-5年', skills: '跨平台架构 / 高并发 / 稳定性治理', salary: '24k-35k' },
    { title: 'C++架构专家', exp: '5年+', skills: '系统架构 / 技术决策 / 团队带教', salary: '35k-48k' }
  ],
  Java: [
    { title: 'Java工程师', exp: '0-1年', skills: 'Java 基础 / SQL / Spring Boot', salary: '12k-18k' },
    { title: '中级Java工程师', exp: '1-3年', skills: '微服务 / Redis / 性能优化', salary: '18k-28k' },
    { title: '高级Java工程师', exp: '3-5年', skills: '架构设计 / 中间件 / 稳定性治理', salary: '28k-38k' },
    { title: 'Java架构师', exp: '5年+', skills: '系统架构 / 技术决策 / 团队带教', salary: '38k-55k' }
  ],
  前端开发: [
    { title: '前端工程师', exp: '0-1年', skills: 'HTML/CSS/JS / 组件开发', salary: '10k-16k' },
    { title: '中级前端工程师', exp: '1-3年', skills: '工程化 / 性能优化 / 联调协作', salary: '16k-24k' },
    { title: '高级前端工程师', exp: '3-5年', skills: '架构设计 / 可视化 / 稳定性建设', salary: '24k-34k' },
    { title: '前端架构师', exp: '5年+', skills: '平台化建设 / 技术规划 / 团队带教', salary: '34k-46k' }
  ],
  实施工程师: [
    { title: '实施工程师', exp: '0-1年', skills: '部署实施 / 客户沟通 / 文档编写', salary: '8k-12k' },
    { title: '高级实施顾问', exp: '1-3年', skills: '需求梳理 / 方案落地 / 项目推进', salary: '12k-18k' },
    { title: '实施经理', exp: '3-5年', skills: '交付管理 / 团队协同 / 风险控制', salary: '18k-26k' },
    { title: '项目经理', exp: '5年+', skills: '项目统筹 / 资源调度 / 业务协同', salary: '26k-38k' }
  ],
  '技术支持与运维': [
    { title: '运维工程师', exp: '0-1年', skills: 'Linux / 监控告警 / 脚本基础', salary: '9k-14k' },
    { title: '高级运维工程师', exp: '1-3年', skills: '自动化运维 / 容器化 / 故障治理', salary: '14k-22k' },
    { title: 'SRE工程师', exp: '3-5年', skills: '稳定性工程 / 可观测性 / 容量规划', salary: '22k-32k' },
    { title: '运维负责人', exp: '5年+', skills: '平台治理 / 体系建设 / 团队管理', salary: '32k-45k' }
  ],
  测试工程师: [
    { title: '测试工程师', exp: '0-1年', skills: '测试用例 / 缺陷管理 / 功能测试', salary: '8k-13k' },
    { title: '测试开发工程师', exp: '1-3年', skills: '接口自动化 / 持续集成 / 脚本能力', salary: '13k-20k' },
    { title: '高级测试工程师', exp: '3-5年', skills: '质量体系 / 性能测试 / 平台建设', salary: '20k-30k' },
    { title: '测试架构师', exp: '5年+', skills: '质量平台 / 组织流程 / 技术规范', salary: '30k-42k' }
  ],
  硬件测试: [
    { title: '硬件测试工程师', exp: '0-1年', skills: '硬件原理 / 仪器使用 / 报告编写', salary: '8k-12k' },
    { title: '硬件验证工程师', exp: '1-3年', skills: '可靠性测试 / 故障定位 / 数据分析', salary: '12k-18k' },
    { title: '高级硬件测试工程师', exp: '3-5年', skills: '测试平台 / 方案设计 / 质量改进', salary: '18k-26k' },
    { title: '硬件测试主管', exp: '5年+', skills: '测试管理 / 跨团队协同 / 体系建设', salary: '26k-36k' }
  ],
  科研人员: [
    { title: '科研助理', exp: '0-1年', skills: '文献调研 / 实验执行 / 数据整理', salary: '10k-15k' },
    { title: '研究员', exp: '1-3年', skills: '课题推进 / 模型验证 / 技术报告', salary: '15k-24k' },
    { title: '高级研究员', exp: '3-5年', skills: '项目申报 / 成果转化 / 技术攻关', salary: '24k-34k' },
    { title: '首席研究员', exp: '5年+', skills: '方向规划 / 团队引领 / 产业协同', salary: '34k-50k' }
  ],
  软件测试: [
    { title: '软件测试工程师', exp: '0-1年', skills: '功能测试 / 回归测试 / 缺陷分析', salary: '8k-12k' },
    { title: '自动化测试工程师', exp: '1-3年', skills: '自动化框架 / 接口脚本 / CI流程', salary: '12k-19k' },
    { title: '高级软件测试工程师', exp: '3-5年', skills: '性能与稳定性 / 质量治理', salary: '19k-28k' },
    { title: '质量负责人', exp: '5年+', skills: '质量体系 / 流程优化 / 团队管理', salary: '28k-40k' }
  ],
  '项目经理/主管': [
    { title: '项目主管', exp: '0-1年', skills: '计划跟踪 / 需求沟通 / 风险识别', salary: '12k-18k' },
    { title: '项目经理', exp: '1-3年', skills: '资源协调 / 进度管理 / 交付闭环', salary: '18k-26k' },
    { title: '高级项目经理', exp: '3-5年', skills: '跨部门协同 / 项目治理 / 复盘优化', salary: '26k-36k' },
    { title: '项目总监', exp: '5年+', skills: '战略执行 / 组织管理 / 业务增长', salary: '36k-50k' }
  ]
}

const currentGrowthStages = computed(() => {
  const key = resolveGraphRoleCategory(activePortrait.value.role)
  return roleGrowthMap[key] || roleGrowthMap.Java
})

const roleProfileMap = {
  'C/C++': {
    responsibilities: ['负责核心模块与底层功能开发', '参与性能调优与稳定性改进', '协同测试定位并修复复杂缺陷']
  },
  Java: {
    responsibilities: ['负责后端业务模块设计与开发', '完成接口设计、联调与上线保障', '参与系统优化与故障排查复盘'],
  },
  前端开发: {
    responsibilities: ['负责页面交互开发与体验优化', '推进组件化与工程化规范落地', '与后端协同完成接口联调与发布']
  },
  实施工程师: {
    responsibilities: ['负责项目现场部署与环境配置', '组织客户培训并输出实施文档', '跟踪问题闭环并推动项目验收']
  },
  '技术支持与运维': {
    responsibilities: ['负责系统巡检、监控告警与故障响应', '推进自动化运维与发布流程优化', '保障线上服务稳定与容量安全']
  },
  测试工程师: {
    responsibilities: ['设计并执行测试用例覆盖核心业务', '跟踪缺陷生命周期并推动修复', '输出测试报告保障版本交付质量']
  },
  硬件测试: {
    responsibilities: ['负责硬件功能与可靠性测试验证', '搭建测试环境并采集分析测试数据', '协同研发优化硬件问题与方案']
  },
  科研人员: {
    responsibilities: ['开展前沿技术调研与实验设计', '推进研究成果验证与工程转化', '输出论文/专利/技术报告支撑项目']
  },
  软件测试: {
    responsibilities: ['负责功能、接口、回归等测试执行', '建设自动化测试脚本提升效率', '参与质量评审并沉淀测试规范']
  },
  '项目经理/主管': {
    responsibilities: ['制定项目计划并推进里程碑交付', '协调跨团队资源与风险管理', '负责项目复盘与团队协同机制优化']
  }
}

const currentRoleProfile = computed(() => {
  if (portraitApiData.value) {
    return {
      responsibilities: [
        String(portraitApiData.value.internship_ability || '').trim(),
        String(portraitApiData.value.communication_ability || '').trim(),
        String(portraitApiData.value.learning_ability || '').trim()
      ].filter(Boolean)
    }
  }
  const key = resolveGraphRoleCategory(activePortrait.value.role)
  return roleProfileMap[key] || roleProfileMap.Java
})

watch([keyword, selectedCategory, selectedScale, selectedSort], () => {
  currentPage.value = 1
  fetchJobListData()
})

// 翻页时重新从后端拉取数据
watch(currentPage, () => {
  fetchJobListData()
})

watch(totalPages, (value) => {
  if (currentPage.value > value) {
    currentPage.value = value
  }
})

watch(
  () => route.path,
  (path) => {
    if (path === '/data-center/job-portrait') {
      activeMenu.value = 'job-portrait'
    } else if (path === '/data-center/job-graph') {
      activeMenu.value = 'job-graph'
    } else {
      activeMenu.value = 'job-data'
    }
  },
  { immediate: true }
)

const go = (path) => {
  if (route.path !== path) {
    router.push(path)
  }
}

const selectMenu = (key) => {
  activeMenu.value = key
  if (key !== 'job-portrait') {
    showPortraitSelector.value = false
  }
  if (key === 'job-portrait' && route.path !== '/data-center/job-portrait') {
    router.push('/data-center/job-portrait')
  }
  if (key === 'job-graph' && route.path !== '/data-center/job-graph') {
    router.push('/data-center/job-graph')
  }
  if (key === 'job-data' && route.path !== '/data-center/job-data') {
    router.push('/data-center/job-data')
  }
}

const viewDetails = async (job) => {
  selectedJobDetail.value = job
  await nextTick()
  jobDetailSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const toPrevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value -= 1
  }
}

const toNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value += 1
  }
}

const salaryText = (item) => `${item.salaryMin}k-${item.salaryMax}k`

const graphFocusShortcuts = [
  { key: 'C/C++_初级', label: 'C++' },
  { key: '前端开发_初级', label: '前端' },
  { key: '测试工程师_初级', label: '测试' },
  { key: '软件测试_初级', label: '软件测试' },
  { key: 'Java_初级', label: 'Java' }
]

watch([activeMenu, selectedGraphJobKey], async ([menu]) => {
  if (menu !== 'job-graph') return
  await nextTick()
  renderJobGraphNetwork()
})

onBeforeUnmount(() => {
  if (radarIntroRaf) {
    cancelAnimationFrame(radarIntroRaf)
    radarIntroRaf = 0
  }
})

onMounted(() => {
  fetchPortraitJobOptions()
  fetchJobListData()
  runRadarIntro()
})
</script>

<template>
  <div class="data-center-page">
    <header class="top-nav card-surface">
      <div class="brand">
        <span>职绘智配</span>
        <img class="brand-logo" src="/logo.png" alt="职绘智配logo" />
      </div>
      <nav class="nav-list">
        <button
          v-for="item in navItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: route.path === item.path || (item.path === '/data-center' && route.path.startsWith('/data-center')) }"
          @click="go(item.path)"
        >
          {{ item.label }}
        </button>
      </nav>
    </header>

    <main class="layout-wrap">
      <aside class="side-menu card-surface">
        <button
          v-for="menu in sidebarMenus"
          :key="menu.key"
          class="side-item"
          :class="{ active: activeMenu === menu.key }"
          @click="selectMenu(menu.key)"
        >
          {{ menu.label }}
        </button>
      </aside>

      <section class="content-area">
        <article v-if="activeMenu === 'job-data'" class="pane card-surface">
          <section class="filter-wrap card-surface">
            <input v-model.trim="keyword" class="filter-input" placeholder="搜索岗位名称或关键词" />
            <select v-model="selectedCategory" class="filter-select">
              <option disabled value="">分类</option>
              <option value="全部">全部</option>
              <option v-for="item in categoryOptions" :key="item" :value="item">{{ item }}</option>
            </select>
            <select v-model="selectedScale" class="filter-select">
              <option disabled value="">公司规模</option>
              <option value="全部">全部</option>
              <option v-for="item in companyScaleOptions" :key="item" :value="item">{{ item }}</option>
            </select>
            <select v-model="selectedSort" class="filter-select">
              <option disabled value="">排序</option>
              <option v-for="item in sortOptions" :key="item" :value="item">{{ item }}</option>
            </select>
          </section>

          <section class="list-wrap card-surface">
            <table class="job-table">
              <thead>
                <tr>
                  <th>岗位名称</th>
                  <th>分类</th>
                  <th>薪资范围</th>
                  <th>公司规模</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="job in pagedJobs" :key="job.id">
                  <td>{{ job.name }}</td>
                  <td><span class="tag">{{ job.category }}</span></td>
                  <td>{{ salaryText(job) }}</td>
                  <td>{{ job.companyScale }}</td>
                  <td><button class="small-btn" @click="viewDetails(job)">查看详情</button></td>
                </tr>
                <tr v-if="jobListLoading">
                  <td colspan="5">岗位数据加载中...</td>
                </tr>
                <tr v-else-if="jobListError">
                  <td colspan="5">{{ jobListError }}</td>
                </tr>
                <tr v-else-if="!pagedJobs.length">
                  <td colspan="5">暂无符合条件的岗位数据</td>
                </tr>
              </tbody>
            </table>
            <div class="pagination">
              <button class="page-btn" :disabled="currentPage === 1" @click="toPrevPage">上一页</button>
              <span>第{{ currentPage }}/{{ totalPages }}页（共{{ jobListTotal }}条）</span>
              <button class="page-btn" :disabled="currentPage === totalPages" @click="toNextPage">下一页</button>
            </div>

            <section ref="jobDetailSectionRef" v-if="selectedJobDetail" class="job-detail-card card-surface">
              <h3>公司详情</h3>
              <p><strong>公司名字：</strong>{{ selectedJobDetail.companyName }}</p>
              <p><strong>地址：</strong>{{ selectedJobDetail.address }}</p>
              <p><strong>公司规模：</strong>{{ selectedJobDetail.companyScale }}</p>
              <p><strong>岗位详情：</strong>{{ selectedJobDetail.jobDetail }}</p>
            </section>
          </section>
        </article>

        <article v-if="activeMenu === 'job-portrait'" class="pane card-surface portrait-layout">
          <section class="portrait-content card-surface">
            <div class="portrait-header">
              <h2>{{ activePortrait.role }}</h2>
              <button class="portrait-toggle-btn" @click="showPortraitSelector = !showPortraitSelector">选择岗位</button>
            </div>
            <p class="duty">{{ activePortrait.duty }}</p>

            <div class="portrait-sections">
              <section class="portrait-module card-surface module-animate">
                <h3>专业技能要求</h3>
                <div class="skill-group-list">
                  <div class="skill-grid">
                    <article v-for="skill in flatPortraitSkills" :key="skill.name" class="skill-card">
                      <div class="skill-strip-row">
                        <strong>{{ skill.name }}</strong>
                        <div class="progress"><span :style="{ width: `${skill.value}%` }"></span></div>
                        <div class="skill-right-meta">
                          <span class="skill-percent">{{ skill.score }}分</span>
                        </div>
                      </div>
                      <div class="skill-tooltip">
                        <p><strong>技能作用：</strong>{{ skill.desc }}</p>
                        <p><strong>学习重点：</strong>{{ skill.focus }}</p>
                        <p><strong>应用场景：</strong>{{ skill.scene }}</p>
                      </div>
                    </article>
                  </div>
                </div>
              </section>

              <section class="portrait-module card-surface module-animate">
                <h3>关键能力评估</h3>
                <div class="ability-layout">
                  <div class="ability-grid">
                    <article v-for="ability in currentAbilityItems" :key="ability.name" class="ability-card">
                      <h4>{{ ability.name }}</h4>
                      <p class="stars">{{ '★'.repeat(ability.stars) }}{{ '☆'.repeat(5 - ability.stars) }}</p>
                      <p class="score">{{ ability.score }}分</p>
                      <p class="ability-insight">{{ abilityAdviceMap[ability.name]?.insight }}</p>
                      <p class="ability-advice">建议：{{ abilityAdviceMap[ability.name]?.advice }}</p>
                    </article>
                  </div>
                  <div class="radar-card card-surface radar-card-tech" @mouseleave="clearRadarHover">
                    <h4>能力雷达图</h4>
                    <div class="radar-svg-wrap">
                      <svg :viewBox="`0 0 ${radarSize} ${radarSize}`" class="radar-svg radar-svg--tech" role="img" aria-label="能力雷达图">
                        <defs>
                          <radialGradient id="dcPortraitRadarFill" cx="50%" cy="50%" r="68%">
                            <stop offset="0%" stop-color="rgba(0, 242, 255, 0.55)" />
                            <stop offset="42%" stop-color="rgba(79, 130, 255, 0.28)" />
                            <stop offset="100%" stop-color="rgba(214, 0, 255, 0.14)" />
                          </radialGradient>
                          <linearGradient id="dcPortraitRadarStroke" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="#5df0ff" />
                            <stop offset="50%" stop-color="#a8d4ff" />
                            <stop offset="100%" stop-color="#ff5cff" />
                          </linearGradient>
                          <filter id="dcPortraitRadarStrokeGlow" x="-50%" y="-50%" width="200%" height="200%">
                            <feGaussianBlur in="SourceGraphic" stdDeviation="1.8" result="blur" />
                            <feMerge>
                              <feMergeNode in="blur" />
                              <feMergeNode in="SourceGraphic" />
                            </feMerge>
                          </filter>
                          <filter id="dcPortraitRadarFillSoft" x="-55%" y="-55%" width="210%" height="210%">
                            <feGaussianBlur in="SourceGraphic" stdDeviation="3.2" result="b" />
                            <feMerge>
                              <feMergeNode in="b" />
                              <feMergeNode in="SourceGraphic" />
                            </feMerge>
                          </filter>
                        </defs>

                        <g class="radar-deco-crosshairs" pointer-events="none">
                          <path d="M 10 10 L 10 22 M 4 16 L 16 16" class="radar-crosshair" />
                          <path :d="`M ${radarSize - 10} 10 L ${radarSize - 10} 22 M ${radarSize - 4} 16 L ${radarSize - 16} 16`" class="radar-crosshair" />
                          <path :d="`M 10 ${radarSize - 10} L 10 ${radarSize - 22} M 4 ${radarSize - 16} L 16 ${radarSize - 16}`" class="radar-crosshair" />
                          <path
                            :d="`M ${radarSize - 10} ${radarSize - 10} L ${radarSize - 10} ${radarSize - 22} M ${radarSize - 4} ${radarSize - 16} L ${radarSize - 16} ${radarSize - 16}`"
                            class="radar-crosshair"
                          />
                        </g>

                        <polygon
                          v-for="(ring, idx) in radarGridRings"
                          :key="`radar-ring-${idx}`"
                          :points="ring"
                          :class="['radar-sonar-ring', `radar-sonar-ring--${idx}`]"
                        />

                        <line
                          v-for="(pt, idx) in radarAxisPoints"
                          :key="`radar-axis-${idx}`"
                          class="radar-axis-ray"
                          :x1="radarCenter"
                          :y1="radarCenter"
                          :x2="pt.x"
                          :y2="pt.y"
                        />

                        <line
                          v-for="(tk, idx) in radarAxisTickLines"
                          :key="`radar-tick-${idx}`"
                          class="radar-axis-tick-line"
                          :x1="tk.x1"
                          :y1="tk.y1"
                          :x2="tk.x2"
                          :y2="tk.y2"
                        />

                        <polygon
                          v-if="radarHoveredIndex !== null && radarWedgePoints"
                          :points="radarWedgePoints"
                          class="radar-sector-highlight"
                        />

                        <polygon
                          :points="radarPolygonPointsAnimated"
                          class="radar-data-bloom"
                          filter="url(#dcPortraitRadarFillSoft)"
                          pointer-events="none"
                        />
                        <polygon
                          :points="radarPolygonPointsAnimated"
                          class="radar-data-surface"
                          fill="url(#dcPortraitRadarFill)"
                          pointer-events="none"
                        />
                        <polygon
                          :points="radarPolygonPointsAnimated"
                          class="radar-data-outline"
                          fill="none"
                          stroke="url(#dcPortraitRadarStroke)"
                          stroke-width="2.6"
                          stroke-linejoin="round"
                          filter="url(#dcPortraitRadarStrokeGlow)"
                          pointer-events="none"
                        />

                        <polygon
                          v-for="(sectorPts, sIdx) in radarSectorHitPolygons"
                          :key="`radar-sector-hit-${sIdx}`"
                          :points="sectorPts"
                          class="radar-sector-hit"
                          @mouseenter="setRadarHover(sIdx)"
                        />

                        <g
                          v-for="(point, index) in radarVertexPointsAnimated"
                          :key="`radar-vertex-${index}`"
                          class="radar-vertex-group"
                          :transform="`translate(${point.x}, ${point.y})`"
                          tabindex="0"
                          @mouseenter="setRadarHover(index)"
                          @focus="setRadarHover(index)"
                          @blur="clearRadarHover"
                        >
                          <circle r="8" class="radar-vertex-ripple" :style="{ animationDelay: `${index * 0.15}s` }" />
                          <circle r="9" class="radar-vertex-core" :style="{ animationDelay: `${index * 0.12}s` }" />
                          <circle r="22" class="radar-vertex-hit" />
                        </g>

                        <g
                          v-if="radarHoveredIndex !== null && currentAbilityItems[radarHoveredIndex]"
                          class="radar-tip-float"
                          :transform="radarFloatingTipTransform"
                          pointer-events="none"
                        >
                          <g class="radar-tip radar-tip--visible">
                            <rect
                              :width="getRadarTipWidth(currentAbilityItems[radarHoveredIndex])"
                              height="44"
                              rx="8"
                              ry="8"
                            />
                            <text x="9" y="17" class="radar-tip-title">
                              {{ currentAbilityItems[radarHoveredIndex].name }} · {{ currentAbilityItems[radarHoveredIndex].score }}分
                            </text>
                            <text x="9" y="33" class="radar-tip-sub">
                              {{ getAbilityHoverSummary(currentAbilityItems[radarHoveredIndex].name) }}
                            </text>
                          </g>
                        </g>
                      </svg>
                    </div>
                  </div>
                </div>
              </section>

              <section class="portrait-module card-surface module-animate">
                <h3>薪资范围</h3>
                <p class="plain-text">当前岗位：{{ activePortrait.salary }}</p>
                <div class="salary-layout">
                  <article class="salary-panel card-surface">
                    <h4>城市薪资对比（k）</h4>
                    <div class="salary-bars">
                      <div v-for="item in currentSalaryInsight.city" :key="`city-${item.name}`" class="salary-bar-item">
                        <span>{{ item.name }}</span>
                        <div class="salary-bar"><em :style="{ width: `${Math.min(100, item.value * 3)}%` }"></em></div>
                        <strong>{{ item.value }}k</strong>
                      </div>
                    </div>
                  </article>
                  <article class="salary-panel card-surface">
                    <h4>经验薪资对比（k）</h4>
                    <div class="salary-bars">
                      <div v-for="item in currentSalaryInsight.exp" :key="`exp-${item.name}`" class="salary-bar-item">
                        <span>{{ item.name }}</span>
                        <div class="salary-bar"><em :style="{ width: `${Math.min(100, item.value * 3)}%` }"></em></div>
                        <strong>{{ item.value }}k</strong>
                      </div>
                    </div>
                  </article>
                </div>
                <div class="salary-extra">
                  <p><strong>薪资构成：</strong>{{ currentSalaryInsight.composition.join(' / ') }}</p>
                  <p><strong>福利说明：</strong>{{ currentSalaryInsight.benefits.join('、') }}</p>
                  <p><strong>行业水平：</strong>{{ currentSalaryInsight.level }}</p>
                </div>
              </section>

              <section class="portrait-module card-surface module-animate">
                <h3>发展前景</h3>
                <div class="growth-path">
                  <article v-for="stage in currentGrowthStages" :key="stage.title" class="growth-card card-surface">
                    <h4>{{ stage.title }}</h4>
                    <p><strong>经验：</strong>{{ stage.exp }}</p>
                    <p><strong>核心技能：</strong>{{ stage.skills }}</p>
                    <p><strong>薪资范围：</strong>{{ stage.salary }}</p>
                  </article>
                </div>
                <div class="future-note">
                  <p><strong>行业趋势：</strong>{{ activePortrait.prospect }}</p>
                  <p><strong>晋升与转岗：</strong>可结合垂直岗位图谱与换岗路径图谱，规划技术晋升或横向转岗路径。</p>
                </div>
              </section>

            </div>
          </section>

          <aside v-if="showPortraitSelector" class="portrait-selector card-surface portrait-selector-popup">
            <h3>选择岗位</h3>
            <p v-if="portraitJobLoading" class="selector-state-text">正在加载岗位列表...</p>
            <p v-else-if="portraitJobError" class="selector-state-text selector-state-text--error">{{ portraitJobError }}</p>
            <button
              v-for="item in allJobsForPortrait"
              :key="item"
              class="selector-item"
              :class="{ active: selectedPortraitJob === item }"
              @click="selectedPortraitJob = item; showPortraitSelector = false"
            >
              {{ item }}
            </button>
          </aside>
        </article>

        <article v-if="activeMenu === 'job-graph'" class="pane card-surface job-graph-pane">
          <section class="module card-surface job-graph-module">
            <div class="graph-section-headings">
              <h3>职业发展路径</h3>
            </div>

            <div class="job-graph-toolbar card-surface">
              <div class="job-graph-toolbar-row">
                <label class="job-graph-label" for="job-graph-key-select">路径起点</label>
                <select id="job-graph-key-select" v-model="selectedPortraitJob" class="job-graph-select">
                  <option v-for="item in allJobsForPortrait" :key="item" :value="item">{{ item }}</option>
                </select>
                <button type="button" class="job-graph-refresh-btn" @click="renderJobGraphNetwork">重新渲染</button>
              </div>
              <div class="graph-focus-chips" aria-label="快捷起点">
                <button
                  v-for="s in graphFocusShortcuts"
                  :key="s.key"
                  type="button"
                  class="graph-focus-chip"
                  :class="{ active: selectedPortraitJob === s.key }"
                  @click="selectedPortraitJob = s.key"
                >
                  {{ s.label }}
                </button>
              </div>
            </div>

            <p v-if="jobGraphDebug.focusJob || jobGraphDebug.nodeCount" class="job-graph-meta">
              焦点节点：{{ jobGraphDebug.focusJob || '—' }} · 节点 {{ jobGraphDebug.nodeCount }} · 边 {{ jobGraphDebug.edgeCount }}
            </p>

            <section class="career-path-panel" aria-label="垂直岗位图谱">
              <h4 class="career-path-panel-title">垂直岗位图谱</h4>
              <div class="career-path-panel-body">
                <template v-if="pathGraphVerticalSteps.length">
                  <div v-for="(step, idx) in pathGraphVerticalSteps" :key="`v-${idx}-${step.from}-${step.to}`" class="ladder-row">
                    <div class="path-node path-node--blue">{{ formatGraphRole(step.from) }}</div>
                    <span class="path-arrow" aria-hidden="true">→</span>
                    <div class="path-node path-node--blue">{{ formatGraphRole(step.to) }}</div>
                    <div v-if="step.skills.length" class="path-skills">
                      <span v-for="(sk, si) in step.skills" :key="`sk-${idx}-${si}`" class="path-chip">{{ sk }}</span>
                    </div>
                  </div>
                </template>
                <p v-else class="career-path-empty">暂无垂直晋升链路</p>
              </div>
            </section>

            <section class="career-path-panel" aria-label="换岗路径图谱">
              <h4 class="career-path-panel-title">换岗路径图谱</h4>
              <div v-if="pathGraphTransferBranches.length" class="career-path-panel-body transfer-layout">
                <div class="transfer-start-col">
                  <div class="path-node path-node--focus">{{ pathGraphFocusShortLabel }}</div>
                </div>
                <div class="transfer-branches-col">
                  <div
                    v-for="(br, bi) in pathGraphTransferBranches"
                    :key="`t-${bi}-${br.target}`"
                    class="transfer-row"
                  >
                    <span class="path-arrow path-arrow--branch" aria-hidden="true">→</span>
                    <div class="path-node path-node--green">{{ formatGraphRole(br.target) }}</div>
                    <div v-if="br.skills.length" class="path-skills">
                      <span v-for="(sk, si) in br.skills" :key="`tsk-${bi}-${si}`" class="path-chip">{{ sk }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <p v-else class="career-path-panel-body career-path-empty">暂无可视换岗路径</p>
            </section>

          </section>
        </article>
      </section>
    </main>
  </div>
</template>

<style scoped>
.data-center-page {
  --page-padding: 24px;
  --nav-block-height: 92px;
  --side-menu-min-height: 580px;
  min-height: 100vh;
  height: 100vh;
  box-sizing: border-box;
  padding: calc(var(--nav-block-height) + 20px) var(--page-padding) var(--page-padding);
  overflow: hidden;
  color: #12294b;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Segoe UI', sans-serif;
  background-color: #e7f1f8;
  background-image: url('/background.jpg');
  background-repeat: no-repeat;
  background-position: center;
  background-size: cover;
}

.data-center-page::before,
.data-center-page::after {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
}

.data-center-page::before {
  display: none;
}

.data-center-page::after {
  background:
    radial-gradient(circle at 78% 34%, rgba(56, 121, 201, 0.22) 0 3px, transparent 4px),
    radial-gradient(circle at 82% 40%, rgba(56, 121, 201, 0.24) 0 3px, transparent 4px),
    radial-gradient(circle at 86% 46%, rgba(56, 121, 201, 0.2) 0 3px, transparent 4px);
  opacity: 0.65;
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
  margin-bottom: 0;
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

.nav-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.nav-item {
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
  padding: 16px 18px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.nav-item:hover,
.nav-item.active {
  background: #3879c9;
}

.layout-wrap {
  position: relative;
  z-index: 2;
  display: block;
  gap: 16px;
}

.side-menu {
  position: fixed;
  top: calc(var(--nav-block-height) + 20px);
  left: var(--page-padding);
  width: 170px;
  padding: 18px;
  min-height: var(--side-menu-min-height);
  background: rgba(231, 241, 248, 0.9);
  display: grid;
  gap: 60px;
}

.side-item {
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #12294b;
  font-size: 22px;
  font-weight: 600;
  text-align: left;
  padding: 16px 18px;
  min-height: 60px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.side-item:hover,
.side-item.active {
  background: #3879c9;
  color: #ffffff;
}

.content-area {
  display: grid;
  gap: 16px;
  margin-left: 221px;
  max-height: calc(100vh - var(--nav-block-height) - 20px - var(--page-padding));
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 2px;
}

.pane {
  background: rgba(255, 255, 255, 0.92);
  padding: 18px;
}

.filter-wrap {
  background: #ffffff;
  padding: 14px;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 12px;
  margin-bottom: 14px;
}

.filter-input,
.filter-select {
  border: 1px solid #b7d2e8;
  border-radius: 10px;
  padding: 11px 12px;
  font-size: 14px;
  color: #12294b;
  background: #f7fbff;
  outline: none;
}

.filter-input:focus,
.filter-select:focus {
  border-color: #3879c9;
  box-shadow: 0 0 0 3px rgba(56, 121, 201, 0.14);
}

.list-wrap {
  background: #ffffff;
  padding: 14px;
}

.job-table {
  width: 100%;
  border-collapse: collapse;
}

.job-table th,
.job-table td {
  padding: 12px 10px;
  border-bottom: 1px solid #e7f1f8;
  font-size: 14px;
  color: #12294b;
  text-align: left;
}

.job-table th {
  font-size: 16px;
}

.job-table th:nth-child(4),
.job-table td:nth-child(4) {
  width: 150px;
  padding-left: 24px;
}

.job-table th:nth-child(5),
.job-table td:nth-child(5) {
  width: 120px;
  padding-left: 20px;
}

.tag {
  display: inline-block;
  background: #e7f1f8;
  border-radius: 999px;
  padding: 2px 8px;
}

.heat-cell {
  display: grid;
  gap: 6px;
  width: 86px;
}

.heat-line {
  height: 6px;
  background: #e7f1f8;
  border-radius: 999px;
  overflow: hidden;
}

.heat-line span {
  display: block;
  height: 100%;
  background: #1f4d9a;
}

.heat-cell small {
  color: #1f4d9a;
}

.small-btn {
  border: none;
  border-radius: 8px;
  background: #1f4d9a;
  color: #ffffff;
  padding: 8px 12px;
  font-size: 13px;
  cursor: pointer;
}

.small-btn:hover {
  background: #3879c9;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  color: #12294b;
}

.page-btn {
  border: 1px solid #b7d2e8;
  background: #ffffff;
  color: #12294b;
  border-radius: 8px;
  padding: 8px 14px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.job-detail-card {
  margin-top: 16px;
  background: #f7fbff;
  padding: 14px;
}

.job-detail-card h3 {
  margin: 0 0 10px;
  font-size: 22px;
  color: #ffffff;
}

.job-detail-card p {
  margin: 0 0 8px;
  line-height: 1.7;
  font-size: 14px;
  color: #b8f8ff;
}

.job-detail-card p strong {
  color: #b8f8ff;
}

.portrait-layout {
  position: relative;
  display: block;
  padding-top: 17px;
}

.portrait-toggle-btn {
  position: static;
  border: none;
  border-radius: 12px;
  background: #1f4d9a;
  color: #ffffff;
  padding: 12px 22px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  z-index: 7;
}

.portrait-toggle-btn:hover {
  background: #3879c9;
}

.portrait-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.portrait-content {
  background: #ffffff;
  padding: 16px;
}

.portrait-content h2 {
  margin: 0;
  font-size: 42px;
}

.duty {
  margin: 6px 0 10px;
  color: #4f647f;
  font-size: 14px;
}

.portrait-sections {
  margin-top: 14px;
  display: grid;
  gap: 18px;
}

.portrait-content h3 {
  margin: 30px 0 12px;
  font-size: 24px;
  color: #12294b;
}

.portrait-content h3:first-of-type {
  margin-top: 24px;
}

.skill-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.portrait-module {
  background: #ffffff;
  padding: 14px;
  border-radius: 12px;
  box-shadow: 0 10px 24px rgba(18, 41, 75, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.portrait-module:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(18, 41, 75, 0.14);
}

.module-animate {
  opacity: 0;
  transform: translateY(10px);
  animation: portraitFadeIn 0.45s ease forwards;
}

.portrait-sections .module-animate:nth-child(1) { animation-delay: 0.04s; }
.portrait-sections .module-animate:nth-child(2) { animation-delay: 0.08s; }
.portrait-sections .module-animate:nth-child(3) { animation-delay: 0.12s; }
.portrait-sections .module-animate:nth-child(4) { animation-delay: 0.16s; }
.portrait-sections .module-animate:nth-child(5) { animation-delay: 0.20s; }
.portrait-sections .module-animate:nth-child(6) { animation-delay: 0.24s; }
.portrait-sections .module-animate:nth-child(7) { animation-delay: 0.28s; }
.portrait-sections .module-animate:nth-child(8) { animation-delay: 0.32s; }

.skill-group-list {
  display: grid;
  gap: 10px;
}

.skill-group {
  background: #f7fbff;
  padding: 12px;
}

.skill-group h4 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #1f4d9a;
}

.skill-card {
  background: #f7fbff;
  border-radius: 12px;
  padding: 10px 12px;
  position: relative;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.skill-card:hover {
  transform: translateY(-2px) scale(1.01);
  box-shadow: 0 10px 18px rgba(18, 41, 75, 0.12);
}

.skill-strip-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.skill-strip-row strong {
  min-width: 140px;
  font-size: 15px;
}

.skill-strip-row .progress {
  flex: 1;
}

.skill-right-meta {
  min-width: 130px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.skill-percent {
  font-size: 14px;
  font-weight: 700;
  color: #ffffff;
}

.priority-tag {
  display: inline-block;
  border-radius: 999px;
  background: #e7f1f8;
  color: #12294b;
  font-size: 12px;
  font-weight: 700;
  padding: 2px 7px;
}

.progress {
  height: 8px;
  background: #e7f1f8;
  border-radius: 999px;
  overflow: hidden;
}

.progress span {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #3879c9 0%, #1f4d9a 100%);
  border-radius: 999px;
  transition: transform 0.2s ease;
}

.skill-card:hover .progress span {
  transform: scaleY(1.15);
}

.skill-tooltip {
  position: absolute;
  left: 50%;
  bottom: calc(100% + 8px);
  transform: translate(-50%, 6px);
  width: min(280px, 76vw);
  background: rgba(8, 27, 48, 0.95);
  border: 1px solid rgba(106, 210, 255, 0.46);
  border-radius: 10px;
  padding: 10px;
  box-shadow:
    inset 0 0 16px rgba(65, 174, 255, 0.14),
    0 12px 22px rgba(2, 11, 25, 0.62);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: opacity 0.18s ease, transform 0.18s ease;
  z-index: 8;
}

.skill-tooltip p {
  margin: 0 0 6px;
  font-size: 12px;
  line-height: 1.45;
  color: #d6efff;
}

.skill-tooltip p:last-child {
  margin-bottom: 0;
}

.skill-card:hover .skill-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translate(-50%, 0);
}

.ability-layout {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 12px;
}

.ability-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.ability-card {
  background: #f7fbff;
  border-radius: 12px;
  padding: 12px;
  text-align: left;
}

.ability-card h4 {
  margin: 0 0 8px;
  font-size: 18px;
}

.stars {
  margin: 0 0 8px;
  color: #6ce3ff;
  font-size: 20px;
  letter-spacing: 1px;
}

.score {
  margin: 0 0 6px;
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
}

.ability-insight,
.ability-advice {
  margin: 0 0 4px;
  font-size: 12px;
  line-height: 1.5;
  color: #ffffff;
}

.radar-card {
  background: #f7fbff;
  padding: 12px;
}

.radar-card-tech {
  background: linear-gradient(145deg, rgba(8, 24, 48, 0.55), rgba(20, 12, 40, 0.45));
  border: 1px solid rgba(106, 210, 255, 0.22);
  box-shadow:
    0 0 0 1px rgba(0, 0, 0, 0.2) inset,
    0 12px 40px rgba(0, 40, 80, 0.25);
  backdrop-filter: blur(10px);
}

.radar-card h4,
.radar-card-tech h4 {
  margin: 0 0 12px;
  color: #1f4d9a;
  font-size: 24px;
  line-height: 1.1;
}

.radar-card-tech h4 {
  color: #c8f4ff;
  text-shadow: 0 0 18px rgba(0, 242, 255, 0.35);
}

.radar-svg-wrap {
  position: relative;
  margin: 20px auto 0;
  max-width: 280px;
}

.radar-svg {
  width: 100%;
  max-width: 260px;
  aspect-ratio: 1;
  display: block;
  margin: 24px auto 0;
  overflow: visible;
}

.radar-svg--tech {
  margin: 0 auto;
  max-width: 272px;
  transform: translateY(25px) scale(1.07);
  transform-origin: center center;
  filter: drop-shadow(0 0 12px rgba(0, 200, 255, 0.12));
}

.radar-deco-crosshairs .radar-crosshair {
  fill: none;
  stroke: rgba(120, 200, 255, 0.35);
  stroke-width: 1.2;
  stroke-linecap: square;
}

.radar-sonar-ring {
  fill: rgba(0, 242, 255, 0.04);
  stroke: rgba(140, 200, 255, 0.12);
  stroke-width: 1;
  vector-effect: non-scaling-stroke;
}

.radar-sonar-ring--0 {
  fill: rgba(0, 242, 255, 0.06);
  stroke: rgba(0, 242, 255, 0.1);
}

.radar-sonar-ring--1 {
  stroke: rgba(100, 180, 255, 0.09);
}

.radar-sonar-ring--2 {
  stroke: rgba(180, 120, 255, 0.09);
}

.radar-sonar-ring--3 {
  stroke: rgba(255, 120, 220, 0.08);
}

.radar-sonar-ring--4 {
  stroke: rgba(0, 242, 255, 0.08);
}

.radar-axis-ray {
  stroke: rgba(100, 170, 230, 0.2);
  stroke-width: 1;
}

.radar-axis-tick-line {
  stroke: rgba(160, 220, 255, 0.45);
  stroke-width: 1.4;
  stroke-linecap: round;
}

.radar-sector-highlight {
  fill: rgba(0, 242, 255, 0.14);
  stroke: rgba(0, 242, 255, 0.35);
  stroke-width: 1;
  pointer-events: none;
  transition: opacity 0.15s ease;
}

.radar-data-bloom {
  fill: rgba(0, 200, 255, 0.35);
  stroke: none;
}

.radar-data-surface {
  stroke: none;
}

.radar-data-outline {
  stroke-linecap: round;
}

.radar-sector-hit {
  fill: transparent;
  stroke: none;
  cursor: pointer;
}

.radar-vertex-group {
  cursor: pointer;
}

.radar-vertex-ripple {
  fill: none;
  stroke: rgba(0, 242, 255, 0.55);
  stroke-width: 1.5;
  animation: radarVertexRipple 2.4s ease-out infinite;
  transform-origin: center;
  transform-box: fill-box;
}

.radar-vertex-core {
  fill: #00e8ff;
  stroke: #ffffff;
  stroke-width: 1.5;
  filter: drop-shadow(0 0 6px rgba(0, 242, 255, 0.9));
  animation: radarVertexPulse 2.2s ease-in-out infinite;
  transform-origin: center;
  transform-box: fill-box;
}

.radar-vertex-hit {
  fill: transparent;
  pointer-events: all;
}

.radar-tip {
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: opacity 0.16s ease, visibility 0.16s ease;
}

.radar-tip--visible {
  opacity: 1;
  visibility: visible;
}

.radar-tip rect {
  fill: rgba(6, 18, 36, 0.94);
  stroke: rgba(0, 242, 255, 0.45);
  stroke-width: 1;
  filter: drop-shadow(0 0 10px rgba(0, 200, 255, 0.35));
}

.radar-tip-title,
.radar-tip-sub {
  fill: #d6efff;
  font-size: 11px;
}

@keyframes radarVertexPulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.12);
  }
}

@keyframes radarVertexRipple {
  0% {
    transform: scale(0.5);
    opacity: 0.65;
  }
  100% {
    transform: scale(2.6);
    opacity: 0;
  }
}

.radar-legend {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
  font-size: 12px;
  color: #12294b;
}

.improve-list {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.improve-card {
  background: #f7fbff;
  padding: 10px;
}

.improve-card h4 {
  margin: 0 0 6px;
  font-size: 14px;
  color: #1f4d9a;
}

.improve-card p {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
}

.cert-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.cert-card {
  background: #f7fbff;
  padding: 12px;
}

.cert-card h4 {
  margin: 0 0 8px;
  color: #1f4d9a;
  font-size: 16px;
}

.cert-card p {
  margin: 0 0 5px;
  font-size: 12px;
  line-height: 1.5;
}

.cert-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.cert-chip {
  background: #e7f1f8;
  color: #12294b;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 16px;
}

.salary-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 10px;
}

.salary-panel {
  background: #f7fbff;
  padding: 12px;
}

.salary-panel h4 {
  margin: 0 0 10px;
  font-size: 15px;
  color: #1f4d9a;
}

.salary-bars {
  display: grid;
  gap: 8px;
}

.salary-bar-item {
  display: grid;
  grid-template-columns: 70px 1fr 42px;
  align-items: center;
  gap: 8px;
}

.salary-bar-item span,
.salary-bar-item strong {
  font-size: 12px;
}

.salary-bar {
  height: 8px;
  border-radius: 999px;
  background: #e7f1f8;
  overflow: hidden;
}

.salary-bar em {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #3879c9 0%, #1f4d9a 100%);
}

.salary-extra {
  margin-top: 18px;
}

.salary-extra p {
  margin: 0 0 6px;
  font-size: 13px;
  line-height: 1.6;
}

.growth-path {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.growth-card {
  background: #f7fbff;
  padding: 12px;
}

.growth-card h4 {
  margin: 0 0 8px;
  font-size: 15px;
  color: #1f4d9a;
}

.growth-card p {
  margin: 0 0 5px;
  font-size: 12px;
  line-height: 1.5;
}

.future-note {
  margin-top: 18px;
}

.future-note p {
  margin: 0 0 6px;
  font-size: 13px;
  line-height: 1.6;
}

.bullet-list {
  margin: 0;
  padding-left: 0;
  list-style: none;
  display: grid;
  gap: 8px;
}

.bullet-list li {
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.34);
  border-radius: 10px;
  padding: 9px 10px;
  font-size: 13px;
  line-height: 1.6;
  color: #caefff;
}

@keyframes portraitFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.plain-text {
  margin: 0;
  font-size: 18px;
  line-height: 1.7;
}

.portrait-selector {
  background: #ffffff;
  padding: 12px;
  display: grid;
  align-content: start;
  gap: 10px;
}

.portrait-selector-popup {
  position: absolute;
  top: 56px;
  right: 8px;
  width: 240px;
  max-height: calc(100vh - var(--nav-block-height) - 140px);
  overflow-y: auto;
  z-index: 8;
}

.portrait-selector h3 {
  margin: 0 0 4px;
  font-size: 18px;
}

.selector-state-text {
  margin: 0;
  font-size: 13px;
  color: #5d718b;
}

.selector-state-text--error {
  color: #c04b4b;
}

.selector-item {
  border: 1px solid #b7d2e8;
  border-radius: 10px;
  background: #f7fbff;
  color: #12294b;
  text-align: left;
  font-size: 16px;
  padding: 10px 12px;
  cursor: pointer;
}

.selector-item.active,
.selector-item:hover {
  background: #3879c9;
  color: #ffffff;
}

.module {
  background: #ffffff;
  padding: 16px;
}

.graph-section-headings {
  margin: 0 0 16px;
}

.graph-section-headings h3 {
  margin: 0;
  font-size: 24px;
  color: #12294b;
}

.graph-subtitle {
  margin: 6px 0 0;
  color: #4a6386;
  font-size: 14px;
}

/* 职业发展路径：大标题下「当前起点…」与选择区下统计行用白字，配深色条保证在浅色页面上可读 */
.job-graph-pane .graph-section-headings {
  margin: 0 0 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(8, 27, 48, 0.9);
  border: 1px solid rgba(106, 210, 255, 0.32);
}

.job-graph-pane .graph-section-headings h3 {
  color: #ffffff;
}

.job-graph-pane {
  min-height: 0;
}

.job-graph-module {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.career-path-panel {
  padding: 14px 16px 16px;
  border-radius: 12px;
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.34);
  box-sizing: border-box;
}

.career-path-panel-title {
  margin: 0 0 18px;
  font-size: 22px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 0.02em;
}

.career-path-panel-body {
  color: #caefff;
}

.ladder-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
  margin-bottom: 14px;
}

.ladder-row:last-child {
  margin-bottom: 0;
}

.path-node {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 46px;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 17px;
  font-weight: 700;
  border: 1px solid transparent;
}

.path-node--blue {
  background: #3879c9;
  color: #ffffff;
  border-color: rgba(147, 211, 255, 0.45);
}

.path-node--green {
  background: #2f9e6b;
  color: #ffffff;
  border-color: rgba(167, 243, 200, 0.45);
}

.path-node--focus {
  background: #f59e0b;
  color: #1a1205;
  border-color: rgba(254, 243, 199, 0.65);
  box-shadow:
    0 0 0 2px rgba(251, 191, 36, 0.35),
    0 0 22px rgba(251, 191, 36, 0.28);
}

.path-arrow {
  font-size: 28px;
  font-weight: 900;
  color: #7dd3fc;
  line-height: 1;
}

.path-arrow--branch {
  flex-shrink: 0;
}

.path-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  flex: 1 1 200px;
}

.path-chip {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  color: #e0f4ff;
  background: rgba(12, 40, 72, 0.92);
  border: 1px solid rgba(106, 210, 255, 0.35);
  line-height: 1.35;
}

.career-path-empty {
  margin: 0;
  font-size: 14px;
  color: rgba(202, 239, 255, 0.75);
}

.transfer-layout {
  display: grid;
  grid-template-columns: minmax(0, auto) minmax(0, 1fr);
  gap: 16px 22px;
  align-items: start;
}

.transfer-start-col {
  align-self: start;
}

.transfer-branches-col {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
}

.transfer-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
}

@media (max-width: 720px) {
  .transfer-layout {
    grid-template-columns: 1fr;
  }
}

.job-graph-toolbar {
  padding: 14px 16px;
  background: #f7fbff;
  border: 1px solid #d5e6f5;
}

.job-graph-toolbar-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.job-graph-label {
  font-size: 14px;
  font-weight: 600;
  color: #12335f;
}

.job-graph-select {
  flex: 1;
  min-width: 200px;
  border: 1px solid #b7d2e8;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  color: #12294b;
  background: #ffffff;
  outline: none;
}

.job-graph-select:focus {
  border-color: #3879c9;
  box-shadow: 0 0 0 3px rgba(56, 121, 201, 0.14);
}

.job-graph-refresh-btn {
  border: none;
  border-radius: 10px;
  background: #1f4d9a;
  color: #fff;
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.job-graph-refresh-btn:hover {
  background: #3879c9;
}

.graph-focus-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.graph-focus-chip {
  border: 1px solid #b7d2e8;
  border-radius: 999px;
  background: #ffffff;
  color: #12335f;
  padding: 8px 14px;
  font-size: 13px;
  cursor: pointer;
  transition:
    background 0.15s ease,
    color 0.15s ease,
    border-color 0.15s ease;
}

.graph-focus-chip:hover,
.graph-focus-chip.active {
  background: #3879c9;
  border-color: #3879c9;
  color: #ffffff;
}

.job-graph-meta {
  margin: 10px 0;
  padding: 0 4px;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
  color: #ffffff;
  text-shadow:
    0 1px 2px rgba(0, 0, 0, 0.45),
    0 0 14px rgba(18, 41, 75, 0.35);
}

.job-graph-pane .job-graph-meta {
  margin: 0 0 12px;
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(8, 27, 48, 0.9);
  border: 1px solid rgba(106, 210, 255, 0.32);
  color: #ffffff;
  text-shadow: none;
}

.job-graph-canvas-wrap {
  position: relative;
  min-height: 520px;
  padding: 0;
  overflow: hidden;
  border: 1px solid rgba(100, 180, 220, 0.28);
  border-radius: 12px;
  background: #1c2838;
}

.job-graph-network {
  width: 100%;
  min-height: 520px;
  height: 56vh;
  max-height: 720px;
  background: transparent;
}

.job-graph-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  z-index: 2;
  background: rgba(28, 40, 56, 0.92);
  padding: 20px;
  text-align: center;
  color: #e2e8f0;
}

.job-graph-overlay.state-error p {
  margin: 0;
  color: #fca5a5;
  max-width: 420px;
  line-height: 1.5;
}

.job-graph-overlay.state-loading {
  color: #e2e8f0;
  font-size: 15px;
  font-weight: 600;
}

@media (max-width: 1220px) {
  .layout-wrap {
    display: block;
  }

  .side-menu {
    position: fixed;
    top: calc(var(--nav-block-height) + 20px);
    left: var(--page-padding);
    width: 205px;
    min-height: auto;
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .content-area {
    margin-left: 221px;
  }

  .skill-grid {
    grid-template-columns: 1fr;
  }

  .ability-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .data-center-page {
    --page-padding: 14px;
    --nav-block-height: 132px;
    padding: calc(var(--nav-block-height) + 20px) var(--page-padding) var(--page-padding);
  }

  .top-nav {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .side-menu {
    left: var(--page-padding);
    top: calc(var(--nav-block-height) + 20px);
    width: 188px;
  }

  .content-area {
    margin-left: 202px;
    max-height: calc(100vh - var(--nav-block-height) - 20px - var(--page-padding));
  }

  .portrait-toggle-btn {
    padding: 10px 14px;
    font-size: 15px;
  }

  .portrait-selector-popup {
    right: 6px;
    top: 50px;
    width: min(220px, calc(100% - 12px));
  }

  .filter-wrap {
    grid-template-columns: 1fr;
  }

  .ability-grid,
  .skill-grid {
    grid-template-columns: 1fr;
  }

  .skill-strip-row {
    flex-wrap: wrap;
    gap: 8px;
  }

  .skill-strip-row strong,
  .skill-right-meta {
    min-width: auto;
    width: 100%;
    justify-content: space-between;
  }

  .ability-layout,
  .salary-layout,
  .cert-grid,
  .improve-list {
    grid-template-columns: 1fr;
  }

  .growth-path {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .skill-tooltip {
    width: min(280px, 88vw);
  }
}
</style>