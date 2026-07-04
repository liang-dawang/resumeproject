/**
 * 数据中心「职业发展路径」与后端 /career/path/vertical、/career/path/transfer 共用的岗位 key 列表。
 */
export const ALL_PORTRAIT_JOB_KEYS = [
  'C/C++_中级',
  'C/C++_初级',
  'C/C++_高级',
  'Java_中级',
  'Java_初级',
  'Java_高级',
  '前端开发_中级',
  '前端开发_初级',
  '前端开发_高级',
  '实施工程师_中级',
  '实施工程师_初级',
  '实施工程师_高级',
  '技术支持工程师_中级',
  '技术支持工程师_初级',
  '技术支持工程师_高级',
  '测试工程师_中级',
  '测试工程师_初级',
  '测试工程师_高级',
  '硬件测试_中级',
  '硬件测试_初级',
  '硬件测试_高级',
  '科研人员_顶级',
  '软件测试_中级',
  '软件测试_初级',
  '软件测试_高级',
  '项目经理/主管_顶级'
]

export function normalizeJobKey(jobName) {
  const text = String(jobName || '').trim()
  if (!text) return ''
  if (text.includes('_')) return text
  const matched = text.match(/^(.*?)(初级|中级|高级|顶级)$/)
  if (matched) {
    return `${matched[1]}_${matched[2]}`
  }
  return text
}
