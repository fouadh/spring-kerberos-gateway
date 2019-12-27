package com.fha.kerberos.scg;

import com.fha.kerberos.scg.security.DummyUserDetailsService;
import com.fha.kerberos.scg.security.ReactiveAuthenticationProviderAdapter;
import com.fha.kerberos.scg.security.kerberos.ReactiveSpnegoAuthenticationEntryPoint;
import com.fha.kerberos.scg.security.kerberos.ReactiveSpnegoAuthenticationProcessingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosClient;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

  @Value("${gateway.service-principal}")
  private String servicePrincipal;

  @Value("${gateway.keytab-location}")
  private String keytabLocation;

  @Bean
  SecurityWebFilterChain authorization(ServerHttpSecurity http) {
    return http
        .csrf().disable()
        .exceptionHandling()
        .authenticationEntryPoint(new ReactiveSpnegoAuthenticationEntryPoint())
        .and()
        .authorizeExchange(e -> e.anyExchange().authenticated())
        .formLogin()
        .and()
        .addFilterBefore(new ReactiveSpnegoAuthenticationProcessingFilter(authenticationManager()), SecurityWebFiltersOrder.HTTP_BASIC) // todo: how to test ? auth headers always empty in this filter !!!
        .build();
  }


  @Bean
  KerberosAuthenticationProvider kerberosAuthenticationProvider() {
    KerberosAuthenticationProvider provider = new KerberosAuthenticationProvider();
    SunJaasKerberosClient client = new SunJaasKerberosClient();
    client.setDebug(true);
    provider.setKerberosClient(client);
    provider.setUserDetailsService(dummyUserDetailsService());
    return provider;
  }

  @Bean
  KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() {
    KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
    provider.setTicketValidator(sunJaasKerberosTicketValidator());
    provider.setUserDetailsService(dummyUserDetailsService());
    return provider;
  }

  @Bean
  SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() {
    LOGGER.info("----> servicePrincipal: {}", servicePrincipal);
    LOGGER.info("----> keytabLocation: {}", keytabLocation);
    SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
    ticketValidator.setServicePrincipal(servicePrincipal);
    ticketValidator.setKeyTabLocation(new FileSystemResource(keytabLocation));
    ticketValidator.setDebug(true);
    return ticketValidator;
  }

  @Bean
  ReactiveAuthenticationManager authenticationManager() {
    return new DelegatingReactiveAuthenticationManager(
        new ReactiveAuthenticationProviderAdapter(kerberosAuthenticationProvider()),
        new ReactiveAuthenticationProviderAdapter(kerberosServiceAuthenticationProvider())
    );
  }

  @Bean
  DummyUserDetailsService dummyUserDetailsService() {
    return new DummyUserDetailsService();
  }
}
