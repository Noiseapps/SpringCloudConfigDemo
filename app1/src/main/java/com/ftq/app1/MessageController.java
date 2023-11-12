package com.ftq.app1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {
    private final String messageSource;

    public MessageController(@Value("${message.source}") String messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping("/message")
    public String getMessage() {
        return messageSource;
    }
}
