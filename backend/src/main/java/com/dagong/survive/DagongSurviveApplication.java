package com.dagong.survive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.dagong.survive.config.GameProperties;

@SpringBootApplication
@EnableConfigurationProperties(GameProperties.class)
public class DagongSurviveApplication {

    public static void main(String[] args) {
        SpringApplication.run(DagongSurviveApplication.class, args);
    }
}
