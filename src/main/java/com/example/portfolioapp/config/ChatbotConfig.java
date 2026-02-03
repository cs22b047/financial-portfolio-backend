package com.example.portfolioapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for chatbot-related beans
 */
@Configuration
public class ChatbotConfig {

    /**
     * RestTemplate bean for calling Python chatbot service
     * Configured with appropriate timeouts
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(30000);   // 30 seconds

        return new RestTemplate(factory);
    }
}
