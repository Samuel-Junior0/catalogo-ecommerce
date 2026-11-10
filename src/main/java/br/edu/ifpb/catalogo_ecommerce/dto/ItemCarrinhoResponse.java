package br.edu.ifpb.catalogo_ecommerce.dto;

import br.edu.ifpb.catalogo_ecommerce.model.carrinho.ItemCarrinho;

public record ItemCarrinhoResponse(
        String produtoId,
        String nome,
        long precoEmCentavos,
        int quantidade,
        long subtotal) {

    public static ItemCarrinhoResponse from(ItemCarrinho item) {
        return new ItemCarrinhoResponse(
                item.getProdutoId(),
                item.getNome(),
                item.getPrecoEmCentavos(),
                item.getQuantidade(),
                item.getSubtotal());
    }
}
