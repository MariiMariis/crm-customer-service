package com.pb.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    private static final String UNIX_DOMAIN_TMPDIR = "jdk.net.unixdomain.tmpdir";

    public static void main(String[] args) {
        configureWindowsLoopbackTempDir();
        SpringApplication.run(ConfigServerApplication.class, args);
    }

    static void configureWindowsLoopbackTempDir() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().startsWith("windows");
        if (windows && System.getProperty(UNIX_DOMAIN_TMPDIR) == null) {
            String systemRoot = System.getenv().getOrDefault("SystemRoot", "C:\\Windows");
            System.setProperty(UNIX_DOMAIN_TMPDIR, systemRoot + "\\Temp");
        }
    }
}
