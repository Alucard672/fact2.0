package com.garment.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 服装生产管理系统 - 主启动类
 *
 * @author garment
 */
@SpringBootApplication
@ComponentScan(
        basePackages = {"com.garment.admin", "com.garment.common"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.garment\\.auth\\..*")
        }
)
@MapperScan("com.garment.admin.mapper")
public class GarmentAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(GarmentAdminApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("服装计件生产管理系统启动成功！");
        System.out.println("接口文档: http://localhost:8080/swagger-ui/");
        System.out.println("健康检查: http://localhost:8080/api/health");
        System.out.println("========================================\n");
    }
}