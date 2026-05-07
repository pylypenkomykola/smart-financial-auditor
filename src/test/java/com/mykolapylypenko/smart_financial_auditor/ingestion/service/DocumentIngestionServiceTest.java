package com.mykolapylypenko.smart_financial_auditor.ingestion.service;

import com.mykolapylypenko.smart_financial_auditor.ingestion.dto.IngestionResponse;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

    private DocumentIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new DocumentIngestionService(
                langChain4jEmbeddingStore,
                openAiEmbeddingModel,
                documentSplitter,
                springAiVectorStore
        );
    }

    @Test
    void ingestDocument_emptyPdfBytes_throwsIllegalArgument() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> ingestionService.ingestDocument(emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Could not parse PDF file");
    }

    @Test
    void ingestDocument_withValidPdf_writesToBothStores() throws Exception {
        // Minimal valid PDF bytes
        byte[] minimalPdf = createMinimalPdfBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-regulation.pdf", "application/pdf", minimalPdf);

        TextSegment segment = TextSegment.from("Sample regulatory text about AML requirements.");
        List<TextSegment> segments = List.of(segment);
        Embedding embedding = Embedding.from(new float[1536]);
        Response<List<Embedding>> embeddingResponse = Response.from(List.of(embedding));

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

    private byte[] createMinimalPdfBytes() {
        // Minimal syntactically valid PDF 1.4 with one empty page
        String pdf = "%PDF-1.4\n"
                + "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
                + "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n"
                + "3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R/Resources<<>>>>endobj\n"
                + "xref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n"
                + "0000000058 00000 n\n0000000115 00000 n\n"
                + "trailer<</Size 4/Root 1 0 R>>\nstartxref\n217\n%%EOF";
        return pdf.getBytes();
    }
}
