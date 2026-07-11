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

export const ROLE_THEMES: Record<RoleThemeVariant, RoleTheme> = {
  default: {
    logoBgFrom: '#2b2450',
    logoBgTo: '#4c1d95',
    logoStar: '#f0d78c',
    logoAccent: '#d4af37',
    logoShadow: 'rgba(43, 36, 80, 0.28)',
    particleBg: ['#0b1527', '#101e36', '#0d1829'],
    particleDot: 'rgba(140, 200, 240, 0.75)',
    particleDotActive: 'rgba(180, 235, 255, 0.95)',
    particleLine: ['rgba(120, 220, 255, 0.85)', 'rgba(32, 148, 243, 0.45)', 'rgba(32, 148, 243, 0.05)'],
    particleGlow: 'rgba(100, 210, 255, 0.6)',
    explosionColors: ['#2094f3', '#a78bfa', '#faad14', '#64d2ff', '#f0d78c'],
    primary: '#7c3aed',
    primaryDark: '#c4b5fd',
    primarySoft: '#a78bfa',
    surface: 'rgba(12, 18, 36, 0.58)',
    surfaceStrong: 'rgba(14, 24, 48, 0.74)',
    surfaceCard: 'rgba(18, 30, 58, 0.68)',
    border: 'rgba(124, 58, 237, 0.28)',
    text: '#eef2ff',
    textMuted: '#a5b4d4',
    textOnHero: '#fff',
    shadow: '0 12px 40px rgba(0, 0, 0, 0.35)',
    heroGradient:
      'linear-gradient(120deg, #2b1f4d 0%, #5b2c83 38%, #7c3aed 70%, #a78bfa 100%)',
    headerBg: 'rgba(11, 21, 39, 0.9)',
    headerBorder: 'rgba(124, 58, 237, 0.22)',
  },
  student: {
    logoBgFrom: '#0c4a6e',
    logoBgTo: '#0891b2',
    logoStar: '#a5f3fc',
    logoAccent: '#22d3ee',
    logoShadow: 'rgba(8, 145, 178, 0.32)',
    particleBg: ['#061525', '#0a2238', '#071a2c'],
    particleDot: 'rgba(100, 210, 240, 0.7)',
    particleDotActive: 'rgba(165, 243, 252, 0.95)',
    particleLine: ['rgba(103, 232, 249, 0.85)', 'rgba(0, 161, 214, 0.45)', 'rgba(0, 161, 214, 0.05)'],
    particleGlow: 'rgba(34, 211, 238, 0.55)',
    explosionColors: ['#00a1d6', '#22d3ee', '#67e8f9', '#2094f3', '#a5f3fc'],
    primary: '#00a1d6',
    primaryDark: '#67e8f9',
    primarySoft: '#22d3ee',
    surface: 'rgba(6, 22, 38, 0.58)',
    surfaceStrong: 'rgba(8, 30, 52, 0.74)',
    surfaceCard: 'rgba(10, 36, 58, 0.68)',
    border: 'rgba(0, 161, 214, 0.28)',
    text: '#e8f8ff',
    textMuted: '#8ec8de',
    textOnHero: '#fff',
    shadow: '0 12px 40px rgba(0, 0, 0, 0.35)',
    heroGradient:
      'linear-gradient(120deg, #0c4a6e 0%, #0891b2 45%, #00a1d6 78%, #22d3ee 100%)',
    headerBg: 'rgba(6, 22, 38, 0.9)',
    headerBorder: 'rgba(0, 161, 214, 0.22)',
  },
  enterprise: {
    logoBgFrom: '#0d4d8a',
    logoBgTo: '#2094f3',
    logoStar: '#bfdbfe',
    logoAccent: '#60a5fa',
    logoShadow: 'rgba(32, 148, 243, 0.32)',
    particleBg: ['#071525', '#0c1f38', '#091a2e'],
    particleDot: 'rgba(120, 190, 255, 0.72)',
    particleDotActive: 'rgba(191, 219, 254, 0.95)',
    particleLine: ['rgba(147, 197, 253, 0.85)', 'rgba(32, 148, 243, 0.45)', 'rgba(32, 148, 243, 0.05)'],
    particleGlow: 'rgba(96, 165, 250, 0.55)',
    explosionColors: ['#2094f3', '#60a5fa', '#93c5fd', '#38bdf8', '#1d4ed8'],
    primary: '#2094f3',
    primaryDark: '#93c5fd',
    primarySoft: '#60a5fa',
    surface: 'rgba(6, 18, 36, 0.58)',
    surfaceStrong: 'rgba(8, 26, 50, 0.74)',
    surfaceCard: 'rgba(10, 32, 58, 0.68)',
    border: 'rgba(32, 148, 243, 0.28)',
    text: '#eff6ff',
    textMuted: '#93b8d8',
    textOnHero: '#fff',
    shadow: '0 12px 40px rgba(0, 0, 0, 0.35)',
    heroGradient:
      'linear-gradient(120deg, #0d4d8a 0%, #1565c0 42%, #2094f3 72%, #60a5fa 100%)',
    headerBg: 'rgba(6, 18, 36, 0.9)',
    headerBorder: 'rgba(32, 148, 243, 0.22)',
  },
  admin: {
    logoBgFrom: '#4c1d95',
    logoBgTo: '#7c3aed',
    logoStar: '#e9d5ff',
    logoAccent: '#c084fc',
    logoShadow: 'rgba(124, 58, 237, 0.32)',
    particleBg: ['#0d0a1f', '#15102e', '#110c24'],
    particleDot: 'rgba(180, 160, 255, 0.72)',
    particleDotActive: 'rgba(233, 213, 255, 0.95)',
    particleLine: ['rgba(216, 180, 254, 0.85)', 'rgba(124, 58, 237, 0.45)', 'rgba(124, 58, 237, 0.05)'],
    particleGlow: 'rgba(192, 132, 252, 0.55)',
    explosionColors: ['#7c3aed', '#a78bfa', '#c084fc', '#e879f9', '#f0abfc'],
    primary: '#7c3aed',
    primaryDark: '#d8b4fe',
    primarySoft: '#a78bfa',
    surface: 'rgba(14, 10, 32, 0.58)',
    surfaceStrong: 'rgba(20, 14, 42, 0.74)',
    surfaceCard: 'rgba(26, 18, 52, 0.68)',
    border: 'rgba(124, 58, 237, 0.28)',
    text: '#f5f0ff',
    textMuted: '#b8a8d8',
    textOnHero: '#fff',
    shadow: '0 12px 40px rgba(0, 0, 0, 0.35)',
    heroGradient:
      'linear-gradient(120deg, #2b1f4d 0%, #5b2c83 38%, #7c3aed 70%, #a78bfa 100%)',
    headerBg: 'rgba(14, 10, 32, 0.9)',
    headerBorder: 'rgba(124, 58, 237, 0.22)',
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
