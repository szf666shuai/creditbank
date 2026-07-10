package com.creditbank.platform.module.message.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.message.dto.MessageRecipientVO;
import com.creditbank.platform.module.message.dto.MessageVO;
import com.creditbank.platform.module.message.dto.SendMessageRequest;
import com.creditbank.platform.module.message.service.MessageService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** 消息列表：box=inbox（默认）收件箱 | outbox 发件箱 */
    @GetMapping
    public Result<List<MessageVO>> listMessages(
            @RequestParam(defaultValue = "inbox") String box,
            @RequestParam(required = false) Integer isRead) {
        if ("outbox".equalsIgnoreCase(box)) {
            return Result.ok(messageService.listOutbox());
        }
        return Result.ok(messageService.listInbox(isRead));
    }

    @GetMapping("/inbox")
    public Result<List<MessageVO>> listInbox(@RequestParam(required = false) Integer isRead) {
        return Result.ok(messageService.listInbox(isRead));
    }

    @GetMapping("/outbox")
    public Result<List<MessageVO>> listOutbox() {
        return Result.ok(messageService.listOutbox());
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.ok(messageService.getUnreadCount());
    }

    @GetMapping("/recipients")
    public Result<List<MessageRecipientVO>> searchRecipients(@RequestParam String keyword) {
        return Result.ok(messageService.searchRecipients(keyword));
    }

    @GetMapping("/{id}")
    public Result<MessageVO> getMessage(@PathVariable Long id) {
        return Result.ok(messageService.getMessage(id));
    }

    @PostMapping
    public Result<MessageVO> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return Result.ok(messageService.sendMessage(request));
    }

    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.ok();
    }
}
