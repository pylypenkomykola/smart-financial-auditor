package com.mykolapylypenko.smart_financial_auditor.ingestion.service;

import com.mykolapylypenko.smart_financial_auditor.ingestion.dto.IngestionResponse;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceTest {

    @Mock
    private EmbeddingStore<TextSegment> langChain4jEmbeddingStore;

    @Mock
    private EmbeddingModel openAiEmbeddingModel;

    @Mock
    private DocumentSplitter documentSplitter;

    @Mock
    private VectorStore springAiVectorStore;

    @Mock
    private DocumentParser documentParser;

    private DocumentIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new DocumentIngestionService(
                langChain4jEmbeddingStore,
                openAiEmbeddingModel,
                documentSplitter,
                springAiVectorStore,
                documentParser
        );
    }

    @Test
    void ingestDocument_emptyPdfBytes_throwsIllegalArgument() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        when(documentParser.parse(any(InputStream.class)))
                .thenThrow(new RuntimeException(new IOException("Error: End-of-File, expected line at offset 0")));

        assertThatThrownBy(() -> ingestionService.ingestDocument(emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Could not parse PDF file");
    }

    @Test
    void ingestDocument_withValidPdf_writesToBothStores() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-regulation.pdf", "application/pdf", new byte[]{1});

        Document document = Document.from("Sample regulatory text about AML requirements.");
        TextSegment segment = TextSegment.from("Sample regulatory text about AML requirements.");
        List<TextSegment> segments = List.of(segment);
        Embedding embedding = Embedding.from(new float[1536]);
        Response<List<Embedding>> embeddingResponse = Response.from(List.of(embedding));

        when(documentParser.parse(any(InputStream.class))).thenReturn(document);
        when(documentSplitter.split(any())).thenReturn(segments);
        when(openAiEmbeddingModel.embedAll(segments)).thenReturn(embeddingResponse);

        IngestionResponse response = ingestionService.ingestDocument(file);

        assertThat(response.filename()).isEqualTo("test-regulation.pdf");
        assertThat(response.totalSegments()).isEqualTo(1);
        assertThat(response.message()).contains("LangChain4j");
        assertThat(response.message()).contains("Spring AI");
        assertThat(response.processedAt()).isNotNull();

        verify(langChain4jEmbeddingStore).addAll(anyList(), eq(segments));
        verify(springAiVectorStore).add(anyList());
    }
}
