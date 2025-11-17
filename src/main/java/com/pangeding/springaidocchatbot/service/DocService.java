package com.pangeding.springaidocchatbot.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static com.pangeding.springaidocchatbot.parameter.milvus.MilvusParameter.CONTENT_NAME;
import static com.pangeding.springaidocchatbot.parameter.milvus.MilvusParameter.FILE_NAME_NAME;

@Service
public class DocService {
    @Autowired
    private MilvusVectorStore milvusVectorStore;// Milvus向量存储（自动注入）

    // 处理上传的文档并存储向量
    public void processAndStoreDocument(MultipartFile file) throws Exception {
        // 1. 临时保存上传文件
        File tempFile = File.createTempFile("doc", file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")));
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        // 2. 读取文档（支持PDF/TXT/MD，Tika自动解析）
        FileSystemResource resource = new FileSystemResource(tempFile);
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();

        // 关键修复：为每个文档添加文件名元数据（file_name字段）

        // 3. 分割文档（按token长度拆分，避免内容过长）
        TextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .build();  // 500token/段
        List<Document> chunks = splitter.split(documents);


        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown_file";
        chunks.forEach(chunk -> {
            chunk.getMetadata().put(FILE_NAME_NAME, originalFileName);
            System.out.println("fileName type: " + chunk.getMetadata().get(FILE_NAME_NAME).getClass().getName());
            System.out.println("fileName value: " + chunk.getMetadata().get(FILE_NAME_NAME));
        });

        // 4. 存储到Milvus（自动生成向量并入库）
        milvusVectorStore.add(chunks);

        //5. 删除tempFile （虽然JVM退出会清理）
        tempFile.delete();
    }
}
