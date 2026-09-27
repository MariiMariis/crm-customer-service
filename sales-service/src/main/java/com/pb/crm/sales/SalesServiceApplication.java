package com.pb.crm.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SalesServiceApplication {

    private static final String UNIX_DOMAIN_TMPDIR = "jdk.net.unixdomain.tmpdir";

    public static void main(String[] args) {
        configureWindowsLoopbackTempDir();
        SpringApplication.run(SalesServiceApplication.class, args);
    }

    static void configureWindowsLoopbackTempDir() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().startsWith("windows");
        if (windows && System.getProperty(UNIX_DOMAIN_TMPDIR) == null) {
            String systemRoot = System.getenv().getOrDefault("SystemRoot", "C:\\Windows");
            System.setProperty(UNIX_DOMAIN_TMPDIR, systemRoot + "\\Temp");
        }
    }
}
