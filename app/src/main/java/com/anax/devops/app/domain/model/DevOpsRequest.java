package com.anax.devops.app.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevOpsRequest {

    @NotBlank(message = "The message field cannot be empty")
    private String message;

    @NotBlank(message = "The 'to' field cannot be empty")
    private String to;

    @NotBlank(message = "The 'from' field cannot be empty")
    private String from;

    @NotNull(message = "The timeToLifeSec field is required")
    private Integer timeToLifeSec;
}