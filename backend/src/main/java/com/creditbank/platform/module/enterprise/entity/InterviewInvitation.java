package com.creditbank.platform.module.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_invitation")
public class InterviewInvitation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long jobId;
    private Long orgId;
    private Long fromUserId;
    private Long toUserId;
    private Long messageId;
    private Long applicationId;
    private Integer status;
    private LocalDateTime inviteTime;
    private String location;
    /** 0现场 1视频 */
    private Integer interviewMode;
    private String roomId;
    private LocalDateTime createTime;
}
