<script setup>
import axios from 'axios'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ResumeForm from './ResumeForm.vue'

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

const sidebarMenus = [
  { key: 'student-input', label: '学生信息录入' },
  { key: 'job-match', label: '人岗匹配分析' },
  { key: 'development-path', label: '岗位知识图谱' },
  { key: 'action-plan', label: '行动计划' }
]

const activeMenu = ref('student-input')
const showTipsModal = ref(false)
const sideMenuRef = ref(null)
const sideMenuHeight = ref(0)
const matchingLoading = ref(false)
const matchingError = ref('')
const matchedJob = ref(null)
const matchingPayload = ref(null)

const pathLoading = ref(false)
const pathError = ref('')
const selectedJobKey = ref('Java_初级')
const jobKeyOptions = ref([])
const graphRef = ref(null)
const graphLoading = ref(false)
const graphError = ref('')
let graphInstance = null

const planLoading = ref(false)
const planError = ref('')
const shortPlans = ref([])
const midPlans = ref([])
const longPlans = ref([])
const resources = ref([])

let sideMenuResizeObserver = null

const pageVars = computed(() => ({
  '--side-menu-height': sideMenuHeight.value > 0 ? `${sideMenuHeight.value}px` : undefined
}))

const softSkillList = computed(() => {
  const scores = matchedJob.value?.soft_skill_scores
  if (!scores || typeof scores !== 'object') {
    return []
  }

  return Object.entries(scores).map(([name, score]) => ({
    name,
    score: Number(score) || 0
  }))
})

const getComment = (skillName) => {
  return matchedJob.value?.soft_skill_comments?.[skillName] || '暂无评语'
}

// 获取评分依据（用于 tooltip）
const getReason = (skillName) => {
  return matchedJob.value?.soft_skill_reasons?.[skillName]?.reason || '暂无评分依据'
}

/** 人岗综合分：硬×0.7 + 软技能五项算术平均×0.3（与后端 overall_match_score 一致） */
const computeOverallMatchScore = (m) => {
  if (!m) return 0
  if (m.overall_match_score != null && m.overall_match_score !== '') {
    return Number(m.overall_match_score)
  }
  const soft = m.soft_skill_scores || {}
  const vals = Object.values(soft)
    .map((x) => Number(x))
    .filter((x) => !Number.isNaN(x))
  const avg = vals.length ? vals.reduce((a, b) => a + b, 0) / vals.length : Number(m.soft_total_score || 0)
  return Math.round((Number(m.hard_skill_score || 0) * 0.7 + avg * 0.3) * 100) / 100
}

const overallMatchScoreDisplay = computed(() => computeOverallMatchScore(matchedJob.value))

const go = (path) => {
  if (route.path !== path) {
    router.push(path)
  }
}

const selectMenu = (key) => {
  activeMenu.value = key
}

const fetchJobMatching = async () => {
  matchingLoading.value = true
  matchingError.value = ''

  try {
    const { data } = await axios.post(`${API_BASE}/career/matching/run`)
    if (data?.code === 200 && data?.data?.best_match) {
      matchedJob.value = data.data.best_match
      matchingPayload.value = data.data
    } else {
      matchedJob.value = null
      matchingPayload.value = null
      matchingError.value = data?.message || '未获取到匹配结果，请稍后重试。'
    }
  } catch (error) {
    console.error('岗位匹配请求失败:', error)
    matchingError.value = '请求失败，请检查后端服务是否启动后重试。'
    matchedJob.value = null
    matchingPayload.value = null
  } finally {
    matchingLoading.value = false
  }
}

const fetchJobKeyOptions = async () => {
  try {
    const { data } = await axios.get(`${API_BASE}/career/options/job-keys`)
    const keys = data?.data?.jobKeys || []
    if (keys.length) {
      jobKeyOptions.value = keys
      if (!keys.includes(selectedJobKey.value)) {
        selectedJobKey.value = keys[0]
      }
    }
  } catch (e) {
    console.warn('加载岗位 key 列表失败', e)
  }
}

