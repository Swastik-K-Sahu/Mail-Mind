package com.mail.mail_mind.service;

import com.mail.mail_mind.models.EmailReply;
import com.mail.mail_mind.models.EmailRequest;
import com.mail.mail_mind.util.PromptBuilder;
import org.springframework.stereotype.Service;

@Service
public class EmailReplyService {

    private final ChatClient chatClient;

    public EmailReplyService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public EmailReply generateReply(EmailRequest request) {
        String prompt = PromptBuilder.buildPrompt(request.getEmailContent(), request.getTone());
        String response = chatClient.call(prompt);
        return new EmailReply(response.trim());
    }
}

