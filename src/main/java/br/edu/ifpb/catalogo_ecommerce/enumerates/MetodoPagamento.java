package br.edu.ifpb.catalogo_ecommerce.enumerates;

import lombok.Getter;

@Getter
public enum MetodoPagamento {

    PIX("Pix"),
    CARTAO_CREDITO("Cartão de crédito"),
    CARTAO_DEBITO("Cartão de debito"),
    BOLETO_BANCARIO("Boleto bancário");

    private final String descricao;

    MetodoPagamento(String descricao) {
        this.descricao = descricao;
    }
}
