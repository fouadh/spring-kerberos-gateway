package org.springframework.security.kerberos.reactive.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * SpnegoAuthenticationProcessingFilter in the reactive world.
 */
public class ReactiveSpnegoAuthenticationProcessingFilter implements WebFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveSpnegoAuthenticationProcessingFilter.class);

  private ReactiveAuthenticationManager authenticationManager;
  private boolean skipIfAlreadyAuthenticated = true;

  public ReactiveSpnegoAuthenticationProcessingFilter(ReactiveAuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

    if (skipIfAlreadyAuthenticated) {
      Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
      if (existingAuth != null && existingAuth.isAuthenticated()
          && (existingAuth instanceof AnonymousAuthenticationToken) == false) {
        return chain.filter(exchange);
      }
    }

    ServerHttpRequest request = exchange.getRequest();
    List<String> authorization = request.getHeaders().get("Authorization");

    if (authorization != null && !authorization.isEmpty()) {
      String header = authorization.get(0);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Received Negotiate Header for request " + request.getURI() + ": " + header);
      }

      try {
        byte[] base64Token = header.substring(header.indexOf(" ") + 1).getBytes("UTF-8");
        byte[] kerberosTicket = Base64.decode(base64Token);
        KerberosServiceRequestToken authenticationRequest = new KerberosServiceRequestToken(kerberosTicket);
        authenticationRequest.setDetails(new WebAuthenticationDetails(exchange));
        Authentication authentication = authenticationManager.authenticate(authenticationRequest).block();
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("boom", e);
      } catch (AuthenticationException e) {
        // That shouldn't happen, as it is most likely a wrong
        // configuration on the server side
        LOGGER.warn("Negotiate Header was invalid: " + header, e);
        SecurityContextHolder.clearContext();
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    return chain.filter(exchange);
  }

}
