package com.fha.kerberos.scg.security.kerberos;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Emulates the behavior of SpnegoEntryPoint since forwarding doesn't seem to work in reactive world.
 *
 * Put it before the login page filter so that the header is correctly filled.
 */
public class KerberosNegotiateFilter implements WebFilter {
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    exchange.getResponse().getHeaders().set("WWW-Authenticate", "Negotiate");
    return chain.filter(exchange);
  }
}
