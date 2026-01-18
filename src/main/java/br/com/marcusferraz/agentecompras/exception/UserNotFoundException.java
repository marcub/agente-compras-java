package br.com.marcusferraz.agentecompras.exception;

public class UserNotFoundException extends AgentException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
