# Introduction

This repository contains a set of applications to experiment with spring security kerberos.

The goals of these experimentations are:

* use spring-security-kerberos with Spring Boot 2 (existing samples are only based on Spring Boot 1) 
* integrate spring-security-kerberos in Zuul (with spring boot 2)
* integrate spring-security-kerberos in Spring Cloud Gateway (with spring boot 2)

# Applications

## minikdc

This application is a Kerberos KDC used for local testing.

### Build the app

```
cd applications/minikdc
mvn clean package
```

### Run the app

```
mvn exec:java -Dexec.mainClass=com.fha.minikdc.Application -Dexec.args=[PATH]
```

* **PATH**: Path to where the configuration and data files will be generated. Default value: `/tmp/minikdc`

When testing with the MiniKDC application, the configuration files will be needed to test the gateways:

* `krb5.conf`
* `example.keytab`

Two principals are created and can be used for testing the gateways:

* `client/localhost`
* `HTTP/localhost`

Their password can be found in the application logs after startup. Look for something like this:

```
2019-12-27 10:14:08 DEBUG LdifReader:1767 - next(): -- returning ldif dn: uid=HTTP/localhost,ou=users,dc=example,dc=com
uid: HTTP/localhost
krb5keyversionnumber: 0
cn: HTTP/localhost
sn: HTTP/localhost
objectclass: top
objectclass: person
objectclass: inetOrgPerson
objectclass: krb5principal
objectclass: krb5kdcentry
userpassword: a017ab75-3a1c-444e-949f-f2c37eb851c9
krb5principalname: HTTP/localhost@EXAMPLE.COM
```

The value that interests you is `userpassword`.

These principals are the ones to use to test the gateways with MiniKDC.

---

## some-service

This is a simple Spring Boot app with one controller endpoint. It is used to fake a downstream service that will be 
reached from a gateway implementing Kerberos security.

In a real environment, such an application would probably be in a private and secured network since it is not directly secured.

### Run the app

```
cd applications/some-service
mvn spring-boot:run
```

The app will be listening on port 9090 by default.

---

## kerberos-zuul

This app integrates Spring Boot 2, Zuul and Spring Security Kerberos.

### Run the app

```
cd applications/kerberos-zuul
SERVICE_ENDPOINT=http://some-service SERVICE_PATH=/some-path KRB5_CONF=/tmp/somewhere/krb5.conf SERVICE_PRINCIPAL=service/localhost KEYTAB_LOCATION=/tmp/somelocation mvn spring-boot:run
```

* **KRB5_CONF**: path to a `krb5` configuration file. If testing with MiniKDC application, it will be the `krb5.conf` file.
* **KEYTAB_LOCATION**: path to a `keytab` configuration file. If testing with MiniKDC application, it will be the `example.keytab` file.
* **SERVICE_ENDPOINT**: the url to the downstream service. The gateway will redirect the user to this endpoint if 
the authentication is ok and the url path matches the `SERVICE_PATH` value. Default value: `http://localhost:9090`.
* **SERVICE_PATH**: the path to the downstream service from the gateway. Default value: `/some-service`.
* **SERVICE_PRINCIPAL**: the principal representing the service. Is it really useful ???. Default value: `HTTP/localhost`.

The app will be listening on port 8080 by default.

### Dockerization

```
docker build . -t kerberos-zuul
docker run -it -p9999:8080 -v [LOCAL_CONF_DIR]:/conf -e SERVICE_ENDPOINT=[SERVICE_ENDPOINT_REACHABLE_FROM_CONTAINER] -e SERVICE_PRINCIPAL=[SERVICE_PRINCIPAL] -e SERVICE_PATH=[SERVICE_PATH] kerberos-zuul
```

