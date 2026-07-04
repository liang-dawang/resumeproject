<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import * as echarts from 'echarts/core'
import { BarChart, RadarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([RadarChart, BarChart, GridComponent, TooltipComponent, CanvasRenderer])

const API_BASE = 'http://127.0.0.1:8000'

const router = useRouter()
const route = useRoute()

const navItems = [
  { label: '首页', path: '/' },
  { label: '职业规划', path: '/career-planning' },
  { label: '数据中心', path: '/data-center' },
  { label: '报告管理', path: '/report-management' },
  { label: '个人中心', path: '/student-profile' },
  
]

// 从人岗匹配分析获取的数据
const jobMatchData = ref(null)
const showPolishResultModal = ref(false)
const currentPolishedContent = ref(null)
const generatedReport = ref(null)
const reportEditorText = ref('')
const checkResult = ref(null)
const reportBusy = ref(false)
const exportFormat = ref('docx')

const showHistory = ref(false)
const historyLoading = ref(false)
const historyError = ref('')
const historyTotal = ref(0)
const historyItems = ref([])
const selectedHistoryReport = ref(null)

// 学生数据：从 /career/overview 接口获取，不再使用硬编码
const overviewLoading = ref(true)
const overviewError = ref('')

const student = reactive({
  name: '',
  major: '',
  education: '',
  targetJob: '',
  strengths: '',
  toImprove: '',
  profileComplete: 0,
  competitiveness: 0,
  overallScore: 0,
  abilityMatch: {
    basic: 0,
    skill: 0,
    quality: 0,
    potential: 0
  },
  goals: {
    short: '',
    mid: '',
    long: ''
  }
})

const fetchOverview = async () => {
  overviewLoading.value = true
  overviewError.value = ''
  try {
    const { data } = await axios.get(`${API_BASE}/career/overview`)
    if (data?.code === 200 && data?.data?.student) {
      const s = data.data.student
      student.name = s.name || ''
      student.major = s.major || ''
      student.education = s.education || ''
      student.targetJob = s.targetJob || ''
      student.strengths = s.strengths || ''
      student.toImprove = s.toImprove || ''
      student.profileComplete = Number(s.profileComplete) || 0
      student.competitiveness = Number(s.competitiveness) || 0
      student.overallScore = Number(s.overallScore) || 0
      if (s.abilityMatch) {
        student.abilityMatch.basic = Number(s.abilityMatch.basic) || 0
        student.abilityMatch.skill = Number(s.abilityMatch.skill) || 0
        student.abilityMatch.quality = Number(s.abilityMatch.quality) || 0
        student.abilityMatch.potential = Number(s.abilityMatch.potential) || 0
      }
      if (s.goals) {
        student.goals.short = s.goals.short || ''
        student.goals.mid = s.goals.mid || ''
        student.goals.long = s.goals.long || ''
      }
      // 缓存人岗匹配原始数据
      if (data.data.matchingRaw?.best_match) {
        jobMatchData.value = data.data.matchingRaw.best_match
      }
    } else {
      overviewError.value = data?.message || '学生概览数据加载失败'
    }
  } catch (error) {
    console.error('加载学生概览失败:', error)
    overviewError.value = '无法加载学生概览，请确保后端服务正常且已上传简历。'
  } finally {
    overviewLoading.value = false
  }
}

onMounted(() => {
  fetchOverview()
  nextTick(() => {
    syncAbilityRadarChart()
    syncMatchBarChart()
  })
})

const generatedDate = computed(() => {
  const now = new Date()
  const y = now.getFullYear()
  const m = `${now.getMonth() + 1}`.padStart(2, '0')
  const d = `${now.getDate()}`.padStart(2, '0')
  return `${y}-${m}-${d}`
})

const clampScore = (value) => Math.max(0, Math.min(100, Number(value) || 0))

/** 与雷达图刻度一致：整数显示整数，否则保留两位小数 */
const formatRadarDisplay = (value) => {
  const n = Number(value)
  if (!Number.isFinite(n)) return '0'
  const r = Math.round(n)
  if (Math.abs(n - r) < 1e-6) return String(r)
  return n.toFixed(2)
}

/** 与左侧「优势/改进」行一致：用人岗四维度合成可量化轴，随 overview 数据变化 */
const strengthAxisScore = computed(() =>
  clampScore((student.abilityMatch.basic + student.abilityMatch.skill) / 2)
)
const improveAxisScore = computed(() =>
  clampScore((student.abilityMatch.quality + student.abilityMatch.potential) / 2)
)

const abilitySummaryCards = computed(() => [
  {
    label: '综合评分',
    value: `${formatRadarDisplay(student.overallScore)}分`,
    desc: student.overallScore >= 80 ? '表现优秀' : '持续提升中'
  },
  {
    label: '竞争力评分',
    value: `${formatRadarDisplay(student.competitiveness)}分`,
    desc: student.competitiveness >= 75 ? '具备岗位竞争力' : '需加强核心能力'
  },
  {
    label: '完整度评分',
    value: `${formatRadarDisplay(student.profileComplete)}分`,
    desc: student.profileComplete >= 80 ? '信息完整' : '建议补充经历'
  },
  {
    label: '优势能力 / 专业基础',
    value: `${formatRadarDisplay(strengthAxisScore.value)}分`,
    desc: student.strengths || '—'
  },
  {
    label: '需改进能力 / 工程经验',
    value: `${formatRadarDisplay(improveAxisScore.value)}分`,
    desc: student.toImprove || '—'
  }
])

const abilityRadarMetrics = computed(() => {
  const overall = clampScore(student.overallScore)
  const comp = clampScore(student.competitiveness)
  const complete = clampScore(student.profileComplete)
  const strength = strengthAxisScore.value
  const improve = improveAxisScore.value
  return [
    {
      label: '综合评分',
      value: overall,
      display: formatRadarDisplay(student.overallScore)
    },
    {
      label: '竞争力评分',
      value: comp,
      display: formatRadarDisplay(student.competitiveness)
    },
    {
      label: '完整度评分',
      value: complete,
      display: formatRadarDisplay(student.profileComplete)
    },
    {
      label: '优势能力 / 专业基础',
      value: strength,
      display: formatRadarDisplay(strength)
    },
    {
      label: '需改进能力 / 工程经验',
      value: improve,
      display: formatRadarDisplay(improve)
    }
  ]
})

const abilityRadarRef = ref(null)
const abilityRadarChart = shallowRef(null)
let abilityRadarResizeObserver = null

const buildAbilityRadarOption = () => {
  const metrics = abilityRadarMetrics.value
  const values = metrics.map((m) => clampScore(m.value))
  return {
    backgroundColor: 'transparent',
    animation: true,
    animationDuration: 550,
    animationEasing: 'cubicOut',
    tooltip: { show: false },
    radar: {
      shape: 'circle',
      center: ['50%', '50%'],
      radius: '66%',
      indicator: metrics.map((m) => ({
        name: `${m.label}\n${m.display}分`,
        max: 100
      })),
      splitNumber: 5,
      axisName: {
        color: '#d6ecff',
        fontSize: 10,
        fontWeight: 600,
        lineHeight: 14,
        fontFamily: "'Microsoft YaHei','PingFang SC','Segoe UI',sans-serif"
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(210, 232, 255, 0.42)',
          width: 1.2
        }
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(106, 210, 255, 0.32)',
          width: 1
        }
      },
      splitArea: {
        show: false
      }
    },
    series: [
      {
        type: 'radar',
        name: '个人能力评估',
        symbol: 'circle',
        symbolSize: 9,
        lineStyle: {
          width: 3,
          color: '#2ecbff',
          shadowBlur: 12,
          shadowColor: 'rgba(46, 203, 255, 0.55)'
        },
        itemStyle: {
          color: '#63f0ff',
          borderColor: '#b8f8ff',
          borderWidth: 2,
          shadowBlur: 10,
          shadowColor: 'rgba(99, 240, 255, 0.65)'
        },
        areaStyle: {
          color: 'rgba(46, 170, 255, 0.24)'
        },
        emphasis: {
          focus: 'series',
          lineStyle: {
            width: 3.5,
            color: '#5ae0ff',
            shadowBlur: 16,
            shadowColor: 'rgba(90, 224, 255, 0.65)'
          },
          itemStyle: {
            color: '#7cf9ff',
            borderColor: '#e8ffff',
            borderWidth: 2.5,
            shadowBlur: 16,
            shadowColor: 'rgba(124, 249, 255, 0.85)'
          },
          areaStyle: {
            color: 'rgba(90, 210, 255, 0.35)'
          }
        },
        data: [{ value: values, name: '个人能力评估' }]
      }
    ]
  }
}

