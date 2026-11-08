package br.edu.ifpb.catalogo_ecommerce.model.pagamento;

import br.edu.ifpb.catalogo_ecommerce.enumerates.MetodoPagamento;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Pagamento {

    @DocumentId
    private String id;

    private String vendaId;

    private MetodoPagamento metodoPagamento;
    private long precoEmCentavos;
    private boolean pago;

    @ServerTimestamp
    private Timestamp criadoEm;

    @ServerTimestamp
    private Timestamp pagoEm;

    public Pagamento(boolean pago, String id, String vendaId, MetodoPagamento metodoPagamento, long precoEmCentavos) {
        this.pago = false;
        this.id = id;
        this.vendaId = vendaId;
        this.metodoPagamento = metodoPagamento;
        this.precoEmCentavos = precoEmCentavos;
    }
}
