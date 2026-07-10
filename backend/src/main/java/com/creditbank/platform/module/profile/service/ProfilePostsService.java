package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.entity.ForumBoard;
import com.creditbank.platform.entity.ForumPost;
import com.creditbank.platform.entity.ForumReply;
import com.creditbank.platform.mapper.ForumBoardMapper;
import com.creditbank.platform.mapper.ForumPostMapper;
import com.creditbank.platform.mapper.ForumReplyMapper;
import com.creditbank.platform.module.profile.dto.MyForumPostVO;
import com.creditbank.platform.module.profile.dto.MyForumReplyVO;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfilePostsService {

    private final AuthSupport authSupport;
    private final ForumBoardMapper forumBoardMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumReplyMapper forumReplyMapper;

    public PageResult<MyForumPostVO> pageMyPosts(long page, long pageSize) {
        Long userId = authSupport.requireUserId();
        validatePage(page, pageSize);

        Page<ForumPost> result = forumPostMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<ForumPost>()
                        .eq(ForumPost::getUserId, userId)
                        .orderByDesc(ForumPost::getCreateTime));

        Map<Long, String> boardNameMap = loadBoardNameMap(
                result.getRecords().stream().map(ForumPost::getBoardId).distinct().toList());

        return PageResult.of(
                result.getRecords().stream().map(post -> toPostVO(post, boardNameMap.get(post.getBoardId()))).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    public PageResult<MyForumReplyVO> pageMyReplies(long page, long pageSize) {
        Long userId = authSupport.requireUserId();
        validatePage(page, pageSize);

        Page<ForumReply> result = forumReplyMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<ForumReply>()
                        .eq(ForumReply::getUserId, userId)
                        .orderByDesc(ForumReply::getCreateTime));

        Map<Long, String> postTitleMap = loadPostTitleMap(
                result.getRecords().stream().map(ForumReply::getPostId).distinct().toList());

        return PageResult.of(
                result.getRecords().stream().map(reply -> toReplyVO(reply, postTitleMap.get(reply.getPostId()))).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    @Transactional
    public void deleteMyPost(Long postId) {
        Long userId = authSupport.requireUserId();
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        authSupport.requireOwner(post.getUserId(), userId, "帖子");
        forumPostMapper.deleteById(postId);
    }

    @Transactional
    public void deleteMyReply(Long replyId) {
        Long userId = authSupport.requireUserId();
        ForumReply reply = forumReplyMapper.selectById(replyId);
        if (reply == null) {
            throw new BusinessException(404, "回复不存在");
        }
        authSupport.requireOwner(reply.getUserId(), userId, "回复");
        forumReplyMapper.deleteById(replyId);

        ForumPost post = forumPostMapper.selectById(reply.getPostId());
        if (post != null && post.getReplyCount() != null && post.getReplyCount() > 0) {
            post.setReplyCount(post.getReplyCount() - 1);
            forumPostMapper.updateById(post);
        }
    }

    private Map<Long, String> loadBoardNameMap(List<Long> boardIds) {
        if (boardIds.isEmpty()) {
            return Map.of();
        }
        return forumBoardMapper.selectList(new LambdaQueryWrapper<ForumBoard>().in(ForumBoard::getId, boardIds))
                .stream()
                .collect(Collectors.toMap(ForumBoard::getId, ForumBoard::getName));
    }

    private Map<Long, String> loadPostTitleMap(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        return forumPostMapper.selectList(new LambdaQueryWrapper<ForumPost>().in(ForumPost::getId, postIds))
                .stream()
                .collect(Collectors.toMap(ForumPost::getId, ForumPost::getTitle));
    }

    private MyForumPostVO toPostVO(ForumPost post, String boardName) {
        return MyForumPostVO.builder()
                .id(post.getId())
                .boardId(post.getBoardId())
                .boardName(boardName)
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .replyCount(post.getReplyCount())
                .likeCount(post.getLikeCount())
                .status(post.getStatus())
                .statusName(postStatusName(post.getStatus()))
                .createTime(post.getCreateTime())
                .build();
    }

    private MyForumReplyVO toReplyVO(ForumReply reply, String postTitle) {
        return MyForumReplyVO.builder()
                .id(reply.getId())
                .postId(reply.getPostId())
                .postTitle(postTitle)
                .parentId(reply.getParentId())
                .content(reply.getContent())
                .likeCount(reply.getLikeCount())
                .status(reply.getStatus())
                .statusName(replyStatusName(reply.getStatus()))
                .createTime(reply.getCreateTime())
                .build();
    }

    static String postStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "隐藏";
            case 1 -> "正常";
            case 2 -> "审核中";
            default -> "未知";
        };
    }

    static String replyStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "隐藏";
            case 1 -> "正常";
            default -> "未知";
        };
    }

    private void validatePage(long page, long pageSize) {
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "分页参数无效");
        }
    }
}
