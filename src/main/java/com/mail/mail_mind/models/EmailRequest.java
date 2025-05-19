package com.mail.mail_mind.models;

import lombok.Data;

@Data
public class EmailRequest {
    private String emailContent;
    private String tone;

    // Getters & Setters
}