package br.edu.ifpb.catalogo_ecommerce.repository;

import br.edu.ifpb.catalogo_ecommerce.model.carrinho.Carrinho;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

@Repository
public class CarrinhoRepository extends BaseRepository<Carrinho>{
    public CarrinhoRepository(Firestore firestore) {
        super(firestore, "carrinho", Carrinho.class);
    }
}
