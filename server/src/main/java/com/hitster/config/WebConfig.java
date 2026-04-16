package com.hitster.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @SuppressWarnings("null")
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/game/**")
                .addPathPatterns("/lobby/**")
                .addPathPatterns("/stats/**")
                .excludePathPatterns("/auth/login")
                .excludePathPatterns("/auth/login")
                .excludePathPatterns("/auth/register")
                .excludePathPatterns("/stats/leaderboard");
    }
}