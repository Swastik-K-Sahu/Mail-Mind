package com.mail.mail_mind.util;

public class PromptBuilder {

    public static String buildPrompt(String emailContent, String tone) {
        return String.format("""
            You are a smart email assistant.
            Read the following email and generate a reply in a %s tone.
            Email: %s
            Reply:""", tone, emailContent);
    }
}
