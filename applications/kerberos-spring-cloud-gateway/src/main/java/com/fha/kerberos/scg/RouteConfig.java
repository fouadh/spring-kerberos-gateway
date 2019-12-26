package com.fha.kerberos.scg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {
  @Value("${service.path}")
  private String servicePath;
  @Value("${service.url}")
  private String serviceUrl;

  @Bean
  public RouteLocator routeLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("some-service", r -> r.path(servicePath)
            .filters(f -> f.rewritePath("^" + servicePath, ""))
            .uri(serviceUrl))
        .build();
  }
}
