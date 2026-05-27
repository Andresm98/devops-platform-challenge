package com.anax.devops.app.domain.model;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank String clientName) {
}