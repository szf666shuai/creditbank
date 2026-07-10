import request from '@/utils/request'

export interface ResumeContent {
  realName: string
  phone: string
  email: string
  education: string
  workExperience: string
  skills: string
  selfIntro: string
  projects: string
}

export interface UserResumeSummary {
  id: number
  title: string
  realName?: string
  isDefault?: number
  version?: number
  updateTime?: string
}

export interface UserResume {
  id: number
  title: string
  content: ResumeContent
  fileUrl?: string
  isDefault?: number
  version?: number
  updateTime?: string
}

export interface SaveUserResumePayload {
  title: string
  content: ResumeContent
  fileUrl?: string
}

export function listResumesApi() {
  return request.get<UserResumeSummary[]>('/profile/resumes')
}

export function getResumeApi(id: number) {
  return request.get<UserResume>(`/profile/resumes/${id}`)
}

export function getDefaultResumeApi() {
  return request.get<UserResume>('/profile/resumes/default')
}

export function createResumeApi(data: SaveUserResumePayload) {
  return request.post<UserResume>('/profile/resumes', data)
}

export function updateResumeApi(id: number, data: SaveUserResumePayload) {
  return request.put<UserResume>(`/profile/resumes/${id}`, data)
}

export function deleteResumeApi(id: number) {
  return request.delete<void>(`/profile/resumes/${id}`)
}

export function setDefaultResumeApi(id: number) {
  return request.post<UserResume>(`/profile/resumes/${id}/default`)
}

export function createEmptyResumeContent(): ResumeContent {
  return {
    realName: '',
    phone: '',
    email: '',
    education: '',
    workExperience: '',
    skills: '',
    selfIntro: '',
    projects: '',
  }
}
