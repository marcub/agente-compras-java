package br.com.marcusferraz.agentecompras.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LlmRequest(String model, List<Message> messages, @JsonProperty("response_format") ResponseFormat responseFormat) {
    public record Message(String role, String content) {}
    public record ResponseFormat(String type) {}
}
