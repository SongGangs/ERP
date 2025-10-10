package com.jsh.erp;

import com.jsh.erp.utils.ComputerInfo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

/**
 * ERP 系统主应用程序入口
 * <p>启动 Spring Boot 应用并初始化所有组件</p>
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.jsh.erp.datasource.mappers")
@ServletComponentScan
@EnableScheduling
@EnableAsync
public class ErpApplication{
    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(ErpApplication.class, args);
        Environment environment = context.getBean(Environment.class);

        String port = environment.getProperty("server.port");
        String apiUrl = String.format("http://%s:%s/erp-boot/doc.html", ComputerInfo.getIpAddr(), port);

        log.info("========================================");
        log.info("启动成功，后端服务API地址：{}", apiUrl);
        log.info("您还需启动前端服务，启动命令：yarn run serve 或 npm run serve");
        log.info("========================================");
    }
}
