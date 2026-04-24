package com.hitster.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path audioDir = Paths.get(
                "server",
                "uploads",
                "audio"
        ).toAbsolutePath();

        String audioPath = audioDir.toUri().toString();

        System.out.println("Serving audio from: " + audioPath);
        System.out.println("Audio dir exists? " + audioDir.toFile().exists());
        System.out.println("sample-3 exists? " + audioDir.resolve("sample-3.mp3").toFile().exists());

        registry.addResourceHandler("/audio/**")
                .addResourceLocations(audioPath);
    }
}