package com.mykolapylypenko.smart_financial_auditor.ingestion.service;

import com.mykolapylypenko.smart_financial_auditor.ingestion.dto.IngestionResponse;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final EmbeddingStore<TextSegment> langChain4jEmbeddingStore;
    private final EmbeddingModel openAiEmbeddingModel;
    private final DocumentSplitter documentSplitter;
    private final VectorStore springAiVectorStore;
    private final DocumentParser documentParser;

    public DocumentIngestionService(
            EmbeddingStore<TextSegment> langChain4jEmbeddingStore,
            EmbeddingModel openAiEmbeddingModel,
            DocumentSplitter documentSplitter,
            VectorStore springAiVectorStore,
            DocumentParser documentParser) {
        this.langChain4jEmbeddingStore = langChain4jEmbeddingStore;
        this.openAiEmbeddingModel = openAiEmbeddingModel;
        this.documentSplitter = documentSplitter;
        this.springAiVectorStore = springAiVectorStore;
        this.documentParser = documentParser;
    }

    public IngestionResponse ingestDocument(MultipartFile file) {
        String filename = file.getOriginalFilename();
        log.info("Starting ingestion for document: {}", filename);

        try {
            // Step A.1 — Load PDF
            Document lc4jDocument = documentParser.parse(file.getInputStream());
            lc4jDocument.metadata().put("filename", filename);
            lc4jDocument.metadata().put("ingested_at", LocalDateTime.now().toString());

            // Step A.2 — Chunk document into segments
            List<TextSegment> segments = documentSplitter.split(lc4jDocument);
            log.info("Document '{}' split into {} segments", filename, segments.size());

            // Step A.3 & A.4 — Embed and store via LangChain4j → document_embeddings table
            ingestViaLangChain4j(segments);

            // Also ingest into Spring AI vector store → vector_store table
            ingestViaSpringAi(segments, filename);

            log.info("Successfully ingested '{}': {} segments stored in both vector stores",
                    filename, segments.size());

            return new IngestionResponse(
                    filename,
                    segments.size(),
                    "Document ingested into both LangChain4j and Spring AI vector stores",
                    LocalDateTime.now());

        } catch (IOException | RuntimeException e) {
            log.error("Failed to parse document: {}", filename, e);
            throw new IllegalArgumentException("Could not parse PDF file: " + e.getMessage(), e);
        }
    }

    private void ingestViaLangChain4j(List<TextSegment> segments) {
        Response<List<Embedding>> embeddingsResponse = openAiEmbeddingModel.embedAll(segments);
        List<Embedding> embeddings = embeddingsResponse.content();
        langChain4jEmbeddingStore.addAll(embeddings, segments);
        log.debug("LangChain4j: stored {} embeddings in document_embeddings", segments.size());
    }

    private void ingestViaSpringAi(List<TextSegment> lc4jSegments, String filename) {
        List<org.springframework.ai.document.Document> springAiDocs = new ArrayList<>(lc4jSegments.size());
        for (int i = 0; i < lc4jSegments.size(); i++) {
            TextSegment segment = lc4jSegments.get(i);
            Map<String, Object> meta = Map.of(
                    "filename", filename,
                    "segment_index", i,
                    "ingested_at", LocalDateTime.now().toString()
            );
            springAiDocs.add(new org.springframework.ai.document.Document(segment.text(), meta));
        }
        // Spring AI VectorStore calls its own configured EmbeddingModel internally
        springAiVectorStore.add(springAiDocs);
        log.debug("Spring AI: stored {} documents in vector_store", springAiDocs.size());
    }
}
