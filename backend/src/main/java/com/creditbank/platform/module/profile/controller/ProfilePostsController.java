package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.profile.dto.MyForumPostVO;
import com.creditbank.platform.module.profile.dto.MyForumReplyVO;
import com.creditbank.platform.module.profile.service.ProfilePostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile/posts")
@RequiredArgsConstructor
public class ProfilePostsController {

    private final ProfilePostsService profilePostsService;

    @GetMapping
    public Result<PageResult<MyForumPostVO>> pageMyPosts(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(profilePostsService.pageMyPosts(page, pageSize));
    }

    @GetMapping("/replies")
    public Result<PageResult<MyForumReplyVO>> pageMyReplies(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(profilePostsService.pageMyReplies(page, pageSize));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMyPost(@PathVariable Long id) {
        profilePostsService.deleteMyPost(id);
        return Result.ok();
    }

    @DeleteMapping("/replies/{id}")
    public Result<Void> deleteMyReply(@PathVariable Long id) {
        profilePostsService.deleteMyReply(id);
        return Result.ok();
    }
}
