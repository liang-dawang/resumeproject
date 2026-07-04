import { useMemo, useRef, useState } from 'react'
import axios from 'axios'

const API_URL = '/api/student-qa'

function createMessage(role, content) {
	return {
		id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
		role,
		content
	}
}

export default function FloatingQaWidget() {
	const [isOpen, setIsOpen] = useState(false)
	const [input, setInput] = useState('')
	const [isThinking, setIsThinking] = useState(false)
	const [messages, setMessages] = useState(() => [
		createMessage('assistant', '你好，我是计算机学习助手。你可以问我算法、前后端、操作系统、数据库、面试准备等问题。')
	])
	const listRef = useRef(null)

	const canSend = useMemo(() => input.trim().length > 0 && !isThinking, [input, isThinking])

	const scrollToBottom = () => {
		requestAnimationFrame(() => {
			if (listRef.current) {
				listRef.current.scrollTop = listRef.current.scrollHeight
			}
		})
	}

	const askQuestion = async (question) => {
		// 默认用 fetch 模拟后端调用；若你后续接真实接口，可删掉 fallback 逻辑。
		try {
			const response = await fetch(API_URL, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ question })
			})
			if (!response.ok) {
				throw new Error(`HTTP ${response.status}`)
			}
			const data = await response.json()
			return data.answer || '我暂时没有拿到答案，请稍后再试。'
		} catch (fetchError) {
			// fetch 失败时，使用 axios 再尝试一次（满足 fetch/axios 模拟要求）。
			try {
				const { data } = await axios.post(API_URL, { question })
				return data.answer || '我暂时没有拿到答案，请稍后再试。'
			} catch (axiosError) {
				// 最终兜底，避免界面空白。
				return '网络波动或后端暂不可用。你可以稍后重试，或把问题拆小后再问我。'
			}
		}
	}

	const onSend = async () => {
		const question = input.trim()
		if (!question || isThinking) {
			return
		}

		const userMessage = createMessage('user', question)
		setMessages((prev) => [...prev, userMessage])
		setInput('')
		setIsThinking(true)
		scrollToBottom()

		const answer = await askQuestion(question)
		const botMessage = createMessage('assistant', answer)
		setMessages((prev) => [...prev, botMessage])
		setIsThinking(false)
		scrollToBottom()
	}

	const onKeyDown = async (event) => {
		if (event.key === 'Enter' && !event.shiftKey) {
			event.preventDefault()
			await onSend()
		}
	}

	return (
		<div className="fixed bottom-6 right-6 z-50">
			{isOpen && (
				<div className="mb-3 w-[360px] max-w-[calc(100vw-2rem)] overflow-hidden rounded-2xl border border-cyan-300/40 bg-slate-950/90 shadow-2xl backdrop-blur">
					<div className="flex items-center justify-between border-b border-cyan-300/25 px-4 py-3">
						<h3 className="text-sm font-semibold text-cyan-100">计算机学习问答助手</h3>
						<button
							type="button"
							onClick={() => setIsOpen(false)}
							className="rounded-md px-2 py-1 text-xs text-cyan-200/80 transition hover:bg-cyan-400/20 hover:text-white"
						>
							收起
						</button>
					</div>

					<div ref={listRef} className="h-80 space-y-3 overflow-y-auto px-4 py-3">
						{messages.map((msg) => (
							<div
								key={msg.id}
								className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
							>
								<div
									className={`max-w-[85%] rounded-xl px-3 py-2 text-sm leading-6 ${
										msg.role === 'user'
											? 'bg-cyan-500/80 text-white'
											: 'border border-cyan-300/25 bg-slate-900/80 text-cyan-50'
									}`}
								>
									{msg.content}
								</div>
							</div>
						))}

						{isThinking && (
							<div className="flex justify-start">
								<div className="rounded-xl border border-cyan-300/25 bg-slate-900/80 px-3 py-2 text-sm text-cyan-100/90">
									正在思考...
								</div>
							</div>
						)}
					</div>

					<div className="border-t border-cyan-300/25 p-3">
						<div className="flex items-end gap-2">
							<textarea
								value={input}
								onChange={(e) => setInput(e.target.value)}
								onKeyDown={onKeyDown}
								disabled={isThinking}
								rows={2}
								placeholder={isThinking ? '正在思考，请稍候...' : '请输入你的计算机相关问题'}
								className="min-h-[44px] flex-1 resize-none rounded-lg border border-cyan-300/30 bg-slate-900/80 px-3 py-2 text-sm text-cyan-50 outline-none transition placeholder:text-cyan-200/55 focus:border-cyan-300/60 disabled:cursor-not-allowed disabled:opacity-70"
							/>
							<button
								type="button"
								onClick={onSend}
								disabled={!canSend}
								className="h-[44px] rounded-lg border border-cyan-300/40 bg-cyan-500/80 px-4 text-sm font-medium text-white transition hover:bg-cyan-500 disabled:cursor-not-allowed disabled:opacity-50"
							>
								发送
							</button>
						</div>
					</div>
				</div>
			)}

			<button
				type="button"
				onClick={() => setIsOpen((prev) => !prev)}
				className="flex h-14 w-14 items-center justify-center rounded-full border border-cyan-300/60 bg-gradient-to-br from-cyan-400 to-violet-500 text-2xl text-white shadow-lg transition hover:scale-105"
				aria-label="打开计算机问答助手"
			>
				💬
			</button>
		</div>
	)
}
