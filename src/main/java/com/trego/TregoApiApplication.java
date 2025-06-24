package com.trego;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement

public class TregoApiApplication {

    private static final Logger log = LoggerFactory.getLogger(TregoApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TregoApiApplication.class, args);
    }

}