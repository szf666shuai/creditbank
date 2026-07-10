package com.creditbank.platform.module.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.constant.UserRole;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.message.dto.MessageRecipientVO;
import com.creditbank.platform.module.message.dto.MessageVO;
import com.creditbank.platform.module.message.dto.SendMessageRequest;
import com.creditbank.platform.module.message.entity.UserMessage;
import com.creditbank.platform.module.message.mapper.UserMessageMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final int MSG_TYPE_NORMAL = 1;
    private static final int MSG_TYPE_INTERVIEW = 2;
    private static final int MSG_TYPE_ACTIVITY = 3;
    private static final int UNREAD = 0;
    private static final int READ = 1;
    private static final int MIN_RECIPIENT_KEYWORD_LEN = 2;

    private final AuthSupport authSupport;
    private final UserMessageMapper userMessageMapper;
    private final SysUserMapper sysUserMapper;

    public List<MessageVO> listInbox(Integer readStatus) {
        SysUser user = authSupport.requireLoginUser();
        Long userId = user.getId();
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getToUserId, userId)
                .eq(readStatus != null, UserMessage::getReadStatus, readStatus)
                .orderByDesc(UserMessage::getCreateTime);
        applyRoleMessageScope(wrapper, user);
        return toVOList(userMessageMapper.selectList(wrapper));
    }

    public List<MessageVO> listOutbox() {
        SysUser user = authSupport.requireLoginUser();
        Long userId = user.getId();
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getFromUserId, userId)
                .orderByDesc(UserMessage::getCreateTime);
        applyRoleMessageScope(wrapper, user);
        return toVOList(userMessageMapper.selectList(wrapper));
    }

    public long getUnreadCount() {
        SysUser user = authSupport.requireLoginUser();
        Long userId = user.getId();
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getToUserId, userId)
                .eq(UserMessage::getReadStatus, UNREAD);
        applyRoleMessageScope(wrapper, user);
        Long count = userMessageMapper.selectCount(wrapper);
        return count != null ? count : 0;
    }

    public List<MessageRecipientVO> searchRecipients(String keyword) {
        Long userId = authSupport.requireUserId();
        if (!StringUtils.hasText(keyword) || keyword.trim().length() < MIN_RECIPIENT_KEYWORD_LEN) {
            return List.of();
        }
        String kw = keyword.trim();
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .ne(SysUser::getId, userId)
                .and(w -> w.like(SysUser::getUsername, kw).or().like(SysUser::getRealName, kw))
                .and(w -> w.isNull(SysUser::getStatus).or().ne(SysUser::getStatus, 0))
                .last("LIMIT 20"));
        return users.stream().map(user -> MessageRecipientVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .displayName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername())
                .role(user.getRole())
                .roleName(UserRole.getName(user.getRole() != null ? user.getRole() : UserRole.STUDENT))
                .build()).toList();
    }

    @Transactional
    public MessageVO getMessage(Long messageId) {
        Long userId = authSupport.requireUserId();
        SysUser user = authSupport.requireLoginUser();
        UserMessage message = authSupport.requireAccessibleMessage(messageId, userId);
        assertVisibleInMessageCenter(user, message);

        if (Objects.equals(message.getToUserId(), userId) && message.getReadStatus() == UNREAD) {
            message.setReadStatus(READ);
            userMessageMapper.updateById(message);
        }

        return toVO(message, loadUserNameMap(message));
    }

    @Transactional
    public MessageVO sendMessage(SendMessageRequest request) {
        Long userId = authSupport.requireUserId();
        if (Objects.equals(userId, request.getReceiverId())) {
            throw new BusinessException(400, "不能给自己发送私信");
        }

        SysUser receiver = sysUserMapper.selectById(request.getReceiverId());
        if (receiver == null) {
            throw new BusinessException(404, "接收人不存在");
        }

        UserMessage message = new UserMessage();
        message.setFromUserId(userId);
        message.setToUserId(request.getReceiverId());
        message.setMsgType(MSG_TYPE_NORMAL);
        message.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : "无标题");
        message.setContent(request.getContent().trim());
        message.setReadStatus(UNREAD);
        userMessageMapper.insert(message);

        UserMessage saved = userMessageMapper.selectById(message.getId());
        return toVO(saved, loadUserNameMap(saved));
    }

    @Transactional
    public void markRead(Long messageId) {
        Long userId = authSupport.requireUserId();
        SysUser user = authSupport.requireLoginUser();
        UserMessage message = authSupport.requireMessageReceiver(messageId, userId);
        assertVisibleInMessageCenter(user, message);
        if (message.getReadStatus() == READ) {
            return;
        }
        message.setReadStatus(READ);
        userMessageMapper.updateById(message);
    }

    /**
     * 消息中心仅展示私信/系统通知；面试/活动邀请在各自专属页面处理。
     */
    private void applyRoleMessageScope(LambdaQueryWrapper<UserMessage> wrapper, SysUser user) {
        wrapper.and(w -> w.isNull(UserMessage::getMsgType)
                .or()
                .notIn(UserMessage::getMsgType, MSG_TYPE_INTERVIEW, MSG_TYPE_ACTIVITY));
    }

    private void assertVisibleInMessageCenter(SysUser user, UserMessage message) {
        if (isInviteMessage(message)) {
            throw new BusinessException(404, "消息不存在");
        }
    }

    private boolean isInviteMessage(UserMessage message) {
        Integer msgType = message.getMsgType();
        return msgType != null && (msgType == MSG_TYPE_INTERVIEW || msgType == MSG_TYPE_ACTIVITY);
    }

    private List<MessageVO> toVOList(List<UserMessage> messages) {
        if (messages.isEmpty()) {
            return List.of();
        }
        Map<Long, String> nameMap = loadUserNameMap(messages);
        return messages.stream().map(msg -> toVO(msg, nameMap)).toList();
    }

    private Map<Long, String> loadUserNameMap(UserMessage message) {
        return loadUserNameMap(List.of(message));
    }

    private Map<Long, String> loadUserNameMap(List<UserMessage> messages) {
        List<Long> userIds = messages.stream()
                .flatMap(msg -> java.util.stream.Stream.of(msg.getFromUserId(), msg.getToUserId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds)).stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        user -> StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername()
                ));
    }

    private MessageVO toVO(UserMessage message, Map<Long, String> nameMap) {
        return MessageVO.builder()
                .id(message.getId())
                .fromUserId(message.getFromUserId())
                .fromUserName(nameMap.get(message.getFromUserId()))
                .toUserId(message.getToUserId())
                .toUserName(nameMap.get(message.getToUserId()))
                .msgType(message.getMsgType())
                .title(message.getTitle())
                .content(message.getContent())
                .readStatus(message.getReadStatus())
                .refType(message.getRefType())
                .refId(message.getRefId())
                .createTime(message.getCreateTime())
                .build();
    }
}
