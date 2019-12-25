package com.fha.minikdc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import org.springframework.security.kerberos.test.MiniKdc;

public class Application {
    private static final String DEFAULT_WORK_DIR = "/tmp/krb-workdir";

    public static void main(String[] args) throws Exception {
        String workDir = DEFAULT_WORK_DIR;
        if (args != null && args.length > 0)
            workDir = args[0];
        System.setProperty("sun.security.krb5.debug", "true");
        String[] config = MiniKdcConfigBuilder.builder()
                                              .workDir(prepareWorkDir(workDir))
                                              .confDir("minikdc-krb5.conf")
                                              .keytabName("example.keytab")
                                              .principals("client/localhost", "HTTP/localhost")
                                              .build();

        MiniKdc.main(config);
    }

    private static String prepareWorkDir(final String workDirectory) throws IOException {
        Path dir = Paths.get(workDirectory);
        File directory = dir.normalize().toFile();

        FileUtils.deleteQuietly(directory);
        FileUtils.forceMkdir(directory);
        return dir.toString();
    }
}
