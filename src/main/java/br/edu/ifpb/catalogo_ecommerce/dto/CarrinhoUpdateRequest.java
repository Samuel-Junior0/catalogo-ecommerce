package br.edu.ifpb.catalogo_ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CarrinhoUpdateRequest(
        @NotNull(message = "itens são obrigatórios")
        @Valid List<ItemCarrinhoRequest> itens) {
}
