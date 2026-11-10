package br.edu.ifpb.catalogo_ecommerce.dto;

import br.edu.ifpb.catalogo_ecommerce.model.venda.Venda;

import java.time.Instant;
import java.util.List;

public record VendaResponse(
        String id,
        String pagamentoId,
        String carrinhoId,
        long totalEmCentavos,
        List<ItemCarrinhoResponse> itens,
        boolean pago,
        Instant criadaEm) {

    public static VendaResponse from(Venda venda) {
        List<ItemCarrinhoResponse> itens = venda.getItens() == null
                ? List.of()
                : venda.getItens().stream().map(ItemCarrinhoResponse::from).toList();

        return new VendaResponse(
                venda.getId(),
                venda.getPagamentoId(),
                venda.getCarrinhoId(),
                venda.getTotalEmCentavos(),
                itens,
                venda.isPago(),
                venda.getCriadaEm() == null ? null
                        : Instant.ofEpochSecond(venda.getCriadaEm().getSeconds(),
                                venda.getCriadaEm().getNanos()));
    }
}
