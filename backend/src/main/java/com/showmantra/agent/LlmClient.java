package com.showmantra.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Client class responsible for interacting with the Spring AI ChatClient.
 * This component acts as the main entry point for processing user messages
 * through the LLM, managing conversation memory, and utilizing RAG capabilities.
 */
@Component
public class LlmClient {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory = new InMemoryChatMemory();
    private final VectorStore vectorStore;
    private final String systemText;

    /**
     * Constructs an LlmClient.
     * 
     * @param chatClientBuilder the builder to construct the ChatClient
     * @param vectorStore the vector store for RAG (Retrieval-Augmented Generation)
     * @param systemPromptResource the resource containing the system prompt text
     * @throws IOException if there is an issue reading the system prompt resource
     */
    public LlmClient(ChatClient.Builder chatClientBuilder, 
                     VectorStore vectorStore, 
                     @Value("classpath:/prompts/agent-system-prompt.st") Resource systemPromptResource) throws IOException {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.systemText = new String(systemPromptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Processes a user message by sending it to the LLM along with the system prompt,
     * conversation memory, and available tools.
     * 
     * @param userMessage the message input by the user
     * @param state the current state of the agent session
     * @return the generated text response from the LLM
     */
    public String process(String userMessage, AgentState state) {
        String formattedSystemText = systemText
                .replace("{current_date}", LocalDate.now().toString())
                .replace("{current_state}", state.toString());

        ChatResponse response = chatClient.prompt()
                .system(formattedSystemText)
                .user(userMessage)
                .advisors(new MessageChatMemoryAdvisor(chatMemory, state.getSessionId(), 100))
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .tools("listMovies", "searchShows", "getSeatLayout", "lockSeats", "processPayment", "cancelBooking")
                .call()
                .chatResponse();

        return response.getResult().getOutput().getText();
    }
}
