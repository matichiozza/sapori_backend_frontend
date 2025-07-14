package com.example.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Value("${google.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    public List<String> obtenerSugerenciasAlias(String alias) {
        RestTemplate restTemplate = new RestTemplate();

        String prompt = "Dame 5 alias alternativos para '" + alias + "', separados por coma. Solamente me tenes que pasar los 5 alias, sin ningun texto mas.";

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> body = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    GEMINI_API_URL + apiKey,
                    entity,
                    String.class
            );

            String responseBody = response.getBody();
            if (responseBody == null) return List.of();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode contentNode = candidates.get(0).path("content");
                JsonNode parts = contentNode.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String texto = parts.get(0).path("text").asText();

                    // Para debugging: ver cómo viene el texto
                    System.out.println("Texto recibido de Gemini:");
                    System.out.println(texto);

                    // Aquí asumimos que texto viene como:
                    // "Matucho, Matías, Mati, Matich, Ticho"
                    // Lo separamos por coma para obtener lista de alias
                    List<String> sugerencias = Arrays.stream(texto.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());

                    return sugerencias;
                }
            }

            return List.of();

        } catch (Exception e) {
            throw new RuntimeException("Error al llamar a la API de Gemini: " + e.getMessage(), e);
        }
    }
}
