package com.internship.gateway_service.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SwaggerRoutingService implements ApplicationListener<HeartbeatEvent> {

    private final SwaggerUiConfigProperties swaggerUiConfigProperties;
    private final SwaggerUiConfigParameters swaggerUiConfigParameters;
    private final DiscoveryClient discoveryClient;

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String apiDocsPath;

    @Override
    public void onApplicationEvent(@NonNull HeartbeatEvent event) {
        updateSwaggerUrls();
    }

    private void updateSwaggerUrls() {
        Set<SwaggerUrl> urls = new HashSet<>();

        discoveryClient.getServices().forEach(serviceId -> {
            String servicePathPrefix = serviceId.toLowerCase();
            String gatewayUrl = "/" + servicePathPrefix + apiDocsPath;

            SwaggerUrl swaggerUrl = new SwaggerUrl(serviceId, gatewayUrl, serviceId);
            urls.add(swaggerUrl);
        });

        swaggerUiConfigProperties.setUrls(urls);
        swaggerUiConfigParameters.setUrls(urls);
    }
}
