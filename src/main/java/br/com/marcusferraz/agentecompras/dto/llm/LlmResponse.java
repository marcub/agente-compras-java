package br.com.marcusferraz.agentecompras.dto.llm;

import java.util.List;

public record LlmResponse(List<Choice> choices) {
    public record Choice(Message message) {}
    public record Message(String content) {}
}
