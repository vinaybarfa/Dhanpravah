package com.vinay.dhanpravah.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final List<String> allowedOrigins;
    private final boolean requireHttps;


    public CorsConfig(
            @Value("${cors.allowed-orgins}") List<String> allowedOrigins,
            @Value("${cors.required-https:false}") boolean requireHttps) {
        this.requireHttps = requireHttps;
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            throw new IllegalArgumentException("cors.allowed-origins must contain at least one origin");
        }
        List<String> validateOrigins = new ArrayList<>();
        for (String configuredOrigin : allowedOrigins) {
            String origin = configuredOrigin == null ? "" : configuredOrigin.trim();
            if (origin.isEmpty() || "*".equals(origin)) {
                throw new IllegalArgumentException(
                        "cors.allowed-origins must contain explicit origins; wildcards are not allowed"
                );
            }

            URI uri;
            uri = URI.create(origin);
            boolean secureScheme = "https".equalsIgnoreCase(uri.getScheme());
            if (!(secureScheme || (!requireHttps && "http".equalsIgnoreCase(uri.getScheme())))
                    || uri.getHost() == null
                    || uri.getPort() > 65535
                    || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment()  != null
                    || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())
            )  {
                throw new IllegalArgumentException(
                        "CORS entries must be exact http(s) origins without paths: "+origin
                );
            }
            validateOrigins.add(origin);
        }
        this.allowedOrigins = Collections.unmodifiableList(validateOrigins);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept")
                .maxAge(3600);
    }
}
