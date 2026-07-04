package org.example.newcarre.service;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 批量生成文本向量
     */
    public float[][] embed(List<String> sentences) {
        float[][] embeddings = new float[sentences.size()][];
        // 分批处理，每批最多10条
        int batchSize = 10;
        for (int i = 0; i < sentences.size(); i += batchSize) {
            int end = Math.min(i + batchSize, sentences.size());
            List<String> batch = sentences.subList(i, end);

            EmbeddingResponse response = embeddingModel.call(
                    new EmbeddingRequest(
                            batch,
                            DashScopeEmbeddingOptions.builder()
                                    .model(DashScopeModel.EmbeddingModel.EMBEDDING_V2.getValue())
                                    .build()
                    )
            );

            List<Embedding> result = response.getResults();
            for (int j = 0; j < result.size(); j++) {
                embeddings[i + j] = result.get(j).getOutput();
            }
        } // 补上for循环闭合大括号

        return embeddings;
    }
}