package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.EnterpriseCourseSaveRequest;
import com.creditbank.platform.module.enterprise.dto.EnterpriseCourseVO;
import com.creditbank.platform.module.enterprise.service.EnterpriseCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/my/courses")
@RequiredArgsConstructor
public class EnterpriseCourseController {

    private final EnterpriseCourseService enterpriseCourseService;

    @GetMapping
    public Result<List<EnterpriseCourseVO>> listMyCourses() {
        return Result.ok(enterpriseCourseService.listMyCourses());
    }

    @PostMapping
    public Result<EnterpriseCourseVO> createCourse(@Valid @RequestBody EnterpriseCourseSaveRequest request) {
        return Result.ok(enterpriseCourseService.createCourse(request));
    }

    @PutMapping("/{id}")
    public Result<EnterpriseCourseVO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody EnterpriseCourseSaveRequest request) {
        return Result.ok(enterpriseCourseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        enterpriseCourseService.deleteCourse(id);
        return Result.ok();
    }
}