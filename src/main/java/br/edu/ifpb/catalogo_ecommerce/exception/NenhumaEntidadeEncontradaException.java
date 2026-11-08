package br.edu.ifpb.catalogo_ecommerce.exception;

public class NenhumaEntidadeEncontradaException extends RuntimeException {
    public NenhumaEntidadeEncontradaException(String entidade) {
        super("Nenhum item " + entidade + "encontrado(a)");
    }
}
