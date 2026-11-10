package br.edu.ifpb.catalogo_ecommerce.dto;

import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProdutoResponse(
        String id,
        String nome,
        String descricao,
        long precoEmCentavos,
        int estoque,
        boolean ativo,
        String imagemURL,
        String categoriaId,
        List<String> tags,
        Map<String, String> atributos,
        Instant criadoEm) {

    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPrecoEmCentavos(),
                produto.getEstoque(),
                produto.isAtivo(),
                produto.getImagemURL(),
                produto.getCategoriaId(),
                produto.getTags(),
                produto.getAtributos(),
                produto.getCriadoEm() == null ? null
                        : Instant.ofEpochSecond(produto.getCriadoEm().getSeconds(),
                                produto.getCriadoEm().getNanos()));
    }
}
