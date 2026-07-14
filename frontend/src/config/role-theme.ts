export type RoleThemeVariant = 'default' | 'student' | 'enterprise' | 'admin'

export interface RoleTheme {
  logoBgFrom: string
  logoBgTo: string
  logoStar: string
  logoAccent: string
  logoShadow: string
  particleBg: [string, string, string]
  particleDot: string
  particleDotActive: string
  particleLine: [string, string, string]
  particleGlow: string
  explosionColors: string[]
  primary: string
  primaryDark: string
  primarySoft: string
  surface: string
  surfaceStrong: string
  surfaceCard: string
  border: string
  text: string
  textMuted: string
  textOnHero: string
  shadow: string
  heroGradient: string
  headerBg: string
  headerBorder: string
}

/** Light neo-brutal theme shared by guest + student public chrome */
const LIGHT_EDU_THEME: RoleTheme = {
  logoBgFrom: '#fda4af',
  logoBgTo: '#fb7185',
  logoStar: '#fff1f2',
  logoAccent: '#22c55e',
  logoShadow: 'rgba(26, 32, 44, 0.35)',
  particleBg: ['#fff9f0', '#fef3c7', '#ffffff'],
  particleDot: 'rgba(34, 197, 94, 0.28)',
  particleDotActive: 'rgba(22, 163, 74, 0.45)',
  particleLine: ['rgba(34, 197, 94, 0.35)', 'rgba(26, 32, 44, 0.12)', 'rgba(26, 32, 44, 0.04)'],
  particleGlow: 'rgba(34, 197, 94, 0.2)',
  explosionColors: ['#22c55e', '#bee3f8', '#fda4af', '#fef08a', '#1a202c'],
  primary: '#22c55e',
  primaryDark: '#16a34a',
  primarySoft: '#86efac',
  surface: 'rgba(255, 255, 255, 0.98)',
  surfaceStrong: '#ffffff',
  surfaceCard: '#ffffff',
  border: 'rgba(26, 32, 44, 0.9)',
  text: '#1a202c',
  textMuted: '#64748b',
  textOnHero: '#1a202c',
  shadow: '4px 4px 0 0 #1a202c',
  heroGradient: 'linear-gradient(135deg, #fff9f0 0%, #fef3c7 42%, #ffffff 100%)',
  headerBg: '#ffffff',
  headerBorder: '#1a202c',
}

export const ROLE_THEMES: Record<RoleThemeVariant, RoleTheme> = {
  default: { ...LIGHT_EDU_THEME },
  student: { ...LIGHT_EDU_THEME },
  /* 全站统一浅色：企业 / 管理沿用同一套 Neo-brutal light，仅保留角色主色差 */
  enterprise: {
    ...LIGHT_EDU_THEME,
    logoBgFrom: '#1d4ed8',
    logoBgTo: '#2563eb',
    logoStar: '#dbeafe',
    logoAccent: '#60a5fa',
    logoShadow: 'rgba(37, 99, 235, 0.28)',
    primary: '#2563eb',
    primaryDark: '#1d4ed8',
    primarySoft: '#60a5fa',
    border: 'rgba(37, 99, 235, 0.35)',
    headerBorder: 'rgba(37, 99, 235, 0.28)',
    heroGradient: 'linear-gradient(135deg, #fff9f0 0%, #dbeafe 45%, #ffffff 100%)',
  },
  admin: {
    ...LIGHT_EDU_THEME,
    logoBgFrom: '#6d28d9',
    logoBgTo: '#7c3aed',
    logoStar: '#ede9fe',
    logoAccent: '#c084fc',
    logoShadow: 'rgba(124, 58, 237, 0.28)',
    primary: '#7c3aed',
    primaryDark: '#6d28d9',
    primarySoft: '#a78bfa',
    border: 'rgba(124, 58, 237, 0.35)',
    headerBorder: 'rgba(124, 58, 237, 0.28)',
    heroGradient: 'linear-gradient(135deg, #fff9f0 0%, #ede9fe 45%, #ffffff 100%)',
  },
}

export function resolveRoleThemeVariant(options: {
  isLoggedIn: boolean
  isStudent: boolean
  isEnterprise: boolean
  isAdmin: boolean
}): RoleThemeVariant {
  if (!options.isLoggedIn) return 'default'
  if (options.isEnterprise) return 'enterprise'
  if (options.isAdmin) return 'admin'
  if (options.isStudent) return 'student'
  return 'default'
}

export function resolveHeaderThemeVariant(options: {
  isHomePage: boolean
  isLoggedIn: boolean
  isStudent: boolean
  isEnterprise: boolean
  isAdmin: boolean
}): RoleThemeVariant {
  if (!options.isLoggedIn) return 'student'
  return resolveRoleThemeVariant(options)
}

export function getRoleTheme(variant: RoleThemeVariant): RoleTheme {
  return ROLE_THEMES[variant]
}
