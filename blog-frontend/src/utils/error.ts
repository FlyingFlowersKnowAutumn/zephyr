import type { AxiosError } from 'axios'

/** 后端统一响应体，出错时的中文提示在 message 字段 */
interface BackendResult {
  code?: number
  message?: string
  success?: boolean
}

/**
 * 把 axios 抛出的错误翻译成能给用户看的中文。
 *
 * 直接用 error.message 会得到 "Request failed with status code 400" 这类英文，
 * 而后端真正想说的话在 error.response.data.message 里。
 */
export function pickErrorMessage(error: unknown, fallback = '操作失败，请稍后重试'): string {
  const err = error as AxiosError<BackendResult> | undefined

  const data = err?.response?.data
  if (data && typeof data === 'object' && typeof data.message === 'string' && data.message.trim()) {
    return data.message.trim()
  }

  if (err?.code === 'ECONNABORTED' || /timeout/i.test(err?.message || '')) {
    return '请求超时，请检查网络后重试'
  }

  if (typeof navigator !== 'undefined' && navigator.onLine === false) {
    return '网络已断开，请检查网络连接'
  }

  const status = err?.response?.status
  if (status === 401) return '登录已过期，请重新登录'
  if (status === 403) return '没有权限执行该操作'
  if (status === 404) return '请求的内容不存在'
  if (status === 413) return '文件太大了，请换一个小一点的'
  if (status === 429) return '操作过于频繁，请稍后再试'
  if (typeof status === 'number' && status >= 500) return '服务器开小差了，请稍后重试'

  if (!err?.response) return '无法连接服务器，请检查网络'

  return fallback
}
