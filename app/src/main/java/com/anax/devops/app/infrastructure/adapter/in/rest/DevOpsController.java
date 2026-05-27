package com.anax.devops.app.infrastructure.adapter.in.rest;

import com.anax.devops.app.application.port.in.ProcessMessageUseCase;
import com.anax.devops.app.domain.model.DevOpsRequest;
import com.anax.devops.app.domain.model.DevOpsResponse;
import com.anax.devops.app.domain.model.TokenRequest;
import com.anax.devops.app.domain.model.TokenResponse;
import com.anax.devops.app.infrastructure.config.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/DevOps")
public class DevOpsController {

    private final ProcessMessageUseCase processMessageUseCase;
    private final JwtService jwtService;

    public DevOpsController(
            ProcessMessageUseCase processMessageUseCase,
            JwtService jwtService
    ) {
        this.processMessageUseCase = processMessageUseCase;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<DevOpsResponse> processMessage(@Valid @RequestBody DevOpsRequest request) {
        DevOpsResponse response = processMessageUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/token")
    public ResponseEntity<TokenResponse> generateToken(@Valid @RequestBody TokenRequest request) {
        String token = jwtService.generateUniqueToken(request.clientName());
        return ResponseEntity.ok(new TokenResponse(token));
    }
}