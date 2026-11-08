package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import br.edu.ifpb.catalogo_ecommerce.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService extends BaseService<Produto> {

    public ProdutoService(ProdutoRepository repository) {
        super(repository);
    }
}
