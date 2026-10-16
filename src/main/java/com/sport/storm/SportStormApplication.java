package com.sport.storm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sport.storm.mapper")
public class SportStormApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportStormApplication.class, args);
    }
}