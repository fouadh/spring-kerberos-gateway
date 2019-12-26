package com.fha.kerberos.scg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {String krb5ConfPath = "/tmp/minikdc/krb5.conf";
        if (args != null && args.length > 0)
            krb5ConfPath = args[0];

        LOGGER.info("-----> KRB5 CONF: {}", krb5ConfPath);
        System.setProperty("java.security.krb5.conf",
                           Paths.get(krb5ConfPath)
                                .normalize().toAbsolutePath().toString());
        System.setProperty("sun.security.krb5.debug", "true");

        SpringApplication.run(Application.class, args);
    }
}
