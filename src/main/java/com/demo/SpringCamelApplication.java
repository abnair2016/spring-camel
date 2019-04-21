package com.demo;

import org.apache.camel.spring.boot.CamelSpringBootApplicationController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringCamelApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SpringCamelApplication.class, args);

        CamelSpringBootApplicationController applicationController = ctx.getBean(CamelSpringBootApplicationController.class);
        applicationController.run();

    }
}