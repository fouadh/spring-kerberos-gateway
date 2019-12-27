package com.fha.kerberos.scg.security.kerberos;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Emulates the behavior of SpnegoEntryPoint since forwarding doesn't seem to work in reactive world.
 *
 * Put it before the login page filter so that the header is correctly filled.
 *
 * @see org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint
 */
public class ReactiveSpnegoAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
  private WebSessionServerRequestCache cache = new WebSessionServerRequestCache();

  private static final String LOGIN_PAGE = "\n" +
      "<!DOCTYPE html>\n" +
      "<html lang=\"en\">\n" +
      "  <head>\n" +
      "    <meta charset=\"utf-8\">\n" +
      "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n" +
      "    <meta name=\"description\" content=\"\">\n" +
      "    <meta name=\"author\" content=\"\">\n" +
      "    <title>Please sign in</title>\n" +
      "    <link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M\" crossorigin=\"anonymous\">\n" +
      "    <link href=\"https://getbootstrap.com/docs/4.0/examples/signin/signin.css\" rel=\"stylesheet\" crossorigin=\"anonymous\"/>\n" +
      "  </head>\n" +
      "  <body>\n" +
      "     <div class=\"container\">\n" +
      "      <form class=\"form-signin\" method=\"post\" action=\"/login\">\n" +
      "        <h2 class=\"form-signin-heading\">Please sign in</h2>\n" +
      "        <p>\n" +
      "          <label for=\"username\" class=\"sr-only\">Username</label>\n" +
      "          <input type=\"text\" id=\"username\" name=\"username\" class=\"form-control\" placeholder=\"Username\" required autofocus>\n" +
      "        </p>\n" +
      "        <p>\n" +
      "          <label for=\"password\" class=\"sr-only\">Password</label>\n" +
      "          <input type=\"password\" id=\"password\" name=\"password\" class=\"form-control\" placeholder=\"Password\" required>\n" +
      "        </p>\n" +
      "          <input type=\"hidden\" name=\"_csrf\" value=\"a16099e7-ed3a-4841-b9f5-8be503fad20e\">\n" +
      "        <button class=\"btn btn-lg btn-primary btn-block\" type=\"submit\">Sign in</button>\n" +
      "      </form>\n" +
      "    </div>\n" +
      "  </body>\n" +
      "</html>";

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
    return cache.saveRequest(exchange)
              .then(Mono.defer(() -> {
                exchange.getResponse().getHeaders().set("WWW-Authenticate", "Negotiate");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);

                // integrate the login page in the response since it is not possible
                // to forward to it from ServerWebExchange
                byte[] bytes = LOGIN_PAGE.getBytes(StandardCharsets.UTF_8);
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                return Mono.just(buffer);
              })).flatMap(buffer -> exchange.getResponse().writeWith(Flux.just(buffer)));
  }

  public static void main(String[] args) {
    Flux.defer(() -> Flux.just(10));
  }
}