const syncAbilityRadarChart = () => {
  const el = abilityRadarRef.value
  if (!el) return
  if (!abilityRadarChart.value) {
    abilityRadarChart.value = echarts.init(el, undefined, { renderer: 'canvas' })
    abilityRadarResizeObserver = new ResizeObserver(() => {
      abilityRadarChart.value?.resize()
    })
    abilityRadarResizeObserver.observe(el)
  }
  abilityRadarChart.value.setOption(buildAbilityRadarOption(), { notMerge: true })
}

watch(
  abilityRadarMetrics,
  () => {
    nextTick(() => syncAbilityRadarChart())
  },
  { deep: true }
)

onBeforeUnmount(() => {
  if (abilityRadarResizeObserver) {
    abilityRadarResizeObserver.disconnect()
    abilityRadarResizeObserver = null
  }
  if (abilityRadarChart.value) {
    abilityRadarChart.value.dispose()
    abilityRadarChart.value = null
  }
})

const matchCards = computed(() => {
  const toTag = (score) => {
    if (score >= 80) return '符合'
    if (score >= 70) return '基本符合'
    return '有成长空间'
  }
  return [
    { label: '总体匹配度', score: Math.round((student.abilityMatch.basic + student.abilityMatch.skill + student.abilityMatch.quality + student.abilityMatch.potential) / 4), tag: toTag(Math.round((student.abilityMatch.basic + student.abilityMatch.skill + student.abilityMatch.quality + student.abilityMatch.potential) / 4)) },
    { label: '基础要求', score: student.abilityMatch.basic, tag: toTag(student.abilityMatch.basic) },
    { label: '职业技能', score: student.abilityMatch.skill, tag: toTag(student.abilityMatch.skill) },
    { label: '职业素养', score: student.abilityMatch.quality, tag: toTag(student.abilityMatch.quality) },
    { label: '发展潜力', score: student.abilityMatch.potential, tag: toTag(student.abilityMatch.potential) }
  ]
})

const reportMatchChartBars = computed(() => {
  const basic = clampScore(student.abilityMatch.basic)
  const skill = clampScore(student.abilityMatch.skill)
  const quality = clampScore(student.abilityMatch.quality)
  const potential = clampScore(student.abilityMatch.potential)
  const overall = Math.round((basic + skill + quality + potential) / 4)
  return [
    { label: '总体匹配度', value: overall },
    { label: '基础要求', value: basic },
    { label: '职业技能', value: skill },
    { label: '职业素养', value: quality },
    { label: '发展潜力', value: potential }
  ]
})

const matchBarRef = ref(null)
const matchBarChart = shallowRef(null)
let matchBarResizeObserver = null

const buildMatchBarOption = () => {
  const bars = reportMatchChartBars.value
  const categories = bars.map((b) => b.label)
  const values = bars.map((b) => clampScore(b.value))
  return {
    backgroundColor: 'transparent',
    animation: true,
    animationDuration: 500,
    animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(6, 20, 40, 0.92)',
      borderColor: 'rgba(106, 210, 255, 0.45)',
      borderWidth: 1,
      padding: [8, 12],
      textStyle: { color: '#e8f8ff', fontSize: 13 },
      formatter: (params) => {
        const p = Array.isArray(params) ? params[0] : params
        if (!p) return ''
        return `${p.name}<br/>匹配度：<b>${p.value}%</b>`
      }
    },
    grid: {
      left: '2%',
      right: '2%',
      top: '14%',
      bottom: '2%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: 'rgba(210, 232, 255, 0.42)', width: 1 } },
      axisTick: { alignWithLabel: true, lineStyle: { color: 'rgba(210, 232, 255, 0.28)' } },
      axisLabel: {
        color: '#d6ecff',
        fontSize: 11,
        fontWeight: 600,
        interval: 0,
        fontFamily: "'Microsoft YaHei','PingFang SC','Segoe UI',sans-serif"
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      splitNumber: 5,
      axisLine: { show: true, lineStyle: { color: 'rgba(210, 232, 255, 0.35)' } },
      splitLine: {
        lineStyle: {
          color: 'rgba(160, 210, 255, 0.1)',
          width: 1,
          type: 'dashed'
        }
      },
      axisLabel: {
        color: 'rgba(220, 235, 255, 0.78)',
        fontSize: 11,
        formatter: '{value}%'
      }
    },
    series: [
      {
        type: 'bar',
        name: '岗位匹配度',
        data: values,
        barWidth: '60%',
        itemStyle: {
          borderRadius: [10, 10, 4, 4],
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#00ccff' },
              { offset: 1, color: '#0066ff' }
            ]
          },
          shadowBlur: 14,
          shadowColor: 'rgba(0, 200, 255, 0.48)'
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 22,
            shadowColor: 'rgba(0, 230, 255, 0.62)'
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%',
          color: '#ffffff',
          fontSize: 12,
          fontWeight: 700,
          fontFamily: "'Microsoft YaHei','PingFang SC','Segoe UI',sans-serif"
        }
      }
    ]
  }
}

const syncMatchBarChart = () => {
  const el = matchBarRef.value
  if (!el) return
  if (!matchBarChart.value) {
    matchBarChart.value = echarts.init(el, undefined, { renderer: 'canvas' })
    matchBarResizeObserver = new ResizeObserver(() => {
      matchBarChart.value?.resize()
    })
    matchBarResizeObserver.observe(el)
  }
  matchBarChart.value.setOption(buildMatchBarOption(), { notMerge: true })
}

watch(
  reportMatchChartBars,
  () => {
    nextTick(() => syncMatchBarChart())
  },
  { deep: true }
)

onBeforeUnmount(() => {
  if (matchBarResizeObserver) {
    matchBarResizeObserver.disconnect()
    matchBarResizeObserver = null
  }
  if (matchBarChart.value) {
    matchBarChart.value.dispose()
    matchBarChart.value = null
  }
})

