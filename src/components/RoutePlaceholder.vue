<script setup>
import { useRoute, useRouter } from 'vue-router'

defineProps({
  title: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const route = useRoute()

const navItems = [
  { label: '首页', path: '/' },
  { label: '职业规划', path: '/career-planning' },
  { label: '数据中心', path: '/data-center' },
  { label: '报告管理', path: '/report-management' },
  { label: '个人中心', path: '/student-profile' },
  
]

const go = (path) => {
  if (route.path !== path) {
    router.push(path)
  }
}
</script>

<template>
  <section class="placeholder-page">
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

    <div class="box card-surface">
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
    </div>
  </section>
</template>

<style scoped>
.placeholder-page {
  --page-padding: 24px;
  --nav-block-height: 92px;
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: calc(var(--nav-block-height) + 20px) var(--page-padding) var(--page-padding);
}

.box {
  width: min(720px, 100%);
  padding: 28px;
}

h1 {
  margin: 0 0 12px;
  font-size: 24px;
}

p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
}

@media (max-width: 860px) {
  .placeholder-page {
    --page-padding: 14px;
    --nav-block-height: 132px;
  }
}
</style>