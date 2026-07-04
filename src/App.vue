<template>
  <RouterView />
</template>

<style>
html,
body,
#app {
  min-height: 100%;
  margin: 0;
  background: #04070f;
}

:root {
  --cyber-bg-0: #03070f;
  --cyber-bg-1: #081426;
  --cyber-bg-2: #101032;
  --cyber-cyan: #00f2ff;
  --cyber-magenta: #ff00ff;
  --cyber-text-main: #e8f8ff;
  --cyber-text-sub: #b8e9ff;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) {
  min-height: 100vh;
  color: var(--cyber-text-main) !important;
  background:
    radial-gradient(ellipse 120% 80% at 18% 40%, rgba(0, 242, 255, 0.14) 0%, transparent 55%),
    radial-gradient(ellipse 100% 90% at 88% 62%, rgba(255, 0, 255, 0.12) 0%, transparent 52%),
    radial-gradient(circle at 50% 120%, rgba(214, 0, 255, 0.1) 0%, transparent 45%),
    linear-gradient(150deg, rgba(9, 16, 38, 0.96) 0%, rgba(11, 16, 32, 0.9) 50%, rgba(18, 12, 36, 0.92) 100%) !important;
  position: relative;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page)::before,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page)::after {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page)::before {
  background-image:
    linear-gradient(rgba(0, 242, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 0, 255, 0.06) 1px, transparent 1px) !important;
  background-size: 44px 44px !important;
  opacity: 0.36 !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page)::after {
  background:
    linear-gradient(115deg, transparent 30%, rgba(0, 242, 255, 0.16) 46%, transparent 62%),
    linear-gradient(295deg, transparent 36%, rgba(255, 0, 255, 0.1) 50%, transparent 64%),
    radial-gradient(circle at 28% 30%, rgba(0, 242, 255, 0.14) 0 1.4px, transparent 2.6px),
    radial-gradient(circle at 72% 64%, rgba(255, 0, 255, 0.12) 0 1.2px, transparent 2.4px);
  background-size: auto, auto, 140px 140px, 120px 120px;
  opacity: 0.44 !important;
}

