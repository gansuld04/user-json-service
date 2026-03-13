package com.lab06.jsonservice.config;

import com.lab06.jsonservice.middleware.AuthMiddleware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ================================================================
 * WebConfig - Web тохиргоо
 * ================================================================
 * 1. AuthMiddleware-г /users/** замд бүртгэнэ
 * 2. CORS тохируулна - frontend (port 3000) холбогдох боломжтой болно
 * ================================================================
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthMiddleware authMiddleware;

    /**
     * AuthMiddleware-г Spring Filter-д бүртгэх
     * /users/* бүх зам дээр ажиллана
     */
    @Bean
    public FilterRegistrationBean<AuthMiddleware> authFilterRegistration() {
        FilterRegistrationBean<AuthMiddleware> registration = new FilterRegistrationBean<>();
        registration.setFilter(authMiddleware);

        // Зөвхөн /users/** замд ажиллана
        registration.addUrlPatterns("/users/*");

        // Filter-ийн дараалал (бага тоо = эрт ажиллана)
        registration.setOrder(1);

        return registration;
    }

    /**
     * CORS тохиргоо - Next.js frontend-г зөвшөөрөх
     * Frontend port 3000 дээр ажиллах тул localhost:3000-г зөвшөөрнө
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Next.js dev server
                .allowedOrigins("http://localhost:3000")
                // Бүх HTTP methods зөвшөөрөх
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // Authorization header зөвшөөрөх
                .allowedHeaders("*")
                // Credentials (cookie) зөвшөөрөх
                .allowCredentials(true);
    }
}