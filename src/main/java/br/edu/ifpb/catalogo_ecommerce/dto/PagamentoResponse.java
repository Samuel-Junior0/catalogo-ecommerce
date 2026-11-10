package br.edu.ifpb.catalogo_ecommerce.dto;

import br.edu.ifpb.catalogo_ecommerce.enumerates.MetodoPagamento;
import br.edu.ifpb.catalogo_ecommerce.model.pagamento.Pagamento;

import java.time.Instant;

public record PagamentoResponse(
        String id,
        String vendaId,
        MetodoPagamento metodoPagamento,
        long precoEmCentavos,
        boolean pago,
        Instant criadoEm,
        Instant pagoEm) {

    public static PagamentoResponse from(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getVendaId(),
                pagamento.getMetodoPagamento(),
                pagamento.getPrecoEmCentavos(),
                pagamento.isPago(),
                pagamento.getCriadoEm() == null ? null
                        : Instant.ofEpochSecond(pagamento.getCriadoEm().getSeconds(),
                                pagamento.getCriadoEm().getNanos()),
                pagamento.getPagoEm() == null ? null
                        : Instant.ofEpochSecond(pagamento.getPagoEm().getSeconds(),
                                pagamento.getPagoEm().getNanos()));
    }
}
