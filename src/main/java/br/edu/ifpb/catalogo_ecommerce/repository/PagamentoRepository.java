package br.edu.ifpb.catalogo_ecommerce.repository;

import br.edu.ifpb.catalogo_ecommerce.model.pagamento.Pagamento;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PagamentoRepository extends BaseRepository<Pagamento> {

    public PagamentoRepository(Firestore firestore) {
        super(firestore, "pagamentos", Pagamento.class);
    }

    public Optional<Pagamento> buscarPorVendaId(String vendaId) {
        List<Pagamento> encontrados = executar(colecao.whereEqualTo("vendaId", vendaId));
        return encontrados.stream().findFirst();
    }
}
