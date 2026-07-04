<script setup>
import { computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const navItems = [
  { label: '首页', path: '/' },
  { label: '职业规划', path: '/career-planning' },
  { label: '数据中心', path: '/data-center' },
  { label: '报告管理', path: '/report-management' },
  { label: '个人中心', path: '/student-profile' },
  
]

const levelOptions = ['初级', '中级', '高级']

const coreFeatures = [
  {
    title: 'Java开发工程师',
    icon: '📊',
    levelProfiles: {
      初级: {
        responsibilities: [
          '参与企业级后端服务开发，完成基础接口与模块编码。',
          '按规范进行接口联调和单元测试，保障基础功能稳定。'
        ],
        requirements: [
          '掌握 Java 基础语法、集合与面向对象编程思想。',
          '了解 SpringBoot、MySQL，能独立完成简单业务需求。'
        ],
        prospects: [
          '成长为可独立负责模块开发的后端工程师。'
        ]
      },
      中级: {
        responsibilities: [
          '负责企业级后端服务开发、接口设计与系统稳定性保障。',
          '主导核心业务模块重构与性能优化，推进高质量交付。'
        ],
        requirements: [
          '熟练使用 Spring/SpringBoot、MySQL、Redis 等技术栈。',
          '具备问题定位和系统优化能力，能指导初级同学开发。'
        ],
        prospects: [
          '可向架构师、技术负责人或分布式平台方向持续成长。'
        ]
      },
      高级: {
        responsibilities: [
          '负责关键系统架构设计与高并发场景下的稳定性建设。',
          '推动跨团队技术方案落地，建立后端开发规范与治理体系。'
        ],
        requirements: [
          '精通分布式系统设计、性能调优与服务治理方案。',
          '具备技术决策能力和项目推进能力，有团队带教经验。'
        ],
        prospects: [
          '可向高级架构师、技术专家与后端团队负责人发展。'
        ]
      }
    }
  },
  {
    title: '前端开发工程师',
    icon: '🧠',
    levelProfiles: {
      初级: {
        responsibilities: [
          '参与 Web 页面开发，完成常规功能与样式还原。',
          '配合后端进行接口联调，修复基础交互问题。'
        ],
        requirements: [
          '熟悉 HTML/CSS/JavaScript，了解 Vue 或 React 基础。',
          '具备基础调试能力，能按规范提交与维护代码。'
        ],
        prospects: [
          '成长为独立负责业务页面的前端开发工程师。'
        ]
      },
      中级: {
        responsibilities: [
          '负责Web端页面开发、交互体验优化与前后端协作交付。',
          '推进组件化与工程化实践，提升研发效率和页面性能。'
        ],
        requirements: [
          '熟练掌握 JavaScript/TypeScript、Vue/React 及构建工具。',
          '具备性能优化与问题定位能力，可承担核心模块开发。'
        ],
        prospects: [
          '可向前端架构、可视化、全栈方向拓展。'
        ]
      },
      高级: {
        responsibilities: [
          '主导前端架构设计与技术选型，建设通用组件与工程规范。',
          '负责复杂交互与高性能场景方案落地，推动跨端一致性。'
        ],
        requirements: [
          '精通前端工程体系、性能优化及大型项目架构设计。',
          '具备跨团队协作与技术影响力，能带领团队持续演进。'
        ],
        prospects: [
          '可向前端架构师、技术负责人或全栈负责人发展。'
        ]
      }
    }
  },
  {
    title: 'C/C++开发工程师',
    icon: '🎯',
    levelProfiles: {
      初级: {
        responsibilities: [
          '参与高性能核心模块编码与基础功能实现。',
          '协助完成跨平台编译、调试与基础性能优化。'
        ],
        requirements: [
          '掌握 C/C++ 语法与常用数据结构，了解 Linux 开发环境。',
          '能阅读并维护已有代码，具备基础问题排查能力。'
        ],
        prospects: [
          '成长为可独立负责模块优化的 C/C++ 工程师。'
        ]
      },
      中级: {
        responsibilities: [
          '负责高性能核心模块开发、底层优化与跨平台功能实现。',
          '参与系统性能分析与关键链路优化，提升运行效率。'
        ],
        requirements: [
          '熟练掌握 C/C++、Linux、网络编程与算法优化方法。',
          '具备跨平台项目经验，能够独立完成复杂模块设计。'
        ],
        prospects: [
          '可向高性能系统开发、基础架构与技术专家方向发展。'
        ]
      },
      高级: {
        responsibilities: [
          '主导底层框架与核心引擎架构设计，解决复杂性能瓶颈。',
          '制定核心模块技术标准，推进关键技术攻关与落地。'
        ],
        requirements: [
          '精通系统级调优、并发编程与跨平台架构设计。',
          '具备技术决策与团队指导能力，能承担关键项目负责人角色。'
        ],
        prospects: [
          '可向系统架构师、技术专家或平台负责人方向发展。'
        ]
      }
    }
  },
  {
    title: '软件测试工程师',
    icon: '📝',
    levelProfiles: {
      初级: {
        responsibilities: [
          '参与功能测试执行，编写基础测试用例并记录缺陷。',
          '配合开发进行缺陷复现和回归验证，保障版本质量。'
        ],
        requirements: [
          '了解软件测试流程，掌握常用测试方法与缺陷管理工具。',
          '熟悉 SQL 基础，能完成简单数据校验与结果核对。'
        ],
        prospects: [
          '成长为能够独立负责模块测试的质量工程师。'
        ]
      },
      中级: {
        responsibilities: [
          '负责功能、性能与自动化测试，推动研发质量持续提升。',
          '负责测试方案设计、缺陷管理与质量保障闭环。'
        ],
        requirements: [
          '熟练掌握接口测试、自动化测试框架与质量分析方法。',
          '具备问题定位能力，能推动跨团队协作解决质量问题。'
        ],
        prospects: [
          '可向测试架构师、质量平台方向发展。'
        ]
      },
      高级: {
        responsibilities: [
          '主导测试体系建设与质量度量机制设计，保障复杂系统交付。',
          '建设自动化与性能平台，推动测试左移与持续质量改进。'
        ],
        requirements: [
          '精通自动化框架、性能测试与质量工程体系设计。',
          '具备质量策略制定与团队协同推动能力。'
        ],
        prospects: [
          '可向测试开发负责人、质量负责人方向发展。'
        ]
      }
    }
  },
  {
    title: '实施工程师',
    icon: '🛠️',
    levelProfiles: {
      初级: {
        responsibilities: [
          '参与系统部署实施与基础配置，协助完成环境初始化与上线准备。',
          '配合客户完成操作培训与使用指导，跟进实施过程中的问题闭环。'
        ],
        requirements: [
          '了解系统部署流程，具备基础 SQL 能力与需求理解能力。',
          '具备良好的沟通执行能力，能够按计划推进实施任务。'
        ],
        prospects: [
          '可成长为能独立负责项目交付的实施工程师。'
        ]
      },
      中级: {
        responsibilities: [
          '负责系统部署实施、需求落地与客户培训支持，保障项目按期交付。',
          '主导实施过程中的问题定位与方案协调，推动跨角色协同执行。'
        ],
        requirements: [
          '熟悉实施方法论与项目协同流程，能够独立推进中型项目落地。',
          '具备较强的需求分析、文档编写与现场沟通能力。'
        ],
        prospects: [
          '可向项目经理、解决方案顾问方向发展。'
        ]
      },
      高级: {
        responsibilities: [
          '负责复杂项目实施方案设计与交付节奏管理，保障多方协同与质量达标。',
          '沉淀实施规范与最佳实践，提升团队交付效率与项目复用能力。'
        ],
        requirements: [
          '精通需求梳理、项目推进与风险管理，具备跨团队推动能力。',
          '具备客户管理与方案落地能力，可指导初中级成员交付。'
        ],
        prospects: [
          '可向高级项目经理、资深解决方案顾问方向持续成长。'
        ]
      }
    }
  },
  {
    title: '技术支持工程师',
    icon: '🧰',
    levelProfiles: {
      初级: {
        responsibilities: [
          '负责基础故障受理、问题排查与工单处理，保障日常服务稳定。',
          '参与监控告警处理与运行巡检，协助完成常规运维操作。'
        ],
        requirements: [
          '了解 Linux 基础操作与网络常识，具备基础脚本能力。',
          '具备较强的服务意识和沟通能力，能够及时响应业务问题。'
        ],
        prospects: [
          '可成长为独立值守与问题闭环的技术支持工程师。'
        ]
      },
      中级: {
        responsibilities: [
          '负责生产环境稳定运行、故障响应与自动化运维优化。',
          '建设并完善监控体系与应急流程，提升系统可用性与恢复效率。'
        ],
        requirements: [
          '熟悉 Linux 运维、监控体系与脚本开发，具备故障定位能力。',
          '能够推动跨团队协作，处理复杂线上问题并输出改进方案。'
        ],
        prospects: [
          '可向 SRE、云平台运维专家方向成长。'
        ]
      },
      高级: {
        responsibilities: [
          '主导高可用运维架构设计，推进容器化与自动化平台能力建设。',
          '负责重大故障复盘与治理，建立稳定性标准与持续改进机制。'
        ],
        requirements: [
          '精通自动化运维、可观测性体系与稳定性治理方法。',
          '具备技术决策能力和团队带教能力，能推动平台化建设落地。'
        ],
        prospects: [
          '可向资深 SRE、云原生平台负责人方向发展。'
        ]
      }
    }
  }
]

const selectedLevels = reactive(
  Object.fromEntries(coreFeatures.map((feature) => [feature.title, '初级']))
)

const setFeatureLevel = (featureTitle, level) => {
  selectedLevels[featureTitle] = level
}

const getFeatureProfile = (feature) => {
  const level = selectedLevels[feature.title] || '初级'
  return feature.levelProfiles[level] || feature.levelProfiles['初级']
}

const portraitJobBaseMap = {
  'Java开发工程师': 'Java',
  '前端开发工程师': '前端开发',
  'C/C++开发工程师': 'C/C++',
  '软件测试工程师': '软件测试',
  '实施工程师': '实施工程师',
  '技术支持工程师': '技术支持工程师'
}

const jobCategoryMap = {
  'Java开发工程师': 'Java',
  '前端开发工程师': '前端开发',
  'C/C++开发工程师': 'C/C++',
  '软件测试工程师': '软件测试',
  '实施工程师': '实施工程师',
  '技术支持工程师': '技术支持与运维'
}

const goToPortraitDetails = (feature) => {
  const level = selectedLevels[feature.title] || '初级'
  const jobBase = portraitJobBaseMap[feature.title] || 'Java'
  router.push({
    path: '/data-center/job-portrait',
    query: {
      jobKey: `${jobBase}_${level}`
    }
  })
}

const goToJobCompanies = (feature) => {
  const jobBase = jobCategoryMap[feature.title] || 'Java'
  router.push({
    path: '/data-center/job-data',
    query: {
      category: jobBase
    }
  })
}

const isCurrent = computed(() => (path) => route.path === path)

const go = (path) => {
  if (route.path !== path) {
    router.push(path)
  }
}
</script>

<template>
  <div class="home-page">
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
          :class="{ active: isCurrent(item.path) }"
          @click="go(item.path)"
        >
          {{ item.label }}
        </button>
      </nav>
    </header>

    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">大学生职业规划平台</h1>
        <p>面向高校计算机类学生的智能职业规划平台，贯通能力评估、岗位匹配、路径设计与报告生成全流程。</p>
        <div class="hero-actions">
          <button class="btn btn-primary hero-cta" @click="go('/career-planning?tab=student-input')">
            <span class="hero-cta-text">开始规划</span>
            <span class="hero-cta-arrow" aria-hidden="true"></span>
          </button>
        </div>
      </div>
    </section>

    <section class="section-wrap">
      <h2>岗位预览</h2>
      <div class="core-grid">
        <article v-for="feature in coreFeatures" :key="feature.title" class="core-card card-surface">
          <div class="core-head">
            <div class="core-icon">{{ feature.icon }}</div>
            <h3>{{ feature.title }}</h3>
          </div>
          <div class="core-level-tabs" role="tablist" :aria-label="`${feature.title}级别切换`">
            <button
              v-for="level in levelOptions"
              :key="level"
              class="core-level-btn"
              :class="{ active: selectedLevels[feature.title] === level }"
              @click="setFeatureLevel(feature.title, level)"
            >
              {{ level }}
            </button>
          </div>
          <div class="core-mini-box core-mini-box-detailed">
              <h4 class="job-mini-title">{{ feature.title }}（{{ selectedLevels[feature.title] }}）</h4>
              <div class="job-section">
                <strong>岗位职责</strong>
                <ul>
                  <li v-for="item in getFeatureProfile(feature).responsibilities" :key="item">{{ item }}</li>
                </ul>
              </div>
              <div class="job-section">
                <strong>任职要求</strong>
                <ul>
                  <li v-for="item in getFeatureProfile(feature).requirements" :key="item">{{ item }}</li>
                </ul>
              </div>
              <div class="job-section">
                <strong>发展前景</strong>
                <ul>
                  <li v-for="item in getFeatureProfile(feature).prospects" :key="item">{{ item }}</li>
                </ul>
              </div>
              <div class="job-detail-action">
                <button class="job-detail-btn" @click="goToPortraitDetails(feature)">查看详情</button>
                <button class="job-company-btn" @click="goToJobCompanies(feature)">查看公司</button>
              </div>
          </div>
        </article>
      </div>
    </section>

  </div>
</template>

<style scoped>
.home-page {
  --page-padding: 24px;
  --nav-block-height: 92px;
  /* 赛博银河撞色：深海军底 + 霓虹青主 + 品红辅 */
  --neo-bg-0: #0b1026;
  --neo-bg-1: #0d1b2a;
  --neo-bg-2: #121e3d;
  --neo-cyan: #00f2ff;
  --neo-magenta: #ff00ff;
  --neo-violet: #d600ff;
  --text-main: #f2fdff;
  --text-body: #b8f8ff;
  --text-muted: #e98de0;
  min-height: 100vh;
  padding: calc(var(--nav-block-height) + 90px) var(--page-padding) var(--page-padding);
  color: var(--text-main);
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Segoe UI', sans-serif;
  background:
    radial-gradient(ellipse 120% 80% at 18% 40%, rgba(0, 242, 255, 0.12) 0%, transparent 55%),
    radial-gradient(ellipse 100% 90% at 88% 62%, rgba(255, 0, 255, 0.14) 0%, transparent 52%),
    radial-gradient(circle at 50% 120%, rgba(214, 0, 255, 0.1) 0%, transparent 45%),
    linear-gradient(150deg, rgba(9, 16, 38, 0.94) 0%, rgba(11, 16, 32, 0.88) 50%, rgba(18, 12, 36, 0.9) 100%);
  position: relative;
  overflow: hidden;
}

.home-page::before,
.home-page::after {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.home-page::before {
  background-image:
    linear-gradient(rgba(0, 242, 255, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 242, 255, 0.07) 1px, transparent 1px);
  background-size: 56px 56px;
  opacity: 0.35;
}

.home-page::after {
  background:
    repeating-linear-gradient(
      180deg,
      rgba(0, 242, 255, 0.035) 0px,
      rgba(0, 242, 255, 0.035) 2px,
      transparent 2px,
      transparent 6px
    ),
    radial-gradient(circle at 72% 18%, rgba(255, 0, 255, 0.2) 0 2px, transparent 3px),
    radial-gradient(circle at 68% 26%, rgba(0, 242, 255, 0.18) 0 2px, transparent 3px),
    radial-gradient(circle at 24% 88%, rgba(214, 0, 255, 0.16) 0 2px, transparent 3px);
  opacity: 0.55;
}

.card-surface {
  border-radius: 16px;
  border: 1px solid rgba(0, 242, 255, 0.42);
  background: linear-gradient(145deg, rgba(10, 18, 42, 0.82) 0%, rgba(8, 14, 32, 0.72) 100%);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  box-shadow:
    inset 0 1px 0 rgba(0, 242, 255, 0.12),
    inset 0 -1px 0 rgba(255, 0, 255, 0.08),
    0 20px 44px rgba(0, 0, 0, 0.55),
    0 0 32px rgba(0, 242, 255, 0.12),
    0 0 48px rgba(255, 0, 255, 0.06);
}

.hero,
.section-wrap {
  position: relative;
  z-index: 1;
}

.hero {
  overflow: hidden;
  border-radius: 30px;
  border: none;
  padding: 44px 32px;
  min-height: 360px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 64px;
  background: transparent;
}

.hero::before {
  content: none;
}

.hero::after {
  content: none;
}

.hero-content {
  width: min(100%, 860px);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding-top: 16px;
  position: relative;
  z-index: 2;
}

.hero-content::before {
  content: '';
  position: absolute;
  width: min(72vw, 620px);
  height: min(72vw, 620px);
  top: 40%;
  left: 52%;
  transform: translate(-50%, -50%);
  background:
    radial-gradient(circle, transparent 40%, rgba(0, 242, 255, 0.14) 41%, transparent 42%),
    radial-gradient(circle, transparent 36%, rgba(255, 0, 255, 0.12) 37.5%, transparent 39%),
    radial-gradient(circle, rgba(214, 0, 255, 0.18) 0%, rgba(0, 242, 255, 0.1) 28%, transparent 68%);
  filter: blur(14px);
  pointer-events: none;
  z-index: -1;
}

.hero-content::after {
  content: none;
}

h1,
h2 {
  font-size: 24px;
  margin: 0;
  color: var(--text-main);
}

h1 {
  font-size: 38px;
  font-weight: 800;
}

.hero-title {
  margin: 0 0 10px;
  font-size: clamp(29px, 5.0vw, 64px);
  line-height: 1.18;
  white-space: normal;
  font-weight: 800;
  letter-spacing: 0.04em;
  position: relative;
  padding: 0;
  z-index: 1;
  color: #ffffff;
  text-align: center;
  text-shadow:
    0 0 18px rgba(255, 255, 255, 0.5),
    0 0 42px rgba(255, 255, 255, 0.22),
    0 0 72px rgba(200, 235, 255, 0.14);
}

.hero p {
  margin: 4px 0 34px;
  max-width: 640px;
  color: var(--text-body);
  font-size: 17px;
  line-height: 1.75;
}

.hero-actions {
  display: flex;
  width: 100%;
  justify-content: center;
}

.btn {
  border: none;
  cursor: pointer;
  border-radius: 999px;
  font-size: 17px;
  padding: 12px 26px;
  transition: transform 0.22s ease, box-shadow 0.22s ease, filter 0.22s ease;
}

.btn-primary {
  background: none;
  color: #ffffff;
}

.hero-cta {
  min-width: 510px;
  min-height: 90px;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 14px 20px 14px 40px;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 0.03em;
  margin-top: 24px;
  border: 1px solid rgba(0, 242, 255, 0.65);
  overflow: hidden;
  background: linear-gradient(
    108deg,
    rgba(0, 242, 255, 0.35) 0%,
    rgba(20, 40, 90, 0.75) 38%,
    rgba(214, 0, 255, 0.45) 72%,
    rgba(255, 0, 255, 0.38) 100%
  );
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.5),
    0 0 32px rgba(0, 242, 255, 0.35),
    0 0 48px rgba(255, 0, 255, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.22);
  position: relative;
  isolation: isolate;
}

.hero-cta::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background:
    linear-gradient(120deg, transparent 14%, rgba(235, 249, 255, 0.44) 42%, transparent 58%),
    linear-gradient(180deg, rgba(132, 212, 255, 0.18), transparent 70%);
  z-index: 0;
  animation: ctaFlow 3.6s linear infinite;
}

