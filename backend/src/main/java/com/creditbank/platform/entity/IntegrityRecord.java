package com.creditbank.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("integrity_record")
public class IntegrityRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer changeValue;
    private Integer scoreAfter;
    /** 1加分 2扣分 */
    private Integer eventType;
    private String reason;
    private String refType;
    private Long refId;
    private Long operatorId;
    private LocalDateTime createTime;
}
