package com.blog.base.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Knife4j documentation metadata.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("blog-base-services API")
                .description("博客平台基础服务：标签管理 / 消息管理（站内通知+留言板）/ 地区管理")
                .version("1.0.0"));
    }
}
