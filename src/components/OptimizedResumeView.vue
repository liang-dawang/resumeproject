<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const optimizedResume = computed(() => {
  try {
    const raw = localStorage.getItem('optimizedResume')
    const data = raw ? JSON.parse(raw) : {}
    return {
      name: data?.name || '',
      education: data?.education || '',
      major: data?.major || '',
      desiredJob: data?.desiredJob || '',
      coreSkill: data?.coreSkill || '',
      certificate: data?.certificate || '',
      desiredSalary: data?.desiredSalary || '',
      innovation: data?.innovation || '',
      learning: data?.learning || '',
      pressureResistance: data?.pressureResistance || '',
      communication: data?.communication || '',
      internshipProject: data?.internshipProject || '',
      internshipDuration: data?.internshipDuration || '',
      projectAchievement: data?.projectAchievement || ''
    }
  } catch {
    return {
      name: '',
      education: '',
      major: '',
      desiredJob: '',
      coreSkill: '',
      certificate: '',
      desiredSalary: '',
      innovation: '',
      learning: '',
      pressureResistance: '',
      communication: '',
      internshipProject: '',
      internshipDuration: '',
      projectAchievement: ''
    }
  }
})

const hasData = computed(() =>
  Object.values(optimizedResume.value).some((item) => String(item || '').trim())
)

const backToReport = () => router.push('/report-management')