.hero-cta::after {
  content: '';
  position: absolute;
  inset: -10px;
  border-radius: inherit;
  background:
    radial-gradient(ellipse at 30% 40%, rgba(0, 242, 255, 0.35) 0%, transparent 55%),
    radial-gradient(ellipse at 70% 60%, rgba(255, 0, 255, 0.28) 0%, transparent 55%);
  filter: blur(12px);
  z-index: -1;
  pointer-events: none;
}

.hero-cta-text {
  color: #ffffff;
  font-size: 1.14em;
  text-shadow:
    0 0 12px rgba(0, 242, 255, 0.85),
    0 0 24px rgba(255, 0, 255, 0.35);
  position: relative;
  z-index: 2;
  flex: 1;
  text-align: center;
  transform: translateX(20px);
}

.hero-cta-arrow {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0;
  color: transparent;
  background: linear-gradient(145deg, #ffffff, #c8fbff);
  box-shadow:
    0 0 20px rgba(0, 242, 255, 0.75),
    0 0 36px rgba(255, 0, 255, 0.35);
  position: relative;
  z-index: 2;
}

.hero-cta-arrow::before {
  content: '';
  position: absolute;
  left: 55%;
  top: 50%;
  width: 46px;
  height: 46px;
  transform: translate(-50%, -50%);
  background: center / 100% 100% no-repeat
    url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 64 64'%3E%3Cpath d='M10 32H44M32 20L44 32L32 44' fill='none' stroke='%230f4ca6' stroke-width='8' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
}

.hero-cta:hover {
  transform: translateY(-2px) scale(1.015);
  box-shadow:
    0 16px 32px rgba(0, 0, 0, 0.55),
    0 0 42px rgba(0, 242, 255, 0.55),
    0 0 72px rgba(255, 0, 255, 0.32);
  filter: saturate(1.12) brightness(1.04);
}

.hero-cta:active {
  transform: translateY(0) scale(0.99);
}

.section-wrap {
  margin-top: 30px;
  margin-bottom: 24px;
}

.section-wrap h2 {
  font-size: 30px;
  margin-top: -15px;
  margin-bottom: 30px;
  text-align: left;
  font-weight: 800;
  background: linear-gradient(90deg, #00f2ff 0%, #ffffff 42%, #ff00ff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: none;
  filter: drop-shadow(0 0 14px rgba(0, 242, 255, 0.35)) drop-shadow(0 0 20px rgba(255, 0, 255, 0.2));
}

.core-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 34px;
  width: 100%;
  margin: 0 auto;
}

.core-card {
  position: relative;
  overflow: hidden;
  border-radius: 28px;
  padding: 22px 24px;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 440px;
  background: linear-gradient(155deg, rgba(10, 18, 42, 0.88) 0%, rgba(14, 12, 36, 0.82) 100%);
  border: 1px solid rgba(0, 242, 255, 0.38);
  box-shadow:
    inset 0 0 26px rgba(0, 242, 255, 0.1),
    inset 0 -20px 40px rgba(255, 0, 255, 0.05),
    0 18px 36px rgba(0, 0, 0, 0.52),
    0 0 28px rgba(0, 242, 255, 0.1),
    0 0 40px rgba(255, 0, 255, 0.06);
}

.core-card::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
  background:
    linear-gradient(120deg, transparent 10%, rgba(0, 242, 255, 0.07) 44%, transparent 62%),
    radial-gradient(circle at 12% 10%, rgba(0, 242, 255, 0.14), transparent 38%),
    radial-gradient(circle at 92% 88%, rgba(255, 0, 255, 0.1), transparent 40%),
    linear-gradient(90deg, #00f2ff, #00f2ff) 14px 14px / 20px 2px no-repeat,
    linear-gradient(180deg, #00f2ff, #00f2ff) 14px 14px / 2px 20px no-repeat,
    linear-gradient(270deg, #ff00ff, #ff00ff) calc(100% - 14px) 14px / 20px 2px no-repeat,
    linear-gradient(180deg, #ff00ff, #ff00ff) calc(100% - 16px) 14px / 2px 20px no-repeat,
    linear-gradient(90deg, #ff00ff, #ff00ff) calc(100% - 34px) calc(100% - 16px) / 20px 2px no-repeat,
    linear-gradient(0deg, #ff00ff, #ff00ff) calc(100% - 16px) calc(100% - 34px) / 2px 20px no-repeat,
    linear-gradient(270deg, #00f2ff, #00f2ff) 14px calc(100% - 16px) / 20px 2px no-repeat,
    linear-gradient(0deg, #00f2ff, #00f2ff) 14px calc(100% - 34px) / 2px 20px no-repeat;
}

.core-card:hover {
  transform: translateY(-5px);
  border-color: rgba(0, 242, 255, 0.62);
  box-shadow:
    inset 0 0 30px rgba(0, 242, 255, 0.14),
    0 22px 40px rgba(0, 0, 0, 0.58),
    0 0 36px rgba(0, 242, 255, 0.22),
    0 0 52px rgba(255, 0, 255, 0.14);
}

.core-head {
  display: grid;
  grid-template-columns: 56px 1fr 56px;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 2;
}

.core-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: radial-gradient(circle at 30% 20%, #5dd9ff, #1965d8);
  border: 1px solid rgba(184, 236, 255, 0.52);
  font-size: 30px;
  color: #fff;
  text-shadow: 0 0 8px rgba(170, 235, 255, 0.56);
  animation: float 2.8s ease-in-out infinite;
}

.core-card h3 {
  margin: 0;
  font-size: 30px;
  transform: translateY(-4px);
  text-align: center;
  background: linear-gradient(95deg, #e8ffff 0%, #00f2ff 45%, #ff9bff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  filter: drop-shadow(0 0 10px rgba(0, 242, 255, 0.25));
}

.core-level-tabs {
  display: flex;
  justify-content: center;
  gap: 10px;
  position: relative;
  z-index: 2;
}

.core-level-btn {
  border: 1px solid rgba(0, 242, 255, 0.4);
  background: rgba(8, 20, 48, 0.65);
  color: #b8f8ff;
  border-radius: 999px;
  padding: 6px 18px;
  font-size: 13px;
  line-height: 1.2;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.core-level-btn:hover,
.core-level-btn.active {
  background: rgba(214, 0, 255, 0.22);
  color: #ffffff;
  border-color: rgba(255, 0, 255, 0.75);
  box-shadow: 0 0 14px rgba(255, 0, 255, 0.35), 0 0 22px rgba(0, 242, 255, 0.2);
}

.core-mini-box {
  width: 440px;
  min-height: 250px;
  max-width: 100%;
  margin: 8px auto 0;
  border-radius: 20px;
  position: relative;
  z-index: 2;
}

.core-mini-box-detailed {
  display: flex;
  flex-direction: column;
  min-height: 250px;
  padding: 16px 18px;
  text-align: left;
  color: var(--text-body);
  background: rgba(6, 14, 36, 0.72);
  border: 1px solid rgba(0, 242, 255, 0.32);
  border-radius: 20px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow:
    inset 0 0 22px rgba(0, 242, 255, 0.08),
    inset 0 -12px 24px rgba(255, 0, 255, 0.04),
    0 12px 28px rgba(0, 0, 0, 0.45),
    0 0 20px rgba(0, 242, 255, 0.08);
}

.core-mini-box-detailed .job-mini-title {
  margin: 0 0 22px;
  font-size: 18px;
  line-height: 1.3;
  font-weight: 800;
  color: #ffffff;
  text-align: center;
  text-shadow:
    0 0 12px rgba(0, 242, 255, 0.45),
    0 0 22px rgba(255, 0, 255, 0.2);
}

.core-mini-box-detailed .job-section {
  margin-top: 8px;
}

.core-mini-box-detailed .job-section:first-of-type {
  margin-top: 0;
}

.core-mini-box-detailed .job-section strong {
  display: block;
  font-size: 14px;
  color: #00f2ff;
  margin-bottom: 4px;
  text-shadow: 0 0 10px rgba(0, 242, 255, 0.35);
}

.core-mini-box-detailed .job-section ul {
  margin: 0;
  padding-left: 18px;
}

.core-mini-box-detailed .job-section li {
  margin: 0 0 4px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-body);
}

.core-mini-box-detailed .job-detail-action {
  margin-top: auto;
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 12px;
}

.core-mini-box-detailed .job-detail-btn,
.core-mini-box-detailed .job-company-btn {
  border: 1px solid rgba(0, 242, 255, 0.55);
  background: linear-gradient(105deg, rgba(0, 242, 255, 0.35) 0%, rgba(30, 20, 70, 0.85) 48%, rgba(214, 0, 255, 0.4) 100%);
  color: #ffffff;
  border-radius: 999px;
  padding: 10px 22px;
  font-size: 15px;
  font-weight: 700;
  line-height: 1;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease;
  box-shadow: 0 0 16px rgba(0, 242, 255, 0.2), 0 0 28px rgba(255, 0, 255, 0.12);
}

.core-mini-box-detailed .job-detail-btn:hover,
.core-mini-box-detailed .job-company-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 0 22px rgba(0, 242, 255, 0.4), 0 0 36px rgba(255, 0, 255, 0.22);
  filter: brightness(1.06);
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

@keyframes heroSweep {
  0% {
    transform: translateX(-8%) translateY(-2%) rotate(0deg);
  }
  50% {
    transform: translateX(8%) translateY(2%) rotate(1deg);
  }
  100% {
    transform: translateX(-8%) translateY(-2%) rotate(0deg);
  }
}

@keyframes ctaFlow {
  0% {
    transform: translateX(-24%);
  }
  100% {
    transform: translateX(24%);
  }
}

@media (max-width: 1200px) {
  .core-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .home-page {
    --page-padding: 14px;
    --nav-block-height: 132px;
    padding: calc(var(--nav-block-height) + 28px) var(--page-padding) var(--page-padding);
  }

  .hero {
    flex-direction: column;
    align-items: center;
    padding: 32px 18px;
    min-height: 0;
  }

  .hero-content {
    width: 100%;
    padding-top: 10px;
  }

  .hero-title {
    width: 100%;
    white-space: normal;
    text-align: center;
  }

  .hero-actions {
    width: 100%;
  }

  .hero-cta {
    min-width: min(100%, 430px);
    width: 100%;
    max-width: 460px;
    font-size: 28px;
    min-height: 84px;
    padding: 14px 16px 14px 22px;
    gap: 14px;
  }

  .hero-cta-text {
    font-size: 1.12em;
    transform: translateX(8px);
  }

  .hero-cta-arrow {
    width: 58px;
    height: 58px;
  }

  .hero-cta-arrow::before {
    width: 38px;
    height: 38px;
  }
}

@media (max-width: 560px) {
  .core-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .core-card {
    min-height: 0;
    border-radius: 22px;
  }
}
</style>