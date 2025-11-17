package com.pangeding.springaidocchatbot.parameter.milvus;

public class MilvusParameter {
    // 集合名称（自定义，如"coding_agent_docs"）
    public static final String COLLECTION_NAME = "vector_store";
    // 向量维度（需与你的Embedding模型输出维度一致，如CodeBERT通常是768）
    public static final int VECTOR_DIMENSION = 1536;
    public static final String ID_NAME = "id";
    public static final String EMBEDDING_NAME = "embedding";
    public static final String CONTENT_NAME = "content";
    public static final String FILE_NAME_NAME = "file_name";


}
