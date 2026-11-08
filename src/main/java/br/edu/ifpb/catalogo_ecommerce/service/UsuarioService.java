package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.model.usuario.Usuario;
import br.edu.ifpb.catalogo_ecommerce.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService extends BaseService<Usuario> {

    public UsuarioService(UsuarioRepository repository) {
        super(repository);
    }
}
