package br.edu.ifpb.catalogo_ecommerce.model.carrinho;

import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.cloud.firestore.annotation.Exclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ItemCarrinho {

    private String produtoId;
    private String nome;
    private long precoEmCentavos;
    private int quantidade;

    @JsonCreator(mode = JsonCreator.Mode.DISABLED)
    public ItemCarrinho(Produto produto, int quantidade) {
        this.produtoId = produto.getId();
        this.nome = produto.getNome();
        this.precoEmCentavos = produto.getPrecoEmCentavos();
        this.quantidade = quantidade;
    }

    @Exclude
    public long getSubtotal() {
        return precoEmCentavos * quantidade;
    }

}
