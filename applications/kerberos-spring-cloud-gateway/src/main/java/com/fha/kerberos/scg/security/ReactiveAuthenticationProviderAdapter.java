package com.fha.kerberos.scg.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

/**
 * Adapt on non reactive authentication provider to the reactive world.
 */
public class ReactiveAuthenticationProviderAdapter implements ReactiveAuthenticationManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveAuthenticationProviderAdapter.class);
  private AuthenticationProvider authenticationProvider;

  public ReactiveAuthenticationProviderAdapter(AuthenticationProvider kerberosServiceAuthenticationProvider) {
    authenticationProvider = kerberosServiceAuthenticationProvider;
  }

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    LOGGER.info("----> Authenticate");
    return Mono.just(authenticationProvider.authenticate(authentication));
  }
}
