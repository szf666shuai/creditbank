package com.creditbank.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchTestDataSeeder {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed-search-data:true}")
    private boolean enabled;

    private static final Map<String, List<String>> REQUIRED_SCHEMA = Map.of(
            "course", List.of("org_id", "publisher_id", "title", "description", "cover_url", "price_credit", "duration_hours", "credit_reward", "status"),
            "course_tag", List.of("course_id", "tag_id"),
            "activity", List.of("org_id", "publisher_id", "title", "description", "location", "start_time", "end_time", "max_participants", "credit_reward", "status"),
            "forum_post", List.of("board_id", "user_id", "title", "content", "status"),
            "job_posting", List.of("org_id", "publisher_id", "title", "description", "requirements", "salary_range", "location", "edu_requirement", "status"),
            "policy_news", List.of("title", "content", "source", "author", "status", "publish_time"),
            "mall_product", List.of("category_id", "name", "description", "cover_url", "product_type", "price_credit", "status"),
            "org_material", List.of("org_id", "publisher_id", "title", "description", "file_url", "material_type", "status"),
            "sys_tag", List.of("id", "name")
    );

    @EventListener(ApplicationReadyEvent.class)
    public void seedSearchTestData() {
        if (!enabled) {
            return;
        }
        List<String> missingSchema = findMissingSchema();
        if (!missingSchema.isEmpty()) {
            log.info("搜索测试数据跳过：当前数据库缺少表或字段 {}", missingSchema);
            return;
        }
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/004_seed_search_test_data.sql"));
            populator.setSeparator(";");
            populator.setCommentPrefix("--");
            populator.execute(dataSource);
            log.info("搜索测试数据已加载（前缀 [测试]）");
        } catch (Exception e) {
            log.warn("搜索测试数据加载失败: {}", rootMessage(e), e);
        }
    }

    private List<String> findMissingSchema() {
        return REQUIRED_SCHEMA.entrySet().stream()
                .flatMap(entry -> {
                    String table = entry.getKey();
                    if (!tableExists(table)) {
                        return List.of(table).stream();
                    }
                    return entry.getValue().stream()
                            .filter(column -> !columnExists(table, column))
                            .map(column -> table + "." + column);
                })
                .toList();
    }

    private boolean tableExists(String table) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Long.class,
                table);
        return count != null && count > 0;
    }

    private boolean columnExists(String table, String column) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Long.class,
                table,
                column);
        return count != null && count > 0;
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage();
    }
}
