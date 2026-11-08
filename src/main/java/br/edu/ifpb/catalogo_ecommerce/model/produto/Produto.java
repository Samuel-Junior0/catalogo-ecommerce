package br.edu.ifpb.catalogo_ecommerce.model.produto;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.service.invoker.UrlArgumentResolver;

import java.net.URL;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class Produto {

    @DocumentId
    private String id;

    private String nome;
    private String descricao;
    private long precoEmCentavos;
    private int estoque;
    private boolean ativo;
    private URL imagemURL;

    private String catregoriaId;
    private List<String> tags;
    private Map<String, String> atributos;

    @ServerTimestamp
    private Timestamp criadoEm;

    public Produto(String nome, String descricao, long precoEmCentavos, int estoque, boolean ativo,
                   String catregoriaId, URL imagemURL) {
        this.nome = nome;
        this.descricao = descricao;
        this.precoEmCentavos = precoEmCentavos;
        this.estoque = estoque;
        this.ativo = true;
        this.catregoriaId = catregoriaId;
        this.imagemURL = imagemURL;
    }
}
