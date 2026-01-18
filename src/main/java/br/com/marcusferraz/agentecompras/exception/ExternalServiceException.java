package br.com.marcusferraz.agentecompras.exception;

public class ExternalServiceException extends AgentException {
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
