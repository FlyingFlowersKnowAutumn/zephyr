export interface TocItem {
  id: string
  text: string
  level: number
  lineNumber: number
}

export function generateTocFromContent(content: string): TocItem[] {
  const toc: TocItem[] = []
  const headingRegex = /<h([1-3])[^>]*id="([^"]+)"[^>]*>([^<]+)<\/h[1-3]>/gi

  let match
  while ((match = headingRegex.exec(content)) !== null) {
    const level = parseInt(match[1])
    const id = match[2]
    const text = match[3].trim()

    toc.push({ id, text, level, lineNumber: 0 })
  }

  return toc
}

export function generateTocFromMarkdown(markdown: string): TocItem[] {
  const toc: TocItem[] = []
  // \u79fb\u9664\u4ee3\u7801\u5757\uff0c\u907f\u514d\u4ee3\u7801\u4e2d\u7684 # \u88ab\u8bef\u8bc6\u522b\u4e3a\u6807\u9898
  const cleanMarkdown = markdown.replace(/```[\s\S]*?```/g, '')
  const headingRegex = /^\s*(#{1,3})\s+(.+?)\s*$/gm

  let match
  while ((match = headingRegex.exec(cleanMarkdown)) !== null) {
    const level = match[1].length
    const text = match[2].trim()
    const id = text
      .toLowerCase()
      .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-')
      .replace(/^-|-$/g, '')

    if (id) {
      const matchPosition = match.index
      const lineNumber = cleanMarkdown.substring(0, matchPosition).split('\n').length
      toc.push({ id, text, level, lineNumber })
    }
  }

  return toc
}

export function generateTocFromHtml(html: string): TocItem[] {
  const toc: TocItem[] = []
  const headingRegex = /<h([1-3])[^>]*id="([^"]+)"[^>]*>([^<]+)<\/h[1-3]>/gi
  
  let match
  while ((match = headingRegex.exec(html)) !== null) {
    const level = parseInt(match[1])
    const id = match[2]
    const text = match[3].trim()
    
    if (id && text) {
      toc.push({ id, text, level, lineNumber: 0 })
    }
  }
  
  return toc
}

export function scrollToHeading(id: string): void {
  const element = document.getElementById(id)
  if (element) {
    const offsetTop = element.offsetTop - 80
    window.scrollTo({
      top: offsetTop,
      behavior: 'smooth'
    })
  }
}
