package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.model.venda.Venda;
import br.edu.ifpb.catalogo_ecommerce.repository.VendaRepository;
import org.springframework.stereotype.Service;

@Service
public class VendaService extends BaseService<Venda> {

    public VendaService(VendaRepository repository) {
        super(repository);
    }
}
