package com.example.individualsapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@UtilityClass
public class LoggingUtils {
    public static final String PROMETHEUS_PATH = "/actuator/prometheus";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String PASSWORD = "password";
    private static final String CONFIRM_PASSWORD = "confirm_password";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String MASK = "**********";
    private static final Set<String> bodySecretFields = Set.of(CLIENT_SECRET, ACCESS_TOKEN, REFRESH_TOKEN, PASSWORD, CONFIRM_PASSWORD);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeHeadersToLog(Map.Entry<String, List<String>> header, StringBuilder target) {
        List<String> headerValues = header.getValue().stream().distinct().toList();
        if (header.getKey().equals(AUTHORIZATION_HEADER)) {
            target.append(header.getKey()).append(" -> ").append(MASK);
        } else {
            target.append(header.getKey()).append(" -> ").append(headerValues);
        }
        target.append(" ");
    }

    public static String maskBodySecrets(String body) {
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            if (jsonNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                for (String secretField : bodySecretFields) {
                    if (objectNode.has(secretField)) {
                        objectNode.put(secretField, MASK);
                    }
                }
            }
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse body");
            throw new RuntimeException(e);
        }
    }
}