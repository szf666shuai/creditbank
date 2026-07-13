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
  /* enterprise / admin: deferred full restyle — keep role hue, lighten surfaces for body bg */
  enterprise: {
    logoBgFrom: '#1d4ed8',
    logoBgTo: '#2563eb',
    logoStar: '#dbeafe',
    logoAccent: '#60a5fa',
    logoShadow: 'rgba(37, 99, 235, 0.28)',
    particleBg: ['#f8fafc', '#eff6ff', '#ffffff'],
    particleDot: 'rgba(37, 99, 235, 0.28)',
    particleDotActive: 'rgba(29, 78, 216, 0.45)',
    particleLine: ['rgba(147, 197, 253, 0.4)', 'rgba(37, 99, 235, 0.18)', 'rgba(37, 99, 235, 0.04)'],
    particleGlow: 'rgba(96, 165, 250, 0.28)',
    explosionColors: ['#2563eb', '#60a5fa', '#93c5fd', '#d97706', '#1d4ed8'],
    primary: '#2563eb',
    primaryDark: '#1d4ed8',
    primarySoft: '#60a5fa',
    surface: 'rgba(255, 255, 255, 0.92)',
    surfaceStrong: '#ffffff',
    surfaceCard: '#ffffff',
    border: 'rgba(37, 99, 235, 0.18)',
    text: '#0f172a',
    textMuted: '#64748b',
    textOnHero: '#0f172a',
    shadow: '0 4px 12px rgba(15, 23, 42, 0.08)',
    heroGradient: 'linear-gradient(135deg, #f8fafc 0%, #dbeafe 45%, #ffffff 100%)',
    headerBg: 'rgba(255, 255, 255, 0.92)',
    headerBorder: 'rgba(37, 99, 235, 0.12)',
  },
  admin: {
    logoBgFrom: '#6d28d9',
    logoBgTo: '#7c3aed',
    logoStar: '#ede9fe',
    logoAccent: '#c084fc',
    logoShadow: 'rgba(124, 58, 237, 0.28)',
    particleBg: ['#faf5ff', '#f3e8ff', '#ffffff'],
    particleDot: 'rgba(124, 58, 237, 0.28)',
    particleDotActive: 'rgba(109, 40, 217, 0.45)',
    particleLine: ['rgba(216, 180, 254, 0.4)', 'rgba(124, 58, 237, 0.18)', 'rgba(124, 58, 237, 0.04)'],
    particleGlow: 'rgba(192, 132, 252, 0.28)',
    explosionColors: ['#7c3aed', '#a78bfa', '#c084fc', '#d97706', '#6d28d9'],
    primary: '#7c3aed',
    primaryDark: '#6d28d9',
    primarySoft: '#a78bfa',
    surface: 'rgba(255, 255, 255, 0.92)',
    surfaceStrong: '#ffffff',
    surfaceCard: '#ffffff',
    border: 'rgba(124, 58, 237, 0.18)',
    text: '#1e1b4b',
    textMuted: '#64748b',
    textOnHero: '#1e1b4b',
    shadow: '0 4px 12px rgba(15, 23, 42, 0.08)',
    heroGradient: 'linear-gradient(135deg, #faf5ff 0%, #ede9fe 45%, #ffffff 100%)',
    headerBg: 'rgba(255, 255, 255, 0.92)',
    headerBorder: 'rgba(124, 58, 237, 0.12)',
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
