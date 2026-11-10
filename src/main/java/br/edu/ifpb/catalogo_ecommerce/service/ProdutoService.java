package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.dto.ReporEstoqueRequest;
import br.edu.ifpb.catalogo_ecommerce.exception.EntidadeNaoEncontradaException;
import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import br.edu.ifpb.catalogo_ecommerce.repository.ProdutoRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService extends BaseService<Produto> {

    private final Firestore firestore;

    public ProdutoService(ProdutoRepository repository, Firestore firestore) {
        super(repository);
        this.firestore = firestore;
    }

    public Produto reporEstoque(String id, ReporEstoqueRequest request) {
        if (request.quantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade de reposição deve ser maior que zero.");
        }

        DocumentReference produtoRef = firestore.collection("produtos").document(id);

        TransacaoFirestore.executar(firestore, transaction -> {
            DocumentSnapshot produto = transaction.get(produtoRef).get();
            if (!produto.exists()) {
                throw new EntidadeNaoEncontradaException("Produto", id);
            }
            transaction.update(produtoRef, "estoque", FieldValue.increment(request.quantidade()));
            return null;
        }, "Erro ao repor estoque do produto " + id);

        return buscarPorId(id).orElseThrow();
    }
}
