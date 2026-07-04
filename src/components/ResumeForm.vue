<script setup>
import { reactive, computed, onMounted, ref } from 'vue'
import axios from 'axios'
import ResumeUpload from './ResumeUpload.vue'

const resumeTextApi = 'http://127.0.0.1:8000/user/resume-text'
const portraitJobApi = 'http://localhost:8080/api/portrait/job_name'

const educationOptions = ['大专及以下', '大专', '本科', '硕士', '博士']

const defaultDesiredJobOptions = [
	'C/C++',
	'Java',
	'前端开发',
	'实施工程师',
	'技术支持与运维',
	'测试工程师',
	'硬件测试',
	'科研人员',
	'软件测试',
	'项目经理/主管'
]

const desiredJobOptions = ref([...defaultDesiredJobOptions])
const desiredJobLoading = ref(false)
const desiredJobError = ref('')

const normalizeJobOptions = (list) => {
	return [...new Set(
		(Array.isArray(list) ? list : [])
			.map((item) => String(item || '').trim())
			.filter(Boolean)
	)]
}

const skillLevelOptions = ['入门', '基础', '熟练', '精通']

const internshipDurationOptions = [
	'无',
	'一年以内',
	'一到三年',
	'三年以上',
]

const formData = reactive({
	name: '',
	education: '',
	major: '',
	desiredJob: '',
	coreSkill: '',
	certificate: '',
	expectedSalary: '',
	skillLevel: '',
	stackDetail: '',
	internshipProject: '',
	internshipDuration: '',
	projectAchievement: ''
})

const requiredFields = {
	name: '姓名',
	education: '学历',
	major: '专业',
	desiredJob: '求职意向岗位',
	coreSkill: '核心技能'
}

const missingRequiredFields = computed(() => {
	return Object.entries(requiredFields)
		.filter(([key]) => !String(formData[key]).trim())
		.map(([, label]) => label)
})

const fetchDesiredJobOptions = async () => {
	desiredJobLoading.value = true
	desiredJobError.value = ''
	try {
		const { data } = await axios.get(portraitJobApi)
		const remoteJobs = normalizeJobOptions(data?.data)
		if (remoteJobs.length) {
			desiredJobOptions.value = remoteJobs
		} else {
			desiredJobOptions.value = [...defaultDesiredJobOptions]
			desiredJobError.value = '岗位列表为空，已使用默认列表。'
		}
	} catch (error) {
		console.warn('加载意向岗位列表失败，已使用默认列表', error)
		desiredJobOptions.value = [...defaultDesiredJobOptions]
		desiredJobError.value = '岗位列表加载失败，已使用默认列表。'
	} finally {
		desiredJobLoading.value = false
	}
}

const onSubmit = async () => {
	if (missingRequiredFields.value.length > 0) {
		window.alert(`请先填写必填项：${missingRequiredFields.value.join('、')}`)
		return
	}

	const payload = {
		name: formData.name,
		education: formData.education,
		major: formData.major,
		desiredJob: formData.desiredJob,
		coreSkill: formData.coreSkill,
		certificate: formData.certificate,
		expectedSalary: formData.expectedSalary,
		skillLevel: formData.skillLevel,
		stackDetail: formData.stackDetail
	}

	console.log('提交简历文本数据:', payload)

	try {
		// 如果浏览器提示跨域错误，需要后端为该接口配置 CORS（Access-Control-Allow-Origin）。
		const response = await axios.post(resumeTextApi, payload)
		console.log('简历文本提交成功:', response.data)
		window.alert('简历信息已填写完成')
	} catch (error) {
		console.error('简历文本提交失败:', error)
		window.alert('提交失败，请检查网络或后端接口配置')
	}
}

const onReset = () => {
	Object.keys(formData).forEach((key) => {
		formData[key] = ''
	})
}

onMounted(() => {
	fetchDesiredJobOptions()
})
</script>

