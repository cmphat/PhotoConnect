package com.photoconnect.config;

import jakarta.servlet.DispatcherType;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.EnumSet;

/**
 * Registers SiteMesh 3 Filter in Spring Boot 3.3 / Jakarta EE 10.
 * Configures decorator mappings, exclusions, and ensures dispatcher handling
 * for both REQUEST and FORWARD dispatch types.
 */
@Configuration
public class SiteMeshConfig {

    public static final String DECORATOR_PREFIX = "/WEB-INF/decorators/";
    public static final String DEFAULT_DECORATOR = "default.jsp";
    public static final String ADMIN_DECORATOR = "admin.jsp";

    public static void configureSiteMesh(SiteMeshFilterBuilder builder) {
        builder.setDecoratorPrefix(DECORATOR_PREFIX)
               .addExcludedPath("/assets/*")
               .addExcludedPath("/ws/*")
               .addExcludedPath("/api/*")
               .addExcludedPath("/error")
               .addExcludedPath("/error/*")
               .addDecoratorPath("/admin/*", ADMIN_DECORATOR)
               .addDecoratorPath("/*", DEFAULT_DECORATOR);
    }

    @Bean
    public FilterRegistrationBean<ConfigurableSiteMeshFilter> siteMeshFilter() {
        FilterRegistrationBean<ConfigurableSiteMeshFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setFilter(new ConfigurableSiteMeshFilter() {
            @Override
            protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
                configureSiteMesh(builder);
            }
        });
        filterBean.addUrlPatterns("/*");
        filterBean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
        filterBean.setOrder(Ordered.LOWEST_PRECEDENCE);
        return filterBean;
    }
}
