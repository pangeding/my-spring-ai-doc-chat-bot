package com.pangeding.springaidocchatbot;

import org.springframework.ai.autoconfigure.vectorstore.milvus.MilvusVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {MilvusVectorStoreAutoConfiguration.class})
public class SpringAiChatDocApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiChatDocApplication.class, args);
    }

}
