package com.net128.sample.activemq.embedded.ssl.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class ReadTestPropertiesConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer testProperties() {
        final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocation(new ClassPathResource("activemq-embedded-ssl.properties"));
        return configurer;
    }
}
