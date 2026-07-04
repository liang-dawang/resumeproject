<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const activeTab = ref('login')
const submitting = ref(false)

const panelTitle = '大学生职业规划平台'
const panelSubtitle = '贯通能力评估、岗位匹配、路径设计与报告生成全程'

const submitText = computed(() => {
  if (submitting.value) {
    return activeTab.value === 'login' ? '登录中...' : '注册中...'
  }
  return activeTab.value === 'login' ? '登录' : '注册'
})

const showConfirmPassword = computed(() => activeTab.value === 'register')

const AUTH_API_BASE = (import.meta.env.VITE_AUTH_API_BASE || 'http://localhost:8080/api').replace(/\/$/, '')

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')
const topToast = ref('')
const topToastType = ref('error')
let _topTimer = null

const showTopToast = (msg, type = 'error', timeout = 3500) => {
  topToast.value = msg
  topToastType.value = type
  if (_topTimer) clearTimeout(_topTimer)
  if (timeout > 0) {
    _topTimer = setTimeout(() => {
      topToast.value = ''
      _topTimer = null
    }, timeout)
  }
}

const switchTab = (tab) => {
  topToast.value = ''
  username.value = ''
  password.value = ''
  confirmPassword.value = ''
  activeTab.value = tab
}

const goToLoginView = () => {
  username.value = ''
  password.value = ''
  confirmPassword.value = ''
  activeTab.value = 'login'
}

const registerUser = async () => {
  error.value = ''
  const u = username.value?.trim()
  const p = password.value
  const cp = confirmPassword.value
  if (!u || !p) {
    showTopToast('用户名与密码不能为空', 'error')
    return false
  }
  if (p !== cp) {
    showTopToast('两次输入的密码不一致', 'error')
    return false
  }

  submitting.value = true
  try {
    const { data } = await axios.post(`${AUTH_API_BASE}/login/register`, {
      username: u,
      password: p
    })

    if (Number(data?.code) !== 200) {
      showTopToast(data?.msg || data?.message || '注册失败，请稍后重试', 'error')
      return false
    }

    showTopToast('注册成功，请使用刚才的账号登录', 'success')
    goToLoginView()
    return true
  } catch (e) {
    console.error('注册失败:', e)
    showTopToast(e?.response?.data?.msg || '注册失败，请检查后端服务', 'error')
    return false
  } finally {
    submitting.value = false
  }
}

const loginUser = async () => {
  error.value = ''
  const u = username.value?.trim()
  const p = password.value
  if (!u || !p) {
    showTopToast('用户名与密码不能为空', 'error')
    return false
  }

  submitting.value = true
  try {
    const { data } = await axios.post(`${AUTH_API_BASE}/login`, {
      username: u,
      password: p
    })

    if (Number(data?.code) !== 200) {
      showTopToast(data?.msg || data?.message || '登录失败，请检查账号密码', 'error')
      return false
    }

    const payload = data?.data || {}
    const token = payload?.token || data?.token || ''
    if (!token) {
      showTopToast('登录失败：后端未返回 token', 'error')
      return false
    }

    // ======================
    // 🔥 这里改成你要的规则
    // ======================
    const roleValue = payload?.role // 后端返回 0 或 1
    let role = 'user'
    if (roleValue === 1) {
      role = 'admin' // 1 = 管理员
    } else {
      role = 'user'  // 0 = 普通用户
    }

    const usernameValue = payload?.username || u

    try {
      localStorage.setItem('authToken', token)
      localStorage.setItem('isAuthed', '1')
      localStorage.setItem('currentUser', usernameValue)
      localStorage.setItem('userRole', role)
    } catch (e) {
      console.warn('localStorage set failed', e)
    }

    // 跳转判断
    if (role === 'admin') {
      router.push('/admin')
    } else {
      router.push('/')
    }
    return true
  } catch (e) {
    console.error('登录失败:', e)
    showTopToast(e?.response?.data?.msg || '登录失败，请检查后端服务', 'error')
    return false
  } finally {
    submitting.value = false
  }
}

const onSubmit = async () => {
  if (submitting.value) return
  if (activeTab.value === 'register') {
    await registerUser()
    return
  }
  await loginUser()
}
</script>

