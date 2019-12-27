package com.fha.kerberos.gateway.zuul;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.util.StringUtils;

@SpringBootApplication
@EnableZuulProxy
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        String krb5ConfPath = "/tmp/minikdc/krb5.conf";
        String confVariable = System.getenv("KRB5_CONF");
        if (StringUtils.hasText(confVariable))
            krb5ConfPath = confVariable;

        LOGGER.info("-----> KRB5 CONF: {}", krb5ConfPath);
        System.setProperty("java.security.krb5.conf",
                           Paths.get(krb5ConfPath)
                                .normalize().toAbsolutePath().toString());
        System.setProperty("sun.security.krb5.debug", "true");

        SpringApplication.run(Application.class, args);
    }
}
