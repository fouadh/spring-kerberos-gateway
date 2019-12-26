package com.fha.kerberos.scg.security.kerberos;

import org.springframework.web.server.ServerWebExchange;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Objects;

class WebAuthenticationDetails implements Serializable {

  private final String remoteAddress;
  private final String sessionId;

  public WebAuthenticationDetails(ServerWebExchange exchange) {
    InetSocketAddress sockAddress = exchange.getRequest().getRemoteAddress();
    if (sockAddress != null) {
      remoteAddress = sockAddress.toString();
    } else {
      remoteAddress = null;
    }

    if (exchange.getSession().block() != null) {
      sessionId = exchange.getSession().block().getId();
    } else {
      sessionId = null;
    }
  }

  /**
   * Indicates the TCP/IP address the authentication request was received from.
   *
   * @return the address
   */
  public String getRemoteAddress() {
    return remoteAddress;
  }

  /**
   * Indicates the <code>HttpSession</code> id the authentication request was received
   * from.
   *
   * @return the session ID
   */
  public String getSessionId() {
    return sessionId;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString()).append(": ");
    sb.append("RemoteIpAddress: ").append(this.getRemoteAddress()).append("; ");
    sb.append("SessionId: ").append(this.getSessionId());

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WebAuthenticationDetails that = (WebAuthenticationDetails) o;
    return Objects.equals(remoteAddress, that.remoteAddress) &&
        Objects.equals(sessionId, that.sessionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(remoteAddress, sessionId);
  }
}
