package br.edu.ifpb.catalogo_ecommerce.dto;

import br.edu.ifpb.catalogo_ecommerce.model.carrinho.Carrinho;

import java.time.Instant;
import java.util.List;

public record CarrinhoResponse(
        String id,
        String usuarioId,
        List<ItemCarrinhoResponse> itens,
        long totalEmCentavos,
        Instant atualizadoEm) {

    public static CarrinhoResponse from(Carrinho carrinho) {
        List<ItemCarrinhoResponse> itens = carrinho.getProdutos() == null
                ? List.of()
                : carrinho.getProdutos().stream().map(ItemCarrinhoResponse::from).toList();

        return new CarrinhoResponse(
                carrinho.getId(),
                carrinho.getUsuarioId(),
                itens,
                carrinho.getTotalEmCentavos(),
                carrinho.getAtualizadoEm() == null ? null
                        : Instant.ofEpochSecond(carrinho.getAtualizadoEm().getSeconds(),
                                carrinho.getAtualizadoEm().getNanos()));
    }
}
