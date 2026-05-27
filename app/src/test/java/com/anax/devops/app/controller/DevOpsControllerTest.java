package com.anax.devops.app.controller;

import com.anax.devops.app.infrastructure.config.JwtService;
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

    // Inyectar el servicio real para generar tokens criptográficos válidos
    @Autowired
    private JwtService jwtService;

    private Map<String, Object> validPayload;
    private String realValidJwt;
    private final String invalidJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0In0.wrongsignature";

    // UNIT TESTING
    @BeforeEach
    void setUp() {
        // Payload estricto solicitado por el reto
        validPayload = Map.of(
                "message", "This is a test",
                "to", "Juan Perez",
                "from", "Rita Asturia",
                "timeToLifeSec", 45
        );

        // Generar un token real y firmado criptográficamente para las pruebas
        realValidJwt = jwtService.generateUniqueToken("NTT-Data-Test-Client");

        System.out.println("\nTOKEN REAL GENERADO PARA PRUEBAS LOCALES:\n" + realValidJwt + "\n");
    }

    @Test
    @DisplayName("POST /DevOps con headers válidos retorna 200 y mensaje de saludo")
    void post_withValidRequest_returns200AndGreeting() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .header(JWT_HEADER, realValidJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello Juan Perez your message will be sent"));
    }

    @Test
    @DisplayName("POST /DevOps sin API Key retorna 401 Unauthorized")
    void post_withoutApiKey_returns401() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .header(JWT_HEADER, realValidJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /DevOps con JWT inválido o mal firmado retorna 401 Unauthorized")
    void post_withInvalidJwt_returns401() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .header(JWT_HEADER, invalidJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /DevOps (Método no permitido) retorna 200 OK con texto ERROR")
    void get_unsupportedMethod_returns200WithErrorBody() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .header(JWT_HEADER, realValidJwt))
                .andExpect(status().isOk())
                .andExpect(content().string("ERROR"));
    }

    @Test
    @DisplayName("Request fuera de /DevOps pasa sin validación")
    void requestOutsideDevOps_passesWithoutValidation() throws Exception {

        mockMvc.perform(get("/health"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /DevOps sin JWT retorna 401")
    void post_withoutJwt_returns401() throws Exception {

        mockMvc.perform(post(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isUnauthorized());
    }

    // INTEGRATION TESTING
    @Test
    @DisplayName("POST /DevOps/auth/token retorna JWT válido")
    void post_generateToken_returnsJwt() throws Exception {

        String requestBody = """
            {
                "clientName": "NTT-Data-Test-Client"
            }
            """;

        mockMvc.perform(post("/DevOps/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    @DisplayName("POST /DevOps/auth/token sin clientName retorna 400")
    void post_generateToken_withoutClientName_returns400() throws Exception {

        String requestBody = """
            {
            }
            """;

        mockMvc.perform(post("/DevOps/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}