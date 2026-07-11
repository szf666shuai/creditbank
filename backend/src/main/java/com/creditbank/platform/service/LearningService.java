package com.creditbank.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.dto.CertificateVerifyResult;
import com.creditbank.platform.dto.CoursePurchaseResult;
import com.creditbank.platform.dto.CreditChangeResult;
import com.creditbank.platform.dto.CreditEarnRequest;
import com.creditbank.platform.dto.CreditSpendRequest;
import com.creditbank.platform.dto.LearningArchiveVO;
import com.creditbank.platform.dto.LearningCertificateVO;
import com.creditbank.platform.dto.LearningCompletionResult;
import com.creditbank.platform.dto.LearningProgressRequest;
import com.creditbank.platform.dto.LearningResourceVO;
import com.creditbank.platform.entity.Course;
import com.creditbank.platform.entity.LearningAchievement;
import com.creditbank.platform.entity.LearningArchive;
import com.creditbank.platform.entity.LearningCertificate;
import com.creditbank.platform.entity.MallProduct;
import com.creditbank.platform.entity.SysTag;
import com.creditbank.platform.entity.UserCourse;
import com.creditbank.platform.mapper.CourseMapper;
import com.creditbank.platform.mapper.LearningAchievementMapper;
import com.creditbank.platform.mapper.LearningArchiveMapper;
import com.creditbank.platform.mapper.LearningCertificateMapper;
import com.creditbank.platform.mapper.MallOrderItemMapper;
import com.creditbank.platform.mapper.MallProductMapper;
import com.creditbank.platform.mapper.SysTagMapper;
import com.creditbank.platform.mapper.UserCourseMapper;
import com.creditbank.platform.module.profile.service.ProfileLearningStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningService {

    private static final DateTimeFormatter CERT_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int CERTIFICATE_REQUIRED_PERCENT = 80;

    private final CourseMapper courseMapper;
    private final SysTagMapper sysTagMapper;
    private final UserCourseMapper userCourseMapper;
    private final LearningCertificateMapper certificateMapper;
    private final LearningArchiveMapper archiveMapper;
    private final LearningAchievementMapper achievementMapper;
    private final CreditService creditService;
    private final MallProductMapper mallProductMapper;
    private final MallOrderItemMapper mallOrderItemMapper;
    private final ProfileLearningStatsService profileLearningStatsService;
    private final LearningEngagementService learningEngagementService;

    public List<LearningResourceVO> listResources(Long userId, String keyword, String tag) {
        List<LearningResourceVO> resources = courseMapper.listResources(trim(keyword), trim(tag));
        if (resources.isEmpty()) {
            return resources;
        }
        Map<Long, MallProduct> paidProductByCourse = mallProductMapper.selectList(
                        new LambdaQueryWrapper<MallProduct>()
                                .eq(MallProduct::getProductType, 3)
                                .eq(MallProduct::getStatus, 1)
                                .eq(MallProduct::getDeleted, 0)
                                .isNotNull(MallProduct::getRefCourseId)
                )
                .stream()
                .collect(Collectors.toMap(MallProduct::getRefCourseId, Function.identity(), (first, ignored) -> first));
        Set<Long> purchasedCourseIds = userId == null
                ? Set.of()
                : new HashSet<>(mallOrderItemMapper.findPurchasedCourseIds(userId));
        for (LearningResourceVO resource : resources) {
            MallProduct paidProduct = paidProductByCourse.get(resource.getId());
            resource.setPaid(paidProduct != null
                    || nz(resource.getPriceCredit()).compareTo(BigDecimal.ZERO) > 0
                    || nz(resource.getPriceMoney()).compareTo(BigDecimal.ZERO) > 0);
            resource.setPurchaseProductId(paidProduct == null ? null : paidProduct.getId());
            BigDecimal effectivePrice = resolveCoursePrice(resource, paidProduct);
            if (Boolean.TRUE.equals(resource.getPaid()) && effectivePrice.compareTo(BigDecimal.ZERO) > 0) {
                resource.setPriceCredit(effectivePrice);
            }
            resource.setPurchased(purchasedCourseIds.contains(resource.getId()));
            resource.setLearned(false);
            if (userId == null) {
                continue;
            }
            UserCourse userCourse = userCourseMapper.selectOne(
                    new LambdaQueryWrapper<UserCourse>()
                            .eq(UserCourse::getUserId, userId)
                            .eq(UserCourse::getCourseId, resource.getId())
            );
            if (userCourse != null) {
                resource.setProgress(userCourse.getProgress());
                resource.setWatchedSeconds(userCourse.getWatchedSeconds());
                resource.setMaxWatchedPositionSeconds(userCourse.getMaxWatchedPositionSeconds());
                resource.setLastPositionSeconds(userCourse.getLastPositionSeconds());
                resource.setLearningStatus(userCourse.getStatus());
                resource.setLearned((userCourse.getWatchedSeconds() != null && userCourse.getWatchedSeconds() > 0)
                        || (userCourse.getProgress() != null && userCourse.getProgress() > 0)
                        || (userCourse.getStatus() != null && userCourse.getStatus() == 1));
                if (!resource.getPurchased()) {
                    BigDecimal price = resolveCoursePrice(resource, paidProductByCourse.get(resource.getId()));
                    resource.setPurchased(nz(userCourse.getPaidCredit()).compareTo(price) >= 0
                            && price.compareTo(BigDecimal.ZERO) > 0);
                }
            }
            LearningCertificate cert = certificateMapper.selectOne(
                    new LambdaQueryWrapper<LearningCertificate>()
                            .eq(LearningCertificate::getUserId, userId)
                            .eq(LearningCertificate::getCourseId, resource.getId())
                            .last("LIMIT 1")
            );
            if (cert != null) {
                resource.setCertificateId(cert.getId());
                resource.setCertNo(cert.getCertNo());
            }
        }
        return resources;
    }

    public LearningResourceVO getResource(Long userId, Long courseId) {
        LearningResourceVO resource = listResources(userId, null, null).stream()
                .filter(item -> item.getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(404, "学习资源不存在或已下架"));
        maskLockedMedia(resource);
        return resource;
    }

    public void assertCourseAccess(Long userId, Long courseId) {
        LearningResourceVO resource = listResources(userId, null, null).stream()
                .filter(item -> item.getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(404, "学习资源不存在或已下架"));
        if (Boolean.TRUE.equals(resource.getPaid()) && !Boolean.TRUE.equals(resource.getPurchased())) {
            throw new BusinessException(403, "请先购买该课程后再学习");
        }
    }

    @Transactional
    public CoursePurchaseResult purchaseCourse(Long userId, Long courseId) {
        LearningResourceVO resource = listResources(userId, null, null).stream()
                .filter(item -> item.getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(404, "学习资源不存在或已下架"));
        if (!Boolean.TRUE.equals(resource.getPaid())) {
            throw new BusinessException(400, "该课程无需购买");
        }
        if (Boolean.TRUE.equals(resource.getPurchased())) {
            return CoursePurchaseResult.builder()
                    .courseId(courseId)
                    .paidCredit(BigDecimal.ZERO)
                    .purchased(true)
                    .build();
        }
        BigDecimal price = resolveCoursePrice(
                resource,
                mallProductMapper.selectOne(
                        new LambdaQueryWrapper<MallProduct>()
                                .eq(MallProduct::getProductType, 3)
                                .eq(MallProduct::getRefCourseId, courseId)
                                .eq(MallProduct::getStatus, 1)
                                .eq(MallProduct::getDeleted, 0)
                                .last("LIMIT 1")
                )
        );
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "课程价格未配置");
        }
        CreditSpendRequest spendRequest = new CreditSpendRequest();
        spendRequest.setAmount(price);
        spendRequest.setBizType("course_purchase");
        spendRequest.setRefType("course");
        spendRequest.setRefId(courseId);
        spendRequest.setSource("购买课程: " + resource.getTitle());
        CreditChangeResult creditChange = creditService.spend(userId, spendRequest);

        UserCourse record = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getCourseId, courseId)
        );
        if (record == null) {
            record = new UserCourse();
            record.setUserId(userId);
            record.setCourseId(courseId);
            record.setProgress(0);
            record.setWatchedSeconds(0);
            record.setMaxWatchedPositionSeconds(0);
            record.setLastPositionSeconds(0);
            record.setStatus(0);
            record.setPaidCredit(price);
            userCourseMapper.insert(record);
        } else {
            record.setPaidCredit(price);
            userCourseMapper.updateById(record);
        }
        return CoursePurchaseResult.builder()
                .courseId(courseId)
                .paidCredit(price)
                .balanceAfter(creditChange.getBalanceAfter())
                .purchased(true)
                .build();
    }

    private void maskLockedMedia(LearningResourceVO resource) {
        if (!Boolean.TRUE.equals(resource.getPaid()) || Boolean.TRUE.equals(resource.getPurchased())) {
            return;
        }
        resource.setVideoUrl(null);
    }

    private BigDecimal resolveCoursePrice(LearningResourceVO resource, MallProduct mallProduct) {
        BigDecimal coursePrice = nz(resource.getPriceCredit());
        BigDecimal productPrice = mallProduct == null ? BigDecimal.ZERO : nz(mallProduct.getPriceCredit());
        return coursePrice.max(productPrice);
    }

    public List<String> listSkillTags() {
        return sysTagMapper.selectList(
                        new LambdaQueryWrapper<SysTag>()
                                .eq(SysTag::getCategory, "skill")
                                .orderByAsc(SysTag::getName)
                )
                .stream()
                .map(SysTag::getName)
                .toList();
    }

    @Transactional
    public UserCourse startCourse(Long userId, Long courseId) {
        assertCourseAccess(userId, courseId);
        Course course = requireCourse(courseId);
        UserCourse existing = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getCourseId, course.getId())
        );
        if (existing != null) {
            return existing;
        }
        UserCourse record = new UserCourse();
        record.setUserId(userId);
        record.setCourseId(course.getId());
        record.setProgress(0);
        record.setWatchedSeconds(0);
        record.setMaxWatchedPositionSeconds(0);
        record.setLastPositionSeconds(0);
        record.setStatus(0);
        record.setPaidCredit(BigDecimal.ZERO);
        userCourseMapper.insert(record);
        return record;
    }

    @Transactional
    public UserCourse reportProgress(Long userId, Long courseId, LearningProgressRequest request) {
        assertCourseAccess(userId, courseId);
        if (learningEngagementService.totalEpisodeDuration(courseId) > 0) {
            return learningEngagementService.reportProgress(userId, courseId, request);
        }
        Course course = requireCourse(courseId);
        int durationSeconds = requireVideoDuration(course);
        UserCourse record = startCourse(userId, courseId);
        if (record.getStatus() != null && record.getStatus() == 1) {
            return record;
        }

        int watchedSeconds = record.getWatchedSeconds() == null ? 0 : record.getWatchedSeconds();
        int maxWatchedPosition = record.getMaxWatchedPositionSeconds() == null
                ? 0
                : record.getMaxWatchedPositionSeconds();
        int watchedDelta = Math.min(Math.max(request.getWatchedDeltaSeconds(), 0), 30);
        int previousWatchedSeconds = watchedSeconds;
        watchedSeconds = Math.min(durationSeconds, watchedSeconds + watchedDelta);
        int acceptedWatchedDelta = Math.max(0, watchedSeconds - previousWatchedSeconds);
        int currentPosition = Math.min(durationSeconds, Math.max(request.getCurrentTimeSeconds(), 0));
        int maximumAllowedPosition = Math.min(durationSeconds, maxWatchedPosition + watchedDelta);
        int acceptedPosition = Math.min(currentPosition, maximumAllowedPosition);
        maxWatchedPosition = Math.max(maxWatchedPosition, acceptedPosition);
        int progress = Math.min(100, (int) Math.floor(maxWatchedPosition * 100.0 / durationSeconds));

        record.setWatchedSeconds(watchedSeconds);
        record.setMaxWatchedPositionSeconds(maxWatchedPosition);
        record.setLastPositionSeconds(acceptedPosition);
        record.setProgress(progress);
        userCourseMapper.updateById(record);
        if (acceptedWatchedDelta > 0) {
            profileLearningStatsService.recordStudySeconds(userId, acceptedWatchedDelta);
        }
        return record;
    }

    @Transactional
    public LearningCompletionResult completeCourse(Long userId, Long courseId) {
        assertCourseAccess(userId, courseId);
        Course course = requireCourse(courseId);
        UserCourse record = startCourse(userId, courseId);
        int durationSeconds = requireVideoDuration(course);
        int maxWatchedPosition = record.getMaxWatchedPositionSeconds() == null
                ? 0
                : record.getMaxWatchedPositionSeconds();
        int requiredSeconds = (int) Math.ceil(durationSeconds * CERTIFICATE_REQUIRED_PERCENT / 100.0);
        if (maxWatchedPosition < requiredSeconds) {
            int currentPercent = Math.min(100, (int) Math.floor(maxWatchedPosition * 100.0 / durationSeconds));
            throw new BusinessException(400,
                    "课程观看进度需达到 80% 才能颁发合格证，当前为 " + currentPercent + "%");
        }
        record.setStatus(1);
        if (record.getCompleteTime() == null) {
            record.setCompleteTime(LocalDateTime.now());
        }
        userCourseMapper.updateById(record);

        LearningCertificate cert = certificateMapper.selectOne(
                new LambdaQueryWrapper<LearningCertificate>()
                        .eq(LearningCertificate::getUserId, userId)
                        .eq(LearningCertificate::getCourseId, courseId)
                        .last("LIMIT 1")
        );
        CreditChangeResult creditChange = null;
        if (cert == null) {
            cert = createCertificate(userId, course);
            certificateMapper.insert(cert);
            insertAchievement(userId, course, cert);
            creditChange = grantCourseReward(userId, course);
            BigDecimal earned = creditChange != null && creditChange.getAmount() != null
                    ? creditChange.getAmount()
                    : nz(course.getCreditReward());
            profileLearningStatsService.recordCourseCompleted(userId, earned);
        }
        LearningArchive archive = ensureArchive(userId, course, cert);
        return LearningCompletionResult.builder()
                .userCourseId(record.getId())
                .certificate(toCertificateVO(cert))
                .archive(toArchiveVO(archive))
                .creditChange(creditChange)
                .build();
    }

    private int requireVideoDuration(Course course) {
        int episodeDuration = learningEngagementService.totalEpisodeDuration(course.getId());
        if (episodeDuration > 0) {
            return episodeDuration;
        }
        if (!StringUtils.hasText(course.getVideoUrl())
                || course.getVideoDurationSeconds() == null
                || course.getVideoDurationSeconds() <= 0) {
            throw new BusinessException(400, "该课程暂未配置可学习的视频");
        }
        return course.getVideoDurationSeconds();
    }

    public List<LearningArchiveVO> listArchives(Long userId, int limit) {
        return archiveMapper.selectList(
                        new LambdaQueryWrapper<LearningArchive>()
                                .eq(LearningArchive::getUserId, userId)
                                .orderByDesc(LearningArchive::getCreateTime)
                                .last("LIMIT " + Math.min(Math.max(limit, 1), 50))
                )
                .stream()
                .map(this::toArchiveVO)
                .toList();
    }

    public List<LearningCertificateVO> listCertificates(Long userId, int limit) {
        return certificateMapper.selectList(
                        new LambdaQueryWrapper<LearningCertificate>()
                                .eq(LearningCertificate::getUserId, userId)
                                .orderByDesc(LearningCertificate::getIssuedAt)
                                .last("LIMIT " + Math.min(Math.max(limit, 1), 50))
                )
                .stream()
                .map(this::toCertificateVO)
                .toList();
    }

    public CertificateVerifyResult verifyCertificate(String certNo, String hash) {
        if (!StringUtils.hasText(certNo)) {
            throw new BusinessException(400, "证书编号不能为空");
        }
        LearningCertificate cert = certificateMapper.selectOne(
                new LambdaQueryWrapper<LearningCertificate>()
                        .eq(LearningCertificate::getCertNo, certNo.trim())
                        .last("LIMIT 1")
        );
        if (cert == null) {
            return CertificateVerifyResult.builder()
                    .valid(false)
                    .certNo(certNo)
                    .message("证书不存在")
                    .build();
        }
        boolean hashMatched = !StringUtils.hasText(hash)
                || cert.getBlockchainHash().equalsIgnoreCase(hash.trim());
        boolean valid = cert.getVerifyStatus() != null && cert.getVerifyStatus() == 1 && hashMatched;
        return CertificateVerifyResult.builder()
                .valid(valid)
                .certNo(cert.getCertNo())
                .title(cert.getTitle())
                .blockchainHash(cert.getBlockchainHash())
                .issuedAt(cert.getIssuedAt())
                .message(valid ? "区块链存证校验通过" : "证书编号或链上哈希不匹配")
                .build();
    }

    private LearningCertificate createCertificate(Long userId, Course course) {
        LearningCertificate cert = new LearningCertificate();
        String certNo = "LC" + LocalDateTime.now().format(CERT_TIME_FORMAT)
                + String.format("%04d", userId % 10000)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        String title = course.getTitle() + " 课程合格证";
        String hash = sha256(certNo + "|" + userId + "|" + course.getId() + "|" + title);
        String verifyUrl = "/api/learning/certificates/verify?certNo=" + certNo + "&hash=" + hash;
        cert.setCertNo(certNo);
        cert.setUserId(userId);
        cert.setCourseId(course.getId());
        cert.setTitle(title);
        cert.setQrContent(verifyUrl);
        cert.setQrImageUrl(null);
        cert.setFileUrl("/certificates/" + certNo + ".pdf");
        cert.setBlockchainHash(hash);
        cert.setVerifyStatus(1);
        cert.setIssuedAt(LocalDateTime.now());
        return cert;
    }

    private LearningArchive ensureArchive(Long userId, Course course, LearningCertificate cert) {
        LearningArchive existing = archiveMapper.selectOne(
                new LambdaQueryWrapper<LearningArchive>()
                        .eq(LearningArchive::getUserId, userId)
                        .eq(LearningArchive::getCourseId, course.getId())
                        .last("LIMIT 1")
        );
        if (existing != null) {
            return existing;
        }
        LearningArchive archive = new LearningArchive();
        archive.setUserId(userId);
        archive.setTitle(course.getTitle());
        archive.setArchiveType(1);
        archive.setCourseId(course.getId());
        archive.setCertificateId(cert.getId());
        archive.setCategory("课程学习");
        archive.setDescription("完成学习资源并通过区块链证书校验: " + cert.getCertNo());
        archive.setStartDate(LocalDate.now());
        archive.setEndDate(LocalDate.now());
        archive.setCreditEarned(nz(course.getCreditReward()));
        archive.setStatus(1);
        archiveMapper.insert(archive);
        return archive;
    }

    private void insertAchievement(Long userId, Course course, LearningCertificate cert) {
        LearningAchievement achievement = new LearningAchievement();
        achievement.setUserId(userId);
        achievement.setTitle(cert.getTitle());
        achievement.setType(1);
        achievement.setOrgId(course.getOrgId());
        achievement.setCertificateId(cert.getId());
        achievement.setCreditValue(nz(course.getCreditReward()));
        achievement.setFileUrl(cert.getFileUrl());
        achievement.setVerifyStatus(1);
        achievement.setBlockchainHash(cert.getBlockchainHash());
        achievementMapper.insert(achievement);
    }

    private CreditChangeResult grantCourseReward(Long userId, Course course) {
        BigDecimal reward = nz(course.getCreditReward());
        if (reward.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        CreditEarnRequest earnRequest = new CreditEarnRequest();
        earnRequest.setRuleCode("COURSE_COMPLETE");
        earnRequest.setAmountOverride(reward);
        earnRequest.setSource("完成学习资源: " + course.getTitle());
        earnRequest.setRefType("course");
        earnRequest.setRefId(course.getId());
        return creditService.earnByRule(userId, earnRequest);
    }

    private Course requireCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException(404, "学习资源不存在或已下架");
        }
        return course;
    }

    public LearningCertificateVO toCertificateVO(LearningCertificate cert) {
        return LearningCertificateVO.builder()
                .id(cert.getId())
                .certNo(cert.getCertNo())
                .userId(cert.getUserId())
                .courseId(cert.getCourseId())
                .title(cert.getTitle())
                .qrContent(cert.getQrContent())
                .qrImageUrl(cert.getQrImageUrl())
                .blockchainHash(cert.getBlockchainHash())
                .verifyStatus(cert.getVerifyStatus())
                .verifyStatusName(cert.getVerifyStatus() != null && cert.getVerifyStatus() == 1 ? "已通过" : "待校验")
                .issuedAt(cert.getIssuedAt())
                .build();
    }

    public LearningArchiveVO toArchiveVO(LearningArchive archive) {
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
                .statusName(archive.getStatus() != null && archive.getStatus() == 1 ? "已完成" : "进行中")
                .createTime(archive.getCreateTime())
                .build();
    }

    private String archiveTypeName(Integer type) {
        if (type == null) return "其他";
        return switch (type) {
            case 1 -> "课程";
            case 2 -> "活动";
            case 3 -> "成果";
            default -> "其他";
        };
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusinessException("生成证书存证哈希失败");
        }
    }
}
