package com.mykolapylypenko.smart_financial_auditor.audit.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * LangChain4j AI Service for RAG-based banking compliance auditing.
 *
 * The {@link dev.langchain4j.rag.content.retriever.ContentRetriever} injected at bean
 * creation time retrieves the top-3 most relevant chunks from the pgvector store and
 * automatically augments the prompt before the LLM call.
 */
public interface BankingAuditorAi {

    @SystemMessage("""
            You are a Professional Banking Auditor specializing in regulatory compliance.
            Your role is to analyze Polish financial regulations, KNF (Komisja Nadzoru \
            Finansowego) guidelines, and banking internal policies.

            STRICT RULES:
            1. Answer ONLY based on the regulatory context provided below.
            2. If the answer is not found in the context, respond exactly with:
               "I cannot find relevant information about this topic in the provided \
            regulatory documents."
            3. Always cite the specific document or section when answering.
            4. Be precise, professional, and use formal banking/legal terminology.
            5. Do NOT speculate or use general knowledge beyond the provided context.
            """)
    String audit(@UserMessage String question);
}
