package org.example.newcarre;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.newcarre.mapper")
public class NewCarreApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewCarreApplication.class, args);
    }

}
