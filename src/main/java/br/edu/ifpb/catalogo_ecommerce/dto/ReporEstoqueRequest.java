package br.edu.ifpb.catalogo_ecommerce.dto;

import jakarta.validation.constraints.Positive;

public record ReporEstoqueRequest(
        @Positive(message = "quantidade deve ser maior que zero") int quantidade) {
}
