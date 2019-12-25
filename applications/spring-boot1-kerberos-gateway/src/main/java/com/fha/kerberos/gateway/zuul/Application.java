package com.fha.kerberos.gateway.zuul;

import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class Application {
    public static void main(String[] args) {
        System.setProperty("java.security.krb5.conf",
                           Paths.get("/tmp/minikdc/krb5.conf")
                                .normalize().toAbsolutePath().toString());
        System.setProperty("sun.security.krb5.debug", "true");

        SpringApplication.run(Application.class, args);
    }
}
