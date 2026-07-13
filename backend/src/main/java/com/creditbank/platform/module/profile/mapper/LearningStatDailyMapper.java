package com.creditbank.platform.module.profile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.platform.module.profile.entity.LearningStatDaily;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper
public interface LearningStatDailyMapper extends BaseMapper<LearningStatDaily> {

    @Insert("""
            INSERT INTO learning_stat_daily
                (user_id, stat_date, study_minutes, courses_completed, credit_earned)
            VALUES
                (#{userId}, #{statDate}, #{studyMinutes}, #{coursesCompleted}, #{creditEarned})
            ON DUPLICATE KEY UPDATE
                study_minutes = study_minutes + VALUES(study_minutes),
                courses_completed = courses_completed + VALUES(courses_completed),
                credit_earned = credit_earned + VALUES(credit_earned)
            """)
    int upsertAdd(
            @Param("userId") Long userId,
            @Param("statDate") LocalDate statDate,
            @Param("studyMinutes") int studyMinutes,
            @Param("coursesCompleted") int coursesCompleted,
            @Param("creditEarned") BigDecimal creditEarned);
}