<template>
  <div class="login-page">
    <div class="login-mask"></div>

    <!-- page-level toast placed above panel so it doesn't affect panel layout -->
    <div v-if="topToast" :class="['page-toast', topToastType]">{{ topToast }}</div>
    <section class="auth-panel">
      <h1 class="panel-title">{{ panelTitle }}</h1>
      <p class="panel-subtitle">{{ panelSubtitle }}</p>

      <div class="tab-row">
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeTab === 'login' }"
          @click="switchTab('login')"
        >
          登录
        </button>
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeTab === 'register' }"
          @click="switchTab('register')"
        >
          注册
        </button>
      </div>

        <form class="auth-form" @submit.prevent="onSubmit">
          <div class="form-inputs">
            <label class="field-label" for="username">
              <span class="required">*</span>
              用户名
            </label>
            <input id="username" v-model="username" type="text" placeholder="请输入用户名" />

            <label class="field-label" for="password">
              <span class="required">*</span>
              密码
            </label>
            <el-input
              id="password"
              v-model="password"
              type="password"
              show-password
              placeholder="请输入密码"
              class="password-input"
            />

            <div class="confirm-wrap" :class="{ visible: showConfirmPassword }">
              <label class="field-label" for="confirm-password">确认密码</label>
              <el-input
                id="confirm-password"
                v-model="confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入密码"
                class="password-input"
              />
            </div>
          </div>
            <div class="form-controls">
              <p class="forgot-line">忘记密码?</p>

              <!-- inline error removed — using page-top toast instead -->

              <button type="submit" class="primary-btn" :disabled="submitting">{{ submitText }}</button>

              <p class="third-title">第三方登录</p>
              <div class="third-row">
                <button type="button" class="third-btn">微信</button>
                <button type="button" class="third-btn">QQ</button>
                <button type="button" class="third-btn">邮箱</button>
              </div>

              <p class="signup-tip">首次使用请先注册账号</p>
            </div>
        </form>
    </section>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 18px clamp(20px, 4vw, 48px);
  box-sizing: border-box;
  overflow: hidden;
  background: transparent;
}

.login-page::before {
  content: '';
  position: absolute;
  inset: -4%;
  background-image: url('/login.jpg');
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  transform: scale(0.94);
  transform-origin: center center;
  filter: none;
  z-index: 0;
}

.login-page::after {
  content: '';
  position: absolute;
  inset: 0;
  /* lighten overlay so original image color shows through */
  background:
    linear-gradient(96deg, rgba(3, 9, 23, 0.12) 0%, rgba(7, 14, 34, 0.08) 46%, rgba(7, 13, 31, 0.14) 100%),
    radial-gradient(circle at 70% 32%, rgba(8, 40, 78, 0.08) 0%, transparent 44%);
  z-index: 1;
}

.login-mask {
  display: none;
  pointer-events: none;
}

.auth-panel {
  position: relative;
  z-index: 2;
  width: min(384px, 100%);
  min-height: 480px;
  padding: 16px 16px 14px;
  display: flex;
  flex-direction: column;
  border-radius: 10px;
  background: linear-gradient(160deg, rgba(7, 14, 33, 0.92) 0%, rgba(10, 18, 42, 0.9) 100%);
  border: 1px solid rgba(96, 177, 255, 0.35);
  box-shadow:
    0 24px 44px rgba(0, 0, 0, 0.45),
    0 0 40px rgba(13, 114, 217, 0.14);
  color: #dfefff;
  backdrop-filter: blur(3px);
}

.panel-title {
  margin: 0;
  font-size: clamp(24px, 2.4vw, 38px);
  line-height: 1.05;
  font-weight: 900;
  color: #eef7ff;
  letter-spacing: 1px;
  text-align: center;
}

.panel-subtitle {
  margin: 6px 0 10px;
  font-size: 13px;
  color: #9ab8d8;
  min-height: 16px;
  visibility: hidden;
}

.tab-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  border-bottom: 1px solid rgba(120, 170, 224, 0.35);
  margin-bottom: 12px;
}

.tab-btn {
  border: 0;
  background: transparent;
  color: #9eb8d5;
  font-size: 20px;
  line-height: 1.1;
  padding: 6px 0 10px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.tab-btn.active {
  color: #6db3ff;
  border-bottom: 3px solid #4f9dff;
  margin-bottom: -2px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
}

.form-inputs {
  display: flex;
  flex-direction: column;
}

.form-controls {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  margin: 6px 0 4px;
  font-size: 16px;
  line-height: 1.15;
  color: #d9e6f6;
}

.required {
  color: #ff6a5b;
  margin-right: 4px;
}

input {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid rgba(109, 150, 198, 0.58);
  border-radius: 4px;
  background: rgba(15, 28, 52, 0.88);
  color: #ebf5ff;
  font-size: 14px;
  line-height: 1.25;
  padding: 10px 12px;
  margin-bottom: 8px;
  outline: none;
}

input::placeholder {
  color: #869bb5;
}

input:focus {
  border-color: rgba(103, 171, 249, 0.9);
  box-shadow: 0 0 0 2px rgba(67, 132, 222, 0.18);
}

.password-input {
  margin-bottom: 8px;
}

.password-input :deep(.el-input__wrapper) {
  border-radius: 4px;
  background: rgba(15, 28, 52, 0.88);
  box-shadow: 0 0 0 1px rgba(109, 150, 198, 0.58) inset;
  padding: 1px 12px;
  --el-input-icon-color: #bcd3f0;
  --el-input-clear-hover-color: #eef7ff;
}

.password-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px rgba(103, 171, 249, 0.9) inset, 0 0 0 2px rgba(67, 132, 222, 0.18);
}

