package com.creditbank.platform.dto;

import lombok.Data;

@Data
public class CourseEpisodeVO {

    private Long id;
    private Long courseId;
    private String title;
    private String videoUrl;
    private Integer videoDurationSeconds;
    private Integer sortOrder;
    private Integer progress;
    private Integer maxWatchedPositionSeconds;
    private Integer lastPositionSeconds;
}
