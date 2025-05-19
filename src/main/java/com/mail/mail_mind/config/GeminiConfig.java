package com.mail.mail_mind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean
    public GeminiApi geminiApi() {
        return new GeminiApi("YOUR_GEMINI_API_KEY");
    }

    @Bean
    public ChatClient chatClient(GeminiApi geminiApi) {
        return new GeminiChatClient(geminiApi, GeminiChatOptions.builder()
                .withTemperature(0.7f)
                .build());
    }
}
