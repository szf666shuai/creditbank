package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_episode_progress")
public class UserEpisodeProgress {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long courseId;
    private Long episodeId;
    private Integer progress;
    private Integer watchedSeconds;
    private Integer maxWatchedPositionSeconds;
    private Integer lastPositionSeconds;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
