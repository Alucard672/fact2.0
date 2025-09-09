package com.garment.admin.controller;

import com.garment.common.vo.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Api(tags = "系统健康检查")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 系统健康检查
     */
    @GetMapping("/health")
    @ApiOperation("系统健康检查")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("application", "garment-production-system");
        health.put("version", "1.0.0");

        // 检查数据库连接
        try (Connection connection = dataSource.getConnection()) {
            health.put("database", Map.of(
                    "status", "UP",
                    "type", "MySQL",
                    "url", connection.getMetaData().getURL()
            ));
        } catch (Exception e) {
            health.put("database", Map.of(
                    "status", "DOWN",
                    "error", e.getMessage()
            ));
        }

        // 检查Redis连接
        try {
            redisTemplate.opsForValue().set("health:check", "ok");
            String result = redisTemplate.opsForValue().get("health:check");
            health.put("redis", Map.of(
                    "status", "UP",
                    "ping", "ok".equals(result) ? "PONG" : "ERROR"
            ));
        } catch (Exception e) {
            health.put("redis", Map.of(
                    "status", "DOWN",
                    "error", e.getMessage()
            ));
        }

        return ApiResponse.success(health);
    }
}