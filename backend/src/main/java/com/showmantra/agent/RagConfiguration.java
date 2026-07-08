package com.showmantra.agent;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import java.io.File;

/**
 * Configuration class for setting up Retrieval-Augmented Generation (RAG) components.
 * This class initializes the vector store by loading existing embeddings or generating
 * them from markdown documents in the classpath.
 */
@Configuration
public class RagConfiguration {

    @Value("classpath*:/docs/*.md")
    private Resource[] documents;

    /**
     * Creates and configures a VectorStore bean.
     * It attempts to load pre-calculated vectors from a local file, and if not present,
     * reads markdown documents, splits them into tokens, and saves their embeddings.
     * 
     * @param embeddingModel the model used to convert text to embeddings
     * @return the configured VectorStore instance
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File vectorStoreFile = new File("vector-store.json");
        
        if (vectorStoreFile.exists()) {
            vectorStore.load(vectorStoreFile);
            return vectorStore;
        }

        try {
            List<Document> allDocs = new ArrayList<>();
            for (Resource res : documents) {
                String content = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                allDocs.add(new Document(content));
            }
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = textSplitter.apply(allDocs);
            vectorStore.add(splitDocuments);
            vectorStore.save(vectorStoreFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return vectorStore;
    }
}
