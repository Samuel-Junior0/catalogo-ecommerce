package br.edu.ifpb.catalogo_ecommerce.model.venda;

import br.edu.ifpb.catalogo_ecommerce.model.carrinho.ItemCarrinho;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Venda {

    @DocumentId
    private String id;

    private String pagamentoId;
    private String carrinhoId;

    private long totalEmCentavos;
    private List<ItemCarrinho> itens;
    private boolean pago;
    private boolean estoqueBaixado;

    @ServerTimestamp
    private Timestamp criadaEm;
}
