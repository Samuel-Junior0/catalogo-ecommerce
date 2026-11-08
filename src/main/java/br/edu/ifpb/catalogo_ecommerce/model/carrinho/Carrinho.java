package br.edu.ifpb.catalogo_ecommerce.model.carrinho;

import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class Carrinho {

    @DocumentId
    private String id;

    private String usuarioId;

    private List<ItemCarrinho> produtos;
    private long totalEmCentavos;
    private List<ItemCarrinho> produtosSelecionados;

    @ServerTimestamp
    private Timestamp atualizadoEm;

    public Carrinho(String id, List<ItemCarrinho> produtos, long totalEmCentavos, List<ItemCarrinho> produtosSelecionados) {
        this.id = id;
        this.produtos = produtos;
        this.totalEmCentavos = totalEmCentavos;
        this.produtosSelecionados = produtosSelecionados;
    }
}
