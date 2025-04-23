package com.internship.gateway_service.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutingService implements ApplicationListener<HeartbeatEvent> {

    private final RouteDefinitionWriter routeDefinitionWriter;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DiscoveryClient discoveryClient;

    @Override
    public void onApplicationEvent(@NonNull HeartbeatEvent event) {
        List<String> services = discoveryClient.getServices();

        for (String service : services) {
            RouteDefinition definition = new RouteDefinition();
            definition.setId(service + "-route");
            definition.setUri(URI.create("lb://" + service));

            PredicateDefinition predicate = new PredicateDefinition();
            predicate.setName("Path");
            predicate.addArg("pattern", "/" + service.toLowerCase() + "/**");
            definition.setPredicates(List.of(predicate));

            FilterDefinition stripPrefix = new FilterDefinition();
            stripPrefix.setName("StripPrefix");
            stripPrefix.addArg("parts", "1");

            FilterDefinition circuitBreaker = new FilterDefinition();
            circuitBreaker.setName("CircuitBreaker");
            circuitBreaker.addArg("name", service + "CB");
            circuitBreaker.addArg("fallbackUri", "forward:/fallback");

            definition.setFilters(List.of(stripPrefix, circuitBreaker));

            routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        }

        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }
}
