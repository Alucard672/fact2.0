package com.garment.stats;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 统计分析模块启动类
 *
 * @author system
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.garment"})
@MapperScan("com.garment.stats.mapper")
@EnableScheduling
@EnableAsync
public class StatsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(StatsApplication.class, args);
    }
}




