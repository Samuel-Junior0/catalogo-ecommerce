package br.edu.ifpb.catalogo_ecommerce.dto;

import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.net.URL;
import java.util.List;
import java.util.Map;

public record ProdutoRequest(
        @NotBlank(message = "nome é obrigatório") String nome,
        String descricao,
        @PositiveOrZero(message = "preço não pode ser negativo") Long precoEmCentavos,
        @PositiveOrZero(message = "estoque não pode ser negativo") Integer estoque,
        Boolean ativo,
        String categoriaId,
        URL imagemURL,
        List<String> tags,
        Map<String, String> atributos) {

    public Produto toEntity() {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPrecoEmCentavos(precoEmCentavos == null ? 0 : precoEmCentavos);
        produto.setEstoque(estoque == null ? 0 : estoque);
        produto.setAtivo(ativo == null || ativo);
        produto.setCategoriaId(categoriaId);
        produto.setImagemURL(imagemURL == null ? null : imagemURL.toString());
        produto.setTags(tags);
        produto.setAtributos(atributos);
        return produto;
    }

    public void aplicarEm(Produto produto) {
        produto.setNome(nome);
        produto.setDescricao(descricao);
        if (precoEmCentavos != null) {
            produto.setPrecoEmCentavos(precoEmCentavos);
        }
        if (estoque != null) {
            produto.setEstoque(estoque);
        }
        if (ativo != null) {
            produto.setAtivo(ativo);
        }
        produto.setCategoriaId(categoriaId);
        produto.setImagemURL(imagemURL == null ? null : imagemURL.toString());
        produto.setTags(tags);
        produto.setAtributos(atributos);
    }
}
