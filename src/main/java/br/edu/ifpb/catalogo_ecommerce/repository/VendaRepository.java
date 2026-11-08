package br.edu.ifpb.catalogo_ecommerce.repository;

import br.edu.ifpb.catalogo_ecommerce.model.venda.Venda;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

@Repository
public class VendaRepository extends BaseRepository<Venda> {
    public VendaRepository(Firestore firestore) {
        super(firestore, "vendas", Venda.class);
    }
}
