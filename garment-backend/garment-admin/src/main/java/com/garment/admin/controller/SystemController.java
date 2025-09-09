package com.garment.admin.controller;

import com.garment.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统控制器
 *
 * @author system
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    @Value("${garment.system.name:服装计件生产管理系统}")
    private String systemName;

    @Value("${garment.system.version:1.0.0}")
    private String systemVersion;

    @Value("${garment.system.company:优衣服装厂}")
    private String companyName;

    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", systemName);
        info.put("version", systemVersion);
        info.put("company", companyName);
        info.put("currentTime", LocalDateTime.now());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        
        return Result.success(info);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("uptime", System.currentTimeMillis());
        
        return Result.success(health);
    }

    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 业务配置
        Map<String, Object> business = new HashMap<>();
        business.put("bundleDefaultSize", 100);
        business.put("qualityThresholds", Map.of(
            "excellent", 0.98,
            "normal", 0.95,
            "poor", 0.90
        ));
        business.put("printConfig", Map.of(
            "defaultPort", 9100,
            "supportedFormats", new String[]{"PDF", "PNG", "JPG"}
        ));
        
        config.put("business", business);
        
        // 系统限制
        Map<String, Object> limits = new HashMap<>();
        limits.put("maxFileSize", "10MB");
        limits.put("maxBundleSize", 500);
        limits.put("maxWorkerPerWorkshop", 100);
        
        config.put("limits", limits);
        
        return Result.success(config);
    }
}