const exportOptimizedResume = () => {
  if (!hasData.value) {
    window.alert('暂无可导出的优化简历数据。')
    return
  }

  const data = optimizedResume.value
  const now = new Date()
  const ts = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`
  const safeName = (data.name || '优化简历').replace(/[\\/:*?"<>|]/g, '_')
  const fileName = `${safeName}_${ts}.json`

  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
</script>

<template>
  <section class="resume-form-wrap">
    <h2 class="title">优化后简历信息展示</h2>
    <p class="subtitle">页面格式与简历信息录入表一致（只读展示）</p>

    <div v-if="!hasData" class="empty-tip">
      暂无优化后的简历数据，请先在报告管理页执行“简历优化”。
    </div>

    <form v-else class="resume-form" @submit.prevent>
      <table class="resume-table" cellspacing="0" cellpadding="0">
        <tbody>
          <tr>
            <th>姓名</th>
            <td><input :value="optimizedResume.name" type="text" readonly /></td>
            <th>学历</th>
            <td><input :value="optimizedResume.education" type="text" readonly /></td>
          </tr>

          <tr>
            <th>专业</th>
            <td><input :value="optimizedResume.major" type="text" readonly /></td>
            <th>求职意向岗位</th>
            <td><input :value="optimizedResume.desiredJob" type="text" readonly /></td>
          </tr>

          <tr>
            <th>核心技能</th>
            <td colspan="3"><input :value="optimizedResume.coreSkill" type="text" readonly /></td>
          </tr>

          <tr>
            <th>证书</th>
            <td><input :value="optimizedResume.certificate" type="text" readonly /></td>
            <th>薪资预期</th>
            <td><input :value="optimizedResume.desiredSalary" type="text" readonly /></td>
          </tr>

          <tr>
            <th>相关工作时长</th>
            <td colspan="3"><input :value="optimizedResume.internshipDuration" type="text" readonly /></td>
          </tr>

          <tr>
            <th>创新能力</th>
            <td colspan="3"><textarea :value="optimizedResume.innovation" rows="3" readonly></textarea></td>
          </tr>

          <tr>
            <th>学习能力</th>
            <td colspan="3"><textarea :value="optimizedResume.learning" rows="3" readonly></textarea></td>
          </tr>

          <tr>
            <th>抗压能力</th>
            <td colspan="3"><textarea :value="optimizedResume.pressureResistance" rows="3" readonly></textarea></td>
          </tr>

          <tr>
            <th>沟通能力</th>
            <td colspan="3"><textarea :value="optimizedResume.communication" rows="3" readonly></textarea></td>
          </tr>

          <tr>
            <th>实习/项目经历</th>
            <td colspan="3"><textarea :value="optimizedResume.internshipProject" rows="4" readonly></textarea></td>
          </tr>

          <tr>
            <th>项目成果</th>
            <td colspan="3"><textarea :value="optimizedResume.projectAchievement" rows="4" readonly></textarea></td>
          </tr>
        </tbody>
      </table>
    </form>

    <div class="actions">
      <button type="button" class="btn primary" @click="exportOptimizedResume">一键导出</button>
      <button type="button" class="btn secondary" @click="backToReport">返回报告管理</button>
    </div>
  </section>
</template>

<style scoped>
.resume-form-wrap {
  max-width: 980px;
  margin: 28px auto;
  padding: 24px;
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
}

.title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #e8f8ff;
  text-shadow: 0 0 12px rgba(0, 242, 255, 0.25);
}

.subtitle {
  margin: 8px 0 18px;
  color: #9cd8ff;
}

.empty-tip {
  margin: 14px 0;
  color: #caefff;
  background: rgba(8, 27, 48, 0.58);
  border: 1px solid rgba(106, 210, 255, 0.4);
  border-radius: 10px;
  padding: 14px;
}

.resume-form {
  width: 100%;
}

.resume-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  border: 1px solid rgba(0, 242, 255, 0.28) !important;
  background: rgba(4, 10, 24, 0.72) !important;
  box-shadow: inset 0 0 24px rgba(0, 242, 255, 0.04);
}

.resume-table th,
.resume-table td {
  border: 1px solid rgba(0, 242, 255, 0.22) !important;
  padding: 12px;
  vertical-align: middle;
  color: #e8f8ff !important;
}

.resume-table th {
  width: 20%;
  background: rgba(6, 18, 48, 0.85) !important;
  color: #ffffff !important;
  text-align: left;
  font-weight: 600;
}

.resume-table td {
  width: 30%;
  background: rgba(5, 14, 32, 0.55) !important;
}

.resume-form input,
.resume-form textarea {
  width: 100%;
  border: 1px solid rgba(0, 242, 255, 0.38) !important;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  color: #e8f8ff !important;
  outline: none;
  box-sizing: border-box;
  background: rgba(3, 8, 22, 0.92) !important;
  box-shadow: inset 0 0 12px rgba(0, 242, 255, 0.06) !important;
}

.resume-form textarea {
  resize: vertical;
  min-height: 84px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.btn {
  min-width: 120px;
  padding: 10px 16px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.btn.primary {
  border: 1px solid rgba(0, 242, 255, 0.55) !important;
  color: #ffffff !important;
  background: linear-gradient(
    105deg,
    rgba(0, 242, 255, 0.35) 0%,
    rgba(30, 20, 70, 0.88) 48%,
    rgba(214, 0, 255, 0.4) 100%
  ) !important;
  box-shadow: 0 0 14px rgba(0, 242, 255, 0.2), 0 0 22px rgba(255, 0, 255, 0.1) !important;
}

.btn.primary:hover {
  filter: brightness(1.08);
  transform: translateY(-1px);
}

.btn.secondary {
  border: 1px solid rgba(0, 242, 255, 0.35) !important;
  color: #b8f8ff !important;
  background: rgba(6, 18, 48, 0.65) !important;
  box-shadow: inset 0 0 12px rgba(0, 242, 255, 0.06) !important;
}

.btn.secondary:hover {
  background: rgba(214, 0, 255, 0.15) !important;
  border-color: rgba(255, 0, 255, 0.4) !important;
  color: #ffffff !important;
}

@media (max-width: 768px) {
  .resume-form-wrap {
    margin: 12px;
    padding: 14px;
  }

  .resume-table,
  .resume-table tbody,
  .resume-table tr,
  .resume-table th,
  .resume-table td {
    display: block;
    width: 100%;
  }

  .resume-table tr {
    border-bottom: 1px solid rgba(0, 242, 255, 0.22);
  }

  .resume-table th {
    border-bottom: none;
  }

  .resume-table td {
    border-top: none;
  }
}
</style>
