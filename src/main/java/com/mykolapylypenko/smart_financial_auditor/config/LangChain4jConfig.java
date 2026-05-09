package com.mykolapylypenko.smart_financial_auditor.config;

import com.mykolapylypenko.smart_financial_auditor.audit.ai.BankingAuditorAi;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.openai.api-key}")
    private String openAiApiKey;

    @Value("${langchain4j.openai.chat-model:gpt-4o}")
    private String openAiChatModel;

    @Value("${langchain4j.openai.embedding-model:text-embedding-3-small}")
    private String openAiEmbeddingModel;

    @Value("${langchain4j.openai.temperature:0.0}")
    private double temperature;

    @Value("${langchain4j.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${langchain4j.ollama.chat-model:llama3.2}")
    private String ollamaChatModel;

    @Value("${langchain4j.embedding.dimension:1536}")
    private int embeddingDimension;

    // ─── Chat Model ─────────────────────────────────────────────────────────

    @Bean
    @ConditionalOnProperty(name = "langchain4j.chat.provider", havingValue = "openai", matchIfMissing = true)
    public ChatModel openAiChatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(openAiChatModel)
                .temperature(temperature)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "langchain4j.chat.provider", havingValue = "ollama")
    public ChatModel ollamaChatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaChatModel)
                .temperature(temperature)
                .build();
    }

    // ─── Embedding Model ────────────────────────────────────────────────────
    // Always uses OpenAI embeddings to ensure consistent vector dimensions.
    // Embedding model must not change after documents are indexed.

    @Bean
    public EmbeddingModel langChain4jEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName(openAiEmbeddingModel)
                .build();
    }

    // ─── PgVector Embedding Store ────────────────────────────────────────────
    // LangChain4j manages the 'document_embeddings' table independently from
    // Spring AI's 'vector_store' table.

    @Bean
    public EmbeddingStore<TextSegment> langChain4jEmbeddingStore(DataSource dataSource) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("document_embeddings")
                .dimension(embeddingDimension)
                .createTable(true)
                .build();
    }

    // ─── RAG Content Retriever ───────────────────────────────────────────────

    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingStore<TextSegment> langChain4jEmbeddingStore,
            EmbeddingModel langChain4jEmbeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(langChain4jEmbeddingStore)
                .embeddingModel(langChain4jEmbeddingModel)
                .maxResults(3)
                .minScore(0.7)
                .build();
    }

    // ─── Document Parser ────────────────────────────────────────────────────

    @Bean
    public DocumentParser documentParser() {
        return new ApachePdfBoxDocumentParser();
    }

    // ─── Document Splitter ──────────────────────────────────────────────────
    // Recursive splitter: tries to split on paragraphs, then sentences,
    // then words. 1000 chars per chunk with 200-char overlap for context.

    @Bean
    public DocumentSplitter documentSplitter() {
        return DocumentSplitters.recursive(1000, 200);
    }

    // ─── BankingAuditorAi (LangChain4j AiService) ───────────────────────────
    // Wires the RAG retriever into the AI service proxy.
    // ContentRetriever automatically appends the top-3 relevant chunks to
    // every user message before sending to the LLM.

    @Bean
    public BankingAuditorAi bankingAuditorAi(
            ChatModel chatLanguageModel,
            ContentRetriever contentRetriever) {
        return AiServices.builder(BankingAuditorAi.class)
                .chatModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .build();
    }
}
