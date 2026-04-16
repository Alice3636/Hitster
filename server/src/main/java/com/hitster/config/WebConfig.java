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
                .addPathPatterns("/game/**") // PROTECT all gameplay endpoints
                .addPathPatterns("/lobby/**") // PROTECT lobby endpoints
                .addPathPatterns("/stats/**") // PROTECT stats endpoints
                .excludePathPatterns("/auth/login") // DO NOT protect login
                .excludePathPatterns("/auth/register"); // DO NOT protect register
    }
}