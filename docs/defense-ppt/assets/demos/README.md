# 演示素材目录

把项目录屏 GIF / MP4 放这里，幻灯片里用 `data-demo-src` 引用。

## 推荐素材

| 文件名 | 对应页面 | 建议时长 |
|--------|----------|----------|
| `interview-trtc.gif` | 视频面试 | 15–20s |
| `course-player.gif` | 课程播放器 | 10–15s |
| `ai-assistant.gif` | AI 助手 | 10s |
| `forum.gif` | 论坛 | 10s |

## 用法（在 slide HTML 里）

```html
<button type="button" class="demo-play-btn" data-demo-open
  data-demo-title="视频面试全流程"
  data-demo-src="../assets/demos/interview-trtc.gif">
  <span class="play-icon">▶</span>
  观看操作演示
</button>
```

MP4 视频：

```html
data-demo-src="../assets/demos/foo.mp4"
data-demo-type="video"
```

## 设计说明

- **纸面页**保持浅色编辑风（讲内容）
- **弹层里的深色 screen 框**放项目 UI（和真实界面一致，不破坏 grammar）
- 没素材时会显示占位 mock，不影响答辩排练
