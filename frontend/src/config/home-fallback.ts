import type { HomeData } from '@/api/home'

/** 接口不可用时的占位数据，保证首页区块结构可见 */
export const homeFallbackData: HomeData = {
  courses: [
    {
      type: 'course',
      typeName: '课程',
      id: 0,
      title: 'Java 程序设计基础',
      summary: '系统学习 Java 语法与面向对象',
      coverUrl: 'https://picsum.photos/seed/java-course-1/400/250',
      extra: '20 学分',
    },
    {
      type: 'course',
      typeName: '课程',
      id: 0,
      title: 'Spring Boot 微服务实战',
      summary: '构建 REST API 与微服务',
      coverUrl: 'https://picsum.photos/seed/java-course-2/400/250',
      extra: '35 学分',
    },
  ],
  hotProducts: [
    {
      type: 'credit',
      typeName: '商品',
      id: 0,
      title: 'Java 电子书兑换券',
      coverUrl: 'https://picsum.photos/seed/java-mall-1/400/250',
      extra: '8 学分',
    },
  ],
  hotActivities: [
    {
      type: 'activity',
      typeName: '活动',
      id: 0,
      title: 'Java 技术沙龙',
      extra: '示例科技集团报告厅',
      createTime: '2026-08-15T14:00:00',
    },
  ],
  microMajors: [
    {
      type: 'course',
      typeName: '微专业',
      id: 0,
      title: 'Java 后端开发微专业',
      summary: '面向就业的系统化学习路径',
      coverUrl: 'https://picsum.photos/seed/java-course-2/400/250',
      extra: '32 学时',
    },
  ],
  hotNews: [
    {
      type: 'news',
      typeName: '资讯',
      id: 0,
      title: '关于编程类课程学分认定指引',
      extra: '学分银行管委会',
      createTime: '2026-07-01T10:00:00',
    },
  ],
  jobs: [
    {
      type: 'job',
      typeName: '招聘',
      id: 0,
      title: 'Java 后端开发工程师',
      extra: '深圳 · 12K-20K',
    },
  ],
  forumPosts: [
    {
      type: 'forum',
      typeName: '论坛',
      id: 0,
      title: 'Java 学习路线求助',
      summary: '请问有没有推荐的入门课程？',
      extra: '0 回复',
    },
  ],
  partners: [
    { type: 'partner', typeName: '合作单位', id: 1, title: '示例大学', extra: 'ORG001' },
    { type: 'partner', typeName: '合作单位', id: 2, title: '示例科技集团', extra: 'ORG002' },
    { type: 'partner', typeName: '合作单位', id: 3, title: '示例培训机构', extra: 'ORG003' },
  ],
}

export function normalizeHomeData(raw?: Partial<HomeData> | null): HomeData {
  return {
    courses: raw?.courses ?? [],
    hotProducts: raw?.hotProducts ?? [],
    hotActivities: raw?.hotActivities ?? [],
    microMajors: raw?.microMajors ?? [],
    hotNews: raw?.hotNews ?? [],
    jobs: raw?.jobs ?? [],
    forumPosts: raw?.forumPosts ?? [],
    partners: raw?.partners ?? [],
  }
}
