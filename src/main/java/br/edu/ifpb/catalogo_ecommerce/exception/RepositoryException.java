package br.edu.ifpb.catalogo_ecommerce.exception;

public class RepositoryException extends RuntimeException {
    public RepositoryException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
