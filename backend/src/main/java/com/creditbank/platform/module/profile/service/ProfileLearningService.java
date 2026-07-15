package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.entity.LearningAchievement;
import com.creditbank.platform.entity.LearningArchive;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.mapper.LearningAchievementMapper;
import com.creditbank.platform.mapper.LearningArchiveMapper;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.module.profile.dto.LearningAchievementVO;
import com.creditbank.platform.module.profile.dto.LearningArchiveVO;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileLearningService {

    private final AuthSupport authSupport;
    private final SysOrganizationMapper orgMapper;
    private final LearningArchiveMapper learningArchiveMapper;
    private final LearningAchievementMapper learningAchievementMapper;

    public List<LearningArchiveVO> listMyArchives() {
        Long userId = authSupport.requireUserId();
        List<LearningArchive> archives = learningArchiveMapper.selectList(
                new LambdaQueryWrapper<LearningArchive>()
                        .eq(LearningArchive::getUserId, userId)
                        .orderByDesc(LearningArchive::getStartDate)
                        .orderByDesc(LearningArchive::getCreateTime));
        return archives.stream().map(this::toArchiveVO).toList();
    }

    public List<LearningAchievementVO> listMyAchievements() {
        Long userId = authSupport.requireUserId();
        List<LearningAchievement> achievements = learningAchievementMapper.selectList(
                new LambdaQueryWrapper<LearningAchievement>()
                        .eq(LearningAchievement::getUserId, userId)
                        .orderByDesc(LearningAchievement::getCreateTime));
        if (achievements.isEmpty()) {
            return List.of();
        }

        List<Long> orgIds = achievements.stream()
                .map(LearningAchievement::getOrgId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        // HashMap 允许 get(null)；Map.of() 在 key 为 null 时会 NPE
        Map<Long, String> orgNameMap = new HashMap<>();
        if (!orgIds.isEmpty()) {
            orgMapper.selectList(new LambdaQueryWrapper<SysOrganization>().in(SysOrganization::getId, orgIds))
                    .forEach(org -> {
                        if (org.getId() != null && org.getName() != null) {
                            orgNameMap.put(org.getId(), org.getName());
                        }
                    });
        }

        return achievements.stream()
                .map(item -> toAchievementVO(item, item.getOrgId() == null ? null : orgNameMap.get(item.getOrgId())))
                .toList();
    }

    private LearningArchiveVO toArchiveVO(LearningArchive archive) {
        return LearningArchiveVO.builder()
                .id(archive.getId())
                .title(archive.getTitle())
                .archiveType(archive.getArchiveType())
                .archiveTypeName(archiveTypeName(archive.getArchiveType()))
                .courseId(archive.getCourseId())
                .certificateId(archive.getCertificateId())
                .category(archive.getCategory())
                .description(archive.getDescription())
                .startDate(archive.getStartDate())
                .endDate(archive.getEndDate())
                .creditEarned(archive.getCreditEarned())
                .status(archive.getStatus())
                .statusName(archiveStatusName(archive.getStatus()))
                .createTime(archive.getCreateTime())
                .build();
    }

    private LearningAchievementVO toAchievementVO(LearningAchievement achievement, String orgName) {
        return LearningAchievementVO.builder()
                .id(achievement.getId())
                .title(achievement.getTitle())
                .type(achievement.getType())
                .typeName(achievementTypeName(achievement.getType()))
                .orgId(achievement.getOrgId())
                .orgName(orgName)
                .certificateId(achievement.getCertificateId())
                .creditValue(achievement.getCreditValue())
                .fileUrl(achievement.getFileUrl())
                .tags(achievement.getTags())
                .verifyStatus(achievement.getVerifyStatus())
                .verifyStatusName(verifyStatusName(achievement.getVerifyStatus()))
                .blockchainHash(achievement.getBlockchainHash())
                .createTime(achievement.getCreateTime())
                .build();
    }

    static String archiveTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case 1 -> "课程";
            case 2 -> "活动";
            case 3 -> "成果";
            case 4 -> "其他";
            default -> "未知";
        };
    }

    static String archiveStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "进行中";
            case 1 -> "已完成";
            default -> "未知";
        };
    }

    static String achievementTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case 1 -> "证书";
            case 2 -> "课程";
            case 3 -> "项目";
            case 4 -> "其他";
            default -> "未知";
        };
    }

    static String verifyStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待校验";
            case 1 -> "已通过";
            case 2 -> "未通过";
            default -> "未知";
        };
    }
}
