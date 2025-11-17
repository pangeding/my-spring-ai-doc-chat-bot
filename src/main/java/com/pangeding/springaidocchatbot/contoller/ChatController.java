package com.pangeding.springaidocchatbot.contoller;


import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author @debug.icu
 * @date 2024-03-10 17:05
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;

    private final MilvusVectorStore milvusVectorStore;

    @Autowired
    public ChatController(ChatClient.Builder builder, MilvusVectorStore milvusVectorStore) {
        this.chatClient = builder.build();
        this.milvusVectorStore = milvusVectorStore;
    }

    @GetMapping("/generate")
    public Map<String, String> generate(@RequestParam(value = "message", defaultValue = "介绍你自己") String message) {
        String response = chatClient.prompt().user(message).call().content();
        log.info("User message: {}, AI response: {}", message, response);
        return Map.of("generation", response);
    }

    @GetMapping(value = "/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        log.info("User message (streaming): {}", message);
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();

    }

    @GetMapping("/generateWithRAG")
    public Map<String, String> generateWithRAG(@RequestParam(value = "message", defaultValue = "介绍你自己") String message) {
        Prompt prompt = generatePrompt(message);
        
        String response = chatClient.prompt().user(message).call().content();
        log.info("RAG User message: {}, AI response: {}", message, response);
        return Map.of("generation", response);
    }

    @GetMapping(value = "/generateStreamWithRAG", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> generateStreamWithRAG(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message, HttpServletResponse response) {
        Prompt prompt = generatePrompt(message);
        
        response.setCharacterEncoding("UTF-8");
        log.info("RAG User message (streaming): {}", message);
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();

    }
    
    private Prompt generatePrompt(String message){
        // 1. 从Milvus检索与问题相关的文档片段（前3条最相关的）
        SearchRequest searchRequest = SearchRequest.builder()
                                                    .query(message)
                                                    .topK(3)
                                                    .build();
        List<Document> relevantDocs = milvusVectorStore.similaritySearch(searchRequest);

        // 2. 构造包含上下文的Prompt
        String promptTemplate = """
            基于以下文档内容回答问题：
            {context}
            
            问题：{question}
            """;
        Map<String, Object> params = new HashMap<>();
        params.put("context", relevantDocs.stream()
                .map(doc -> doc.getText())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("无相关文档"));
        params.put("question", message);
        
        return new PromptTemplate(promptTemplate, params).create();
    }


}
