import request from '@/utils/request'
import type { SearchItem } from '@/api/search'

export interface HomeData {
  courses: SearchItem[]
  hotProducts: SearchItem[]
  hotActivities: SearchItem[]
  microMajors: SearchItem[]
  hotNews: SearchItem[]
  jobs: SearchItem[]
  forumPosts: SearchItem[]
  partners: SearchItem[]
}

export function fetchHomeData() {
  return request.get<HomeData>('/home')
}
