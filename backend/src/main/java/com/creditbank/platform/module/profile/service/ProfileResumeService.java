package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.LearningAchievement;
import com.creditbank.platform.entity.LearningArchive;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.entity.UserResume;
import com.creditbank.platform.mapper.LearningAchievementMapper;
import com.creditbank.platform.mapper.LearningArchiveMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.mapper.UserResumeMapper;
import com.creditbank.platform.module.profile.dto.ResumeAiGenerateRequest;
import com.creditbank.platform.module.profile.dto.ResumeContentVO;
import com.creditbank.platform.module.profile.dto.UpdateUserResumeRequest;
import com.creditbank.platform.module.profile.dto.UserResumeSummaryVO;
import com.creditbank.platform.module.profile.dto.UserResumeVO;
import com.creditbank.platform.security.AuthSupport;
import com.creditbank.platform.service.LlmService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileResumeService {

    private static final String DEFAULT_TITLE = "默认简历";
    private static final int IS_DEFAULT = 1;
    private static final int NOT_DEFAULT = 0;

    private final ObjectMapper objectMapper;
    private final AuthSupport authSupport;
    private final SysUserMapper userMapper;
    private final UserResumeMapper userResumeMapper;
    private final LearningArchiveMapper learningArchiveMapper;
    private final LearningAchievementMapper learningAchievementMapper;
    private final LlmService llmService;

    public List<UserResumeSummaryVO> listMyResumes() {
        Long userId = authSupport.requireUserId();
        ensureAtLeastOneResume(userId);
        return userResumeMapper.selectList(new LambdaQueryWrapper<UserResume>()
                        .eq(UserResume::getUserId, userId)
                        .orderByDesc(UserResume::getIsDefault)
                        .orderByDesc(UserResume::getUpdateTime))
                .stream()
                .map(this::toSummaryVO)
                .toList();
    }

    public UserResumeVO getMyDefaultResume() {
        Long userId = authSupport.requireUserId();
        return toVO(getOrCreateDefaultResume(userId));
    }

    public UserResumeVO getMyResume(Long resumeId) {
        Long userId = authSupport.requireUserId();
        return toVO(requireOwnedResume(resumeId, userId));
    }

    @Transactional
    public UserResumeVO createMyResume(UpdateUserResumeRequest request) {
        Long userId = authSupport.requireUserId();
        validateContent(request);

        long count = countUserResumes(userId);
        boolean makeDefault = count == 0;

        UserResume resume = new UserResume();
        resume.setUserId(userId);
        resume.setTitle(resolveTitle(request.getTitle(), count));
        resume.setContentJson(toJson(request.getContent()));
        resume.setFileUrl(trimToNull(request.getFileUrl()));
        resume.setIsDefault(makeDefault ? IS_DEFAULT : NOT_DEFAULT);
        resume.setVersion(1);
        userResumeMapper.insert(resume);
        return toVO(resume);
    }

    @Transactional
    public UserResumeVO updateMyResume(Long resumeId, UpdateUserResumeRequest request) {
        Long userId = authSupport.requireUserId();
        validateContent(request);

        UserResume resume = requireOwnedResume(resumeId, userId);
        resume.setTitle(resolveTitle(request.getTitle(), 0));
        resume.setContentJson(toJson(request.getContent()));
        resume.setFileUrl(trimToNull(request.getFileUrl()));
        resume.setVersion(resume.getVersion() != null ? resume.getVersion() + 1 : 1);
        userResumeMapper.updateById(resume);
        return toVO(resume);
    }

    @Transactional
    public void deleteMyResume(Long resumeId) {
        Long userId = authSupport.requireUserId();
        UserResume resume = requireOwnedResume(resumeId, userId);
        if (countUserResumes(userId) <= 1) {
            throw new BusinessException(400, "至少保留一份简历");
        }

        boolean wasDefault = Objects.equals(resume.getIsDefault(), IS_DEFAULT);
        userResumeMapper.deleteById(resumeId);

        if (wasDefault) {
            UserResume next = userResumeMapper.selectOne(new LambdaQueryWrapper<UserResume>()
                    .eq(UserResume::getUserId, userId)
                    .orderByDesc(UserResume::getUpdateTime)
                    .last("LIMIT 1"));
            if (next != null) {
                next.setIsDefault(IS_DEFAULT);
                userResumeMapper.updateById(next);
            }
        }
    }

    @Transactional
    public UserResumeVO setDefaultResume(Long resumeId) {
        Long userId = authSupport.requireUserId();
        requireOwnedResume(resumeId, userId);

        userResumeMapper.update(null, new LambdaUpdateWrapper<UserResume>()
                .eq(UserResume::getUserId, userId)
                .set(UserResume::getIsDefault, NOT_DEFAULT));
        UserResume resume = requireOwnedResume(resumeId, userId);
        resume.setIsDefault(IS_DEFAULT);
        userResumeMapper.updateById(resume);
        return toVO(resume);
    }

    public UserResume ensureDefaultResume(Long userId) {
        return getOrCreateDefaultResume(userId);
    }

    public ResumeContentVO generateResumeWithAi(ResumeAiGenerateRequest request) {
        Long userId = authSupport.requireUserId();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        String targetRole = request != null && StringUtils.hasText(request.getTargetRole())
                ? request.getTargetRole().trim()
                : "通用技术岗位";
        String extraHint = request != null && StringUtils.hasText(request.getExtraHint())
                ? request.getExtraHint().trim()
                : "";

        String context = buildLearningContext(userId);
        String prompt = """
                请根据学员档案与学习记录，生成一份面向「%s」的结构化中文简历草稿。
                %s
                要求：
                1. 内容真实可信，可合理润色但勿捏造与档案明显矛盾的经历；
                2. 若档案信息不足，工作经历与项目可写「待补充」或基于已学课程合理推断实习/实训项目；
                3. 只输出一个 JSON 对象，不要 Markdown 代码块，不要额外说明。
                JSON 字段（均为字符串）：
                realName, phone, email, education, workExperience, skills, selfIntro, projects
                """.formatted(
                targetRole,
                StringUtils.hasText(extraHint) ? "补充要求：" + extraHint + "\n" : "");

        String raw = llmService.chat(prompt, null, context);
        ResumeContentVO generated = parseGeneratedContent(raw);
        return mergeWithUserProfile(generated, user);
    }

    private String buildLearningContext(Long userId) {
        StringBuilder sb = new StringBuilder();
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            sb.append("姓名：").append(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
            if (StringUtils.hasText(user.getPhone())) {
                sb.append("\n手机：").append(user.getPhone());
            }
            if (StringUtils.hasText(user.getEmail())) {
                sb.append("\n邮箱：").append(user.getEmail());
            }
        }

        List<LearningArchive> archives = learningArchiveMapper.selectList(
                new LambdaQueryWrapper<LearningArchive>()
                        .eq(LearningArchive::getUserId, userId)
                        .orderByDesc(LearningArchive::getEndDate)
                        .last("LIMIT 8"));
        if (!archives.isEmpty()) {
            sb.append("\n\n学习档案：");
            for (LearningArchive archive : archives) {
                sb.append("\n- ").append(archive.getTitle());
                if (StringUtils.hasText(archive.getCategory())) {
                    sb.append("（").append(archive.getCategory()).append("）");
                }
            }
        }

        List<LearningAchievement> achievements = learningAchievementMapper.selectList(
                new LambdaQueryWrapper<LearningAchievement>()
                        .eq(LearningAchievement::getUserId, userId)
                        .orderByDesc(LearningAchievement::getCreateTime)
                        .last("LIMIT 8"));
        if (!achievements.isEmpty()) {
            sb.append("\n\n学习成果：");
            sb.append(achievements.stream()
                    .map(LearningAchievement::getTitle)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining("；", "", "")));
        }
        return sb.toString();
    }

    private ResumeContentVO parseGeneratedContent(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new BusinessException(502, "AI 未返回有效内容");
        }
        String json = extractJsonObject(raw.trim());
        try {
            JsonNode node = objectMapper.readTree(json);
            return ResumeContentVO.builder()
                    .realName(text(node, "realName"))
                    .phone(text(node, "phone"))
                    .email(text(node, "email"))
                    .education(text(node, "education"))
                    .workExperience(text(node, "workExperience"))
                    .skills(text(node, "skills"))
                    .selfIntro(text(node, "selfIntro"))
                    .projects(text(node, "projects"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new BusinessException(502, "AI 返回格式无法解析，请重试");
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        return value.asText("").trim();
    }

    private static String extractJsonObject(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    private ResumeContentVO mergeWithUserProfile(ResumeContentVO generated, SysUser user) {
        ResumeContentVO base = generated != null ? generated : emptyContent();
        return ResumeContentVO.builder()
                .realName(StringUtils.hasText(base.getRealName())
                        ? base.getRealName()
                        : (StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername()))
                .phone(StringUtils.hasText(base.getPhone()) ? base.getPhone() : nullToEmpty(user.getPhone()))
                .email(StringUtils.hasText(base.getEmail()) ? base.getEmail() : nullToEmpty(user.getEmail()))
                .education(nullToEmpty(base.getEducation()))
                .workExperience(nullToEmpty(base.getWorkExperience()))
                .skills(nullToEmpty(base.getSkills()))
                .selfIntro(nullToEmpty(base.getSelfIntro()))
                .projects(nullToEmpty(base.getProjects()))
                .build();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private UserResume getOrCreateDefaultResume(Long userId) {
        UserResume resume = userResumeMapper.selectOne(new LambdaQueryWrapper<UserResume>()
                .eq(UserResume::getUserId, userId)
                .eq(UserResume::getIsDefault, IS_DEFAULT)
                .orderByDesc(UserResume::getUpdateTime)
                .last("LIMIT 1"));
        if (resume != null) {
            return resume;
        }

        resume = userResumeMapper.selectOne(new LambdaQueryWrapper<UserResume>()
                .eq(UserResume::getUserId, userId)
                .orderByDesc(UserResume::getUpdateTime)
                .last("LIMIT 1"));
        if (resume != null) {
            resume.setIsDefault(IS_DEFAULT);
            userResumeMapper.updateById(resume);
            return resume;
        }

        resume = new UserResume();
        resume.setUserId(userId);
        resume.setTitle(DEFAULT_TITLE);
        resume.setContentJson(toJson(buildInitialContent(userId)));
        resume.setIsDefault(IS_DEFAULT);
        resume.setVersion(1);
        userResumeMapper.insert(resume);
        return resume;
    }

    private void ensureAtLeastOneResume(Long userId) {
        if (countUserResumes(userId) == 0) {
            getOrCreateDefaultResume(userId);
        }
    }

    private long countUserResumes(Long userId) {
        Long count = userResumeMapper.selectCount(new LambdaQueryWrapper<UserResume>()
                .eq(UserResume::getUserId, userId));
        return count != null ? count : 0;
    }

    private UserResume requireOwnedResume(Long resumeId, Long userId) {
        UserResume resume = userResumeMapper.selectById(resumeId);
        if (resume == null || !Objects.equals(resume.getUserId(), userId)) {
            throw new BusinessException(404, "简历不存在");
        }
        return resume;
    }

    private void validateContent(UpdateUserResumeRequest request) {
        if (request == null || request.getContent() == null) {
            throw new BusinessException(400, "请填写简历内容");
        }
    }

    private String resolveTitle(String title, long existingCount) {
        if (StringUtils.hasText(title)) {
            return title.trim();
        }
        return existingCount > 0 ? "简历版本 " + (existingCount + 1) : DEFAULT_TITLE;
    }

    private ResumeContentVO buildInitialContent(Long userId) {
        SysUser user = userMapper.selectById(userId);
        return ResumeContentVO.builder()
                .realName(user != null && StringUtils.hasText(user.getRealName()) ? user.getRealName() : "")
                .phone(user != null && StringUtils.hasText(user.getPhone()) ? user.getPhone() : "")
                .email(user != null && StringUtils.hasText(user.getEmail()) ? user.getEmail() : "")
                .education("")
                .workExperience("")
                .skills("")
                .selfIntro("")
                .projects("")
                .build();
    }

    private UserResumeSummaryVO toSummaryVO(UserResume resume) {
        ResumeContentVO content = parseContent(resume.getContentJson());
        return UserResumeSummaryVO.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .realName(content.getRealName())
                .isDefault(resume.getIsDefault())
                .version(resume.getVersion())
                .updateTime(resume.getUpdateTime())
                .build();
    }

    private UserResumeVO toVO(UserResume resume) {
        return UserResumeVO.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .content(parseContent(resume.getContentJson()))
                .fileUrl(resume.getFileUrl())
                .isDefault(resume.getIsDefault())
                .version(resume.getVersion())
                .updateTime(resume.getUpdateTime())
                .build();
    }

    private ResumeContentVO parseContent(String contentJson) {
        if (!StringUtils.hasText(contentJson)) {
            return emptyContent();
        }
        try {
            return objectMapper.readValue(contentJson, ResumeContentVO.class);
        } catch (JsonProcessingException e) {
            return emptyContent();
        }
    }

    private String toJson(ResumeContentVO content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "简历内容保存失败");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private ResumeContentVO emptyContent() {
        return ResumeContentVO.builder()
                .realName("")
                .phone("")
                .email("")
                .education("")
                .workExperience("")
                .skills("")
                .selfIntro("")
                .projects("")
                .build();
    }
}
