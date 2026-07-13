package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseCommentLikeResult {

    private Long commentId;
    private Integer likeCount;
    private Boolean liked;
}
