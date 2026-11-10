package br.edu.ifpb.catalogo_ecommerce.repository;

import br.edu.ifpb.catalogo_ecommerce.model.usuario.Usuario;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepository extends BaseRepository<Usuario>{

    public UsuarioRepository(Firestore firestore) {
        super(firestore, "usuarios", Usuario.class);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        List<Usuario> encontrados = executar(colecao.whereEqualTo("email", email));
        return encontrados.stream().findFirst();
    }
}