const renderCareerGraph = async () => {
  if (!graphRef.value || !selectedJobKey.value) return
  pathLoading.value = true
  pathError.value = ''
  graphLoading.value = true
  graphError.value = ''
  try {
    const [{ Network }, { data }] = await Promise.all([
      import('vis-network/standalone'),
      axios.get(`${API_BASE}/career/path/graph`, { params: { jobKey: selectedJobKey.value } })
    ])

    if (!data?.data?.focusJob) {
      graphError.value = '图谱数据为空'
      pathError.value = '图谱数据为空'
      return
    }

    if (graphInstance && typeof graphInstance.destroy === 'function') {
      graphInstance.destroy()
      graphInstance = null
    }

    const graphData = data.data
    const nodes = (graphData.nodes || []).map((n) => ({
      id: n.id,
      label: n.name || '未知岗位',
      title: `${n.name || ''}${n.level ? ` (${n.level})` : ''}`,
      shape: 'dot',
      size: n.name === graphData.focusJob ? 30 : 18,
      color: n.name === graphData.focusJob ? '#1f4d9a' : '#5b8ecb',
      font: { color: '#12335f', size: 14 }
    }))
    const edges = (graphData.edges || []).map((e) => ({
      id: e.id,
      from: e.from,
      to: e.to,
      label: e.type || '',
      arrows: 'to',
      color: e.type === 'PROMOTE_TO' ? '#1f4d9a' : '#26a69a',
      font: { align: 'middle', size: 11 }
    }))

    graphInstance = new Network(
      graphRef.value,
      { nodes, edges },
      {
        autoResize: true,
        physics: { enabled: true, stabilization: { iterations: 180 } },
        interaction: { hover: true, navigationButtons: true, keyboard: true }
      }
    )
  } catch (e) {
    console.error('图谱渲染失败:', e)
    graphError.value = '图谱渲染失败，请确认后端 /career/path/graph 接口可用。'
    pathError.value = graphError.value
  } finally {
    graphLoading.value = false
    pathLoading.value = false
  }
}

const mapPlanContent = (content) => {
  if (!content || typeof content !== 'object') return
  shortPlans.value = (content.shortTerm || []).map((item) => ({
    period: item.period || '',
    title: item.title || '',
    detail: item.detail || ''
  }))
  midPlans.value = (content.midTerm || []).map((item) => ({
    period: item.period || '',
    title: item.title || '',
    detail: item.detail || ''
  }))
  longPlans.value = (content.longTerm || []).map((item) => ({
    period: item.period || '',
    title: item.title || '',
    detail: item.detail || ''
  }))
  resources.value = Array.isArray(content.resources) ? content.resources : []
}

const fetchOrGeneratePlan = async () => {
  planLoading.value = true
  planError.value = ''
  try {
    const latest = await axios.get(`${API_BASE}/career/plan/latest`)
    if (latest.data?.code === 200 && latest.data?.data?.content_json) {
      mapPlanContent(latest.data.data.content_json)
      planLoading.value = false
      return
    }

    const mj = matchedJob.value
    const genBody = {
      jobTitle: mj?.job_title || '',
      jobId: mj?.job_id != null ? String(mj.job_id) : null,
      hardSkillScore: Number(mj?.hard_skill_score) || 0,
      totalScore: computeOverallMatchScore(mj)
    }
    const { data } = await axios.post(`${API_BASE}/career/plan/generate`, genBody)
    if (data?.code === 200 && data?.data) {
      mapPlanContent(data.data)
    } else {
      planError.value = data?.message || '计划生成失败'
    }
  } catch (error) {
    console.error('行动计划加载失败:', error)
    planError.value = '行动计划加载失败，请先完成人岗匹配或检查后端。'
  } finally {
    planLoading.value = false
  }
}

const abilityDimensions = computed(() => matchingPayload.value?.abilityDimensions || null)

const openTips = () => {
  showTipsModal.value = true
}

const closeTips = () => {
  showTipsModal.value = false
}

const syncSideMenuHeight = () => {
  sideMenuHeight.value = sideMenuRef.value?.offsetHeight ?? 0
}

onMounted(async () => {
  await nextTick()
  syncSideMenuHeight()
  fetchJobKeyOptions()
  fetchJobMatching()

  if (typeof ResizeObserver !== 'undefined') {
    sideMenuResizeObserver = new ResizeObserver(() => {
      syncSideMenuHeight()
    })
    if (sideMenuRef.value) {
      sideMenuResizeObserver.observe(sideMenuRef.value)
    }
  }

  window.addEventListener('resize', syncSideMenuHeight)
})

