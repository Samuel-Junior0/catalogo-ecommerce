package br.edu.ifpb.catalogo_ecommerce.dto;

import br.edu.ifpb.catalogo_ecommerce.model.usuario.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioUpdateRequest(
        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido") String email,
        String senha) {

    public void aplicarEm(Usuario usuario) {
        usuario.setEmail(email);
        if (senha != null && !senha.isBlank()) {
            usuario.setSenha(senha);
        }
    }
}
