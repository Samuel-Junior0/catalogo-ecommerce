package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.model.pagamento.Pagamento;
import br.edu.ifpb.catalogo_ecommerce.repository.PagamentoRepository;
import org.springframework.stereotype.Service;

@Service
public class PagamentoService extends BaseService<Pagamento> {

    public PagamentoService(PagamentoRepository repository) {
        super(repository);
    }
}
