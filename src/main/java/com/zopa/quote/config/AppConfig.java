package com.zopa.quote.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan({ "com.zopa.quote.*"})
@PropertySource({"classpath:application.properties"})
public class AppConfig {

}
