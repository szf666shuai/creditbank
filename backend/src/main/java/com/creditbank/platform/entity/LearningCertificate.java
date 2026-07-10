package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_certificate")
public class LearningCertificate {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String certNo;
    private Long userId;
    private Long courseId;
    private String title;
    private String qrContent;
    private String qrImageUrl;
    private String fileUrl;
    private String blockchainHash;
    private Integer verifyStatus;
    private LocalDateTime issuedAt;
}
