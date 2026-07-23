package com.showmantra.controllers;

import com.showmantra.agent.AgentState;
import com.showmantra.agent.LlmClient;
import com.showmantra.agent.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentInteractionController {

    private final LlmClient llmClient;
    private final StateRepository stateRepo;

    public record ChatRequest(String sessionId, String message) {}
    public record ChatResponse(String sessionId, String reply, String status) {}

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request, Principal principal) {
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        AgentState state = stateRepo.findById(sessionId).orElse(new AgentState());
        state.setSessionId(sessionId);
        if (principal != null && principal.getName() != null) {
            state.setUserId(UUID.fromString(principal.getName()));
        }
        stateRepo.save(state);

        // Process message through LLM and automatic function calling loop
        try {
            String reply = llmClient.process(request.message(), state);
            AgentState updatedState = stateRepo.findById(sessionId).orElse(state);
            return ResponseEntity.ok(new ChatResponse(sessionId, reply, updatedState.getStatus()));
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                return ResponseEntity.ok(new ChatResponse(sessionId, "The AI is currently receiving too many requests (API Rate Limit). Please wait 15 seconds and try again!", "ERROR"));
            }
            return ResponseEntity.ok(new ChatResponse(sessionId, "Sorry, I encountered an internal API error connecting to Gemini. Error: " + e.getMessage(), "ERROR"));
        }
    }
}