const comprehensiveEvaluation = computed(() => {
  const overall = Number(student.overallScore) || 0
  const competitiveness = Number(student.competitiveness) || 0
  const complete = Number(student.profileComplete) || 0
  const strengths = student.strengths || '具备一定优势'
  const toImprove = student.toImprove || '仍需持续补强核心能力'

  if (!student.name) {
    return '请先加载学生概览数据后再生成综合评价。'
  }

  if (overall >= 85 && competitiveness >= 80) {
    return `${student.name}整体表现较为突出，基础能力、职业素养与发展潜力均较为均衡，当前已具备较好的岗位适配基础。结合其优势：${strengths}，建议继续保持并在实践中强化深度能力；同时关注：${toImprove}，以进一步提升目标岗位的竞争力。`
  }

  if (overall >= 70 || competitiveness >= 70) {
    return `${student.name}当前综合表现处于良好水平，已具备一定岗位适配能力和发展基础。其优势为：${strengths}；后续建议重点关注：${toImprove}，并结合学习与实习实践不断夯实能力结构，以稳步提升职业竞争力。`
  }

  return `${student.name}目前基础条件与岗位要求仍存在一定差距，但已具备继续提升的潜力。现阶段优势为：${strengths}；需要重点补强：${toImprove}。建议在补齐基础能力的同时，通过课程学习、项目实践和岗位实习逐步提升综合竞争力。`
})

const go = (path) => {
  if (route.path !== path) {
    router.push(path)
  }
}

/** 数据中心「职业发展路径」页（与 DataCenter 中 job-graph 一致） */
const viewPath = () => {
  router.push('/data-center/job-graph')
}

/** 职业规划侧边栏「行动计划」（与 CareerPlanning ?tab=action-plan 一致） */
const viewPlan = () => {
  router.push({ path: '/career-planning', query: { tab: 'action-plan' } })
}

const buildReportPayload = () => ({
  studentName: student.name,
  major: student.major,
  education: student.education,
  desiredJob: student.targetJob,
  strengths: student.strengths,
  toImprove: student.toImprove,
  overallScore: Number(student.overallScore) || 0,
  competitiveness: Number(student.competitiveness) || 0,
  profileComplete: Number(student.profileComplete) || 0,
  goals: { ...student.goals },
  abilityMatch: { ...student.abilityMatch },
  jobMatchAnalysis: jobMatchData.value
})

const generateCareerReport = async () => {
  reportBusy.value = true
  try {
    const { data } = await axios.post(`${API_BASE}/ai/career-report/generate`, buildReportPayload())
    if (data?.status === 'success' && data?.data) {
      generatedReport.value = data.data
      reportEditorText.value = JSON.stringify(data.data, null, 2)
      let savedNote = ''
      try {
        const saveRes = await axios.post(`${API_BASE}/career/generated-development-reports`, {
          title: `${student.name || '学生'}_职业发展报告`,
          content: data.data,
          matchingSnapshot: jobMatchData.value ? { best_match: jobMatchData.value } : null,
          studentKey: 'latest'
        })
        if (saveRes.data?.code === 200) {
          savedNote = '已自动保存到历史记录（最近保留 3 条）。'
          if (showHistory.value) {
            await fetchHistoryReports()
          }
        } else {
          savedNote = '（自动存档失败，可稍后使用「保存到数据库」）'
        }
      } catch (saveErr) {
        console.warn('生成报告自动入库失败:', saveErr)
        savedNote = '（自动存档失败，请检查后端或稍后重试）'
      }
      window.alert(`职业发展报告已生成。${savedNote}`)
      return
    }
    window.alert(`报告生成失败：${data?.message || '未知错误'}`)
  } catch (error) {
    console.error('报告生成失败:', error)
    window.alert('报告生成失败，请检查后端服务。')
  } finally {
    reportBusy.value = false
  }
}

const saveEditorToReport = () => {
  try {
    const parsed = JSON.parse(reportEditorText.value || '{}')
    generatedReport.value = parsed
    window.alert('报告编辑内容已保存。')
  } catch {
    window.alert('JSON 格式不正确，请修正后再保存。')
  }
}

const checkReportCompleteness = async () => {
  if (!generatedReport.value) {
    window.alert('请先生成报告。')
    return
  }
  reportBusy.value = true
  try {
    const { data } = await axios.post(`${API_BASE}/ai/career-report/check`, {
      report: generatedReport.value
    })
    if (data?.status === 'success') {
      checkResult.value = data.data
      return
    }
    window.alert(`完整性检查失败：${data?.message || '未知错误'}`)
  } catch (error) {
    console.error('完整性检查失败:', error)
    window.alert('完整性检查失败，请检查后端服务。')
  } finally {
    reportBusy.value = false
  }
}

const exportReportFile = async () => {
  if (!generatedReport.value) {
    window.alert('请先生成报告。')
    return
  }
  reportBusy.value = true
  try {
    const { data } = await axios.post(`${API_BASE}/ai/career-report/export`, {
      title: `${student.name}_职业发展报告`,
      report: generatedReport.value,
      format: exportFormat.value
    })
    if (data?.status === 'success') {
      const p = data?.data?.filePath || ''
      const fmt = data?.data?.format || exportFormat.value
      window.alert(`报告已导出（${fmt}）：${p}`)
      return
    }
    window.alert(`导出失败：${data?.message || '未知错误'}`)
  } catch (error) {
    console.error('导出失败:', error)
    window.alert('导出失败，请检查后端服务。')
  } finally {
    reportBusy.value = false
  }
}

