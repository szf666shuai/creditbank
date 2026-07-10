package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.entity.UserResume;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.mapper.UserResumeMapper;
import com.creditbank.platform.module.profile.dto.ResumeContentVO;
import com.creditbank.platform.module.profile.dto.UpdateUserResumeRequest;
import com.creditbank.platform.module.profile.dto.UserResumeSummaryVO;
import com.creditbank.platform.module.profile.dto.UserResumeVO;
import com.creditbank.platform.security.AuthSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

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