* **LOCAL_CONF_DIR**: local directory containing a `krb5.conf` and `gateway.keytab` configuration files. Their names can 
also be overriden with `KRB5_CONF` and `KEYTAB_LOCATION` environment variables. If using the MiniKDC application, `gateway.keytab`
content must be the same than `example.keytab` and `krb5.conf` must be copied and modified to reference a valid KDC address.
* **SERVICE_ENDPOINT**: the url to the downstream service. The gateway will redirect the user to this endpoint if 
the authentication is ok and the url path matches the `SERVICE_PATH` value.
* **SERVICE_PATH**: the path to the downstream service from the gateway.
* **SERVICE_PRINCIPAL**: the principal representing the service. Is it really useful ???

### Conclusion of the experimentation

spring-security-kerberos integrates very well with Zuul and Spring Boot 2. The configuration is straightforward and the
same as if you were working with Spring Boot 1.

---

## kerberos-spring-cloud-gateway

This app integrates Spring Cloud Gateway and Spring Security Kerberos.

### Run the app

```
cd applications/kerberos-spring-cloud-gateway
SERVICE_ENDPOINT=http://some-service SERVICE_PATH=/some-path KRB5_CONF=/tmp/somewhere/krb5.conf SERVICE_PRINCIPAL=service/localhost KEYTAB_LOCATION=/tmp/somelocation mvn spring-boot:run
```

The configuration is the same than the Zuul gateway.

The app will be listening on port 8080 by default.

### Dockerization

```
docker build . -t kerberos-scg
docker run -it -p9999:8080 -v [LOCAL_CONF_DIR]:/conf -e SERVICE_ENDPOINT=[SERVICE_ENDPOINT_REACHABLE_FROM_CONTAINER] -e SERVICE_PRINCIPAL=[SERVICE_PRINCIPAL] -e SERVICE_PATH=[SERVICE_PATH] kerberos-scg
```

The configuration is the same than the Zuul gateway.

SERVICE_ENDPOINT=http://localhost:9090 SERVICE_PATH=/toto KRB5_CONF=/Users/fhamdi/tmp/kerberos/krb5-local.conf SERVICE_PRINCIPAL=HTTP/localhost KEYTAB_LOCATION=/Users/fhamdi/tmp/kerberos/gateway.keytab mvn spring-boot:run

### Conclusion of the experimentation
 
spring-security-kerberos doesn't integrate in a straightforward way with Spring Cloud Gateway. The reason is that SCG is built on
Webflux and spring security must be configured in a reactive fashion.

If you try to configure the security in a non reactive way, yow will run in this 
[issue](https://github.com/spring-cloud/spring-cloud-gateway/issues/1356).

To integrate spring-security-kerberos, the following adaptations have been made:

1. Create a custom authentication entry point named `ReactiveSpnegoAuthenticationEntryPoint`
 
The goal of this entry point is to:

* set the http header `WWW-Authentication` with `Negotiate` value.
* save in the session the request that has been performed 

Note: it is not done in the non reactive version of this entry point since it is
managed by `ExceptionTranslationFilter`. In the reactive world,  `ExceptionTranslationWebFilter`
does not manage this cache

* populate the response body with the login page. 

In the non reactive world, a simple request forward is done which is not possible with Spring Webflux.

2. Create a custom filter named `ReactiveSpnegoAuthenticationProcessingFilter`

It is the equivalent of `SpnegoAuthenticationProcessingFilter` filter but in the reactive world.

Note that this version is not as configurable as the original one but it should do the job
with its defaults.

For a production-ready version, there will probably need some deeper investigation to check if this level of configuration
is good enough or not.

# References

* [Spring Security Kerberos](https://docs.spring.io/spring-security-kerberos/docs/1.0.1.RELEASE/reference/htmlsingle/)
* [Spring Security Reactive](https://docs.spring.io/spring-security/site/docs/5.2.1.RELEASE/reference/htmlsingle/#reactive-applications)
* [Spring Cloud Gateway](https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.1.RELEASE/reference/html/)
* [Spring Cloud Netflix - Zuul](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.2.1.RELEASE/reference/html/#router-and-filter-zuul)
* [Introduction to SPNEGO/Kerberos Authentication in Spring](https://www.baeldung.com/spring-security-kerberos)
* [Spring Security Kerberos Integration](https://www.baeldung.com/spring-security-kerberos-integration)