const smartPolish = async () => {
  try {
    console.log('✨ [智能润色] 开始执行智能润色流程')
    
    // 第一步：从人岗匹配分析获取已 AI 分析过的数据
    let matchAnalysisData = jobMatchData.value
    
    console.log('📊 [智能润色] 初始人岗匹配数据:', jobMatchData.value ? '已存在' : '不存在')
    
    if (!matchAnalysisData) {
      console.log('⚠️ [智能润色] 未找到缓存的人岗匹配数据，开始调用接口...')
      
      // 如果还没有人岗匹配数据，先调用接口获取
      try {
        const matchStartTime = Date.now()
        console.log('📡 [人岗匹配] 发送请求到：http://127.0.0.1:8000/user/resume/matching-trigger')
        
        const matchResponse = await axios.post(`${API_BASE}/user/resume/matching-trigger`, {}, {
          timeout: 30000,
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        const matchEndTime = Date.now()
        console.log('✅ [人岗匹配] 接口调用成功!')
        console.log('⏱️ [人岗匹配] 耗时:', (matchEndTime - matchStartTime) + 'ms')
        console.log('📦 [人岗匹配] HTTP 状态码:', matchResponse.status)
        console.log('📦 [人岗匹配] 完整响应数据:', JSON.stringify(matchResponse.data, null, 2))
        
        // 兼容多种响应格式
        matchAnalysisData = matchResponse.data?.best_match || 
                           matchResponse.data?.data?.best_match || 
                           matchResponse.data?.data || 
                           matchResponse.data || 
                           null
        
        console.log('🔍 [人岗匹配] 解析后的数据:', matchAnalysisData)
        
        if (matchAnalysisData) {
          jobMatchData.value = matchAnalysisData
          console.log('✓ [人岗匹配] 数据已成功存储到 jobMatchData')
          console.log('✓ [人岗匹配] jobMatchData.value 当前值:', JSON.stringify(jobMatchData.value, null, 2))
        } else {
          console.warn('⚠️ [人岗匹配] 无法从响应中提取有效数据')
        }
      } catch (matchError) {
        console.error('❌ [人岗匹配] 接口调用失败:', matchError.message)
        console.error('❌ [人岗匹配] 错误类型:', matchError.code)
        console.error('❌ [人岗匹配] 错误详情:', matchError.response?.data || matchError)
        
        // 提示用户
        const userConfirmed = window.confirm(
          '人岗匹配分析服务暂时不可用，将使用基础数据进行智能润色。\n\n' +
          '点击"确定"继续润色，或点击"取消"重试。'
        )
        
        if (!userConfirmed) {
          return
        }
      }
    } else {
      console.log('✓ [智能润色] 使用缓存的人岗匹配数据')
    }
    
    // 第二步：准备要润色的报告内容（包含人岗匹配分析数据）
    // 严格按照后端 PolishRequest 模型定义字段 (camelCase)
    const reportContent = {
      // === 必填字段 ===
      studentName: student.name,
      major: student.major,
      
      // === 可选字段 ===
      education: student.education,
      desiredJob: student.targetJob,
      profileComplete: parseFloat(student.profileComplete) || 0.0,
      
      // === AI 分析用额外字段（会被 Config.extra="allow" 接收）===
      strengths: student.strengths,
      toImprove: student.toImprove,
      goals: student.goals,
      abilityMatch: student.abilityMatch,
      overallScore: parseFloat(student.overallScore) || 0.0,
      competitiveness: parseFloat(student.competitiveness) || 0.0,
      
      // === 人岗匹配分析数据（如果存在）===
      jobMatchAnalysis: matchAnalysisData ? {
        totalScore: parseFloat(matchAnalysisData.total_score) || 0.0,
        softSkillScores: matchAnalysisData.soft_skill_scores || {},
        hardSkillScore: 75.0,  // 默认值，后端会从人岗匹配数据计算
        jobFitnessScore: parseFloat(matchAnalysisData.total_score) || 0.0,
        matchedJobs: [{
          jobId: matchAnalysisData.job_id,
          jobTitle: matchAnalysisData.job_title,
          score: parseFloat(matchAnalysisData.total_score) || 0.0
        }]
      } : null
    }

    console.log('📤 [智能润色] 准备发送给后端的完整数据:')
    console.log('📤 [智能润色]', JSON.stringify(reportContent, null, 2))
    console.log('📤 [智能润色] jobMatchAnalysis:', reportContent.jobMatchAnalysis ? '有数据' : '无数据')

    // 第三步：调用 AI 大模型接口进行智能润色
    console.log('📡 [智能润色] 发送请求到：http://127.0.0.1:8000/ai/smart-polish')
    const polishStartTime = Date.now()
    
    const response = await axios.post('http://127.0.0.1:8000/ai/smart-polish', reportContent, {
      headers: {
        'Content-Type': 'application/json'
      },
      timeout: 60000  // 增加超时时间，AI 处理可能较慢
    })
    
    const polishEndTime = Date.now()
    console.log('✅ [智能润色] AI 接口调用成功!')
    console.log('⏱️ [智能润色] AI 处理耗时:', (polishEndTime - polishStartTime) + 'ms')
    console.log('📦 [智能润色] HTTP 状态码:', response.status)
    console.log('📦 [智能润色] 完整响应数据:', JSON.stringify(response.data, null, 2))

    // 根据后端 PolishResponse 格式处理响应：{ status, message, data }
    if (response.data && (response.data.status === 'success' || response.data.status === 'fallback')) {
      console.log('✓ [智能润色] 收到 AI 润色结果')
      
      const aiData = response.data.data
      
      // 增强验证：先检查 aiData 是否为 null/undefined，再检查必需字段
      if (!aiData || typeof aiData !== 'object') {
        console.error('❌ [智能润色] AI 返回的 data 为 null 或不是对象')
        console.error('❌ [智能润色] 完整响应:', response.data)
        window.alert('润色失败：AI 返回的数据为空或格式不正确')
        return
      }
      
      if (aiData.overallScore === undefined || aiData.overallScore === null) {
        console.error('❌ [智能润色] AI 返回的数据缺少 overallScore 字段')
        console.error('❌ [智能润色] 实际数据:', aiData)
        window.alert('润色失败：AI 返回的数据格式不正确，缺少必要字段')
        return
      }
      
      console.log('📝 [智能润色] AI 生成的综合评分:', aiData.overallScore)
      console.log('📝 [智能润色] AI 生成的潜力评分:', aiData.potential)
      console.log('📝 [智能润色] AI 生成的优势:', aiData.strengths)
      console.log('📝 [智能润色] AI 生成的待改进:', aiData.weaknesses)
      console.log('📝 [智能润色] AI 生成的建议数量:', aiData.suggestions?.length || 0)
      console.log('📝 [智能润色] AI 生成的个人总结:', aiData.polishedSummary)
      
      // 将润色后的内容存储到 localStorage
      // 严格按照后端返回的数据结构保存
      const polishedData = {
        // AI 生成的核心数据（直接使用后端返回的字段）
        overallScore: aiData.overallScore,
        potential: aiData.potential || 0,
        strengths: Array.isArray(aiData.strengths) ? aiData.strengths : [],
        weaknesses: Array.isArray(aiData.weaknesses) ? aiData.weaknesses : [],
        suggestions: Array.isArray(aiData.suggestions) ? aiData.suggestions : [],
        polishedSummary: aiData.polishedSummary || '',
        
        // 保存人岗匹配数据供后续使用
        jobMatchAnalysis: aiData.jobMatchAnalysis || matchAnalysisData,
        // 保存原始学生数据
        originalStudent: { ...student },
        // 时间戳
        timestamp: aiData.timestamp || new Date().toISOString(),
        // 标记是否为降级方案
        isFallback: aiData.isFallback || false,
        // 状态
        status: response.data.status
      }
      
      localStorage.setItem('polishedReport', JSON.stringify(polishedData))
      console.log('✓ [智能润色] 数据已保存到 localStorage')
      console.log('✓ [智能润色] 保存的完整数据:', polishedData)
      
      // 不直接跳转，而是显示 AI 润色结果预览弹窗（符合 AI 功能交互规范）
      currentPolishedContent.value = polishedData
      showPolishResultModal.value = true
      console.log('✓ [智能润色] 已显示结果预览弹窗')
      
    } else if (response.data && response.data.status === 'error') {
      console.error('❌ [智能润色] 后端返回错误:', response.data.message)
      window.alert(`润色失败：${response.data.message || '未知错误'}`)
    } else {
      console.error('❌ [智能润色] 响应数据格式不正确')
      console.error('❌ [智能润色] 实际收到的数据:', response.data)
      const errorMsg = response.data?.message || response.data?.error || '数据格式不正确'
      window.alert(`润色失败：${errorMsg}\n\n期望：{ status: "success|fallback|error", message: "...", data: {...} }\n实际：${JSON.stringify(response.data)}`)
    }
  } catch (error) {
    console.error('❌ [智能润色] 请求失败:', error)
    console.error('❌ [智能润色] 错误类型:', error.constructor.name)
    console.error('❌ [智能润色] 错误消息:', error.message)
    console.error('❌ [智能润色] 错误代码:', error.code)
    console.error('❌ [智能润色] 响应数据:', error.response?.data)
    console.error('❌ [智能润色] 响应状态:', error.response?.status)
    console.error('❌ [智能润色] 响应头:', error.response?.headers)
    
    let errorMessage = '智能润色失败！\n\n'
    
    if (error.response) {
      // 后端返回了错误响应
      const status = error.response.status
      const data = error.response.data
      
      if (status === 422) {
        errorMessage += `❌ 参数验证失败 (422)\n\n`
        errorMessage += `可能的原因:\n`
        errorMessage += `1. 缺少必需的字段\n`
        errorMessage += `2. 字段格式不正确\n\n`
        errorMessage += `请检查控制台日志查看详细发送的数据\n\n`
        if (data && data.detail) {
          errorMessage += `后端错误详情:\n${JSON.stringify(data.detail, null, 2)}`
        }
      } else if (status === 500) {
        errorMessage += `❌ 服务器内部错误 (500)\n\n`
        errorMessage += `请联系后端开发人员检查服务器日志`
      } else {
        errorMessage += `❌ 服务器错误 (状态码：${status})\n\n`
        errorMessage += `错误详情:\n${JSON.stringify(data, null, 2)}`
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      errorMessage += `❌ 网络错误\n\n`
      errorMessage += `无法连接到后端服务，请检查:\n`
      errorMessage += `1. 后端服务是否启动\n`
      errorMessage += `2. 端口号是否正确 (8000)\n`
      errorMessage += `3. 网络连接是否正常`
    } else {
      errorMessage += `❌ 未知错误\n\n`
      errorMessage += `错误信息：${error.message}`
    }
    
    window.alert(errorMessage)
  }
}

const reEvaluate = () => {
  router.push('/career-planning?tab=student-input')
}

const saveReportToDb = async () => {
  if (!generatedReport.value) {
    window.alert('请先生成报告，再保存到数据库。')
    return
  }
  reportBusy.value = true
  try {
    const { data } = await axios.post(`${API_BASE}/career/reports`, {
      title: `${student.name || '未知学生'}_职业发展报告`,
      content: generatedReport.value,
      matchingSnapshot: jobMatchData.value ? { best_match: jobMatchData.value } : null,
      studentKey: 'latest'
    })
    if (data?.code === 200) {
      window.alert(`报告已保存到数据库！报告ID：${data.data?.reportId ?? ''}`)
      // 刷新历史列表
      if (showHistory.value) {
        await fetchHistoryReports()
      }
    } else {
      window.alert(`保存失败：${data?.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('保存报告失败:', error)
    window.alert('保存报告失败，请检查后端服务。')
  } finally {
    reportBusy.value = false
  }
}

const fetchHistoryReports = async () => {
  historyLoading.value = true
  historyError.value = ''
  try {
    const { data } = await axios.get(`${API_BASE}/career/generated-development-reports`, {
      params: { studentKey: 'latest' }
    })
    if (data?.code === 200 && data?.data?.items) {
      historyItems.value = data.data.items
      historyTotal.value = data.meta?.total ?? data.data.items.length
    } else {
      historyItems.value = []
      historyError.value = data?.message || '历史报告加载失败'
    }
  } catch (error) {
    console.error('历史报告加载失败:', error)
    historyItems.value = []
    historyError.value = '历史报告加载失败，请检查后端服务。'
  } finally {
    historyLoading.value = false
  }
}

const openHistoryReport = async (item) => {
  if (!item?.id) return
  try {
    const { data } = await axios.get(`${API_BASE}/career/generated-development-reports/${item.id}`)
    if (data?.code === 200 && data?.data) {
      const report = data.data
      const content = report.content_json || report.contentJson || report.content || {}
      generatedReport.value = content
      reportEditorText.value = JSON.stringify(content, null, 2)
      selectedHistoryReport.value = report
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } else {
      window.alert(data?.message || '加载报告详情失败')
    }
  } catch (error) {
    console.error('加载报告详情失败:', error)
    window.alert('加载报告详情失败，请检查后端服务。')
  }
}

const viewHistory = async () => {
  showHistory.value = !showHistory.value
  if (showHistory.value && !historyItems.value.length && !historyLoading.value) {
    await fetchHistoryReports()
  }
}

const cancelAndStay = () => {
  showPolishResultModal.value = false
}

const confirmAndGoToEdit = () => {
  if (currentPolishedContent.value) {
    const polished = currentPolishedContent.value
    if (Array.isArray(polished.strengths) && polished.strengths.length) {
      student.strengths = polished.strengths.join('；')
    }
    if (Array.isArray(polished.weaknesses) && polished.weaknesses.length) {
      student.toImprove = polished.weaknesses.join('；')
    }
    if (polished.overallScore !== undefined) {
      student.overallScore = Number(polished.overallScore) || student.overallScore
    }
  }
  showPolishResultModal.value = false
  generateCareerReport()
}
</script>

<template>
  <div class="report-page">
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

    <main class="report-content">
      <h1>个性化职业规划报告</h1>
      <section class="report-layout">
      <section class="report-sheet card-surface">
        <div class="sheet-head">
          <h2>职业规划报告</h2>
          <p>{{ student.name }} · 生成日期 {{ generatedDate }}</p>
        </div>

        <div class="sheet-block">
          <h3>个人能力评估总结</h3>
          <div class="report-ability-layout">
            <div class="row-table">
              <div class="row-item" v-for="item in abilitySummaryCards" :key="item.label">
                <span class="row-label">{{ item.label }}：</span>
                <span class="row-value">{{ item.value }}（{{ item.desc }}）</span>
              </div>
            </div>
            <aside class="report-ability-chart card-surface">
              <div
                ref="abilityRadarRef"
                class="ability-radar-echart"
                role="img"
                aria-label="个人能力评估雷达图"
              />
            </aside>
          </div>
        </div>

        <div class="sheet-block">
          <h3>岗位匹配分析</h3>
          <div class="report-match-layout">
            <div class="row-table">
              <div class="row-item">
                <span class="row-label">目标岗位：</span>
                <span class="row-value">{{ student.targetJob }}</span>
              </div>
              <div class="row-item" v-for="item in matchCards" :key="item.label">
                <span class="row-label">{{ item.label }}：</span>
                <span class="row-value">{{ item.score }}%（{{ item.tag }}）</span>
              </div>
            </div>

            <aside class="report-match-chart">
              <div
                ref="matchBarRef"
                class="match-bar-echart"
                role="img"
                aria-label="岗位匹配度柱状图"
              />
            </aside>
          </div>
        </div>

        <div class="sheet-block">
          <h3>职业目标</h3>
          <div class="row-table">
            <div class="row-item">
              <span class="row-label">短期目标（1-6个月）：</span>
              <span class="row-value">{{ student.goals.short }}</span>
            </div>
            <div class="row-item">
              <span class="row-label">中期目标（1-2年）：</span>
              <span class="row-value">{{ student.goals.mid }}</span>
            </div>
            <div class="row-item">
              <span class="row-label">长期目标（3-5年）：</span>
              <span class="row-value">{{ student.goals.long }}</span>
            </div>
          </div>
        </div>

        <div class="sheet-block">
          <h3>综合评价</h3>
          <div class="row-table">
            <div class="row-item">
              <span class="row-label">综合评价：</span>
              <span class="row-value">{{ comprehensiveEvaluation }}</span>
            </div>
          </div>
        </div>

        <div class="sheet-block print-hidden">
          <h3>详细模块</h3>
          <div class="quick-column">
            <button class="btn detail-link-btn" @click="viewPath">
              <span class="detail-icon" aria-hidden="true">🧭</span>
              <span class="detail-text">查看职业发展路径</span>
            </button>
            <button class="btn detail-link-btn" @click="viewPlan">
              <span class="detail-icon" aria-hidden="true">🗓</span>
              <span class="detail-text">查看行动计划</span>
            </button>
          </div>
        </div>

        <div class="bottom-row">
          <button class="btn unified-btn" @click="smartPolish">智能润色</button>
          <button class="btn unified-btn" :disabled="reportBusy" @click="generateCareerReport">生成发展报告</button>
          <button class="btn unified-btn" :disabled="reportBusy" @click="checkReportCompleteness">完整性检查</button>
          <select v-model="exportFormat" class="export-select">
            <option value="docx">Word(.docx)</option>
            <option value="pdf">PDF(.pdf)</option>
            <option value="json">JSON(.json)</option>
            <option value="txt">TXT(.txt)</option>
          </select>
          <button class="btn unified-btn" :disabled="reportBusy" @click="exportReportFile">一键导出报告</button>
          <button class="btn unified-btn" @click="reEvaluate">重新评估</button>
          <button class="btn unified-btn" @click="viewHistory">查看历史报告</button>
        </div>

        <div class="sheet-block">
          <h3>报告编辑与优化</h3>
          <p class="row-value">可手动编辑报告 JSON 内容，保存后再执行完整性检查与导出。</p>
          <textarea v-model="reportEditorText" class="report-editor" placeholder="点击“生成发展报告”后可在此编辑"></textarea>
          <div class="bottom-row" style="padding: 12px 0 0;">
            <button class="btn unified-btn" @click="saveEditorToReport">保存编辑内容</button>
          </div>
        </div>

        <div v-if="checkResult" class="sheet-block">
          <h3>完整性检查结果</h3>
          <div class="row-table">
            <div class="row-item">
              <span class="row-label">完整性评分：</span>
              <span class="row-value">{{ checkResult.completenessScore }}分</span>
            </div>
            <div class="row-item">
              <span class="row-label">缺失章节：</span>
              <span class="row-value">{{ (checkResult.missingSections || []).length ? checkResult.missingSections.join('、') : '无' }}</span>
            </div>
            <div class="row-item">
              <span class="row-label">优化建议：</span>
              <span class="row-value">{{ (checkResult.suggestions || []).join('；') }}</span>
            </div>
          </div>
        </div>

        <div v-if="showHistory" class="sheet-block">
          <h3>历史报告列表</h3>
          <p class="row-value" style="margin: 0 0 10px">
            展示「生成发展报告」自动存档的最近 3 条（共已生成 {{ historyTotal }} 条）。
          </p>
          <div v-if="historyLoading" class="job-state-box">
            <p class="state-text">正在加载历史报告...</p>
          </div>
          <div v-else-if="historyError" class="job-state-box">
            <p class="state-text error">{{ historyError }}</p>
            <button class="btn unified-btn" @click="() => fetchHistoryReports()">重试</button>
          </div>
          <template v-else>
            <div v-if="historyItems.length" class="row-table">
              <div
                v-for="item in historyItems"
                :key="item.id"
                class="row-item"
              >
                <span class="row-label">报告 #{{ item.id }}（自动生成）</span>
                <span class="row-value">
                  {{ item.title || '职业发展报告' }} ·
                  生成时间：{{ item.created_at || item.createdAt }}
                  <button class="small-btn" style="margin-left: 12px" @click="openHistoryReport(item)">查看内容</button>
                </span>
              </div>
            </div>
            <p v-else class="plain-text">暂无自动生成记录，请先点击「生成发展报告」。</p>
          </template>
        </div>
      </section>
      </section>
    </main>

    <!-- AI 润色结果预览弹窗 -->
    <div v-if="showPolishResultModal" class="modal-mask" @click="cancelAndStay">
      <div class="polish-result-modal card-surface" @click.stop>
        <div class="modal-header">
          <h2>✨ AI 智能润色完成</h2>
          <button class="close-btn" @click="cancelAndStay">×</button>
        </div>
        
        <div class="modal-body">
          <!-- 优化后的优势 -->
          <div v-if="currentPolishedContent?.strengths && currentPolishedContent.strengths.length > 0" class="result-section">
            <h3>🎯 优化后的优势</h3>
            <ul class="info-list">
              <li v-for="(strength, index) in currentPolishedContent.strengths" :key="index">
                {{ strength }}
              </li>
            </ul>
          </div>
          
          <!-- 待提升领域 -->
          <div v-if="currentPolishedContent?.weaknesses && currentPolishedContent.weaknesses.length > 0" class="result-section">
            <h3>📈 待提升领域（优化后）</h3>
            <ul class="info-list">
              <li v-for="(weakness, index) in currentPolishedContent.weaknesses" :key="index">
                {{ weakness }}
              </li>
            </ul>
          </div>
          
          <!-- AI 优化建议 -->
          <div v-if="currentPolishedContent?.suggestions && currentPolishedContent.suggestions.length > 0" class="result-section">
            <h3>💡 AI 优化建议</h3>
            <ul class="suggestion-list">
              <!-- 显示完整的建议信息（类别 + 内容 + 优先级） -->
              <li v-for="(suggestion, index) in currentPolishedContent.suggestions" :key="index">
                <div class="suggestion-item">
                  <span class="suggestion-category">{{ suggestion.category }}</span>
                  <span class="suggestion-content">{{ suggestion.content }}</span>
                  <span 
                    class="suggestion-priority"
                    :class="{
                      'priority-high': suggestion.priority === 'high',
                      'priority-medium': suggestion.priority === 'medium',
                      'priority-low': suggestion.priority === 'low'
                    }"
                  >
                    {{ suggestion.priority === 'high' ? '🔴 高' : suggestion.priority === 'medium' ? '🟡 中' : '🟢 低' }}
                  </span>
                </div>
              </li>
            </ul>
          </div>
          
          <!-- 个人总结 -->
          <div v-if="currentPolishedContent?.polishedSummary" class="result-section">
            <h3>📝 AI 个人总结</h3>
            <p class="result-text summary-text">{{ currentPolishedContent.polishedSummary }}</p>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn secondary-btn" @click="cancelAndStay">再想想</button>
          <button class="btn primary-btn" @click="confirmAndGoToEdit">
            确认并前往编辑导出
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.report-page {
  --page-padding: 24px;
  --nav-block-height: 92px;
  min-height: 100vh;
  padding: calc(var(--nav-block-height) + 20px) var(--page-padding) var(--page-padding);
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Segoe UI', sans-serif;
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

.logout-btn {
  background: linear-gradient(90deg, rgba(255,255,255,0.06), rgba(255,255,255,0.02));
  border: 1px solid rgba(255,255,255,0.12);
  color: #fff;
}

.logout-btn:hover {
  background: #ff6b6b;
  color: #fff;
}

.report-content {
  position: relative;
  z-index: 2;
  max-width: none;
  width: 100%;
  margin: 0;
}

.report-layout {
  display: block;
  width: 100%;
}

.report-ability-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 300px);
  gap: 14px;
  align-items: stretch;
}

.report-ability-chart {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 6px;
  border: 1px solid rgba(106, 210, 255, 0.42);
  background: rgba(8, 27, 48, 0.62);
  box-shadow:
    inset 0 0 20px rgba(65, 174, 255, 0.16),
    0 0 20px rgba(64, 148, 255, 0.2);
}

.ability-radar-echart {
  width: 240px;
  height: 240px;
  max-width: 100%;
  margin: 2px 0 0;
}

h1 {
  margin: 0 0 18px;
  text-align: center;
  color: #12294b;
  font-size: 46px;
  font-weight: 800;
}

.report-head {
  padding: 20px;
  text-align: center;
  background: linear-gradient(135deg, rgba(183, 210, 232, 0.92), rgba(231, 241, 248, 0.96));
  margin-bottom: 14px;
}

.report-head h2 {
  margin: 0 0 8px;
  font-size: 30px;
  color: #1f4d9a;
}

.report-head p {
  margin: 0;
  font-size: 16px;
  color: #12294b;
}

.report-sheet {
  background: rgba(255, 255, 255, 0.95);
  overflow: hidden;
  margin-bottom: 14px;
}

.sheet-head {
  padding: 20px;
  text-align: center;
  background: rgba(8, 27, 48, 0.58);
  border-bottom: 1px solid rgba(106, 210, 255, 0.34);
  box-shadow: inset 0 0 18px rgba(65, 174, 255, 0.14);
}

.sheet-head h2 {
  margin: 0 0 8px;
  font-size: 34px;
  color: #dff6ff;
  text-shadow: 0 0 12px rgba(72, 197, 255, 0.34);
}

.sheet-head p {
  margin: 0;
  font-size: 16px;
  color: #a8e7ff;
}

.sheet-block {
  padding: 18px;
  border-bottom: 1px solid #d6e3f1;
}

.sheet-block h3 {
  margin: 0 0 12px;
  font-size: 26px;
  color: #12294b;
}

.sheet-block:last-child {
  border-bottom: none;
}

.module {
  background: rgba(255, 255, 255, 0.95);
  padding: 18px;
  margin-bottom: 14px;
}

.module h3 {
  margin: 0 0 12px;
  font-size: 28px;
  color: #12294b;
}

.row-table {
  border-radius: 12px;
  border: 1px solid rgba(106, 210, 255, 0.34);
  overflow: hidden;
  background: rgba(8, 27, 48, 0.58);
}

.row-item {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 12px;
  padding: 12px 14px;
  border-bottom: 1px solid rgba(106, 210, 255, 0.24);
  align-items: center;
}

.row-item:last-child {
  border-bottom: none;
}

.row-label {
  color: #95dfff;
  font-size: 18px;
  font-weight: 700;
}

.row-value {
  color: #caefff;
  font-size: 16px;
  line-height: 1.7;
}

.report-match-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 380px);
  gap: 14px;
  align-items: stretch;
}

.report-match-chart {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
  padding: 12px;
  border: 1px solid rgba(106, 210, 255, 0.42);
  border-radius: 10px;
  background:
    linear-gradient(rgba(99, 192, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 192, 255, 0.06) 1px, transparent 1px),
    rgba(8, 21, 39, 0.56);
  background-size: 100% 25%, 20% 100%, auto;
  box-shadow:
    inset 0 0 20px rgba(65, 174, 255, 0.16),
    0 0 20px rgba(64, 148, 255, 0.2);
}

.match-bar-echart {
  flex: 1 1 auto;
  width: 100%;
  min-height: 260px;
  height: 280px;
}

.ability-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.info-card {
  background: #f7fbff;
  border-radius: 12px;
  padding: 14px;
}

.info-card h4 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #1f4d9a;
}

.info-card strong {
  display: block;
  font-size: 28px;
  margin-bottom: 8px;
}

.info-card p {
  margin: 0;
  font-size: 14px;
  color: #4f6888;
  line-height: 1.7;
}

.target-job {
  margin-bottom: 10px;
  font-size: 20px;
  color: #1f4d9a;
  font-weight: 700;
}

.match-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.match-card {
  background: #f7fbff;
  border-radius: 12px;
  padding: 14px;
}

.match-card h4 {
  margin: 0 0 8px;
  font-size: 17px;
  color: #1f4d9a;
}

.score-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score-row strong {
  font-size: 28px;
}

.score-row span {
  color: #4f6888;
  font-size: 14px;
}

.goal-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.goal-card {
  background: #f7fbff;
  border-radius: 12px;
  padding: 14px;
}

.goal-card h4 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #1f4d9a;
}

.goal-card p {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: #3f5f86;
}

.quick-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.quick-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: min(360px, 100%);
}

/* 与首页「开始规划」.hero-cta 同系青→蓝→紫渐变；排除 App.vue 中 .btn 全局 !important 后由本规则生效 */
.btn.detail-link-btn {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  column-gap: 12px;
  text-align: left;
  font-size: 17px;
  font-weight: 700;
  width: 100%;
  max-width: 100%;
  color: #ffffff;
  background: linear-gradient(
    90deg,
    rgba(0, 242, 255, 0.35) 0%,
    rgba(0, 132, 255, 0.52) 38%,
    rgba(64, 90, 255, 0.48) 68%,
    rgba(118, 58, 235, 0.44) 100%
  );
  border: 1px solid rgba(0, 242, 255, 0.65);
  border-radius: 999px;
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.5),
    0 0 32px rgba(0, 242, 255, 0.35),
    0 0 48px rgba(255, 0, 255, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  overflow: hidden;
  padding: 11px 18px;
  text-shadow:
    0 0 10px rgba(0, 242, 255, 0.75),
    0 0 18px rgba(255, 0, 255, 0.28);
  transition: transform 0.22s ease, box-shadow 0.22s ease, filter 0.22s ease;
}

.detail-icon {
  font-size: 1.35em;
  line-height: 1;
  width: 1.6em;
  flex-shrink: 0;
  text-align: center;
}

.detail-text {
  font-size: 1em;
  line-height: 1.25;
  font-weight: 700;
}

.btn.detail-link-btn:hover {
  transform: translateY(-2px) scale(1.015);
  filter: saturate(1.12) brightness(1.04);
  box-shadow:
    0 16px 32px rgba(0, 0, 0, 0.55),
    0 0 42px rgba(0, 242, 255, 0.55),
    0 0 72px rgba(255, 0, 255, 0.32);
}

.btn.detail-link-btn:active {
  transform: translateY(0) scale(0.99);
}

.bottom-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 12px;
  align-items: center;
  padding: 18px;
}

.btn {
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  padding: 12px 18px;
  transition: all 0.2s ease;
}

.quick-btn {
  background: #e7f1f8;
  color: #1f4d9a;
}

.quick-btn:hover {
  background: #b7d2e8;
}

.unified-btn {
  background: #1f4d9a;
  color: #ffffff;
  width: max-content;
  min-width: 132px;
  padding: 10px 14px;
}

.unified-btn:hover {
  background: #3879c9;
}

.unified-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.report-editor {
  width: 100%;
  min-height: 260px;
  border: 1px solid rgba(106, 210, 255, 0.4);
  border-radius: 10px;
  background: rgba(8, 27, 48, 0.58);
  color: #def6ff;
  padding: 12px;
  line-height: 1.6;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 13px;
  box-sizing: border-box;
  box-shadow: inset 0 0 14px rgba(56, 166, 255, 0.14);
}

.export-select {
  border: 1px solid #d6e3f1;
  border-radius: 8px;
  background: #f7fbff;
  color: #12294b;
  font-size: 14px;
  padding: 10px 12px;
  min-width: 140px;
}

@media print {
  @page {
    size: auto;
    margin: 8mm 10mm 10mm;
  }

  html,
  body {
    margin: 0 !important;
    padding: 0 !important;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }

  .top-nav,
  .print-hidden,
  .bottom-row {
    display: none !important;
  }

  .report-page {
    padding: 0;
    min-height: auto;
    background: #ffffff;
  }

  .report-page::before,
  .report-page::after {
    display: none;
  }

  .report-content {
    max-width: none;
    margin: 0;
    padding: 0 2mm 6mm;
    position: relative;
  }

  .report-content::after {
    content: '职途智链';
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) rotate(-22deg);
    font-size: 92px;
    font-weight: 800;
    letter-spacing: 6px;
    color: rgba(31, 77, 154, 0.16);
    white-space: nowrap;
    z-index: 3;
    pointer-events: none;
  }

  h1,
  .report-sheet,
  .sheet-head,
  .sheet-block,
  .row-table,
  .row-item,
  .row-label,
  .row-value {
    position: relative;
    z-index: 1;
  }

  .card-surface,
  .report-sheet {
    border-radius: 0;
    box-shadow: none;
    background: #ffffff;
  }

  h1 {
    margin: 0 0 8px;
  }

  .sheet-block {
    break-inside: avoid;
    page-break-inside: avoid;
  }
}

@media (max-width: 1120px) {
  .report-match-layout {
    grid-template-columns: 1fr;
  }

  .report-ability-layout {
    grid-template-columns: 1fr;
  }

  .report-ability-chart {
    justify-content: flex-start;
  }

  .row-item {
    grid-template-columns: 1fr;
    gap: 6px;
  }
}

@media (max-width: 860px) {
  .report-page {
    --page-padding: 14px;
    --nav-block-height: 132px;
    padding: calc(var(--nav-block-height) + 20px) var(--page-padding) var(--page-padding);
  }

  .top-nav {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }
}

/* AI 润色结果预览弹窗样式 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.polish-result-modal {
  width: 90%;
  max-width: 900px;
  max-height: 85vh;
  overflow-y: auto;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 2px solid #e7f1f8;
  background: linear-gradient(135deg, #1f4d9a 0%, #3879c9 100%);
  color: #ffffff;
  border-radius: 16px 16px 0 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
}

.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-size: 28px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.modal-body {
  padding: 32px;
}

.result-section {
  margin-bottom: 28px;
  padding-bottom: 28px;
  border-bottom: 1px solid #e7f1f8;
}

.result-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.result-section h3 {
  margin: 0 0 16px;
  font-size: 20px;
  color: #1f4d9a;
  font-weight: 700;
}

.result-text {
  margin: 0;
  padding: 16px;
  background: rgba(8, 27, 48, 0.58);
  border-radius: 8px;
  border-left: 4px solid rgba(106, 210, 255, 0.72);
  line-height: 1.8;
  color: #caefff;
  font-size: 15px;
}

.summary-text {
  background: rgba(20, 37, 56, 0.72);
  border-left-color: rgba(108, 227, 255, 0.7);
  line-height: 2;
}

.info-list {
  margin: 0;
  padding-left: 24px;
  display: grid;
  gap: 10px;
}

.info-list li {
  line-height: 1.8;
  color: #caefff;
  font-size: 15px;
  padding: 12px 16px;
  background: rgba(8, 27, 48, 0.58);
  border-radius: 8px;
  border-left: 4px solid rgba(106, 210, 255, 0.7);
}

.goal-preview {
  display: grid;
  gap: 12px;
}

.goal-item {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  background: rgba(8, 27, 48, 0.58);
  border-radius: 8px;
  border-left: 4px solid rgba(106, 210, 255, 0.7);
}

.goal-label {
  font-weight: 700;
  color: #95dfff;
  white-space: nowrap;
  min-width: 80px;
}

.suggestion-list {
  margin: 0;
  padding-left: 24px;
  display: grid;
  gap: 12px;
}

.suggestion-list li {
  line-height: 1.8;
  color: #caefff;
  font-size: 15px;
  padding: 16px;
  background: rgba(8, 27, 48, 0.58);
  border-radius: 8px;
  border-left: 4px solid rgba(106, 210, 255, 0.7);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestion-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.suggestion-category {
  font-weight: 700;
  color: #95dfff;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.suggestion-category::before {
  content: '📌';
  font-size: 16px;
}

.suggestion-content {
  color: #caefff;
  line-height: 1.7;
  font-size: 15px;
  padding-left: 8px;
  border-left: 3px solid rgba(106, 210, 255, 0.52);
  margin-left: 4px;
}

.suggestion-priority {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  align-self: flex-start;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.suggestion-priority.priority-high {
  background: #f8d7da;
  color: #721c24;
  border-color: #dc3545;
}

.suggestion-priority.priority-medium {
  background: #fff3cd;
  color: #856404;
  border-color: #ffc107;
}

.suggestion-priority.priority-low {
  background: #d4edda;
  color: #155724;
  border-color: #28a745;
}

.highlight-section {
  background: linear-gradient(135deg, #f7fbff 0%, #e7f1f8 100%);
  padding: 20px;
  border-radius: 12px;
  border: 2px solid #3879c9;
}

.match-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.match-summary p {
  margin: 0;
  padding: 14px 18px;
  background: rgba(8, 27, 48, 0.58);
  border-radius: 8px;
  border: 2px solid rgba(106, 210, 255, 0.52);
  font-size: 15px;
  font-weight: 600;
  color: #95dfff;
}

.soft-skills-preview h4 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #12294b;
  font-weight: 600;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.skill-tag {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  background: #e7f1f8;
  color: #12294b;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.skill-tag.skill-high {
  background: #d4edda;
  color: #155724;
  border-color: #28a745;
}

.skill-tag.skill-mid {
  background: #fff3cd;
  color: #856404;
  border-color: #ffc107;
}

.skill-tag.skill-low {
  background: #f8d7da;
  color: #721c24;
  border-color: #dc3545;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px 32px;
  border-top: 2px solid #e7f1f8;
  background: #f7fbff;
  border-radius: 0 0 16px 16px;
}

.modal-footer .btn {
  min-width: 140px;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.primary-btn {
  background: linear-gradient(135deg, #1f4d9a 0%, #3879c9 100%);
  color: #ffffff;
  border: none;
  box-shadow: 0 4px 12px rgba(31, 77, 154, 0.3);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(31, 77, 154, 0.4);
}

.secondary-btn {
  background: #ffffff;
  color: #1f4d9a;
  border: 2px solid #1f4d9a;
}

.secondary-btn:hover {
  background: #f7fbff;
}

@media (max-width: 768px) {
  .polish-result-modal {
    width: 95%;
    max-height: 90vh;
  }
  
  .modal-header {
    padding: 20px;
  }
  
  .modal-body {
    padding: 20px;
  }
  
  .match-summary {
    grid-template-columns: 1fr;
  }
  
  .modal-footer {
    flex-direction: column;
    padding: 20px;
  }
  
  .modal-footer .btn {
    width: 100%;
  }
}
</style>