.password-input :deep(.el-input__inner) {
  color: #ebf5ff;
  font-size: 14px;
  line-height: 1.25;
  height: 40px;
}

.password-input :deep(.el-input__suffix-inner) {
  min-width: 24px;
  color: #a7b9cf;
}

.password-input :deep(.el-input__password) {
  color: #bcd3f0;
  font-size: 16px;
}

.password-input :deep(.el-input__password:hover) {
  color: #eef7ff;
}

/* keep dark theme when browser auto-fills username/password */
input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus,
input:-webkit-autofill:active {
  -webkit-text-fill-color: #ebf5ff;
  caret-color: #ebf5ff;
  border: 1px solid rgba(109, 150, 198, 0.58);
  -webkit-box-shadow: 0 0 0 1000px rgba(15, 28, 52, 0.88) inset;
  box-shadow: 0 0 0 1000px rgba(15, 28, 52, 0.88) inset;
  transition: background-color 9999s ease-out 0s;
}

.forgot-line {
  margin: 0 2px 6px auto;
  font-size: 12px;
  color: #8fa4c0;
}

.error-line {
  color: #ffb3b3;
  background: rgba(255, 80, 80, 0.08);
  border: 1px solid rgba(255, 80, 80, 0.14);
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 8px;
  font-size: 13px;
}

.primary-btn {
  border: 0;
  border-radius: 4px;
  background: linear-gradient(90deg, #2c8ed7 0%, #3f7dc5 100%);
  color: #f1f8ff;
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
  padding: 14px 0;
  margin-bottom: 10px;
  cursor: pointer;
  transition: filter 0.2s ease;
}

.primary-btn:hover {
  filter: brightness(1.08);
}

.primary-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
  filter: none;
}

.third-title {
  margin: 0 0 8px;
  font-size: 13px;
  color: #a6bdd8;
}

.third-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.third-btn {
  border: 1px solid rgba(122, 158, 196, 0.5);
  border-radius: 4px;
  background: rgba(10, 24, 46, 0.9);
  color: #c8d9ee;
  font-size: 14px;
  line-height: 1;
  padding: 10px 0;
  cursor: pointer;
}

.signup-tip {
  margin: 6px 0 0;
  color: #8fa4c0;
  font-size: 12px;
}

.confirm-wrap {
  min-height: 74px; /* reserve vertical space so panel doesn't jump */
  overflow: hidden;
  opacity: 0;
  transform: translateY(-4px);
  transition: opacity 0.18s ease, transform 0.18s ease;
  pointer-events: none;
}

.confirm-wrap.visible {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

@media (max-width: 1200px) {
  .auth-panel {
    width: min(384px, 100%);
  }

  .panel-title {
    font-size: clamp(22px, 4vw, 34px);
  }

  .panel-subtitle {
    font-size: 12px;
  }

  .tab-btn {
    font-size: clamp(18px, 4vw, 20px);
  }

  .field-label {
    font-size: clamp(14px, 3vw, 16px);
  }

  input {
    font-size: clamp(13px, 2.8vw, 14px);
  }

  .primary-btn {
    font-size: clamp(18px, 4vw, 20px);
  }

  .third-title,
  .third-btn,
  .forgot-line,
  .signup-tip {
    font-size: clamp(12px, 2.8vw, 14px);
  }
}

@media (max-width: 768px) {
  .login-page {
    justify-content: center;
    padding: 12px;
  }

  .login-page::before {
    transform: scale(0.92);
  }

  .auth-panel {
    width: 100%;
    max-width: 384px;
    min-height: 560px;
    padding: 18px 16px 14px;
  }
}

/* page-top toast */
.page-toast {
  position: fixed;
  top: 18px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  min-width: 280px;
  max-width: calc(100% - 48px);
  padding: 12px 16px;
  border-radius: 8px;
  color: #fff;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,0.45);
  font-size: 14px;
}
.page-toast.success { background: linear-gradient(90deg, #30c88b, #2bb27a); }
.page-toast.error { background: linear-gradient(90deg, #ff6b6b, #ff4b4b); }
</style>