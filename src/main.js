import { createApp } from 'vue'
import axios from 'axios'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

axios.interceptors.request.use((config) => {
  const url = String(config.url || '')
  const isAuthEndpoint = /\/login$|\/register$/i.test(url)
  if (isAuthEndpoint) {
    return config
  }

  const token = localStorage.getItem('authToken')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

createApp(App).use(router).use(ElementPlus).mount('#app')
