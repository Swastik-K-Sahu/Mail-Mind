package com.mail.mail_mind_spring;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailRequestDto {
    @NotBlank(message = "Email content is required")
    @JsonProperty("emailContent")
    private String emailContent;

    @JsonProperty("tone")
    private String tone;
}
