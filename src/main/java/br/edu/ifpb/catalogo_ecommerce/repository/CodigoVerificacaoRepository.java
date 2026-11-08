package br.edu.ifpb.catalogo_ecommerce.repository;

import br.edu.ifpb.catalogo_ecommerce.exception.RepositoryException;
import br.edu.ifpb.catalogo_ecommerce.model.Verificacao.CodigoVerificacao;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class CodigoVerificacaoRepository extends BaseRepository<CodigoVerificacao> {

    public CodigoVerificacaoRepository(Firestore firestore) {
        super(firestore, "codigosVerificacao", CodigoVerificacao.class);
    }

    public void salvarComId(String id, CodigoVerificacao codigo) {
        try {
            colecao.document(id).set(codigo).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RepositoryException("Erro ao salvar código de " + id, e);
        }
    }
}
