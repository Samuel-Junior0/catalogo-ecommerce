package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.exception.EmailJaCadastradoException;
import br.edu.ifpb.catalogo_ecommerce.model.usuario.Usuario;
import br.edu.ifpb.catalogo_ecommerce.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UsuarioService extends BaseService<Usuario> {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public String criar(Usuario usuario) {
        normalizarEmail(usuario);
        validarEmailUnico(usuario.getEmail(), null);
        return super.criar(usuario);
    }

    @Override
    public String salvar(String id, Usuario usuario) {
        normalizarEmail(usuario);
        validarEmailUnico(usuario.getEmail(), id);
        return super.salvar(id, usuario);
    }

    private void normalizarEmail(Usuario usuario) {
        if (usuario.getEmail() != null) {
            usuario.setEmail(usuario.getEmail().trim().toLowerCase(Locale.ROOT));
        }
    }

    private void validarEmailUnico(String email, String idAtual) {
        if (email == null || email.isBlank()) {
            return;
        }
        repository.buscarPorEmail(email).ifPresent(encontrado -> {
            boolean outroUsuario = idAtual == null || !idAtual.equals(encontrado.getId());
            if (outroUsuario) {
                throw new EmailJaCadastradoException(email);
            }
        });
    }
}
