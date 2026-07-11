package com.creditbank.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchTestDataSeeder {

    private final DataSource dataSource;

    @Value("${app.seed-search-data:true}")
    private boolean enabled;

    @EventListener(ApplicationReadyEvent.class)
    public void seedSearchTestData() {
        if (!enabled) {
            return;
        }
        executeScript("db/004_seed_search_test_data.sql", "搜索");
        executeScript("db/007_course_video_progress.sql", "课程视频字段");
        executeScript("db/006_learning_mall_demo.sql", "学习资源与积分商城");
        executeScript("db/008_course_watched_position.sql", "课程观看位置字段");
        executeScript("db/009_mall_redemption_code.sql", "商城兑换码字段");
        executeScript("db/010_course_interaction.sql", "课程评论弹幕课件");
        executeScript("db/011_course_comment_reply_materials.sql", "评论回复与课件文件");
        executeScript("db/012_course_episodes_checkin_mall.sql", "课程分集打卡与商城审核");
    }

    private void executeScript(String path, String description) {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource(path));
            populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
            populator.setSeparator(";");
            populator.setCommentPrefix("--");
            populator.execute(dataSource);
            log.info("{}测试数据已加载", description);
        } catch (Exception e) {
            log.warn("{}测试数据加载失败: {}", description, e.getMessage());
        }
    }
}
