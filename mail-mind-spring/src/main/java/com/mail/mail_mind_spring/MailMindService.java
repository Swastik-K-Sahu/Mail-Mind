package com.mail.mail_mind_spring;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MailMindService {
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String DEFAULT_TONE = "professional";

    private final WebClient httpClient;
    private final ObjectMapper jsonProcessor;

    @Value("${ai.service.endpoint}")
    private String aiServiceEndpoint;

    @Value("${ai.service.key}")
    private String aiServiceKey;

    public MailMindService(WebClient.Builder clientBuilder) {
        this.httpClient = clientBuilder.build();
        this.jsonProcessor = new ObjectMapper();
    }

    public Mono<String> generateResponse(MailRequestDto mailRequestDto) {
        if (!isValidRequest(mailRequestDto)) {
            return Mono.just("Invalid request: Email content cannot be empty");
        }

        String contextualPrompt = constructContextualPrompt(mailRequestDto);
        Map<String, Object> apiPayload = buildApiPayload(contextualPrompt);
        return executeAiRequest(apiPayload)
                .map(this::parseAiResponse)
                .doOnError(error -> log.error("Error processing mail request", error));
    }

    private boolean isValidRequest(MailRequestDto request) {
        return request != null &&
                StringUtils.hasText(request.getEmailContent()) ;
    }

    private Mono<String> executeAiRequest(Map<String, Object> payload) {
        return httpClient.post()
                .uri(aiServiceEndpoint + "?key=" + aiServiceKey)
                .header(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(15));
    }

    private String parseAiResponse(String rawResponse) {
        try {
            JsonNode responseTree = jsonProcessor.readTree(rawResponse);
            return responseTree.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText("Unable to generate response"); // Default response
        } catch (Exception processingError) {
            log.warn("Failed to parse AI response", processingError);
            return "Response processing failed: " + processingError.getMessage();
        }
    }

    private String constructContextualPrompt(MailRequestDto requestDto) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("Generate an email response to the following message.\n");
        promptBuilder.append("Don't include any subject line.\n\n");

        // Tone instruction (conditional)
        if (StringUtils.hasText(requestDto.getTone())) {
            promptBuilder.append("Communication Style:\n");
            promptBuilder.append("- Adopt a ")
                    .append(requestDto.getTone().toLowerCase().trim())
                    .append(" communication style.\n\n");
        } else {
            promptBuilder.append("Communication Style:\n");
            promptBuilder.append("- Adopt a ").append(DEFAULT_TONE).append("tone\n\n");
        }

        // Incoming message context
        promptBuilder.append("Incoming Email for Context:\n");
        promptBuilder.append(requestDto.getEmailContent().trim());

        return promptBuilder.toString();
    }

    private Map<String, Object> buildApiPayload(String promptText) {
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", promptText);

        Map<String, Object> contentPart = new HashMap<>();
        contentPart.put("parts", Collections.singletonList(textPart));

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("contents", Collections.singletonList(contentPart));

        return requestPayload;
    }
}
