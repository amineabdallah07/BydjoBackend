package com.bydjo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.cors.allowed-origins:http://localhost:4200,http://localhost:3000,https://khlayelstore.vercel.app}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        java.nio.file.Path absolutePath = java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath.toString() + "/");
    }
}