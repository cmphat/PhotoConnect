package com.photoconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class PhotoConnectApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PhotoConnectApplication.class);
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        java.util.Locale.setDefault(java.util.Locale.US);
    }

    public static void main(String[] args) {
        java.util.Locale.setDefault(java.util.Locale.US);
        SpringApplication.run(PhotoConnectApplication.class, args);
    }
}
