package br.com.marcusferraz.agentecompras.exception;

import java.time.LocalDateTime;

public record AgentErrorDTO(
    String error,
    String message,
    int status,
    LocalDateTime timestamp
) {}
