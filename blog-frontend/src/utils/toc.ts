export interface TocItem {
  id: string
  text: string
  level: number
}

export function generateTocFromContent(content: string): TocItem[] {
  const toc: TocItem[] = []
  const headingRegex = /<h([1-3])[^>]*id="([^"]+)"[^>]*>([^<]+)<\/h[1-3]>/gi
  
  let match
  while ((match = headingRegex.exec(content)) !== null) {
    const level = parseInt(match[1])
    const id = match[2]
    const text = match[3].trim()
    
    toc.push({ id, text, level })
  }
  
  return toc
}

export function generateTocFromMarkdown(markdown: string): TocItem[] {
  const toc: TocItem[] = []
  const headingRegex = /^\s*(#{1,3})\s+(.+?)\s*$/gm
  
  let match
  while ((match = headingRegex.exec(markdown)) !== null) {
    const level = match[1].length
    const text = match[2].trim()
    const id = text
      .toLowerCase()
      .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-')
      .replace(/^-|-$/g, '')
    
    if (id) {
      toc.push({ id, text, level })
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
      toc.push({ id, text, level })
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
