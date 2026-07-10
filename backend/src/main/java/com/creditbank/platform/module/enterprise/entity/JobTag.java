package com.creditbank.platform.module.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("job_tag")
public class JobTag {

    private Long jobId;
    private Long tagId;
}
