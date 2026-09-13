package com.blog.base;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Blog base services entry point.
 */
@SpringBootApplication
@MapperScan("com.blog.base.**.mapper")
public class BaseServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaseServicesApplication.class, args);
    }
}
