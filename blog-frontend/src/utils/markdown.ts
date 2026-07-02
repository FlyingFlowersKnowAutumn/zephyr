import { marked } from 'marked'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

marked.setOptions({
  gfm: true,
  breaks: false,
})

const renderer = new marked.Renderer()

renderer.code = function ({ text, lang }: { text: string; lang?: string }) {
  const validLang = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
  const highlighted = hljs.highlight(text, { language: validLang }).value
  return `<pre><code class="hljs language-${validLang}">${highlighted}</code></pre>`
}

renderer.heading = function ({ text, depth }: { text: string; depth: number }) {
  const id = text.toLowerCase().replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-').replace(/^-|-$/g, '')
  return `<h${depth} id="${id}">${text}</h${depth}>`
}

marked.use({ renderer })

export function renderMarkdown(raw: string): string {
  if (!raw) return ''
  const html = marked.parse(raw) as string
  return DOMPurify.sanitize(html)
}
