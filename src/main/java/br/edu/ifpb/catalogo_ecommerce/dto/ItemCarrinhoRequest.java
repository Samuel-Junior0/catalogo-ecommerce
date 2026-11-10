package br.edu.ifpb.catalogo_ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ItemCarrinhoRequest(
        @NotBlank(message = "produtoId é obrigatório") String produtoId,
        @Positive(message = "quantidade deve ser maior que zero") int quantidade) {
}
