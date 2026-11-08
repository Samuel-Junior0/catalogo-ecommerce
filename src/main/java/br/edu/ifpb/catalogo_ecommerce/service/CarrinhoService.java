package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.model.carrinho.Carrinho;
import br.edu.ifpb.catalogo_ecommerce.repository.CarrinhoRepository;
import org.springframework.stereotype.Service;

@Service
public class CarrinhoService extends BaseService<Carrinho> {

    public CarrinhoService(CarrinhoRepository repository) {
        super(repository);
    }
}
