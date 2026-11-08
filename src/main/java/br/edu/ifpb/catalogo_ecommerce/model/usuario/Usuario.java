package br.edu.ifpb.catalogo_ecommerce.model.usuario;

import com.google.cloud.firestore.annotation.DocumentId;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Usuario {

    @DocumentId
    private String id;

    @Email
    private String email;

    private String senha;
}
