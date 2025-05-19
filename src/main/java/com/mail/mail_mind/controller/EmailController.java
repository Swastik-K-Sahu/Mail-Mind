package com.mail.mail_mind.controller;

import com.mail.mail_mind.models.EmailReply;
import com.mail.mail_mind.models.EmailRequest;
import com.mail.mail_mind.service.EmailReplyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailReplyService replyService;

    public EmailController(EmailReplyService replyService) {
        this.replyService = replyService;
    }

    @PostMapping("/reply")
    public EmailReply generateReply(@RequestBody EmailRequest request) {
        return replyService.generateReply(request);
    }
}
