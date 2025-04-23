package com.internship.gateway_service.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SwaggerGatewayConfig {

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String apiDocsPath;

    @Value("${spring.cloud.gateway.discovery.locator.lower-case-service-id:true}")
    private boolean lowerCaseServiceId;

    @Bean
    @Primary
    public SwaggerUiConfigParameters swaggerUiConfigParameters(
            SwaggerUiConfigProperties swaggerUiConfigProperties,
            DiscoveryClient discoveryClient) {

        Set<SwaggerUrl> urls = new HashSet<>();

        if (swaggerUiConfigProperties.getUrls() != null) {
            urls.addAll(swaggerUiConfigProperties.getUrls());
        }

        discoveryClient.getServices().forEach(serviceId -> {
            String servicePathPrefix = lowerCaseServiceId ? serviceId.toLowerCase() : serviceId;
            String gatewayUrl = "/" + servicePathPrefix + apiDocsPath;

            SwaggerUrl swaggerUrl = new SwaggerUrl(serviceId, gatewayUrl, serviceId);

            if (urls.stream().noneMatch(url -> url.getName() != null && url.getName().equals(serviceId))) {
                urls.add(swaggerUrl);
            }
        });

        swaggerUiConfigProperties.setUrls(urls);

        return new SwaggerUiConfigParameters(swaggerUiConfigProperties);
    }
}
