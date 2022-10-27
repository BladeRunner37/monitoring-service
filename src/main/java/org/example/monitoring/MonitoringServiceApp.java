package org.example.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.example.monitoring")
public class MonitoringServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(MonitoringServiceApp.class, args);
    }
}
