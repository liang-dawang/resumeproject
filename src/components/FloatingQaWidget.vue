<script setup>
import { computed, nextTick, ref } from 'vue'
import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8000'
const API_URL = `${API_BASE}/ai/student-qa`

const isOpen = ref(false)
const input = ref('')
const isThinking = ref(false)
const listRef = ref(null)
const messages = ref([
	{
		id: `${Date.now()}-init`,
		role: 'assistant',
		content: '你好，我是计算机学习助手。你可以问我算法、前后端、操作系统、数据库、面试准备等问题。'
	}
])

const canSend = computed(() => input.value.trim().length > 0 && !isThinking.value)

const pushMessage = (role, content) => {
	messages.value.push({
		id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
		role,
		content
	})
}

const scrollToBottom = async () => {
	await nextTick()
	if (listRef.value) {
		listRef.value.scrollTop = listRef.value.scrollHeight
	}
}

const askQuestion = async (question) => {
	try {
		const res = await fetch(API_URL, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ question })
		})
		if (!res.ok) throw new Error(`HTTP ${res.status}`)
		const data = await res.json()
		return data.answer || '我暂时没有拿到答案，请稍后再试。'
	} catch (fetchError) {
		try {
			const { data } = await axios.post(API_URL, { question })
			return data.answer || '我暂时没有拿到答案，请稍后再试。'
		} catch (axiosError) {
			// 本地无接口时的模拟兜底，避免界面空白。
			await new Promise((resolve) => setTimeout(resolve, 1200))
			return `你问的是：“${question}”。我建议从定义、原理、应用场景、常见面试题四个角度来准备。`
		}
	}
}

const onSend = async () => {
	const question = input.value.trim()
	if (!question || isThinking.value) return

	pushMessage('user', question)
	input.value = ''
	isThinking.value = true
	await scrollToBottom()

	const answer = await askQuestion(question)
	pushMessage('assistant', answer)
	isThinking.value = false
	await scrollToBottom()
}

const onEnterSend = async (event) => {
	if (event.key === 'Enter' && !event.shiftKey) {
		event.preventDefault()
		await onSend()
	}
}
</script>

<template>
	<div class="qa-fab-root">
		<div v-if="isOpen" class="qa-window">
			<div class="qa-header">
				<h3>计算机学习问答助手</h3>
				<button type="button" class="qa-collapse-btn" @click="isOpen = false">收起</button>
			</div>

			<div ref="listRef" class="qa-messages">
				<div v-for="msg in messages" :key="msg.id" :class="['qa-row', msg.role === 'user' ? 'qa-row-user' : 'qa-row-bot']">
					<div :class="['qa-bubble', msg.role === 'user' ? 'qa-bubble-user' : 'qa-bubble-bot']">
						{{ msg.content }}
					</div>
				</div>

				<div v-if="isThinking" class="qa-row qa-row-bot">
					<div class="qa-bubble qa-bubble-bot">正在思考...</div>
				</div>
			</div>

			<div class="qa-input-area">
				<textarea
					v-model="input"
					rows="2"
					:disabled="isThinking"
					:placeholder="isThinking ? '正在思考，请稍候...' : '请输入你的计算机相关问题'"
					@keydown="onEnterSend"
				></textarea>
				<button type="button" :disabled="!canSend" @click="onSend">发送</button>
			</div>
		</div>

		<button type="button" class="qa-fab-btn" aria-label="打开计算机问答助手" @click="isOpen = !isOpen">
			💬
		</button>
	</div>
</template>

<style scoped>
.qa-fab-root {
	position: fixed;
	right: 24px;
	bottom: 24px;
	z-index: 9999;
}

.qa-fab-btn {
	width: 56px;
	height: 56px;
	border: 1px solid rgba(0, 242, 255, 0.55);
	border-radius: 999px;
	background: linear-gradient(135deg, rgba(0, 242, 255, 0.8), rgba(214, 0, 255, 0.75));
	color: #fff;
	font-size: 24px;
	cursor: pointer;
	box-shadow: 0 10px 24px rgba(0, 0, 0, 0.4), 0 0 18px rgba(0, 242, 255, 0.35);
}

.qa-window {
	width: min(360px, calc(100vw - 24px));
	max-height: 520px;
	margin-bottom: 12px;
	border-radius: 14px;
	border: 1px solid rgba(0, 242, 255, 0.35);
	background: rgba(5, 14, 32, 0.93);
	backdrop-filter: blur(10px);
	box-shadow: 0 18px 38px rgba(0, 0, 0, 0.5), 0 0 20px rgba(0, 242, 255, 0.18);
	overflow: hidden;
}

.qa-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 10px 12px;
	border-bottom: 1px solid rgba(0, 242, 255, 0.2);
}

.qa-header h3 {
	margin: 0;
	font-size: 14px;
	color: #e8f8ff;
}

.qa-collapse-btn {
	border: 1px solid rgba(0, 242, 255, 0.35);
	border-radius: 8px;
	background: transparent;
	color: #b8e9ff;
	padding: 3px 8px;
	cursor: pointer;
}

.qa-messages {
	height: 320px;
	overflow-y: auto;
	padding: 10px;
}

.qa-row {
	display: flex;
	margin-bottom: 10px;
}

.qa-row-user {
	justify-content: flex-end;
}

.qa-row-bot {
	justify-content: flex-start;
}

.qa-bubble {
	max-width: 85%;
	padding: 8px 10px;
	border-radius: 10px;
	font-size: 13px;
	line-height: 1.6;
	word-break: break-word;
}

.qa-bubble-user {
	background: rgba(0, 242, 255, 0.82);
	color: #fff;
}

.qa-bubble-bot {
	border: 1px solid rgba(0, 242, 255, 0.24);
	background: rgba(8, 27, 48, 0.68);
	color: #def6ff;
}

.qa-input-area {
	display: flex;
	gap: 8px;
	padding: 10px;
	border-top: 1px solid rgba(0, 242, 255, 0.2);
}

.qa-input-area textarea {
	flex: 1;
	resize: none;
	border-radius: 8px;
	border: 1px solid rgba(0, 242, 255, 0.35);
	background: rgba(5, 14, 32, 0.88);
	color: #e8f8ff;
	padding: 8px 10px;
	outline: none;
}

.qa-input-area textarea:disabled {
	opacity: 0.7;
	cursor: not-allowed;
}

.qa-input-area button {
	align-self: flex-end;
	height: 38px;
	min-width: 60px;
	border-radius: 8px;
	border: 1px solid rgba(0, 242, 255, 0.45);
	background: rgba(0, 242, 255, 0.35);
	color: #fff;
	cursor: pointer;
}

.qa-input-area button:disabled {
	opacity: 0.5;
	cursor: not-allowed;
}
</style>
