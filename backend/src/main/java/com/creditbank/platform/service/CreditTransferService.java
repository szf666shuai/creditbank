package com.creditbank.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.dto.CreditTransferApplyRequest;
import com.creditbank.platform.dto.CreditTransferApplicationVO;
import com.creditbank.platform.dto.CreditTransferRuleVO;
import com.creditbank.platform.entity.Course;
import com.creditbank.platform.entity.CreditTransferApplication;
import com.creditbank.platform.entity.CreditTransferRule;
import com.creditbank.platform.entity.LearningAchievement;
import com.creditbank.platform.entity.LearningArchive;
import com.creditbank.platform.entity.LearningCertificate;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.CourseMapper;
import com.creditbank.platform.mapper.CreditTransferApplicationMapper;
import com.creditbank.platform.mapper.CreditTransferRuleMapper;
import com.creditbank.platform.mapper.LearningAchievementMapper;
import com.creditbank.platform.mapper.LearningArchiveMapper;
import com.creditbank.platform.mapper.LearningCertificateMapper;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditTransferService {

    private final AuthSupport authSupport;
    private final CreditTransferRuleMapper ruleMapper;
    private final CreditTransferApplicationMapper applicationMapper;
    private final CourseMapper courseMapper;
    private final LearningAchievementMapper achievementMapper;
    private final SysOrganizationMapper sysOrgMapper;
    private final SysUserMapper sysUserMapper;
    private final LearningArchiveMapper archiveMapper;
    private final LearningCertificateMapper certificateMapper;
    private final CreditService creditService;

    public List<CreditTransferRuleVO> listRules(Long orgId) {
        LambdaQueryWrapper<CreditTransferRule> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null) {
            wrapper.eq(CreditTransferRule::getOrgId, orgId);
        }
        wrapper.eq(CreditTransferRule::getStatus, 1);
        wrapper.orderByDesc(CreditTransferRule::getCreateTime);
        return ruleMapper.selectList(wrapper).stream()
                .map(this::toRuleVO)
                .toList();
    }

    public CreditTransferRuleVO getRule(Long ruleId) {
        CreditTransferRule rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException(404, "转换规则不存在");
        }
        return toRuleVO(rule);
    }

    @Transactional
    public CreditTransferRuleVO createRule(CreditTransferRule rule) {
        SysUser user = authSupport.requireEnterpriseWritable();
        rule.setOrgId(user.getOrgId());
        if (rule.getSourceType() == null) rule.setSourceType(1);
        if (rule.getTargetType() == null) rule.setTargetType(1);
        rule.setStatus(1);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        ruleMapper.insert(rule);
        return toRuleVO(rule);
    }

    @Transactional
    public CreditTransferRuleVO updateRule(Long ruleId, CreditTransferRule rule) {
        SysUser user = authSupport.requireEnterpriseWritable();
        CreditTransferRule existing = requireOwnedRule(ruleId, user.getOrgId());
        existing.setSourceType(rule.getSourceType());
        existing.setSourceCourseId(rule.getSourceCourseId());
        existing.setSourceTags(rule.getSourceTags());
        existing.setTargetType(rule.getTargetType());
        existing.setTargetCourseId(rule.getTargetCourseId());
        existing.setTargetCertificateId(rule.getTargetCertificateId());
        existing.setTargetAchievementId(rule.getTargetAchievementId());
        existing.setTargetOrgId(rule.getTargetOrgId());
        existing.setCreditRatio(rule.getCreditRatio());
        existing.setDescription(rule.getDescription());
        existing.setStatus(rule.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        ruleMapper.updateById(existing);
        return toRuleVO(existing);
    }

    @Transactional
    public void deleteRule(Long ruleId) {
        SysUser user = authSupport.requireEnterpriseWritable();
        CreditTransferRule existing = requireOwnedRule(ruleId, user.getOrgId());
        existing.setStatus(0);
        ruleMapper.updateById(existing);
    }

    @Transactional
    public CreditTransferApplicationVO apply(CreditTransferApplyRequest request) {
        SysUser user = authSupport.requireStudent();

        Integer sourceType = request.getSourceType() != null ? request.getSourceType() : 1;
        BigDecimal sourceCredit = BigDecimal.ZERO;
        Long sourceOrgId = null;
        String sourceTitle = "";

        if (sourceType == 1) {
            if (request.getSourceCourseId() == null) {
                throw new BusinessException(400, "源课程ID不能为空");
            }
            Course sourceCourse = courseMapper.selectById(request.getSourceCourseId());
            if (sourceCourse == null || sourceCourse.getStatus() == null || sourceCourse.getStatus() != 1) {
                throw new BusinessException(404, "源课程不存在或已下架");
            }
            LearningArchive archive = archiveMapper.selectOne(
                    new LambdaQueryWrapper<LearningArchive>()
                            .eq(LearningArchive::getUserId, user.getId())
                            .eq(LearningArchive::getCourseId, request.getSourceCourseId())
                            .eq(LearningArchive::getStatus, 1)
                            .last("LIMIT 1")
            );
            if (archive == null) {
                throw new BusinessException(400, "您尚未完成该课程的学习，无法申请转换");
            }
            if (archive.getTransferStatus() != null && archive.getTransferStatus() == 2) {
                throw new BusinessException(400, "该课程学分已转换过，无法重复申请");
            }
            sourceCredit = archive.getCreditEarned() != null ? archive.getCreditEarned() : BigDecimal.ZERO;
            sourceOrgId = sourceCourse.getOrgId();
            sourceTitle = sourceCourse.getTitle();
        } else if (sourceType == 2) {
            if (request.getSourceAchievementId() == null) {
                throw new BusinessException(400, "源成果ID不能为空");
            }
            LearningAchievement achievement = achievementMapper.selectById(request.getSourceAchievementId());
            if (achievement == null) {
                throw new BusinessException(404, "学习成果不存在");
            }
            if (!achievement.getUserId().equals(user.getId())) {
                throw new BusinessException(403, "无权操作该学习成果");
            }
            if (achievement.getVerifyStatus() == null || achievement.getVerifyStatus() != 1) {
                throw new BusinessException(400, "该学习成果尚未通过校验，无法申请转换");
            }
            sourceCredit = achievement.getCreditValue() != null ? achievement.getCreditValue() : BigDecimal.ZERO;
            sourceOrgId = achievement.getOrgId();
            sourceTitle = achievement.getTitle();
        } else {
            throw new BusinessException(400, "无效的源类型");
        }

        Integer targetType = request.getTargetType() != null ? request.getTargetType() : 1;

        CreditTransferApplication application = new CreditTransferApplication();
        application.setUserId(user.getId());
        application.setSourceType(sourceType);
        application.setSourceCourseId(request.getSourceCourseId());
        application.setSourceAchievementId(request.getSourceAchievementId());
        application.setSourceOrgId(sourceOrgId);
        application.setSourceCredit(sourceCredit);
        application.setTargetType(targetType);
        application.setTargetCourseId(request.getTargetCourseId());
        application.setTargetCertificateId(request.getTargetCertificateId());
        application.setTargetAchievementId(request.getTargetAchievementId());
        application.setTargetOrgId(request.getTargetOrgId());
        application.setApplyReason(request.getApplyReason());
        application.setStatus(0);
        application.setApplyTime(LocalDateTime.now());
        applicationMapper.insert(application);

        if (sourceType == 1 && request.getSourceCourseId() != null) {
            LearningArchive archive = archiveMapper.selectOne(
                    new LambdaQueryWrapper<LearningArchive>()
                            .eq(LearningArchive::getUserId, user.getId())
                            .eq(LearningArchive::getCourseId, request.getSourceCourseId())
                            .last("LIMIT 1")
            );
            if (archive != null) {
                archive.setTransferStatus(1);
                archive.setTransferApplicationId(application.getId());
                archiveMapper.updateById(archive);
            }
        }

        return toApplicationVO(application);
    }

    public List<CreditTransferApplicationVO> listApplications(Long orgId, Integer status) {
        LambdaQueryWrapper<CreditTransferApplication> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null) {
            wrapper.eq(CreditTransferApplication::getTargetOrgId, orgId);
        }
        if (status != null) {
            wrapper.eq(CreditTransferApplication::getStatus, status);
        }
        wrapper.orderByDesc(CreditTransferApplication::getApplyTime);
        return applicationMapper.selectList(wrapper).stream()
                .map(this::toApplicationVO)
                .toList();
    }

    public List<CreditTransferApplicationVO> listMyApplications(Long userId) {
        LambdaQueryWrapper<CreditTransferApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditTransferApplication::getUserId, userId);
        wrapper.orderByDesc(CreditTransferApplication::getApplyTime);
        return applicationMapper.selectList(wrapper).stream()
                .map(this::toApplicationVO)
                .toList();
    }

    @Transactional
    public CreditTransferApplicationVO review(Long applicationId, Integer status, String comment) {
        SysUser user = authSupport.requireEnterpriseWritable();
        CreditTransferApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(404, "转换申请不存在");
        }
        if (!user.getOrgId().equals(application.getTargetOrgId())) {
            throw new BusinessException(403, "无权审核该申请");
        }
        if (application.getStatus() != null && application.getStatus() != 0) {
            throw new BusinessException(400, "该申请已处理过");
        }

        application.setStatus(status);
        application.setReviewerId(user.getId());
        application.setReviewComment(comment);
        application.setReviewTime(LocalDateTime.now());

        if (status == 1) {
            BigDecimal actualCredit = application.getSourceCredit();
            Integer targetType = application.getTargetType() != null ? application.getTargetType() : 1;

            if (targetType == 1 && application.getTargetCourseId() != null) {
                Course targetCourse = courseMapper.selectById(application.getTargetCourseId());
                if (targetCourse != null && targetCourse.getCreditValue() != null) {
                    actualCredit = targetCourse.getCreditValue();
                }
            }
            application.setActualCredit(actualCredit);

            grantTransferResult(application);

            if (application.getSourceType() != null && application.getSourceType() == 1
                    && application.getSourceCourseId() != null) {
                LearningArchive archive = archiveMapper.selectOne(
                        new LambdaQueryWrapper<LearningArchive>()
                                .eq(LearningArchive::getUserId, application.getUserId())
                                .eq(LearningArchive::getCourseId, application.getSourceCourseId())
                                .last("LIMIT 1")
                );
                if (archive != null) {
                    archive.setTransferStatus(2);
                    archive.setTransferredToOrgId(application.getTargetOrgId());
                    archive.setTransferredToCourseId(application.getTargetCourseId());
                    archive.setTransferredToCertificateId(application.getTargetCertificateId());
                    archiveMapper.updateById(archive);
                }
            }

            creditService.earnDirect(application.getUserId(), actualCredit,
                    "credit_transfer", sourceTypeText(application.getSourceType()),
                    application.getTargetCourseId() != null ? application.getTargetCourseId()
                            : (application.getTargetAchievementId() != null ? application.getTargetAchievementId() : 0L),
                    "学分转换: " + getTargetDisplay(application));
        }

        applicationMapper.updateById(application);
        return toApplicationVO(application);
    }

    private void grantTransferResult(CreditTransferApplication application) {
        Integer targetType = application.getTargetType() != null ? application.getTargetType() : 1;

        if (targetType == 1 && application.getTargetCourseId() != null) {
            Course targetCourse = courseMapper.selectById(application.getTargetCourseId());
            if (targetCourse == null) return;

            LearningCertificate existingCert = certificateMapper.selectOne(
                    new LambdaQueryWrapper<LearningCertificate>()
                            .eq(LearningCertificate::getUserId, application.getUserId())
                            .eq(LearningCertificate::getCourseId, application.getTargetCourseId())
                            .last("LIMIT 1")
            );
            if (existingCert != null) return;

            LearningCertificate cert = new LearningCertificate();
            cert.setCertNo("LC" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%04d", application.getUserId() % 10000)
                    + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase());
            cert.setUserId(application.getUserId());
            cert.setCourseId(application.getTargetCourseId());
            cert.setTitle(targetCourse.getTitle() + " 学分转换证书");
            cert.setQrContent("/api/learning/certificates/verify?certNo=" + cert.getCertNo());
            cert.setBlockchainHash(sha256(cert.getCertNo() + "|" + application.getUserId() + "|" + application.getTargetCourseId()));
            cert.setVerifyStatus(1);
            cert.setIssuedAt(LocalDateTime.now());
            certificateMapper.insert(cert);

            LearningArchive archive = new LearningArchive();
            archive.setUserId(application.getUserId());
            archive.setTitle(targetCourse.getTitle());
            archive.setArchiveType(1);
            archive.setCourseId(application.getTargetCourseId());
            archive.setCertificateId(cert.getId());
            archive.setCategory("学分转换");
            archive.setDescription("通过学分转换获得: " + cert.getCertNo());
            archive.setStartDate(java.time.LocalDate.now());
            archive.setEndDate(java.time.LocalDate.now());
            archive.setCreditEarned(application.getActualCredit());
            archive.setStatus(1);
            archiveMapper.insert(archive);
        } else if (targetType == 2) {
            String title = getTargetDisplay(application);
            LearningAchievement achievement = new LearningAchievement();
            achievement.setUserId(application.getUserId());
            achievement.setTitle(title + " 转换成果");
            achievement.setType(1);
            achievement.setOrgId(application.getTargetOrgId());
            achievement.setCreditValue(application.getActualCredit());
            achievement.setVerifyStatus(1);
            achievement.setTags("");
            achievement.setCreateTime(LocalDateTime.now());
            achievement.setUpdateTime(LocalDateTime.now());
            achievementMapper.insert(achievement);

            LearningArchive archive = new LearningArchive();
            archive.setUserId(application.getUserId());
            archive.setTitle(title + " 转换成果");
            archive.setArchiveType(3);
            archive.setCategory("学分转换");
            archive.setDescription("通过学分转换获得的学习成果");
            archive.setStartDate(java.time.LocalDate.now());
            archive.setEndDate(java.time.LocalDate.now());
            archive.setCreditEarned(application.getActualCredit());
            archive.setStatus(1);
            archiveMapper.insert(archive);
        }
    }

    public List<CreditTransferRuleVO> matchRules(Integer sourceType, Long sourceCourseId, Long sourceAchievementId) {
        if (sourceType == null) sourceType = 1;

        String sourceTags = "";
        Long sourceOrgId = null;

        if (sourceType == 1) {
            if (sourceCourseId == null) return List.of();
            Course sourceCourse = courseMapper.selectById(sourceCourseId);
            if (sourceCourse == null) return List.of();
            sourceTags = sourceCourse.getTags();
            sourceOrgId = sourceCourse.getOrgId();
        } else if (sourceType == 2) {
            if (sourceAchievementId == null) return List.of();
            LearningAchievement achievement = achievementMapper.selectById(sourceAchievementId);
            if (achievement == null) return List.of();
            sourceTags = achievement.getTags();
            sourceOrgId = achievement.getOrgId();
        } else {
            return List.of();
        }

        List<CreditTransferRule> rules = ruleMapper.selectList(
                new LambdaQueryWrapper<CreditTransferRule>()
                        .eq(CreditTransferRule::getStatus, 1)
                        .eq(CreditTransferRule::getSourceType, sourceType)
        );

        List<CreditTransferRuleVO> matched = new ArrayList<>();
        if (StringUtils.hasText(sourceTags)) {
            String[] sourceTagArray = sourceTags.split(",");
            for (CreditTransferRule rule : rules) {
                if (sourceOrgId != null && sourceOrgId.equals(rule.getOrgId())) {
                    continue;
                }
                String ruleTags = rule.getSourceTags();
                if (StringUtils.hasText(ruleTags)) {
                    for (String tag : sourceTagArray) {
                        if (ruleTags.contains(tag.trim())) {
                            matched.add(toRuleVO(rule));
                            break;
                        }
                    }
                }
            }
        }

        return matched;
    }

    private CreditTransferRule requireOwnedRule(Long ruleId, Long orgId) {
        CreditTransferRule rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException(404, "转换规则不存在");
        }
        if (!orgId.equals(rule.getOrgId())) {
            throw new BusinessException(403, "无权操作该规则");
        }
        return rule;
    }

    private CreditTransferRuleVO toRuleVO(CreditTransferRule rule) {
        CreditTransferRuleVO vo = new CreditTransferRuleVO();
        vo.setId(rule.getId());
        vo.setOrgId(rule.getOrgId());
        vo.setOrgName(getOrgName(rule.getOrgId()));
        vo.setSourceType(rule.getSourceType() != null ? rule.getSourceType() : 1);
        vo.setSourceTypeName(sourceTypeText(rule.getSourceType()));
        vo.setSourceCourseId(rule.getSourceCourseId());
        vo.setSourceCourseName(getCourseName(rule.getSourceCourseId()));
        vo.setSourceTags(rule.getSourceTags());
        vo.setTargetType(rule.getTargetType() != null ? rule.getTargetType() : 1);
        vo.setTargetTypeName(targetTypeText(rule.getTargetType()));
        vo.setTargetCourseId(rule.getTargetCourseId());
        vo.setTargetCourseName(getCourseName(rule.getTargetCourseId()));
        vo.setTargetCertificateId(rule.getTargetCertificateId());
        vo.setTargetAchievementId(rule.getTargetAchievementId());
        vo.setTargetOrgId(rule.getTargetOrgId());
        vo.setCreditRatio(rule.getCreditRatio());
        vo.setDescription(rule.getDescription());
        vo.setStatus(rule.getStatus());
        vo.setStatusName(rule.getStatus() != null && rule.getStatus() == 1 ? "启用" : "停用");
        vo.setCreateTime(rule.getCreateTime());
        vo.setUpdateTime(rule.getUpdateTime());
        return vo;
    }

    private CreditTransferApplicationVO toApplicationVO(CreditTransferApplication application) {
        CreditTransferApplicationVO vo = new CreditTransferApplicationVO();
        vo.setId(application.getId());
        vo.setUserId(application.getUserId());
        vo.setUserName(getUserName(application.getUserId()));
        vo.setSourceType(application.getSourceType() != null ? application.getSourceType() : 1);
        vo.setSourceTypeName(sourceTypeText(application.getSourceType()));
        vo.setSourceCourseId(application.getSourceCourseId());
        vo.setSourceCourseName(getCourseName(application.getSourceCourseId()));
        vo.setSourceAchievementId(application.getSourceAchievementId());
        vo.setSourceAchievementTitle(getAchievementTitle(application.getSourceAchievementId()));
        vo.setSourceOrgId(application.getSourceOrgId());
        vo.setSourceOrgName(getOrgName(application.getSourceOrgId()));
        vo.setSourceCredit(application.getSourceCredit());
        vo.setTargetType(application.getTargetType() != null ? application.getTargetType() : 1);
        vo.setTargetTypeName(targetTypeText(application.getTargetType()));
        vo.setTargetCourseId(application.getTargetCourseId());
        vo.setTargetCourseName(getCourseName(application.getTargetCourseId()));
        vo.setTargetCertificateId(application.getTargetCertificateId());
        vo.setTargetAchievementId(application.getTargetAchievementId());
        vo.setTargetAchievementTitle(getAchievementTitle(application.getTargetAchievementId()));
        vo.setTargetOrgId(application.getTargetOrgId());
        vo.setTargetOrgName(getOrgName(application.getTargetOrgId()));
        vo.setApplyReason(application.getApplyReason());
        vo.setStatus(application.getStatus());
        vo.setStatusName(applicationStatusName(application.getStatus()));
        vo.setReviewerId(application.getReviewerId());
        vo.setReviewerName(getUserName(application.getReviewerId()));
        vo.setReviewComment(application.getReviewComment());
        vo.setActualCredit(application.getActualCredit());
        vo.setApplyTime(application.getApplyTime());
        vo.setReviewTime(application.getReviewTime());
        return vo;
    }

    private String getTargetDisplay(CreditTransferApplication application) {
        Integer targetType = application.getTargetType() != null ? application.getTargetType() : 1;
        if (targetType == 1 && application.getTargetCourseId() != null) {
            return getCourseName(application.getTargetCourseId());
        } else if (targetType == 2) {
            if (application.getTargetAchievementId() != null) {
                return getAchievementTitle(application.getTargetAchievementId());
            }
            return "学习成果";
        }
        return "未知目标";
    }

    private String getOrgName(Long orgId) {
        if (orgId == null) return null;
        SysOrganization org = sysOrgMapper.selectById(orgId);
        return org == null ? null : org.getName();
    }

    private String getCourseName(Long courseId) {
        if (courseId == null) return null;
        Course course = courseMapper.selectById(courseId);
        return course == null ? null : course.getTitle();
    }

    private String getAchievementTitle(Long achievementId) {
        if (achievementId == null) return null;
        LearningAchievement achievement = achievementMapper.selectById(achievementId);
        return achievement == null ? null : achievement.getTitle();
    }

    private String getUserName(Long userId) {
        if (userId == null) return null;
        SysUser user = sysUserMapper.selectById(userId);
        return user == null ? null : user.getNickname();
    }

    private String sourceTypeText(Integer type) {
        if (type == null) return "课程";
        return type == 2 ? "学习成果" : "课程";
    }

    private String targetTypeText(Integer type) {
        if (type == null) return "课程";
        return type == 2 ? "成果/证书" : "课程";
    }

    private String sourceTypeTextObj(Integer type) {
        return sourceTypeText(type);
    }

    private String applicationStatusName(Integer status) {
        if (status == null) return "待审核";
        return switch (status) {
            case 1 -> "已通过";
            case 2 -> "已驳回";
            default -> "待审核";
        };
    }

    private String sha256(String value) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new BusinessException("生成证书存证哈希失败");
        }
    }
}
