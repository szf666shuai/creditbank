const STORAGE_NOTIFY_KEY = 'singularis_guest_checkin_notify'
const STORAGE_DATE_KEY = 'singularis_guest_checkin_notify_date'
const SESSION_KEY = 'singularis_guest_session_started'

function todayKey() {
  return new Date().toISOString().slice(0, 10)
}

export function isGuestDailyReminderEnabled() {
  return localStorage.getItem(STORAGE_NOTIFY_KEY) === '1'
}

export function shouldNotifyToday() {
  return localStorage.getItem(STORAGE_DATE_KEY) !== todayKey()
}

function markNotifiedToday() {
  localStorage.setItem(STORAGE_DATE_KEY, todayKey())
}

function isNewBrowserSession() {
  if (sessionStorage.getItem(SESSION_KEY)) return false
  sessionStorage.setItem(SESSION_KEY, '1')
  return true
}

function showDesktopReminder() {
  if (typeof Notification === 'undefined' || Notification.permission !== 'granted') return false
  new Notification('星秩存册 · 今日学习打卡', {
    body: '今天还没有学习打卡，打开网站登录后开始学习吧。',
    tag: 'guest-daily-checkin',
    icon: '/favicon.svg',
  })
  markNotifiedToday()
  return true
}

export async function registerGuestReminderServiceWorker() {
  if (!('serviceWorker' in navigator)) return
  try {
    await navigator.serviceWorker.register('/guest-reminder-sw.js', { scope: '/' })
  } catch {
    // Service worker is optional; desktop notifications still work from the page.
  }
}

/**
 * 开启每日桌面提醒。开启后，每次新开浏览器会话（如开机后首次打开浏览器）会弹出系统通知。
 */
export async function enableGuestDailyReminder() {
  if (typeof Notification === 'undefined') {
    throw new Error('当前浏览器不支持桌面提醒')
  }
  const permission = await Notification.requestPermission()
  if (permission !== 'granted') {
    throw new Error('需要允许通知权限才能开启每日提醒')
  }
  localStorage.setItem(STORAGE_NOTIFY_KEY, '1')
  await registerGuestReminderServiceWorker()
  showDesktopReminder()
}

export function disableGuestDailyReminder() {
  localStorage.removeItem(STORAGE_NOTIFY_KEY)
}

/**
 * 在新浏览器会话中弹出一次桌面提醒（不显示站内弹窗）。
 */
export function initGuestDailyReminder(isLoggedIn: boolean) {
  if (isLoggedIn || !isGuestDailyReminderEnabled()) return
  if (!shouldNotifyToday()) return
  if (!isNewBrowserSession()) return
  showDesktopReminder()
}
