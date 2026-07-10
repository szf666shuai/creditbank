package com.creditbank.platform.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.constant.UserRole;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.admin.dto.SendSystemNotificationRequest;
import com.creditbank.platform.module.message.entity.UserMessage;
import com.creditbank.platform.module.message.mapper.UserMessageMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private static final int UNREAD = 0;

    private final AuthSupport authSupport;
    private final SysUserMapper sysUserMapper;
    private final UserMessageMapper userMessageMapper;

    @Transactional
    public int sendSystemNotification(SendSystemNotificationRequest request) {
        Long adminId = authSupport.requireAdmin().getId();
        String title = request.getTitle().trim();
        String content = request.getContent().trim();
        String scope = request.getScope().trim().toLowerCase();

        List<SysUser> recipients = switch (scope) {
            case "all" -> listActiveUsers(null);
            case "role" -> {
                if (request.getTargetRole() == null) {
                    throw new BusinessException(400, "请指定目标角色");
                }
                if (request.getTargetRole() != UserRole.STUDENT
                        && request.getTargetRole() != UserRole.ENTERPRISE
                        && request.getTargetRole() != UserRole.ADMIN) {
                    throw new BusinessException(400, "目标角色无效");
                }
                yield listActiveUsers(request.getTargetRole());
            }
            case "user" -> {
                if (request.getTargetUserId() == null) {
                    throw new BusinessException(400, "请指定目标用户");
                }
                SysUser user = sysUserMapper.selectById(request.getTargetUserId());
                if (user == null) {
                    throw new BusinessException(404, "目标用户不存在");
                }
                if (Objects.equals(user.getStatus(), 0)) {
                    throw new BusinessException(400, "目标用户已被禁用");
                }
                yield List.of(user);
            }
            default -> throw new BusinessException(400, "发送范围无效，可选 all / role / user");
        };

        if (recipients.isEmpty()) {
            return 0;
        }

        for (SysUser recipient : recipients) {
            if (Objects.equals(recipient.getId(), adminId)) {
                continue;
            }
            UserMessage message = new UserMessage();
            message.setFromUserId(adminId);
            message.setToUserId(recipient.getId());
            message.setMsgType(AdminSupport.MSG_TYPE_SYSTEM);
            message.setTitle(StringUtils.hasText(title) ? title : "系统通知");
            message.setContent(content);
            message.setRefType("system");
            message.setReadStatus(UNREAD);
            userMessageMapper.insert(message);
        }
        return (int) recipients.stream().filter(u -> !Objects.equals(u.getId(), adminId)).count();
    }

    private List<SysUser> listActiveUsers(Integer role) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .and(w -> w.isNull(SysUser::getStatus).or().eq(SysUser::getStatus, 1));
        if (role != null) {
            wrapper.eq(SysUser::getRole, role);
        }
        return sysUserMapper.selectList(wrapper);
    }
}
