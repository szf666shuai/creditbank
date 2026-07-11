package com.creditbank.platform.module.forum.controller;

import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.forum.dto.CreateForumPostRequest;
import com.creditbank.platform.module.forum.dto.CreateForumReplyRequest;
import com.creditbank.platform.module.forum.dto.ForumBoardVO;
import com.creditbank.platform.module.forum.dto.ForumPostVO;
import com.creditbank.platform.module.forum.dto.ForumReplyVO;
import com.creditbank.platform.module.forum.dto.ReportForumRequest;
import com.creditbank.platform.module.forum.service.ForumService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @GetMapping("/boards")
    public Result<List<ForumBoardVO>> listBoards() {
        return Result.ok(forumService.listBoards());
    }

    @GetMapping("/posts")
    public Result<PageResult<ForumPostVO>> pagePosts(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) String keyword) {
        return Result.ok(forumService.pagePosts(page, pageSize, boardId, keyword));
    }

    @GetMapping("/posts/{id}")
    public Result<ForumPostVO> getPost(@PathVariable Long id) {
        return Result.ok(forumService.getPost(id));
    }

    @PostMapping("/posts")
    public Result<ForumPostVO> createPost(@RequestBody CreateForumPostRequest request) {
        return Result.ok(forumService.createPost(request));
    }

    @GetMapping("/posts/{id}/replies")
    public Result<PageResult<ForumReplyVO>> pageReplies(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return Result.ok(forumService.pageReplies(id, page, pageSize));
    }

    @PostMapping("/posts/{id}/replies")
    public Result<ForumReplyVO> createReply(
            @PathVariable Long id,
            @RequestBody CreateForumReplyRequest request) {
        return Result.ok(forumService.createReply(id, request));
    }

    @PostMapping("/likes/{targetType}/{targetId}")
    public Result<Boolean> toggleLike(@PathVariable Integer targetType, @PathVariable Long targetId) {
        return Result.ok(forumService.toggleLike(targetType, targetId));
    }

    @PostMapping("/reports/{targetType}/{targetId}")
    public Result<Void> report(
            @PathVariable Integer targetType,
            @PathVariable Long targetId,
            @RequestBody ReportForumRequest request) {
        forumService.report(targetType, targetId, request);
        return Result.ok();
    }
}
