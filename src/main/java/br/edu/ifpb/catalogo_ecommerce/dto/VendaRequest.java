package br.edu.ifpb.catalogo_ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public record VendaRequest(
        @NotBlank(message = "carrinhoId é obrigatório") String carrinhoId) {
}
