export const DANMAKU_COLORS = [
  { value: '#ffffff', label: '白色' },
  { value: '#fb7299', label: '粉色' },
  { value: '#6bcbff', label: '蓝色' },
  { value: '#ffd93d', label: '黄色' },
  { value: '#00d1b2', label: '青色' },
  { value: '#c9a0ff', label: '紫色' },
] as const

export type DanmakuColor = (typeof DANMAKU_COLORS)[number]['value']
