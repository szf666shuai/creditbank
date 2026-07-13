export interface BannerSlide {
  id: string
  type: 'hero' | 'image'
  title?: string
  subtitle?: string
  image?: string
  alt?: string
  link?: string
}

import { BRAND_NAME, BRAND_SLOGAN } from '@/config/brand'

/** 首页 Banner 轮播 — 首张为统领图，其余为功能宣传图 */
export const homeBannerSlides: BannerSlide[] = [
  {
    id: 'hero',
    type: 'hero',
    title: BRAND_NAME,
    subtitle: BRAND_SLOGAN,
  },
  {
    id: 'learning-track',
    type: 'image',
    image: '/images/banner/slide-learning-track.png',
    alt: '个人学习记录追踪，助您科学学习',
    link: '/profile/learning',
  },
  {
    id: 'resources',
    type: 'image',
    image: '/images/banner/slide-resources.png',
    alt: '丰富学习资源，满足您的求知欲',
    link: '/resources',
  },
  {
    id: 'credit-mall',
    type: 'image',
    image: '/images/banner/slide-credit-mall.png',
    alt: '秩点商城，学习秩点换好礼',
    link: '/credit',
  },
  {
    id: 'forum',
    type: 'image',
    image: '/images/banner/slide-forum.png',
    alt: '资讯论坛，掌握一手消息互帮互助',
    link: '/forum',
  },
  {
    id: 'enterprise',
    type: 'image',
    image: '/images/banner/slide-enterprise.png',
    alt: '多家企业机构加盟，打破求职壁垒',
    link: '/enterprise',
  },
]

export const BANNER_AUTOPLAY_MS = 5000
