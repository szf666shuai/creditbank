package com.creditbank.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.dto.CreditTransferAiScreenResult;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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
    private final LlmService llmService;

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

    public List<CreditTransferRuleVO> listEnabledRules(Long orgId) {
        return listRules(orgId);
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
        // 机构只配置「本机构接收目标」；源课程适配由人工/AI 审核判断
        rule.setSourceType(1);
        rule.setSourceTags(null);
        rule.setSourceCourseId(null);
        if (rule.getTargetType() == null) rule.setTargetType(1);
        if (rule.getTargetOrgId() == null) rule.setTargetOrgId(user.getOrgId());
        if (rule.getCreditRatio() == null) rule.setCreditRatio(BigDecimal.ONE);
        assertUniqueTargetCourse(user.getOrgId(), rule.getTargetType(), rule.getTargetCourseId(), null);
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
        existing.setSourceType(1);
        existing.setSourceCourseId(null);
        existing.setSourceTags(null);
        existing.setTargetType(rule.getTargetType() != null ? rule.getTargetType() : 1);
        existing.setTargetCourseId(rule.getTargetCourseId());
        existing.setTargetCertificateId(rule.getTargetCertificateId());
        existing.setTargetAchievementId(rule.getTargetAchievementId());
        existing.setTargetOrgId(rule.getTargetOrgId() != null ? rule.getTargetOrgId() : user.getOrgId());
        existing.setCreditRatio(rule.getCreditRatio());
        existing.setDescription(rule.getDescription());
        Integer nextStatus = rule.getStatus() != null ? rule.getStatus() : existing.getStatus();
        existing.setStatus(nextStatus);
        if (nextStatus != null && nextStatus == 1) {
            assertUniqueTargetCourse(user.getOrgId(), existing.getTargetType(), existing.getTargetCourseId(), ruleId);
        }
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

        // 提交时同步 AI 初筛（失败不影响申请落库）
        try {
            runAiScreenAndPersist(application);
        } catch (Exception ignored) {
            // ignore
        }

        return toApplicationVO(applicationMapper.selectById(application.getId()));
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

    /**
     * 学员可选接收规则：返回其他机构已启用的转入规则（不再按标签自动过滤）。
     * 是否与源课程等价，由机构人工审核或 AI 初筛判断。
     */
    public List<CreditTransferRuleVO> matchRules(Integer sourceType, Long sourceCourseId, Long sourceAchievementId) {
        Long sourceOrgId = null;
        if (sourceType == null) sourceType = 1;

        if (sourceType == 1 && sourceCourseId != null) {
            Course sourceCourse = courseMapper.selectById(sourceCourseId);
            if (sourceCourse != null) {
                sourceOrgId = sourceCourse.getOrgId();
            }
        } else if (sourceType == 2 && sourceAchievementId != null) {
            LearningAchievement achievement = achievementMapper.selectById(sourceAchievementId);
            if (achievement != null) {
                sourceOrgId = achievement.getOrgId();
            }
        }

        LambdaQueryWrapper<CreditTransferRule> wrapper = new LambdaQueryWrapper<CreditTransferRule>()
                .eq(CreditTransferRule::getStatus, 1)
                .orderByDesc(CreditTransferRule::getUpdateTime);
        if (sourceOrgId != null) {
            wrapper.ne(CreditTransferRule::getOrgId, sourceOrgId);
        }

        return ruleMapper.selectList(wrapper).stream()
                .map(rule -> {
                    CreditTransferRuleVO vo = toRuleVO(rule);
                    if (vo.getTargetOrgId() == null) {
                        vo.setTargetOrgId(rule.getOrgId());
                    }
                    return vo;
                })
                .toList();
    }

    /**
     * AI 初筛（企业可手动复看；正常流程在学员提交申请时已自动写入）。
     */
    public CreditTransferAiScreenResult aiScreen(Long applicationId) {
        SysUser user = authSupport.requireEnterprise();
        CreditTransferApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(404, "转换申请不存在");
        }
        if (!user.getOrgId().equals(application.getTargetOrgId())) {
            throw new BusinessException(403, "无权查看该申请");
        }
        return runAiScreenAndPersist(application);
    }

    private CreditTransferAiScreenResult runAiScreenAndPersist(CreditTransferApplication application) {
        CreditTransferAiScreenResult result = evaluateAiScreen(application);
        application.setAiSuggestion(result.getSuggestion());
        application.setAiReason(result.getReason());
        application.setAiLlmUsed(result.isLlmUsed() ? 1 : 0);
        application.setAiScreenTime(LocalDateTime.now());
        applicationMapper.updateById(application);
        return result;
    }

    private CreditTransferAiScreenResult evaluateAiScreen(CreditTransferApplication application) {
        CreditTransferApplicationVO vo = toApplicationVO(application);
        String sourceName = vo.getSourceCourseName() != null ? vo.getSourceCourseName() : vo.getSourceAchievementTitle();
        String targetName = vo.getTargetCourseName() != null ? vo.getTargetCourseName() : vo.getTargetAchievementTitle();
        String sourceOrg = vo.getSourceOrgName() != null ? vo.getSourceOrgName() : "-";
        String reason = vo.getApplyReason() != null ? vo.getApplyReason() : "-";
        BigDecimal sourceCredit = vo.getSourceCredit() != null ? vo.getSourceCredit() : BigDecimal.ZERO;

        String ruleDescription = "-";
        LambdaQueryWrapper<CreditTransferRule> ruleQuery = new LambdaQueryWrapper<CreditTransferRule>()
                .eq(CreditTransferRule::getOrgId, application.getTargetOrgId())
                .eq(CreditTransferRule::getStatus, 1)
                .orderByDesc(CreditTransferRule::getUpdateTime)
                .last("LIMIT 1");
        if (application.getTargetType() != null && application.getTargetType() == 1
                && application.getTargetCourseId() != null) {
            ruleQuery.eq(CreditTransferRule::getTargetCourseId, application.getTargetCourseId());
        } else if (application.getTargetType() != null && application.getTargetType() == 2) {
            ruleQuery.eq(CreditTransferRule::getTargetType, 2);
            if (application.getTargetAchievementId() != null) {
                ruleQuery.eq(CreditTransferRule::getTargetAchievementId, application.getTargetAchievementId());
            }
        }
        CreditTransferRule matchedRule = ruleMapper.selectOne(ruleQuery);
        if (matchedRule != null && matchedRule.getDescription() != null) {
            ruleDescription = matchedRule.getDescription();
        }

        String prompt = """
                你是学分银行平台的学分互认初筛助手。请以「机构规则说明」为主要判断依据，对照学员源课程/成果与申请理由，判断是否建议机构通过该转入申请。
                只输出一行 JSON，不要 Markdown，格式严格为：
                {"suggestion":"approve|reject|uncertain","reason":"不超过80字的中文理由"}

                机构规则说明（最重要）：
                %s

                申请信息：
                - 源类型：%s
                - 源名称：%s
                - 源机构：%s
                - 源学分：%s
                - 目标类型：%s
                - 目标名称：%s
                - 学员理由：%s
                """.formatted(
                ruleDescription,
                vo.getSourceTypeName(),
                sourceName != null ? sourceName : "-",
                sourceOrg,
                sourceCredit.toPlainString(),
                vo.getTargetTypeName(),
                targetName != null ? targetName : "-",
                reason
        );

        try {
            String raw = llmService.chat(prompt, null, "业务：学分转换申请 AI 初筛");
            return parseAiScreen(raw, true);
        } catch (Exception e) {
            return heuristicScreen(sourceName, targetName, false);
        }
    }

    private CreditTransferAiScreenResult parseAiScreen(String raw, boolean llmUsed) {
        if (raw == null) {
            return heuristicScreen(null, null, llmUsed);
        }
        String text = raw.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1);
        }
        String lower = text.toLowerCase(Locale.ROOT);
        String suggestion = "uncertain";
        if (lower.contains("\"approve\"") || lower.contains("approve")) {
            suggestion = "approve";
        } else if (lower.contains("\"reject\"") || lower.contains("reject")) {
            suggestion = "reject";
        }
        String reason = text;
        int reasonIdx = text.indexOf("\"reason\"");
        if (reasonIdx >= 0) {
            int colon = text.indexOf(':', reasonIdx);
            int q1 = text.indexOf('"', colon + 1);
            int q2 = text.indexOf('"', q1 + 1);
            if (q1 >= 0 && q2 > q1) {
                reason = text.substring(q1 + 1, q2);
            }
        }
        if (reason.length() > 120) {
            reason = reason.substring(0, 120) + "…";
        }
        return CreditTransferAiScreenResult.builder()
                .suggestion(suggestion)
                .reason(reason)
                .llmUsed(llmUsed)
                .build();
    }

    private CreditTransferAiScreenResult heuristicScreen(String sourceName, String targetName, boolean llmUsed) {
        String s = sourceName != null ? sourceName : "";
        String t = targetName != null ? targetName : "";
        boolean overlap = false;
        for (String token : List.of("Java", "Python", "AI", "数据", "算法", "前端", "后端", "C++")) {
            if (s.contains(token) && t.contains(token)) {
                overlap = true;
                break;
            }
        }
        if (overlap) {
            return CreditTransferAiScreenResult.builder()
                    .suggestion("approve")
                    .reason("源与目标课程名称存在相同技能关键词，建议人工复核后通过（启发式，未调用大模型）。")
                    .llmUsed(llmUsed)
                    .build();
        }
        return CreditTransferAiScreenResult.builder()
                .suggestion("uncertain")
                .reason("未能自动确认课程等价性，建议人工对照培养方案与证明材料审核。")
                .llmUsed(llmUsed)
                .build();
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

    /** 同一机构下，同一目标课程仅允许一条启用中的转换规则 */
    private void assertUniqueTargetCourse(Long orgId, Integer targetType, Long targetCourseId, Long excludeRuleId) {
        if (targetType == null || targetType != 1 || targetCourseId == null) {
            return;
        }
        LambdaQueryWrapper<CreditTransferRule> wrapper = new LambdaQueryWrapper<CreditTransferRule>()
                .eq(CreditTransferRule::getOrgId, orgId)
                .eq(CreditTransferRule::getStatus, 1)
                .eq(CreditTransferRule::getTargetType, 1)
                .eq(CreditTransferRule::getTargetCourseId, targetCourseId);
        if (excludeRuleId != null) {
            wrapper.ne(CreditTransferRule::getId, excludeRuleId);
        }
        Long count = ruleMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "该课程已存在启用中的转换规则，每个课程仅允许一条规则");
        }
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
        vo.setTargetOrgId(rule.getTargetOrgId() != null ? rule.getTargetOrgId() : rule.getOrgId());
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
        vo.setAiSuggestion(application.getAiSuggestion());
        vo.setAiReason(application.getAiReason());
        vo.setAiLlmUsed(application.getAiLlmUsed() != null && application.getAiLlmUsed() == 1);
        vo.setAiScreenTime(application.getAiScreenTime());
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
        return user == null ? null : user.getRealName();
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
