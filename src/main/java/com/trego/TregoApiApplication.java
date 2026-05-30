package com.trego;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement

public class TregoApiApplication {



    public static void main(String[] args) {
        SpringApplication.run(TregoApiApplication.class, args);
    }

} 