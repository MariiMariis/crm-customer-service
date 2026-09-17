package com.pb.notification;

import com.pb.notification.config.NotificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NotificationProperties.class)
public class NotificationServiceApplication {

    private static final String UNIX_DOMAIN_TMPDIR = "jdk.net.unixdomain.tmpdir";

    public static void main(String[] args) {
        configureWindowsLoopbackTempDir();
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    static void configureWindowsLoopbackTempDir() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().startsWith("windows");
        if (windows && System.getProperty(UNIX_DOMAIN_TMPDIR) == null) {
            String systemRoot = System.getenv().getOrDefault("SystemRoot", "C:\\Windows");
            System.setProperty(UNIX_DOMAIN_TMPDIR, systemRoot + "\\Temp");
        }
    }
}
