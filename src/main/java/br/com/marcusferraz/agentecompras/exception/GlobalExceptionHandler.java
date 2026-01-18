package br.com.marcusferraz.agentecompras.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AgentException.class)
    public ResponseEntity<AgentErrorDTO> handleAgentException(AgentException ex) {
        logger.warn("AgentException occurred: {}", ex.getMessage());

        AgentErrorDTO errorDTO = new AgentErrorDTO(
                "Agent Error",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<AgentErrorDTO> handleUserNotFoundException(UserNotFoundException ex) {
        logger.warn("UserNotFoundException occurred: {}", ex.getMessage());

        AgentErrorDTO errorDTO = new AgentErrorDTO(
                "User Not Found",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDTO);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<AgentErrorDTO> handleExternalServiceException(ExternalServiceException ex) {
        logger.error("ExternalServiceException occurred: {}", ex.getMessage(), ex);

        AgentErrorDTO errorDTO = new AgentErrorDTO(
                "External Service Error",
                "Dependency failed: " + ex.getMessage(),
                HttpStatus.BAD_GATEWAY.value(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AgentErrorDTO> handleGeneralException(Exception ex) {
        logger.error("Unexpected error occurred: ", ex);

        AgentErrorDTO errorDTO = new AgentErrorDTO(
                "Internal Server Error",
                "An unexpected error occurred. Please try again later or contact support.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
    }
}
