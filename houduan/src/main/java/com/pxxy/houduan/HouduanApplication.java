package com.pxxy.houduan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pxxy.houduan.mapper")
public class HouduanApplication {

    public static void main(String[] args) {
        SpringApplication.run(HouduanApplication.class, args);
    }

}