/* 全站导航栏统一为首页样式 */
:is(.home-page, .planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .top-nav {
  position: fixed !important;
  top: 0 !important;
  left: var(--page-padding, 24px) !important;
  right: var(--page-padding, 24px) !important;
  z-index: 20 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: space-between !important;
  padding: 10px 26px !important;
  min-height: 64px !important;
  margin-bottom: 0 !important;
  color: #fff !important;
  border-radius: 16px !important;
  border: 1px solid rgba(0, 242, 255, 0.48) !important;
  background: linear-gradient(145deg, rgba(10, 18, 42, 0.9) 0%, rgba(14, 10, 38, 0.82) 100%) !important;
  backdrop-filter: blur(16px) !important;
  -webkit-backdrop-filter: blur(16px) !important;
  box-shadow:
    inset 0 1px 0 rgba(0, 242, 255, 0.14),
    inset 0 -1px 0 rgba(255, 0, 255, 0.1),
    0 20px 40px rgba(0, 0, 0, 0.52),
    0 0 34px rgba(0, 242, 255, 0.16),
    0 0 50px rgba(255, 0, 255, 0.09) !important;
}

:is(.home-page, .planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .top-nav::before {
  content: '' !important;
  position: absolute !important;
  inset: 0 !important;
  border-radius: inherit !important;
  pointer-events: none !important;
  background: linear-gradient(95deg, rgba(0, 242, 255, 0.24), rgba(255, 0, 255, 0.18)) !important;
  mix-blend-mode: screen !important;
}

:is(.home-page, .planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .brand {
  display: flex !important;
  align-items: center !important;
  gap: 10px !important;
  font-size: 32px !important;
  font-weight: 800 !important;
  letter-spacing: 1px !important;
  color: #f2fdff !important;
  text-shadow:
    0 0 12px rgba(0, 242, 255, 0.48),
    0 0 28px rgba(255, 0, 255, 0.26) !important;
}

:is(.home-page, .planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .brand-logo {
  width: 68px !important;
  height: 68px !important;
  margin-left: 16px !important;
  transform: translateX(3px) scale(1.08) !important;
  transform-origin: center center !important;
  object-fit: contain !important;
  filter: drop-shadow(0 0 12px rgba(0, 242, 255, 0.5)) drop-shadow(0 0 20px rgba(255, 0, 255, 0.28)) !important;
}

:is(.home-page, .planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .nav-list {
  display: flex !important;
  gap: 8px !important;
  flex-wrap: wrap !important;
}

:is(.home-page, .planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .nav-item {
  border: 1px solid rgba(0, 242, 255, 0.28) !important;
  border-radius: 999px !important;
  background: rgba(6, 18, 48, 0.58) !important;
  color: #b8f8ff !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  line-height: 1 !important;
  padding: 11px 16px !important;
  cursor: pointer !important;
  transition: all 0.22s ease !important;
}

:is(.home-page, .planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .nav-item:hover,
:is(.home-page, .planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .nav-item.active {
  color: #ffffff !important;
  border-color: rgba(255, 0, 255, 0.62) !important;
  background: rgba(214, 0, 255, 0.22) !important;
  box-shadow:
    0 0 18px rgba(255, 0, 255, 0.38),
    0 0 30px rgba(0, 242, 255, 0.22) !important;
}

/* 板块统一为首页“岗位预览卡片”质感 */
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .card-surface,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .panel,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .module,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .summary-card,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .plan-card,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .resource-card,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .skill-card,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .ability-card,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .cert-card,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .salary-panel,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .growth-card,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .list-item,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .state-box,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .hint-box,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .resume-form-wrap,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .resume-upload-wrap,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .box {
  border-radius: 20px !important;
  border: 1px solid rgba(0, 242, 255, 0.32) !important;
  background: linear-gradient(155deg, rgba(10, 18, 42, 0.88) 0%, rgba(14, 12, 36, 0.82) 100%) !important;
  box-shadow:
    inset 0 0 22px rgba(0, 242, 255, 0.1),
    inset 0 -16px 30px rgba(255, 0, 255, 0.05),
    0 16px 34px rgba(0, 0, 0, 0.52),
    0 0 24px rgba(0, 242, 255, 0.1),
    0 0 36px rgba(255, 0, 255, 0.06) !important;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

:is(.planning-page, .data-center-page, .admin-page) .side-item,
:is(.planning-page, .data-center-page, .admin-page) .tab-item {
  color: #d8f5ff !important;
  text-shadow: 0 0 8px rgba(0, 242, 255, 0.16) !important;
  font-weight: 700 !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .side-item:hover,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .side-item.active,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .tab-item:hover,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .tab-item.active {
  color: #ffffff !important;
  background: rgba(214, 0, 255, 0.22) !important;
  border-color: rgba(255, 0, 255, 0.74) !important;
  box-shadow: 0 0 12px rgba(255, 0, 255, 0.3), 0 0 18px rgba(0, 242, 255, 0.22) !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) h1,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) h2,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) h3,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) h4,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) label,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) td,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) th {
  color: var(--cyber-text-main) !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .state-text,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .plain-text,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .subtitle {
  color: var(--cyber-text-sub) !important;
}

/* 普通按钮统一，明确排除首页开始规划按钮；排除报告页「详细模块」链接按钮（由页面内与首页 CTA 一致的渐变单独控制） */
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .btn:not(.detail-link-btn),
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .small-btn,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .page-btn,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .unified-btn {
  border: 1px solid rgba(0, 242, 255, 0.55) !important;
  color: #ffffff !important;
  background: linear-gradient(105deg, rgba(0, 242, 255, 0.35) 0%, rgba(30, 20, 70, 0.85) 48%, rgba(214, 0, 255, 0.4) 100%) !important;
  box-shadow: 0 0 16px rgba(0, 242, 255, 0.2), 0 0 28px rgba(255, 0, 255, 0.12) !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .btn:not(.detail-link-btn):hover,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .small-btn:hover,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .page-btn:hover,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .unified-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 0 22px rgba(0, 242, 255, 0.4), 0 0 36px rgba(255, 0, 255, 0.22) !important;
  filter: brightness(1.06);
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) input,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) select,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) textarea {
  color: #def6ff !important;
  border: 1px solid rgba(107, 210, 255, 0.4) !important;
  background: rgba(8, 27, 48, 0.58) !important;
  box-shadow: inset 0 0 14px rgba(56, 166, 255, 0.14) !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) ::placeholder {
  color: rgba(164, 224, 255, 0.66) !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) table,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .resume-table,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .job-table {
  background: rgba(6, 14, 36, 0.68) !important;
  border-color: rgba(86, 188, 255, 0.34) !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) th,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .resume-table th {
  background: rgba(24, 64, 101, 0.62) !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .tag,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .chip {
  color: #eafcff !important;
  background: rgba(31, 128, 186, 0.48) !important;
  border: 1px solid rgba(126, 229, 255, 0.4) !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .progress,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .score-track,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .bar-track,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .salary-bar {
  background: rgba(24, 64, 101, 0.52) !important;
}

:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .progress-bar,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .score-track i,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .bar-track i,
:is(.planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .salary-bar em {
  background: linear-gradient(90deg, #00f2ff, #3f8eff, #d600ff) !important;
}

:is(.home-page, .planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .brand span {
  display: inline-block !important;
  line-height: 1 !important;
  transform: scale(1.12) !important;
  transform-origin: left center !important;
  color: #f2fdff !important;
  text-shadow:
    0 0 12px rgba(0, 242, 255, 0.45),
    0 0 24px rgba(255, 0, 255, 0.22) !important;
}

@media (max-width: 860px) {
  :is(.home-page, .planning-page, .data-center-page, .report-page, .archive-page, .placeholder-page, .admin-page) .top-nav {
    align-items: flex-start !important;
    flex-direction: column !important;
    gap: 10px !important;
  }
}
</style>
