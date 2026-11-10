package br.edu.ifpb.catalogo_ecommerce.controller;

import br.edu.ifpb.catalogo_ecommerce.dto.UsuarioRequest;
import br.edu.ifpb.catalogo_ecommerce.dto.UsuarioResponse;
import br.edu.ifpb.catalogo_ecommerce.dto.UsuarioUpdateRequest;
import br.edu.ifpb.catalogo_ecommerce.model.usuario.Usuario;
import br.edu.ifpb.catalogo_ecommerce.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Usuários", description = "CRUD de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/listar/usuarios")
    @Operation(summary = "Lista todos os usuários")
    public List<UsuarioResponse> listar() {
        return usuarioService.buscarTodos().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @GetMapping("/buscar/usuarios/{id}")
    @Operation(summary = "Busca um usuário por id")
    public UsuarioResponse buscar(@PathVariable String id) {
        return UsuarioResponse.from(usuarioService.buscarPorId(id).orElseThrow());
    }

    @PostMapping("/criar/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um usuário")
    public Map<String, String> criar(@Valid @RequestBody UsuarioRequest request) {
        return Map.of("id", usuarioService.criar(request.toEntity()));
    }

    @PutMapping("/atualizar/usuarios/{id}")
    @Operation(summary = "Atualiza um usuário")
    public UsuarioResponse atualizar(@PathVariable String id,
                                     @Valid @RequestBody UsuarioUpdateRequest request) {
        Usuario usuario = usuarioService.buscarPorId(id).orElseThrow();
        request.aplicarEm(usuario);
        usuarioService.salvar(id, usuario);
        return UsuarioResponse.from(usuarioService.buscarPorId(id).orElseThrow());
    }

    @DeleteMapping("/deletar/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um usuário")
    public void deletar(@PathVariable String id) {
        usuarioService.deletar(id);
    }
}
