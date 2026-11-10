package br.edu.ifpb.catalogo_ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CarrinhoRequest(
        @NotBlank(message = "usuarioId é obrigatório") String usuarioId,
        @Valid List<ItemCarrinhoRequest> itens) {
}