onBeforeUnmount(() => {
  if (sideMenuResizeObserver) {
    sideMenuResizeObserver.disconnect()
    sideMenuResizeObserver = null
  }
  window.removeEventListener('resize', syncSideMenuHeight)
  if (graphInstance && typeof graphInstance.destroy === 'function') {
    graphInstance.destroy()
    graphInstance = null
  }
})

watch(activeMenu, async (key) => {
  await nextTick()
  syncSideMenuHeight()
  if (key === 'development-path') {
    await fetchJobKeyOptions()
    nextTick(() => renderCareerGraph())
  }
  if (key === 'action-plan') {
    fetchOrGeneratePlan()
  }
})

watch(
  () => route.query.tab,
  (value) => {
    if (typeof value === 'string' && sidebarMenus.some((item) => item.key === value)) {
      activeMenu.value = value
    }
  },
  { immediate: true }
)

watch(selectedJobKey, () => {
  if (activeMenu.value === 'development-path') {
    nextTick(() => renderCareerGraph())
  }
})
</script>

<template>
  <div class="planning-page" :style="pageVars">
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

    <main class="layout-wrap">
      <aside ref="sideMenuRef" class="side-menu card-surface">
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

      <section class="content-area" :class="{ 'content-area-student': activeMenu === 'student-input' }">
        <article v-if="activeMenu === 'student-input'" class="resume-pane">
          <div class="resume-box">
            <button class="tips-btn" @click="openTips">ⓘ</button>
            <ResumeForm />
          </div>
        </article>

        <article
          v-if="activeMenu === 'job-match'"
          class="pane card-surface job-match-pane"
        >
          <div v-if="matchingLoading" class="job-state-box">
            <p class="state-text">正在进行人岗匹配，请稍候...</p>
          </div>

          <div v-else-if="matchingError" class="job-state-box">
            <p class="state-text error">{{ matchingError }}</p>
            <button class="btn btn-primary" @click="fetchJobMatching">重新匹配</button>
          </div>

          <template v-else-if="matchedJob">
            <section class="summary card-surface summary-job">
              <div class="target-role">
                <h3>最佳匹配岗位</h3>
                <p>{{ matchedJob.job_title }}</p>
                <span class="job-id">岗位编号：{{ matchedJob.job_id }}</span>
              </div>
              <div class="total-score-row">
                <div class="total-score-card">
                  <strong>{{ overallMatchScoreDisplay }}</strong>
                  <span>人岗综合分</span>
                  <small class="score-hint">硬×0.7 + 软均值×0.3</small>
                </div>
                <div class="total-score-card total-score-card--compete">
                  <strong>{{ matchedJob.total_score }}</strong>
                  <span>竞争力评分</span>
                  <small class="score-hint">按岗位画像 / cmetadata 权重</small>
                </div>
              </div>
            </section>

            <section v-if="abilityDimensions" class="module card-surface">
              <h3>能力维度匹配（后端计算）</h3>
              <div class="metric-list">
                <div class="metric-item">
                  <div class="metric-top">
                    <span>基础要求契合度</span>
                    <strong>{{ abilityDimensions.basicRequirement }}</strong>
                  </div>
                  <div class="progress">
                    <div class="progress-bar" :style="{ width: `${Math.min(abilityDimensions.basicRequirement, 100)}%` }"></div>
                  </div>
                </div>
                <div class="metric-item">
                  <div class="metric-top">
                    <span>专业技能契合度</span>
                    <strong>{{ abilityDimensions.professionalSkills }}</strong>
                  </div>
                  <div class="progress">
                    <div class="progress-bar" :style="{ width: `${Math.min(abilityDimensions.professionalSkills, 100)}%` }"></div>
                  </div>
                </div>
                <div class="metric-item">
                  <div class="metric-top">
                    <span>通用素质</span>
                    <strong>{{ abilityDimensions.generalQuality }}</strong>
                  </div>
                  <div class="progress">
                    <div class="progress-bar" :style="{ width: `${Math.min(abilityDimensions.generalQuality, 100)}%` }"></div>
                  </div>
                </div>
                <div class="metric-item">
                  <div class="metric-top">
                    <span>发展潜力</span>
                    <strong>{{ abilityDimensions.developmentPotential }}</strong>
                  </div>
                  <div class="progress">
                    <div class="progress-bar" :style="{ width: `${Math.min(abilityDimensions.developmentPotential, 100)}%` }"></div>
                  </div>
                </div>
              </div>
              <p class="skill-comment skill-comment-dimension-summary">
                硬技能：{{ matchedJob.hard_skill_score }} 分；软技能（画像加权均分）：{{ matchedJob.soft_total_score }} 分；
                <template v-if="matchedJob.soft_simple_average != null">软技能算术均分：{{ matchedJob.soft_simple_average }} 分。</template>
              </p>
            </section>

            <section class="module card-surface">
              <h3>软技能分项得分</h3>
              <div class="metric-list">
                <div v-for="item in softSkillList" :key="item.name" class="metric-item">
                  <div class="metric-top">
                    <span>{{ item.name }}</span>
                    <strong :title="getReason(item.name)">{{ item.score }}</strong>
                  </div>
                  <div class="progress">
                    <div class="progress-bar" :style="{ width: `${Math.min(item.score, 100)}%` }"></div>
                  </div>
                  <p class="skill-comment">{{ getComment(item.name) }}</p>
                </div>
              </div>
            </section>

            <div class="action-row">
              <button class="btn btn-primary" @click="fetchJobMatching">重新匹配</button>
              <button class="btn btn-primary" @click="go('/report-management')">生成职业规划报告</button>
              <button class="btn btn-primary" @click="go('/career-planning?tab=action-plan')">查看行动计划</button>
            </div>
          </template>
        </article>

        <article v-if="activeMenu === 'development-path'" class="pane card-surface">
          <section class="module card-surface development-module">
            <h3>路径起点岗位</h3>
            <p class="path-select-row">
              <label for="job-key-select">选择岗位（与岗位画像 key 一致）</label>
              <select id="job-key-select" v-model="selectedJobKey" class="job-key-select">
                <option v-for="k in jobKeyOptions" :key="k" :value="k">{{ k }}</option>
              </select>
            </p>
          </section>

          <section class="module card-surface development-module">
            <h3>完整职业图谱（Neo4j）</h3>
            <div class="action-row" style="margin-bottom: 10px;">
              <button class="btn btn-primary" @click="renderCareerGraph">手动渲染</button>
            </div>
            <div v-if="pathLoading || graphLoading" class="job-state-box">
              <p class="state-text">正在渲染图谱...</p>
            </div>
            <div v-else-if="pathError || graphError" class="job-state-box">
              <p class="state-text error">{{ pathError || graphError }}</p>
              <button class="btn btn-primary" @click="renderCareerGraph">重新加载</button>
            </div>
            <div v-show="!(pathLoading || graphLoading)" ref="graphRef" class="neovis-graph"></div>
          </section>

          <div class="action-row">
            <button class="btn btn-primary" @click="selectMenu('action-plan')">查看详细行动计划</button>
          </div>
        </article>

        <article v-if="activeMenu === 'action-plan'" class="pane card-surface">
          <div v-if="planLoading" class="job-state-box">
            <p class="state-text">正在加载行动计划...</p>
          </div>
          <div v-else-if="planError" class="job-state-box">
            <p class="state-text error">{{ planError }}</p>
            <button class="btn btn-primary" @click="fetchOrGeneratePlan">重试</button>
          </div>
          <template v-else>
            <section class="module card-surface action-plan-module">
              <h3>短期计划（1-6个月）</h3>
              <div class="plan-list">
                <article v-for="item in shortPlans" :key="item.period" class="plan-card">
                  <h4>{{ item.period }} · {{ item.title }}</h4>
                  <p>{{ item.detail }}</p>
                </article>
              </div>
            </section>

            <section class="module card-surface action-plan-module">
              <h3>中期计划（6-12个月）</h3>
              <div class="plan-list">
                <article v-for="item in midPlans" :key="item.period" class="plan-card">
                  <h4>{{ item.period }} · {{ item.title }}</h4>
                  <p>{{ item.detail }}</p>
                </article>
              </div>
            </section>

            <section v-if="longPlans.length" class="module card-surface action-plan-module">
              <h3>长期规划</h3>
              <div class="plan-list">
                <article v-for="item in longPlans" :key="item.period" class="plan-card">
                  <h4>{{ item.period }} · {{ item.title }}</h4>
                  <p>{{ item.detail }}</p>
                </article>
              </div>
            </section>

            <section class="module card-surface action-plan-module">
              <h3>学习与实践建议（后端生成）</h3>
              <div class="resource-grid">
                <article v-for="resource in resources" :key="resource.name + resource.title" class="resource-card">
                  <h4>{{ resource.title }}</h4>
                  <p>{{ resource.name }}</p>
                  <span>{{ resource.type }}</span>
                </article>
              </div>
            </section>
          </template>
        </article>
      </section>
    </main>

    <div v-if="showTipsModal" class="modal-mask" @click="closeTips">
      <div class="tips-modal card-surface" @click.stop>
        <h3>填写提示</h3>
        <ul>
          <li>核心技能请量化为熟练工具与典型项目场景，能显著提升匹配准确度。</li>
          <li>项目经历建议写清角色职责、关键成果与业务价值，更利于求职展示。</li>
          <li>职业意向尽量具体到岗位方向和行业偏好，系统会给出更精准路径建议。</li>
          <li>软素质建议结合真实案例描述，便于识别沟通协作与成长潜力。</li>
        </ul>
        <button class="btn btn-primary" @click="closeTips">我知道了</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.planning-page {
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

.planning-page::before,
.planning-page::after {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
}

.planning-page::before {
  display: none;
}

.planning-page::after {
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

.content-area-student {
  min-height: var(--side-menu-height, var(--side-menu-min-height));
}

.pane {
  background: rgba(255, 255, 255, 0.92);
  padding: 18px;
}

.job-match-pane-pending {
  min-height: var(--side-menu-height, var(--side-menu-min-height));
  background: transparent;
  box-shadow: none;
  padding: 0;
}

.job-match-intro {
  min-height: var(--side-menu-height, var(--side-menu-min-height));
  display: grid;
  place-items: center;
}

.btn.start-match-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 240px;
  min-height: 96px;
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: 1px;
  padding: 0 28px;
  border-radius: 12px;
}

.resume-pane {
  position: relative;
  min-height: var(--side-menu-height, var(--side-menu-min-height));
  background: transparent;
  box-shadow: none;
  border-radius: 0;
  padding: 0;
}

.resume-box {
  position: relative;
  min-height: 100%;
}

.resume-box :deep(.resume-form-wrap) {
  width: 100%;
  max-width: none;
  margin: 0;
  min-height: 100%;
  box-sizing: border-box;
}

.resume-box :deep(.resume-table th) {
  background: #f7fbff;
  color: #12294b;
}

.tips-btn {
  position: absolute;
  top: 52px;
  right: 24px;
  z-index: 5;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: #3879c9;
  color: #ffffff;
  font-size: 18px;
  cursor: pointer;
}

.summary {
  background: #e7f1f8;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.summary-job {
  gap: 16px;
  flex-wrap: wrap;
}

.total-score-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: stretch;
}

