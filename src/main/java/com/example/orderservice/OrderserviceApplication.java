package com.example.orderservice;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class OrderserviceApplication {

    private static final Logger logger = LoggerFactory.getLogger(OrderserviceApplication.class);
    @Value("${spring.data.mongodb.uri}")
    private String mongodbUri;

    public static void main(String[] args) {
        SpringApplication.run(OrderserviceApplication.class, args);
    }

    @PostConstruct
    void postConstruct() {
        logger.info("Mongodb Url received- {}", mongodbUri);
    }

}
