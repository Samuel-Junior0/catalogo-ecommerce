package br.edu.ifpb.catalogo_ecommerce.repository;

import br.edu.ifpb.catalogo_ecommerce.model.usuario.Usuario;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepository extends BaseRepository<Usuario> {
    public UsuarioRepository(Firestore firestore) {
        super(firestore, "usuarios", Usuario.class);
    }
}
