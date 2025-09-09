package com.garment.production;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 生产管理模块启动类
 *
 * @author system
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.garment"})
@MapperScan("com.garment.production.mapper")
public class ProductionApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ProductionApplication.class, args);
    }
}




