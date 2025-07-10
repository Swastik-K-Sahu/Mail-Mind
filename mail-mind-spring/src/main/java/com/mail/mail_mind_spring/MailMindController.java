package com.mail.mail_mind_spring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class MailMindController {

    private final MailMindService mailMindService;

    @PostMapping("/compose-reply")
    public Mono<ResponseEntity<String>> composeSmartReply(@RequestBody MailRequestDto mailRequestDto) {
        log.info("Received request to compose mail reply");

        return mailMindService.generateResponse(mailRequestDto)
                .map(response -> {
                    log.info("Successfully generated mail response");
                    return ResponseEntity.ok(response);
                })
                .onErrorReturn(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Service temporarily unavailable")
                );
    }

}
