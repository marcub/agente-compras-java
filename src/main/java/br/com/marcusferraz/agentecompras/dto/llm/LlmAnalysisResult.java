package br.com.marcusferraz.agentecompras.dto.llm;

public record LlmAnalysisResult(boolean isPurchaseIntent, String productName, String simpleResponse) {
}
