package com.pangeding.springaidocchatbot.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.MetricType;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;

import static com.pangeding.springaidocchatbot.parameter.milvus.MilvusParameter.*;

@Configuration
public class MilvusConfig {

    @Value("${spring.ai.vectorstore.milvus.client.host:localhost}")
    private String host;

    @Value("${spring.ai.vectorstore.milvus.client.port:19530}")
    private Integer port;

    @Value("${milvus.token:}")
    private String token;

    /**
     * 创建 Milvus v1 客户端实例
     */
    @Bean
    public MilvusServiceClient milvusClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .withToken(token) // v1 可选token认证
                .build();
        return new MilvusServiceClient(connectParam);
    }

    /**
     * 构建 Milvus 向量存储实例
     */
    @Bean
    public MilvusVectorStore milvusVectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName(COLLECTION_NAME)
                .embeddingFieldName(EMBEDDING_NAME)  // 与初始化的向量字段名一致
                .iDFieldName(ID_NAME)
                .autoId(true)
                .metadataFieldName(FILE_NAME_NAME)
                .contentFieldName(CONTENT_NAME)// 与主键字段名一致
                .metricType(MetricType.L2)
                .build();
    }
}
