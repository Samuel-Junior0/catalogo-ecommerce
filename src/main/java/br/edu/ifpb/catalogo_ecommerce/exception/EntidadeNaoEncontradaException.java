package br.edu.ifpb.catalogo_ecommerce.exception;

public class EntidadeNaoEncontradaException extends RuntimeException {
    public EntidadeNaoEncontradaException(String entidadeNome, String id) {
        super(entidadeNome + "não encontrado(a): " + id);
    }
}