<template>
	<section class="resume-form-wrap">
		<h2 class="title">简历信息录入表</h2>
		<p class="subtitle">标注 <span class="required-mark">*</span> 的字段为必填项</p>
		<ResumeUpload />

		<form class="resume-form" @submit.prevent="onSubmit">
			<table class="resume-table" cellspacing="0" cellpadding="0">
				<tbody>
					<tr>
						<th>
							姓名 <span class="required-mark">*</span>
						</th>
						<td>
							<input v-model.trim="formData.name" type="text" placeholder="请输入姓名" />
						</td>
						<th>
							学历 <span class="required-mark">*</span>
						</th>
						<td>
							<select v-model="formData.education">
								<option value="">请选择学历</option>
								<option v-for="item in educationOptions" :key="item" :value="item">{{ item }}</option>
							</select>
						</td>
					</tr>

					<tr>
						<th>
							专业 <span class="required-mark">*</span>
						</th>
						<td>
							<input v-model.trim="formData.major" type="text" placeholder="请输入专业" />
						</td>
						<th>
							求职意向岗位 <span class="required-mark">*</span>
						</th>
						<td>
							<select v-model="formData.desiredJob">
								<option value="">请选择意向岗位</option>
								<option v-for="item in desiredJobOptions" :key="item" :value="item">{{ item }}</option>
							</select>
							<p v-if="desiredJobLoading" class="field-hint">正在加载岗位列表...</p>
							<p v-else-if="desiredJobError" class="field-hint field-hint--error">{{ desiredJobError }}</p>
						</td>
					</tr>

					<tr>
						<th>
							核心技能 <span class="required-mark">*</span>
						</th>
						<td colspan="3">
							<input v-model.trim="formData.coreSkill" type="text" placeholder="请输入核心技能（多个技能可用逗号分隔）" />
						</td>
					</tr>

					<tr>
						<th>证书</th>
						<td>
							<input v-model.trim="formData.certificate" type="text" placeholder="如：软考中级、CET-6" />
						</td>
						<th>薪资预期</th>
						<td>
							<input v-model.trim="formData.expectedSalary" type="text" placeholder="如：12k-18k" />
						</td>
					</tr>

					<tr>
						<th>技能熟练度</th>
						<td>
							<select v-model="formData.skillLevel">
								<option value="">请选择熟练度</option>
								<option v-for="item in skillLevelOptions" :key="item" :value="item">{{ item }}</option>
							</select>
						</td>
						<th>相关工作时长</th>
						<td>
							<select v-model="formData.internshipDuration">
								<option value="">请选择工作时长</option>
								<option v-for="item in internshipDurationOptions" :key="item" :value="item">{{ item }}</option>
							</select>
						</td>
					</tr>

					<tr>
						<th>技术栈详情</th>
						<td colspan="3">
							<textarea
								v-model.trim="formData.stackDetail"
								rows="3"
								placeholder="补充技能描述，如框架、工具链、擅长场景"
							></textarea>
						</td>
					</tr>

					<tr>
						<th>实习/项目经历</th>
						<td colspan="3">
							<textarea
								v-model.trim="formData.internshipProject"
								rows="4"
								placeholder="请描述实习或项目背景、职责和技术要点"
							></textarea>
						</td>
					</tr>

					<tr>
						<th>项目成果</th>
						<td colspan="3">
							<textarea
								v-model.trim="formData.projectAchievement"
								rows="4"
								placeholder="请描述结果数据、业务价值或技术产出"
							></textarea>
						</td>
					</tr>
				</tbody>
			</table>

			<div class="actions">
				<button type="button" class="btn secondary" @click="onReset">重置</button>
				<button type="submit" class="btn primary">提交</button>
			</div>
		</form>
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

.required-mark {
	color: #dc2626;
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
.resume-form select,
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

.resume-form input::placeholder,
.resume-form textarea::placeholder {
	color: rgba(156, 216, 255, 0.55) !important;
}

.resume-form select {
	color: #e8f8ff !important;
}

.resume-form select option {
	background: #0a1428;
	color: #e8f8ff;
}

.resume-form textarea {
	resize: vertical;
	min-height: 84px;
}

.resume-form input:focus,
.resume-form select:focus,
.resume-form textarea:focus {
	border-color: rgba(0, 242, 255, 0.65) !important;
	box-shadow:
		0 0 0 2px rgba(0, 242, 255, 0.2),
		0 0 18px rgba(0, 242, 255, 0.15) !important;
}

.actions {
	display: flex;
	justify-content: flex-end;
	gap: 12px;
	margin-top: 16px;
}

.btn {
	min-width: 96px;
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