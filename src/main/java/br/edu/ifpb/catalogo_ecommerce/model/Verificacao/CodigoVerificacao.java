package br.edu.ifpb.catalogo_ecommerce.model.Verificacao;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CodigoVerificacao {

    @DocumentId
    private String email;

    private String codigoHash;
    private long expiraEmMillis;
    private int tentativas;

}
