package com.anax.devops.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("DevOps Endpoint TDD Integration Tests")
class DevOpsControllerTest {

    private static final String ENDPOINT = "/DevOps";
    private static final String API_KEY_HEADER = "X-Parse-REST-API-Key";
    private static final String JWT_HEADER = "X-JWT-KWY";
    private static final String VALID_API_KEY = "2f5ae96c-b558-4c7b-a590-a501ae1c3f6c";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> validPayload;
    private String dummyJwt;

    @BeforeEach
    void setUp() {
        // Payload estricto solicitado por el reto
        validPayload = Map.of(
                "message", "This is a test",
                "to", "Juan Perez",
                "from", "Rita Asturia",
                "timeToLifeSec", 45
        );

        // Un token JWT simulado estructuralmente para las pruebas iniciales
        dummyJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjEifQ.dummy-signature";
    }

    @Test
    @DisplayName("POST /DevOps con headers válidos retorna 200 y mensaje de saludo")
    void post_withValidRequest_returns200AndGreeting() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .header(JWT_HEADER, dummyJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello Juan Perez your message will be sent"));
    }

    @Test
    @DisplayName("POST /DevOps sin API Key retorna 401 Unauthorized")
    void post_withoutApiKey_returns401() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .header(JWT_HEADER, dummyJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /DevOps (Método no permitido) retorna 200 OK con texto ERROR")
    void get_unsupportedMethod_returns200WithErrorBody() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .header(JWT_HEADER, dummyJwt))
                .andExpect(status().isOk())
                .andExpect(content().string("ERROR"));
    }
}