package com.creditbank.platform.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.entity.ForumPost;
import com.creditbank.platform.entity.ForumReply;
import com.creditbank.platform.entity.ForumReport;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.ForumPostMapper;
import com.creditbank.platform.mapper.ForumReplyMapper;
import com.creditbank.platform.mapper.ForumReportMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.admin.dto.AdminForumReportVO;
import com.creditbank.platform.module.admin.dto.HandleForumReportRequest;
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
public class AdminReportService {

    private final AuthSupport authSupport;
    private final ForumReportMapper forumReportMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumReplyMapper forumReplyMapper;
    private final SysUserMapper sysUserMapper;

    public PageResult<AdminForumReportVO> pageReports(long page, long pageSize, Integer status) {
        authSupport.requireAdmin();
        AdminSupport.validatePage(page, pageSize);

        Page<ForumReport> result = forumReportMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<ForumReport>()
                        .eq(status != null, ForumReport::getStatus, status)
                        .orderByDesc(ForumReport::getCreateTime));

        Map<Long, String> userNameMap = loadUserNameMap(result.getRecords());
        return PageResult.of(
                result.getRecords().stream().map(report -> toVO(report, userNameMap)).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    @Transactional
    public AdminForumReportVO handleReport(Long reportId, HandleForumReportRequest request) {
        authSupport.requireAdmin();
        ForumReport report = forumReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(404, "举报记录不存在");
        }
        if (report.getStatus() != null && report.getStatus() != AdminSupport.REPORT_PENDING) {
            throw new BusinessException(400, "该举报已处理");
        }
        if (request.getStatus() != AdminSupport.REPORT_HANDLED
                && request.getStatus() != AdminSupport.REPORT_REJECTED) {
            throw new BusinessException(400, "处理状态无效");
        }

        report.setStatus(request.getStatus());
        report.setHandleRemark(StringUtils.hasText(request.getHandleRemark())
                ? request.getHandleRemark().trim()
                : null);
        forumReportMapper.updateById(report);

        if (request.getStatus() == AdminSupport.REPORT_HANDLED
                && Boolean.TRUE.equals(request.getHideTarget())) {
            hideTarget(report.getTargetType(), report.getTargetId());
        }

        Map<Long, String> userNameMap = loadUserNameMap(List.of(report));
        return toVO(report, userNameMap);
    }

    private void hideTarget(Integer targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return;
        }
        if (targetType == AdminSupport.TARGET_POST) {
            ForumPost post = forumPostMapper.selectById(targetId);
            if (post != null) {
                post.setStatus(0);
                forumPostMapper.updateById(post);
            }
            return;
        }
        if (targetType == AdminSupport.TARGET_REPLY) {
            ForumReply reply = forumReplyMapper.selectById(targetId);
            if (reply != null) {
                reply.setStatus(0);
                forumReplyMapper.updateById(reply);
            }
        }
    }

    private Map<Long, String> loadUserNameMap(List<ForumReport> reports) {
        List<Long> userIds = reports.stream()
                .map(ForumReport::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds))
                .stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        user -> StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername()));
    }

    private AdminForumReportVO toVO(ForumReport report, Map<Long, String> userNameMap) {
        return AdminForumReportVO.builder()
                .id(report.getId())
                .userId(report.getUserId())
                .reporterName(userNameMap.get(report.getUserId()))
                .targetType(report.getTargetType())
                .targetTypeName(AdminSupport.targetTypeName(report.getTargetType()))
                .targetId(report.getTargetId())
                .targetSummary(loadTargetSummary(report.getTargetType(), report.getTargetId()))
                .reason(report.getReason())
                .status(report.getStatus())
                .statusName(AdminSupport.reportStatusName(report.getStatus()))
                .handleRemark(report.getHandleRemark())
                .createTime(report.getCreateTime())
                .build();
    }

    private String loadTargetSummary(Integer targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return null;
        }
        if (targetType == AdminSupport.TARGET_POST) {
            ForumPost post = forumPostMapper.selectById(targetId);
            return post != null ? post.getTitle() : "帖子已删除";
        }
        if (targetType == AdminSupport.TARGET_REPLY) {
            ForumReply reply = forumReplyMapper.selectById(targetId);
            if (reply == null) {
                return "回复已删除";
            }
            String content = reply.getContent();
            if (!StringUtils.hasText(content)) {
                return "回复内容";
            }
            return content.length() > 60 ? content.substring(0, 60) + "…" : content;
        }
        return null;
    }
}
