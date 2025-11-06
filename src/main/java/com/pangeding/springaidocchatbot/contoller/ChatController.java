package com.pangeding.springaidocchatbot.contoller;


import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

/**
 * @author @debug.icu
 * @date 2024-03-10 17:05
 */
@Slf4j
@RestController
public class ChatController {

    private final ChatClient chatClient;

    @Autowired
    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/ai/generate")
    public Map<String, String> generate(@RequestParam(value = "message", defaultValue = "介绍你自己") String message) {
        String response = chatClient.prompt().user(message).call().content();
        log.info("User message: {}, AI response: {}", message, response);
        return Map.of("generation", response);
    }

    @GetMapping(value = "/ai/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        log.info("User message (streaming): {}", message);
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();

    }
}
