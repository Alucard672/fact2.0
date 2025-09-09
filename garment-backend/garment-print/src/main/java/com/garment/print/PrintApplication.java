package com.garment.print;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 打印服务模块启动类
 *
 * @author system
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.garment"})
@MapperScan("com.garment.print.mapper")
public class PrintApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PrintApplication.class, args);
    }
}




