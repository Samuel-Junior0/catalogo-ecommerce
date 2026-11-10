package br.edu.ifpb.catalogo_ecommerce.dto;

import br.edu.ifpb.catalogo_ecommerce.enumerates.MetodoPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PagamentoRequest(
        @NotBlank(message = "vendaId é obrigatório") String vendaId,
        @NotNull(message = "método de pagamento é obrigatório") MetodoPagamento metodoPagamento) {
}