.job-id {
  display: inline-block;
  margin-top: 8px;
  font-size: 14px;
  color: #1f4d9a;
  font-weight: 600;
}

.job-state-box {
  min-height: 220px;
  display: grid;
  place-items: center;
  gap: 12px;
  text-align: center;
}

.state-text {
  margin: 0;
  font-size: 18px;
  color: #12294b;
}

.state-text.error {
  color: #d64545;
}

.total-score-card {
  min-width: 170px;
  border-radius: 12px;
  color: #ffffff;
  padding: 18px 20px;
  display: grid;
  text-align: center;
  /* 与顶栏导航同系：深蓝 → 品牌蓝 → 亮青 */
  background: linear-gradient(145deg, #1f4d9a 0%, #3879c9 46%, #32c9fd 100%);
  border: 1px solid rgba(106, 210, 255, 0.55);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.2),
    0 10px 28px rgba(31, 77, 154, 0.38),
    0 0 22px rgba(50, 201, 253, 0.18);
}

.total-score-card strong {
  font-size: 46px;
  line-height: 1;
  text-shadow: 0 2px 12px rgba(0, 30, 60, 0.35);
}

.total-score-card span {
  margin-top: 6px;
  font-size: 14px;
}

.total-score-card .score-hint {
  display: block;
  margin-top: 8px;
  font-size: 11px;
  opacity: 0.92;
  line-height: 1.3;
  color: rgba(255, 255, 255, 0.88);
}

