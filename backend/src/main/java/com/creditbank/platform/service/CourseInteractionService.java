package com.creditbank.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.dto.CourseCommentCreateRequest;
import com.creditbank.platform.dto.CourseCommentVO;
import com.creditbank.platform.dto.CourseDanmakuCreateRequest;
import com.creditbank.platform.dto.CourseDanmakuVO;
import com.creditbank.platform.dto.CourseMaterialVO;
import com.creditbank.platform.entity.Course;
import com.creditbank.platform.entity.CourseComment;
import com.creditbank.platform.entity.CourseDanmaku;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.CourseCommentMapper;
import com.creditbank.platform.mapper.CourseDanmakuMapper;
import com.creditbank.platform.mapper.CourseMapper;
import com.creditbank.platform.mapper.CourseMaterialMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseInteractionService {

    public static final List<String> DANMAKU_COLORS = List.of(
            "#ffffff", "#fb7299", "#6bcbff", "#ffd93d", "#00d1b2", "#c9a0ff"
    );

    private final CourseMapper courseMapper;
    private final CourseCommentMapper commentMapper;
    private final CourseDanmakuMapper danmakuMapper;
    private final CourseMaterialMapper materialMapper;
    private final SysUserMapper sysUserMapper;

    public List<CourseCommentVO> listComments(Long courseId, int limit) {
        ensureCourseExists(courseId);
        List<CourseCommentVO> flat = commentMapper.listByCourseId(courseId, Math.min(Math.max(limit, 1), 200));
        return buildCommentTree(flat);
    }

    @Transactional
    public CourseCommentVO createComment(Long userId, Long courseId, CourseCommentCreateRequest request) {
        ensureCourseExists(courseId);
        SysUser user = requireUser(userId);
        CourseComment parent = null;
        if (request.getParentId() != null) {
            parent = commentMapper.selectOne(
                    new LambdaQueryWrapper<CourseComment>()
                            .eq(CourseComment::getId, request.getParentId())
                            .eq(CourseComment::getCourseId, courseId)
                            .eq(CourseComment::getDeleted, 0)
            );
            if (parent == null) {
                throw new BusinessException(404, "被回复的评论不存在");
            }
        }
        CourseComment comment = new CourseComment();
        comment.setCourseId(courseId);
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent().trim());
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        CourseCommentVO vo = toCommentVO(comment, user);
        if (parent != null) {
            SysUser parentUser = sysUserMapper.selectById(parent.getUserId());
            if (parentUser != null) {
                vo.setReplyToAuthorName(displayName(parentUser));
            }
        }
        return vo;
    }

    public List<CourseDanmakuVO> listDanmaku(Long courseId) {
        ensureCourseExists(courseId);
        return danmakuMapper.listByCourseId(courseId);
    }

    @Transactional
    public CourseDanmakuVO createDanmaku(Long userId, Long courseId, CourseDanmakuCreateRequest request) {
        ensureCourseExists(courseId);
        SysUser user = requireUser(userId);
        CourseDanmaku danmaku = new CourseDanmaku();
        danmaku.setCourseId(courseId);
        danmaku.setUserId(userId);
        danmaku.setContent(request.getContent().trim());
        danmaku.setVideoTimeSeconds(request.getVideoTimeSeconds() == null
                ? BigDecimal.ZERO
                : request.getVideoTimeSeconds());
        danmaku.setColor(resolveDanmakuColor(request.getColor()));
        danmaku.setCreateTime(LocalDateTime.now());
        danmakuMapper.insert(danmaku);
        return toDanmakuVO(danmaku, user);
    }

    public List<CourseMaterialVO> listMaterials(Long courseId) {
        ensureCourseExists(courseId);
        return materialMapper.listByCourseId(courseId);
    }

    private List<CourseCommentVO> buildCommentTree(List<CourseCommentVO> flat) {
        Map<Long, CourseCommentVO> index = new HashMap<>();
        List<CourseCommentVO> roots = new ArrayList<>();
        for (CourseCommentVO item : flat) {
            index.put(item.getId(), item);
        }
        for (CourseCommentVO item : flat) {
            if (item.getParentId() == null) {
                roots.add(item);
                continue;
            }
            CourseCommentVO parent = index.get(item.getParentId());
            if (parent == null) {
                roots.add(item);
                continue;
            }
            item.setReplyToAuthorName(parent.getAuthorName());
            parent.getReplies().add(item);
        }
        roots.sort((left, right) -> right.getCreateTime().compareTo(left.getCreateTime()));
        for (CourseCommentVO root : roots) {
            root.getReplies().sort((left, right) -> left.getCreateTime().compareTo(right.getCreateTime()));
        }
        return roots;
    }

    private void ensureCourseExists(Long courseId) {
        Course course = courseMapper.selectOne(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getId, courseId)
                        .eq(Course::getDeleted, 0)
                        .eq(Course::getStatus, 1)
        );
        if (course == null) {
            throw new BusinessException(404, "课程不存在或已下架");
        }
    }

    private SysUser requireUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private String resolveDanmakuColor(String color) {
        if (!StringUtils.hasText(color)) {
            return DANMAKU_COLORS.get((int) (Math.random() * DANMAKU_COLORS.size()));
        }
        return DANMAKU_COLORS.contains(color) ? color : DANMAKU_COLORS.get(0);
    }

    private CourseCommentVO toCommentVO(CourseComment comment, SysUser user) {
        CourseCommentVO vo = new CourseCommentVO();
        vo.setId(comment.getId());
        vo.setCourseId(comment.getCourseId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setAuthorName(displayName(user));
        vo.setAvatar(user.getAvatar());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    private CourseDanmakuVO toDanmakuVO(CourseDanmaku danmaku, SysUser user) {
        CourseDanmakuVO vo = new CourseDanmakuVO();
        vo.setId(danmaku.getId());
        vo.setCourseId(danmaku.getCourseId());
        vo.setUserId(danmaku.getUserId());
        vo.setAuthorName(displayName(user));
        vo.setContent(danmaku.getContent());
        vo.setVideoTimeSeconds(danmaku.getVideoTimeSeconds());
        vo.setColor(danmaku.getColor());
        vo.setCreateTime(danmaku.getCreateTime());
        return vo;
    }

    private String displayName(SysUser user) {
        return StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername();
    }
}
