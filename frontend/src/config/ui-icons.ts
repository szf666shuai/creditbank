import type { Component } from 'vue'
import {
  Bell,
  Briefcase,
  Calendar,
  ChatDotRound,
  Collection,
  Document,
  EditPen,
  Flag,
  FolderOpened,
  Goods,
  House,
  Link,
  Medal,
  Message,
  Notebook,
  OfficeBuilding,
  Plus,
  Present,
  Reading,
  School,
  ShoppingCart,
  Stamp,
  Star,
  Ticket,
  Tickets,
  TrendCharts,
  User,
  UserFilled,
  VideoCamera,
  View,
  Wallet,
  Warning,
} from '@element-plus/icons-vue'

/** 与示例图一致的扁平单色蓝 */
export const UI_ICON_COLOR = '#1890ff'

/**
 * 语义化图标名 → Element Plus 实心图标
 * 全局 UI 表情统一走这里，避免各处散落 emoji
 */
export const uiIcons = {
  home: House,
  course: Reading,
  reading: Reading,
  collection: Collection,
  hot: TrendCharts,
  goods: Goods,
  gift: Present,
  cart: ShoppingCart,
  activity: Calendar,
  calendar: Calendar,
  news: Document,
  document: Document,
  notebook: Notebook,
  job: Briefcase,
  briefcase: Briefcase,
  forum: ChatDotRound,
  chat: ChatDotRound,
  message: Message,
  bell: Bell,
  enterprise: OfficeBuilding,
  school: School,
  partner: OfficeBuilding,
  credit: Wallet,
  wallet: Wallet,
  integrity: Star,
  star: Star,
  medal: Medal,
  ticket: Ticket,
  tickets: Tickets,
  resume: Notebook,
  folder: FolderOpened,
  applications: Tickets,
  interview: ChatDotRound,
  invite: Ticket,
  material: FolderOpened,
  report: Flag,
  warning: Warning,
  admin: Stamp,
  users: User,
  user: UserFilled,
  notify: Bell,
  edit: EditPen,
  link: Link,
  video: VideoCamera,
  view: View,
  plus: Plus,
  flag: Flag,
} as const satisfies Record<string, Component>

export type UiIconName = keyof typeof uiIcons

export function resolveUiIcon(name?: string | null): Component {
  if (name && name in uiIcons) {
    return uiIcons[name as UiIconName]
  }
  return uiIcons.collection
}
