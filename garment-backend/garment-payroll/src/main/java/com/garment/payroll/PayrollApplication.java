package com.garment.payroll;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 计件工资模块启动类
 *
 * @author system
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.garment"})
@MapperScan("com.garment.payroll.mapper")
@EnableScheduling
@EnableAsync
public class PayrollApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PayrollApplication.class, args);
    }
}




