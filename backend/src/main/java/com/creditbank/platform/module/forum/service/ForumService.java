package com.creditbank.platform.module.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.entity.ForumBoard;
import com.creditbank.platform.entity.ForumLike;
import com.creditbank.platform.entity.ForumPost;
import com.creditbank.platform.entity.ForumReply;
import com.creditbank.platform.entity.ForumReport;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.ForumBoardMapper;
import com.creditbank.platform.mapper.ForumLikeMapper;
import com.creditbank.platform.mapper.ForumPostMapper;
import com.creditbank.platform.mapper.ForumReplyMapper;
import com.creditbank.platform.mapper.ForumReportMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.forum.dto.CreateForumPostRequest;
import com.creditbank.platform.module.forum.dto.CreateForumReplyRequest;
import com.creditbank.platform.module.forum.dto.ForumBoardVO;
import com.creditbank.platform.module.forum.dto.ForumPostVO;
import com.creditbank.platform.module.forum.dto.ForumReplyVO;
import com.creditbank.platform.module.forum.dto.ReportForumRequest;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumService {

    private static final int TARGET_POST = 1;
    private static final int TARGET_REPLY = 2;
    private static final int NORMAL = 1;

    private final AuthSupport authSupport;
    private final ForumBoardMapper forumBoardMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumReplyMapper forumReplyMapper;
    private final ForumLikeMapper forumLikeMapper;
    private final ForumReportMapper forumReportMapper;
    private final SysUserMapper sysUserMapper;

    public List<ForumBoardVO> listBoards() {
        List<ForumBoard> boards = forumBoardMapper.selectList(new LambdaQueryWrapper<ForumBoard>()
                .eq(ForumBoard::getStatus, NORMAL)
                .orderByAsc(ForumBoard::getSortOrder)
                .orderByAsc(ForumBoard::getId));

        return boards.stream().map(board -> ForumBoardVO.builder()
                .id(board.getId())
                .name(board.getName())
                .description(board.getDescription())
                .icon(board.getIcon())
                .sortOrder(board.getSortOrder())
                .postCount(forumPostMapper.selectCount(new LambdaQueryWrapper<ForumPost>()
                        .eq(ForumPost::getBoardId, board.getId())
                        .eq(ForumPost::getStatus, NORMAL)))
                .build()).toList();
    }

    public PageResult<ForumPostVO> pagePosts(long page, long pageSize, Long boardId, String keyword) {
        validatePage(page, pageSize);
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, NORMAL)
                .eq(boardId != null, ForumPost::getBoardId, boardId)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(ForumPost::getTitle, keyword)
                        .or()
                        .like(ForumPost::getContent, keyword))
                .orderByDesc(ForumPost::getIsTop)
                .orderByDesc(ForumPost::getCreateTime);

        Page<ForumPost> result = forumPostMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<Long, String> boardNames = loadBoardNames(result.getRecords().stream()
                .map(ForumPost::getBoardId)
                .distinct()
                .toList());
        Map<Long, String> userNames = loadUserNames(result.getRecords().stream()
                .map(ForumPost::getUserId)
                .distinct()
                .toList());
        Long currentUserId = UserContext.getUserId();

        return PageResult.of(
                result.getRecords().stream()
                        .map(post -> toPostVO(post, boardNames.get(post.getBoardId()), userNames.get(post.getUserId()), currentUserId))
                        .toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    @Transactional
    public ForumPostVO getPost(Long id) {
        ForumPost post = forumPostMapper.selectOne(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getId, id)
                .eq(ForumPost::getStatus, NORMAL));
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        post.setViewCount(defaultCount(post.getViewCount()) + 1);
        forumPostMapper.updateById(post);
        String boardName = loadBoardNames(List.of(post.getBoardId())).get(post.getBoardId());
        String userName = loadUserNames(List.of(post.getUserId())).get(post.getUserId());
        return toPostVO(post, boardName, userName, UserContext.getUserId());
    }

    public PageResult<ForumReplyVO> pageReplies(Long postId, long page, long pageSize) {
        validatePage(page, pageSize);
        ensurePostExists(postId);
        Page<ForumReply> result = forumReplyMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<ForumReply>()
                        .eq(ForumReply::getPostId, postId)
                        .eq(ForumReply::getStatus, NORMAL)
                        .orderByAsc(ForumReply::getCreateTime));
        List<Long> authorIds = result.getRecords().stream()
                .map(ForumReply::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> parentIds = result.getRecords().stream()
                .map(ForumReply::getParentId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        Map<Long, ForumReply> parentMap = parentIds.isEmpty()
                ? Map.of()
                : forumReplyMapper.selectList(new LambdaQueryWrapper<ForumReply>().in(ForumReply::getId, parentIds))
                .stream()
                .collect(Collectors.toMap(ForumReply::getId, r -> r, (a, b) -> a));
        parentMap.values().stream()
                .map(ForumReply::getUserId)
                .filter(id -> id != null && !authorIds.contains(id))
                .forEach(authorIds::add);
        Map<Long, String> userNames = loadUserNames(authorIds);
        Long currentUserId = UserContext.getUserId();

        return PageResult.of(
                result.getRecords().stream()
                        .map(reply -> {
                            String parentAuthor = null;
                            if (reply.getParentId() != null && reply.getParentId() > 0) {
                                ForumReply parent = parentMap.get(reply.getParentId());
                                if (parent != null) {
                                    parentAuthor = displayName(userNames.get(parent.getUserId()), parent.getUserId());
                                }
                            }
                            return toReplyVO(
                                    reply,
                                    userNames.get(reply.getUserId()),
                                    parentAuthor,
                                    currentUserId);
                        })
                        .toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    @Transactional
    public ForumPostVO createPost(CreateForumPostRequest request) {
        Long userId = authSupport.requireStudent().getId();
        validateText(request.getTitle(), "标题", 2, 100);
        validateText(request.getContent(), "正文", 5, 5000);
        ForumBoard board = forumBoardMapper.selectOne(new LambdaQueryWrapper<ForumBoard>()
                .eq(ForumBoard::getId, request.getBoardId())
                .eq(ForumBoard::getStatus, NORMAL));
        if (board == null) {
            throw new BusinessException(404, "板块不存在");
        }

        ForumPost post = new ForumPost();
        post.setBoardId(board.getId());
        post.setUserId(userId);
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setViewCount(0);
        post.setReplyCount(0);
        post.setLikeCount(0);
        post.setIsTop(0);
        post.setStatus(NORMAL);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        post.setDeleted(0);
        forumPostMapper.insert(post);
        return toPostVO(post, board.getName(), authSupport.requireLoginUser().getRealName(), userId);
    }

    @Transactional
    public ForumReplyVO createReply(Long postId, CreateForumReplyRequest request) {
        Long userId = authSupport.requireStudent().getId();
        validateText(request.getContent(), "回复内容", 2, 1000);
        ForumPost post = ensurePostExists(postId);
        Long parentId = request.getParentId() == null ? 0L : request.getParentId();
        if (parentId > 0) {
            ForumReply parent = forumReplyMapper.selectOne(new LambdaQueryWrapper<ForumReply>()
                    .eq(ForumReply::getId, parentId)
                    .eq(ForumReply::getPostId, postId)
                    .eq(ForumReply::getStatus, NORMAL));
            if (parent == null) {
                throw new BusinessException(404, "父回复不存在");
            }
        }

        ForumReply reply = new ForumReply();
        reply.setPostId(postId);
        reply.setUserId(userId);
        reply.setParentId(parentId);
        reply.setContent(request.getContent().trim());
        reply.setLikeCount(0);
        reply.setStatus(NORMAL);
        reply.setCreateTime(LocalDateTime.now());
        reply.setDeleted(0);
        forumReplyMapper.insert(reply);

        post.setReplyCount(defaultCount(post.getReplyCount()) + 1);
        forumPostMapper.updateById(post);

        String parentAuthor = null;
        if (parentId > 0) {
            ForumReply parent = forumReplyMapper.selectById(parentId);
            if (parent != null) {
                parentAuthor = loadUserNames(List.of(parent.getUserId())).get(parent.getUserId());
                parentAuthor = displayName(parentAuthor, parent.getUserId());
            }
        }
        return toReplyVO(reply, authSupport.requireLoginUser().getRealName(), parentAuthor, userId);
    }

    @Transactional
    public Boolean toggleLike(Integer targetType, Long targetId) {
        Long userId = authSupport.requireUserId();
        if (targetType == null || (targetType != TARGET_POST && targetType != TARGET_REPLY)) {
            throw new BusinessException(400, "点赞对象无效");
        }
        ensureTargetExists(targetType, targetId);
        ForumLike existing = forumLikeMapper.selectOne(new LambdaQueryWrapper<ForumLike>()
                .eq(ForumLike::getUserId, userId)
                .eq(ForumLike::getTargetType, targetType)
                .eq(ForumLike::getTargetId, targetId));
        if (existing != null) {
            forumLikeMapper.deleteById(existing.getId());
            adjustLikeCount(targetType, targetId, -1);
            return false;
        }
        ForumLike like = new ForumLike();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        like.setCreateTime(LocalDateTime.now());
        forumLikeMapper.insert(like);
        adjustLikeCount(targetType, targetId, 1);
        return true;
    }

    @Transactional
    public void report(Integer targetType, Long targetId, ReportForumRequest request) {
        Long userId = authSupport.requireUserId();
        if (targetType == null || (targetType != TARGET_POST && targetType != TARGET_REPLY)) {
            throw new BusinessException(400, "举报对象无效");
        }
        ensureTargetExists(targetType, targetId);
        validateText(request.getReason(), "举报原因", 2, 255);
        ForumReport report = new ForumReport();
        report.setUserId(userId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(request.getReason().trim());
        report.setStatus(0);
        report.setCreateTime(LocalDateTime.now());
        forumReportMapper.insert(report);
    }

    private ForumPost ensurePostExists(Long postId) {
        ForumPost post = forumPostMapper.selectOne(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getId, postId)
                .eq(ForumPost::getStatus, NORMAL));
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        return post;
    }

    private void ensureTargetExists(Integer targetType, Long targetId) {
        if (targetType == TARGET_POST) {
            ensurePostExists(targetId);
            return;
        }
        ForumReply reply = forumReplyMapper.selectOne(new LambdaQueryWrapper<ForumReply>()
                .eq(ForumReply::getId, targetId)
                .eq(ForumReply::getStatus, NORMAL));
        if (reply == null) {
            throw new BusinessException(404, "回复不存在");
        }
    }

    private void adjustLikeCount(Integer targetType, Long targetId, int delta) {
        if (targetType == TARGET_POST) {
            ForumPost post = forumPostMapper.selectById(targetId);
            post.setLikeCount(Math.max(0, defaultCount(post.getLikeCount()) + delta));
            forumPostMapper.updateById(post);
            return;
        }
        ForumReply reply = forumReplyMapper.selectById(targetId);
        reply.setLikeCount(Math.max(0, defaultCount(reply.getLikeCount()) + delta));
        forumReplyMapper.updateById(reply);
    }

    private ForumPostVO toPostVO(ForumPost post, String boardName, String authorName, Long currentUserId) {
        return ForumPostVO.builder()
                .id(post.getId())
                .boardId(post.getBoardId())
                .boardName(boardName)
                .userId(post.getUserId())
                .authorName(displayName(authorName, post.getUserId()))
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(defaultCount(post.getViewCount()))
                .replyCount(defaultCount(post.getReplyCount()))
                .likeCount(defaultCount(post.getLikeCount()))
                .isTop(post.getIsTop())
                .status(post.getStatus())
                .statusName("正常")
                .liked(isLiked(currentUserId, TARGET_POST, post.getId()))
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .build();
    }

    private ForumReplyVO toReplyVO(ForumReply reply, String authorName, String parentAuthorName, Long currentUserId) {
        return ForumReplyVO.builder()
                .id(reply.getId())
                .postId(reply.getPostId())
                .userId(reply.getUserId())
                .authorName(displayName(authorName, reply.getUserId()))
                .parentId(reply.getParentId())
                .parentAuthorName(parentAuthorName)
                .content(reply.getContent())
                .likeCount(defaultCount(reply.getLikeCount()))
                .status(reply.getStatus())
                .statusName("正常")
                .liked(isLiked(currentUserId, TARGET_REPLY, reply.getId()))
                .createTime(reply.getCreateTime())
                .build();
    }

    private boolean isLiked(Long userId, Integer targetType, Long targetId) {
        if (userId == null) {
            return false;
        }
        return forumLikeMapper.selectCount(new LambdaQueryWrapper<ForumLike>()
                .eq(ForumLike::getUserId, userId)
                .eq(ForumLike::getTargetType, targetType)
                .eq(ForumLike::getTargetId, targetId)) > 0;
    }

    private Map<Long, String> loadBoardNames(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return forumBoardMapper.selectList(new LambdaQueryWrapper<ForumBoard>().in(ForumBoard::getId, ids))
                .stream()
                .collect(Collectors.toMap(ForumBoard::getId, ForumBoard::getName));
    }

    private Map<Long, String> loadUserNames(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, ids))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, user -> {
                    if (StringUtils.hasText(user.getRealName())) {
                        return user.getRealName();
                    }
                    return user.getUsername();
                }));
    }

    private String displayName(String name, Long userId) {
        if (StringUtils.hasText(name)) {
            return name;
        }
        return "用户" + userId;
    }

    private int defaultCount(Integer value) {
        return value == null ? 0 : value;
    }

    private void validatePage(long page, long pageSize) {
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "分页参数无效");
        }
    }

    private void validateText(String value, String label, int min, int max) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, label + "不能为空");
        }
        int length = value.trim().length();
        if (length < min || length > max) {
            throw new BusinessException(400, label + "长度需在 " + min + "-" + max + " 个字符之间");
        }
    }
}
