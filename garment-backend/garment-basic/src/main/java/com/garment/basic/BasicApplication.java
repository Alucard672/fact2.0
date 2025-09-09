package com.garment.basic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 基础数据管理服务启动类
 *
 * @author garment
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.garment"})
@MapperScan(basePackages = {"com.garment.basic.mapper"})
public class BasicApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicApplication.class, args);
    }
}