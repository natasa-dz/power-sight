package com.example.epsnwtbackend.configuration;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

public class SimulatorHandshakeInterceptor implements HandshakeInterceptor {

    private final Map<String, WebSocketSession> activeSessions;

    public SimulatorHandshakeInterceptor(Map<String, WebSocketSession> activeSessions) {
        this.activeSessions = activeSessions;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler,
                               Exception exception) {
        String simulatorId = request.getURI().getQuery().split("=")[1]; // Extract simulatorId from query
        if (simulatorId != null) {
            activeSessions.remove(simulatorId);
            System.out.println("Simulator ID " + simulatorId + " removed from active sessions.");
        }
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String simulatorId = request.getURI().getQuery();
        System.out.println(attributes);
        if (simulatorId != null) {
            activeSessions.put(simulatorId, (WebSocketSession) attributes.get("webSocketSession"));
            System.out.println("Simulator ID registered: " + simulatorId);
        }
        return true;
    }
}

