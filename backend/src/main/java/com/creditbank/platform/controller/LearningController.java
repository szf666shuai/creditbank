package com.creditbank.platform.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.dto.CoursePurchaseResult;
import com.creditbank.platform.dto.CertificateVerifyResult;
import com.creditbank.platform.dto.LearningArchiveVO;
import com.creditbank.platform.dto.LearningCertificateVO;
import com.creditbank.platform.dto.LearningCompletionResult;
import com.creditbank.platform.dto.CourseCommentLikeResult;
import com.creditbank.platform.dto.CourseCommentCreateRequest;
import com.creditbank.platform.dto.CourseCommentVO;
import com.creditbank.platform.dto.CourseDanmakuCreateRequest;
import com.creditbank.platform.dto.CourseDanmakuVO;
import com.creditbank.platform.dto.CourseMaterialVO;
import com.creditbank.platform.dto.LearningProgressRequest;
import com.creditbank.platform.dto.LearningResourceVO;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.constant.UserRole;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.entity.UserCourse;
import com.creditbank.platform.security.AuthSupport;
import com.creditbank.platform.service.CourseInteractionService;
import com.creditbank.platform.dto.CourseEpisodeVO;
import com.creditbank.platform.dto.LearningCheckinVO;
import com.creditbank.platform.dto.LearningReminderRequest;
import com.creditbank.platform.dto.LearningReminderVO;
import com.creditbank.platform.service.LearningEngagementService;
import com.creditbank.platform.service.LearningService;
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
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;
    private final CourseInteractionService courseInteractionService;
    private final LearningEngagementService learningEngagementService;
    private final AuthSupport authSupport;

    @GetMapping("/resources")
    public Result<List<LearningResourceVO>> resources(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String tag) {
        // 游客可浏览列表；登录学员/管理员带个性化进度
        return Result.ok(learningService.listResources(optionalLearningUserId(), keyword, tag));
    }

    @GetMapping("/resources/{courseId}")
    public Result<LearningResourceVO> resource(@PathVariable Long courseId) {
        SysUser user = authSupport.requireStudentOrAdmin();
        return Result.ok(learningService.getResource(user.getId(), courseId));
    }

    @GetMapping("/tags")
    public Result<List<String>> tags() {
        return Result.ok(learningService.listSkillTags());
    }

    /** 未登录或非学员/管理员时按游客浏览，不抛错 */
    private Long optionalLearningUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return null;
        }
        try {
            SysUser user = authSupport.requireLoginUser();
            if (user.getRole() != null
                    && (user.getRole() == UserRole.STUDENT || user.getRole() == UserRole.ADMIN)) {
                return user.getId();
            }
        } catch (Exception ignored) {
            // token 失效或用户不存在：按游客
        }
        return null;
    }

    @GetMapping("/resources/{courseId}/comments")
    public Result<List<CourseCommentVO>> comments(@PathVariable Long courseId,
                                                  @RequestParam(defaultValue = "50") int limit) {
        SysUser user = authSupport.requireStudentOrAdmin();
        return Result.ok(courseInteractionService.listComments(courseId, user.getId(), limit));
    }

    @PostMapping("/resources/{courseId}/comments")
    public Result<CourseCommentVO> createComment(@PathVariable Long courseId,
                                                 @Valid @RequestBody CourseCommentCreateRequest request) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(courseInteractionService.createComment(student.getId(), courseId, request));
    }

    @PostMapping("/resources/{courseId}/comments/{commentId}/like")
    public Result<CourseCommentLikeResult> toggleCommentLike(@PathVariable Long courseId,
                                                             @PathVariable Long commentId) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(courseInteractionService.toggleCommentLike(student.getId(), courseId, commentId));
    }

    @GetMapping("/resources/{courseId}/danmaku")
    public Result<List<CourseDanmakuVO>> danmaku(@PathVariable Long courseId) {
        authSupport.requireStudentOrAdmin();
        return Result.ok(courseInteractionService.listDanmaku(courseId));
    }

    @PostMapping("/resources/{courseId}/danmaku")
    public Result<CourseDanmakuVO> createDanmaku(@PathVariable Long courseId,
                                                   @Valid @RequestBody CourseDanmakuCreateRequest request) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(courseInteractionService.createDanmaku(student.getId(), courseId, request));
    }

    @GetMapping("/resources/{courseId}/materials")
    public Result<List<CourseMaterialVO>> materials(@PathVariable Long courseId) {
        authSupport.requireStudentOrAdmin();
        return Result.ok(courseInteractionService.listMaterials(courseId));
    }

    @GetMapping("/resources/{courseId}/episodes")
    public Result<List<CourseEpisodeVO>> episodes(@PathVariable Long courseId) {
        SysUser user = authSupport.requireStudentOrAdmin();
        learningService.assertCourseAccess(user.getId(), courseId);
        return Result.ok(learningEngagementService.listEpisodes(user.getId(), courseId));
    }

    @PostMapping("/resources/{courseId}/purchase")
    public Result<CoursePurchaseResult> purchase(@PathVariable Long courseId) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(learningService.purchaseCourse(student.getId(), courseId));
    }

    @GetMapping("/resources/{courseId}/reminder")
    public Result<LearningReminderVO> getReminder(@PathVariable Long courseId) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(learningEngagementService.getReminder(student.getId(), courseId));
    }

    @PostMapping("/resources/{courseId}/reminder")
    public Result<LearningReminderVO> saveReminder(@PathVariable Long courseId,
                                                   @Valid @RequestBody LearningReminderRequest request) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(learningEngagementService.saveReminder(student.getId(), courseId, request));
    }

    @GetMapping("/resources/{courseId}/checkin/status")
    public Result<LearningCheckinVO> checkinStatus(@PathVariable Long courseId) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(learningEngagementService.getCheckinStatus(student.getId(), courseId));
    }

    @PostMapping("/resources/{courseId}/checkin")
    public Result<LearningCheckinVO> checkin(@PathVariable Long courseId) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(learningEngagementService.checkin(student.getId(), courseId));
    }

    @PostMapping("/resources/{courseId}/start")
    public Result<UserCourse> start(@PathVariable Long courseId) {
        SysUser student = authSupport.requireStudent();
        learningService.assertCourseAccess(student.getId(), courseId);
        return Result.ok(learningService.startCourse(student.getId(), courseId));
    }

    @PostMapping("/resources/{courseId}/progress")
    public Result<UserCourse> progress(@PathVariable Long courseId,
                                       @Valid @RequestBody LearningProgressRequest request) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(learningService.reportProgress(student.getId(), courseId, request));
    }

    @PostMapping("/resources/{courseId}/complete")
    public Result<LearningCompletionResult> complete(@PathVariable Long courseId) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(learningService.completeCourse(student.getId(), courseId));
    }

    @GetMapping("/archives")
    public Result<List<LearningArchiveVO>> archives(@RequestParam(defaultValue = "20") int limit) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(learningService.listArchives(student.getId(), limit));
    }

    @GetMapping("/certificates")
    public Result<List<LearningCertificateVO>> certificates(@RequestParam(defaultValue = "20") int limit) {
        SysUser student = authSupport.requireStudent();
        return Result.ok(learningService.listCertificates(student.getId(), limit));
    }

    @GetMapping("/certificates/verify")
    public Result<CertificateVerifyResult> verify(@RequestParam String certNo,
                                                  @RequestParam(required = false) String hash) {
        return Result.ok(learningService.verifyCertificate(certNo, hash));
    }
}
