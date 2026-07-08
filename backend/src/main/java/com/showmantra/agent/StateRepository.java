package com.showmantra.agent;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository class for managing and persisting the state of the AI agent.
 * Currently uses an in-memory concurrent map, but is designed to be
 * backed by a persistent data store like Redis or a database in production.
 */
@Repository
public class StateRepository {
    
    // In-memory state storage for development. 
    // In production, this should be backed by Redis or a Database.
    private final Map<String, AgentState> store = new ConcurrentHashMap<>();

    /**
     * Retrieves the agent state for a specific session ID.
     * 
     * @param sessionId the unique identifier for the user session
     * @return an Optional containing the agent state if found, or empty otherwise
     */
    public Optional<AgentState> findById(String sessionId) {
        return Optional.ofNullable(store.get(sessionId));
    }

    /**
     * Saves or updates the agent state for a given session.
     * 
     * @param state the agent state object to save
     */
    public void save(AgentState state) {
        store.put(state.getSessionId(), state);
    }
    
    /**
     * Deletes the agent state associated with a session ID.
     * 
     * @param sessionId the unique identifier for the user session
     */
    public void delete(String sessionId) {
        store.remove(sessionId);
    }
}
