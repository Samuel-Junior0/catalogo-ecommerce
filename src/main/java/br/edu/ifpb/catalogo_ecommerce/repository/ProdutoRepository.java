package br.edu.ifpb.catalogo_ecommerce.repository;

import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

@Repository
public class ProdutoRepository extends BaseRepository<Produto>{

    public ProdutoRepository(Firestore firestore) {
        super(firestore, "produtos", Produto.class);
    }
}
