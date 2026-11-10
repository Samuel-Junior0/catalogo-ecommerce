package br.edu.ifpb.catalogo_ecommerce.dto;

import java.time.Instant;

public record ErroResponse(int status, String erro, String mensagem, Instant timestamp) {

    public static ErroResponse of(int status, String erro, String mensagem) {
        return new ErroResponse(status, erro, mensagem, Instant.now());
    }
}