.total-score-card--compete {
  /* 与整站冷色背景协调的青绿渐变 */
  background: linear-gradient(145deg, #0a4d52 0%, #168a72 42%, #2ec4a6 100%);
  border: 1px solid rgba(120, 230, 200, 0.5);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.18),
    0 10px 28px rgba(10, 77, 82, 0.36),
    0 0 20px rgba(46, 196, 166, 0.2);
}

.score-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.score-card {
  border-radius: 10px;
  padding: 14px;
  background: #f7fbff;
}

.score-card h4 {
  margin: 0;
  font-size: 16px;
  color: #1f4d9a;
}

.score-card p {
  margin: 8px 0 0;
  font-size: 30px;
  font-weight: 700;
}

.target-role h3 {
  margin: 0 0 8px;
  font-size: 20px;
}

.target-role p {
  margin: 0;
  font-size: 44px;
  font-weight: 700;
}

.match-circle {
  --percent: 80%;
  width: 168px;
  height: 168px;
  border-radius: 50%;
  background: conic-gradient(#1f4d9a var(--percent), #b7d2e8 0);
  display: grid;
  place-items: center;
}

.match-inner {
  width: 132px;
  height: 132px;
  border-radius: 50%;
  background: #ffffff;
  display: grid;
  place-items: center;
  text-align: center;
}

.match-inner strong {
  font-size: 46px;
  line-height: 1;
  transform: translateY(15px);
}

.match-inner span {
  font-size: 14px;
}

.module {
  background: #ffffff;
  padding: 16px;
  margin-bottom: 14px;
}

.module h3 {
  margin: 0 0 10px;
  font-size: 22px;
}

.dimension-tabs {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.dimension-tab {
  border: none;
  border-radius: 10px;
  background: #e7f1f8;
  color: #12294b;
  padding: 10px;
  font-size: 18px;
  cursor: pointer;
  display: grid;
  gap: 4px;
}

.development-module h3 {
  font-size: 28px;
}

.action-plan-module h3 {
  font-size: 26px;
}

.dimension-tab span {
  font-size: 13px;
  color: #1f4d9a;
  font-weight: 700;
}

.dimension-tab.active {
  background: #3879c9;
  color: #ffffff;
}

.dimension-tab.active span {
  color: #ffffff;
}

.metric-list {
  display: grid;
  gap: 12px;
}

.metric-item {
  background: #f7fbff;
  border-radius: 10px;
  padding: 12px;
}

.job-match-pane .metric-item {
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.34);
  border-radius: 10px;
}

.job-match-pane .metric-top {
  color: #ffffff;
}

.job-match-pane .metric-top span,
.job-match-pane .metric-top strong {
  color: #ffffff;
}

.metric-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.progress {
  height: 10px;
  border-radius: 999px;
  background: #e7f1f8;
  overflow: hidden;
}

.job-match-pane .metric-item .progress {
  background: rgba(255, 255, 255, 0.14);
}

.progress-bar {
  height: 100%;
  border-radius: 999px;
  background: #3879c9;
}

.job-match-pane .metric-item .progress-bar {
  background: linear-gradient(90deg, #7c5cff 0%, #e040b0 52%, #ff6b9d 100%);
}

.metric-item p {
  margin: 8px 0 0;
  font-size: 13px;
  color: #12294b;
}

.job-match-pane .metric-item .skill-comment {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.55;
  color: #ffffff;
  border-top-color: rgba(255, 255, 255, 0.22);
}

.job-match-pane .skill-comment-dimension-summary {
  margin-top: 14px;
  padding-top: 10px;
  font-size: 16px;
  line-height: 1.6;
  color: #ffffff;
  border-top: 1px dashed rgba(255, 255, 255, 0.28);
}

.gap-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.gap-grid h4 {
  margin: 0 0 8px;
  font-size: 16px;
  display: inline-block;
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid #e7f1f8;
  background: #e7f1f8;
}

.gap-grid ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.job-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.job-card {
  background: #e7f1f8;
  border-radius: 12px;
  padding: 14px;
}

.job-card h4 {
  margin: 0 0 8px;
  font-size: 18px;
}

.job-card p {
  margin: 0;
  font-size: 14px;
  color: #1f4d9a;
  font-weight: 700;
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn {
  border: none;
  cursor: pointer;
  border-radius: 8px;
  font-size: 15px;
  padding: 11px 20px;
  transition: all 0.2s ease;
}

.btn-primary {
  background: #1f4d9a;
  color: #ffffff;
}

.btn-primary:hover {
  background: #3879c9;
}

.btn-secondary {
  background: #b7d2e8;
  color: #12294b;
}

.btn-secondary:hover {
  background: #3879c9;
  color: #ffffff;
}

.path-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.path-line {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 34px;
}

.stage-card {
  position: relative;
  background: #f7fbff;
  border-radius: 12px;
  padding: 16px;
  min-height: 220px;
}

.stage-card h3 {
  margin: 0 0 6px;
  font-size: 22px;
}

.stage-card .years {
  margin: 0 0 12px;
  color: #1f4d9a;
  font-weight: 700;
}

.stage-card p {
  margin: 0 0 10px;
  font-size: 14px;
  line-height: 1.65;
}

.arrow {
  position: absolute;
  top: 50%;
  right: -28px;
  transform: translateY(-50%);
  font-size: 42px;
  color: #1f4d9a;
}

.plan-list {
  display: grid;
  gap: 12px;
}

.plan-card {
  background: #f7fbff;
  border-radius: 12px;
  padding: 14px;
}

.plan-card h4 {
  margin: 0 0 8px;
  font-size: 18px;
}

.plan-card p {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.resource-card {
  background: #e7f1f8;
  border-radius: 12px;
  padding: 14px;
}

.resource-card h4 {
  margin: 0 0 8px;
  font-size: 18px;
}

.resource-card p {
  margin: 0 0 8px;
  font-size: 14px;
  line-height: 1.6;
}

.resource-card span {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 8px;
  background: #1f4d9a;
  color: #ffffff;
  font-size: 13px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(18, 41, 75, 0.35);
  display: grid;
  place-items: center;
  z-index: 40;
}

.tips-modal {
  width: min(560px, calc(100vw - 32px));
  background: #ffffff;
  padding: 22px;
}

.tips-modal h3 {
  margin: 0 0 10px;
  font-size: 24px;
}

.tips-modal ul {
  margin: 0 0 14px;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  font-size: 14px;
  line-height: 1.7;
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

  .resume-pane {
    min-height: auto;
  }

  .content-area {
    margin-left: 221px;
  }

  .path-line {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .arrow {
    display: none;
  }
}

@media (max-width: 860px) {
  .planning-page {
    --page-padding: 14px;
    --nav-block-height: 132px;
    padding: calc(var(--nav-block-height) + 20px) var(--page-padding) var(--page-padding);
  }

  .top-nav {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .total-score-card {
    width: 100%;
  }

  .score-grid {
    grid-template-columns: 1fr;
  }

  .tips-btn {
    top: 42px;
    right: 24px;
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

  .dimension-tabs,
  .job-cards,
  .resource-grid,
  .gap-grid {
    grid-template-columns: 1fr;
  }
}
.skill-comment {
  margin-top: 6px;
  font-size: 13px;
  color: #555;
  line-height: 1.5;
  padding-top: 4px;
  border-top: 1px dashed #e0e8f0;
}

.path-select-row {
  margin: 0;
  display: grid;
  gap: 8px;
  font-size: 15px;
  color: #12294b;
}

.job-key-select {
  max-width: 320px;
  border: 1px solid #b7d2e8;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 15px;
  background: #f7fbff;
  color: #12294b;
}

.neovis-graph {
  width: 100%;
  height: 460px;
  border-radius: 10px;
  background: #f7fbff;
  border: 1px solid #d6e3f1;
}

/* 确保 tooltip 可见（可选） */
.metric-top strong {
  cursor: help;
}
</style>