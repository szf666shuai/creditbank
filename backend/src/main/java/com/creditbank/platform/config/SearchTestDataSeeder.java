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
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/004_seed_search_test_data.sql"));
            populator.addScript(new ClassPathResource("db/006_seed_learning_profile_demo.sql"));
            populator.setSeparator(";");
            populator.setCommentPrefix("--");
            populator.execute(dataSource);
            log.info("搜索与学习画像演示数据已加载（前缀 [测试]/[画像演示]）");
        } catch (Exception e) {
            log.warn("搜索测试数据加载失败: {}", e.getMessage());
        }
    }
}
