/** 轻量 Markdown → 安全 HTML（加粗 / 斜体 / 列表 / 标题 / 换行） */

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function formatInline(text: string): string {
  let html = escapeHtml(text)
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/__([^_]+?)__/g, '<strong>$1</strong>')
  html = html.replace(/(^|[^*\w])\*(?!\s)([^*\n]+?)\*(?!\*)/g, '$1<em>$2</em>')
  html = html.replace(/`([^`]+?)`/g, '<code>$1</code>')
  // 未成对的残留星号去掉，避免「奇怪位置的 *」
  html = html.replace(/\*{1,2}/g, '')
  return html
}

function isBullet(line: string) {
  return /^\s*[-*•]\s+/.test(line)
}

function isOrdered(line: string) {
  return /^\s*\d+[.)、]\s+/.test(line)
}

function bulletBody(line: string) {
  return line.replace(/^\s*[-*•]\s+/, '')
}

function orderedBody(line: string) {
  return line.replace(/^\s*\d+[.)、]\s+/, '')
}

/**
 * 将模型常用 Markdown 转为可 v-html 的片段（已转义）。
 */
export function renderLiteMarkdown(source: string): string {
  if (!source) return ''

  const lines = source.replace(/\r\n/g, '\n').split('\n')
  const parts: string[] = []
  let i = 0

  while (i < lines.length) {
    const line = lines[i]
    const trimmed = line.trim()

    if (!trimmed) {
      i += 1
      continue
    }

    if (/^#{1,3}\s+/.test(trimmed)) {
      const level = Math.min(3, (trimmed.match(/^#+/)?.[0].length ?? 1))
      const title = trimmed.replace(/^#{1,3}\s+/, '')
      parts.push(`<h${level + 2} class="md-h">${formatInline(title)}</h${level + 2}>`)
      i += 1
      continue
    }

    if (isBullet(line)) {
      const items: string[] = []
      while (i < lines.length && isBullet(lines[i])) {
        items.push(`<li>${formatInline(bulletBody(lines[i]))}</li>`)
        i += 1
      }
      parts.push(`<ul class="md-ul">${items.join('')}</ul>`)
      continue
    }

    if (isOrdered(line)) {
      const items: string[] = []
      while (i < lines.length && isOrdered(lines[i])) {
        items.push(`<li>${formatInline(orderedBody(lines[i]))}</li>`)
        i += 1
      }
      parts.push(`<ol class="md-ol">${items.join('')}</ol>`)
      continue
    }

    const block: string[] = []
    while (
      i < lines.length &&
      lines[i].trim() &&
      !isBullet(lines[i]) &&
      !isOrdered(lines[i]) &&
      !/^#{1,3}\s+/.test(lines[i].trim())
    ) {
      block.push(formatInline(lines[i].trim()))
      i += 1
    }
    parts.push(`<p class="md-p">${block.join('<br>')}</p>`)
  }

  return parts.join('')
}
