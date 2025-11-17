package com.pangeding.springaidocchatbot.initializer.milvus;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.*;
import io.milvus.param.index.CreateIndexParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pangeding.springaidocchatbot.parameter.milvus.MilvusParameter.*;

@Component
public class MilvusCollectionInitializer {

    private final MilvusServiceClient milvusClient;

    @Autowired
    public MilvusCollectionInitializer(MilvusServiceClient milvusClient) {
        this.milvusClient = milvusClient;
        initCollection();
    }

    private void initCollection() {
        try {
            // 检查集合是否存在，存在则删除
            R<Boolean> hasCollection = milvusClient.hasCollection(
                    HasCollectionParam.newBuilder()
                            .withCollectionName(COLLECTION_NAME)
                            .build()
            );
            if (hasCollection.getData()) {
                milvusClient.dropCollection(
                        DropCollectionParam.newBuilder()
                                .withCollectionName(COLLECTION_NAME)
                                .build()
                );
            }

            // 定义字段
            List<FieldType> fields = new ArrayList<>();

            // 主键字段
            fields.add(FieldType.newBuilder()
                    .withName(ID_NAME)
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(true) // 不自动生成ID
                    .build());

            // 向量字段
            fields.add(FieldType.newBuilder()
                    .withName(EMBEDDING_NAME)
                    .withDataType(DataType.FloatVector)
                    .withDimension(VECTOR_DIMENSION)
                    .build());

            // 文档内容字段
            fields.add(FieldType.newBuilder()
                    .withName(CONTENT_NAME)
                    .withDataType(DataType.VarChar)
                    .withMaxLength(4096)
                    .build());

            // 文件名字段
            fields.add(FieldType.newBuilder()
                    .withName(FILE_NAME_NAME)
                    .withDataType(DataType.JSON)
                    .withMaxLength(256)
                    .build());

            // 创建集合
            milvusClient.createCollection(
                    CreateCollectionParam.newBuilder()
                            .withCollectionName(COLLECTION_NAME)
                            .withFieldTypes(fields)
                            .withDescription("Coding Agent文档向量集合（Milvus v1）")
                            .build()
            );



            milvusClient.createIndex(
                    CreateIndexParam.newBuilder()
                            .withCollectionName(COLLECTION_NAME)
                            .withFieldName(EMBEDDING_NAME)
                            .withIndexType(IndexType.IVF_FLAT)
                            .withMetricType(MetricType.L2)
                            .withExtraParam("{\"nlist\": 1024}")
                            .build()
            );

            // 添加加载集合操作
            milvusClient.loadCollection(
                    LoadCollectionParam.newBuilder()
                            .withCollectionName(COLLECTION_NAME)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("Milvus集合初始化失败", e);
        }
    }
}
