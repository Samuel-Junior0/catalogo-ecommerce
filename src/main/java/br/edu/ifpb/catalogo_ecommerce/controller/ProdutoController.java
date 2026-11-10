package br.edu.ifpb.catalogo_ecommerce.controller;

import br.edu.ifpb.catalogo_ecommerce.dto.ProdutoRequest;
import br.edu.ifpb.catalogo_ecommerce.dto.ProdutoResponse;
import br.edu.ifpb.catalogo_ecommerce.dto.ReporEstoqueRequest;
import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import br.edu.ifpb.catalogo_ecommerce.service.ProdutoService;
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
@Tag(name = "Produtos", description = "CRUD de produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/listar/produtos")
    @Operation(summary = "Lista todos os produtos")
    public List<ProdutoResponse> listar() {
        return produtoService.buscarTodos().stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    @GetMapping("/buscar/produtos/{id}")
    @Operation(summary = "Busca um produto por id")
    public ProdutoResponse buscar(@PathVariable String id) {
        return ProdutoResponse.from(produtoService.buscarPorId(id).orElseThrow());
    }

    @PostMapping("/criar/produtos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um produto")
    public Map<String, String> criar(@Valid @RequestBody ProdutoRequest request) {
        return Map.of("id", produtoService.criar(request.toEntity()));
    }

    @PutMapping("/atualizar/produtos/{id}")
    @Operation(summary = "Atualiza um produto")
    public ProdutoResponse atualizar(@PathVariable String id, @Valid @RequestBody ProdutoRequest request) {
        Produto produto = produtoService.buscarPorId(id).orElseThrow();
        request.aplicarEm(produto);
        produtoService.salvar(id, produto);
        return ProdutoResponse.from(produtoService.buscarPorId(id).orElseThrow());
    }

    @DeleteMapping("/deletar/produtos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um produto")
    public void deletar(@PathVariable String id) {
        produtoService.deletar(id);
    }

    @PostMapping("/repor-estoque/produtos/{id}")
    @Operation(summary = "Repõe o estoque de um produto",
            description = "Soma a quantidade informada ao estoque atual do produto.")
    public ProdutoResponse reporEstoque(@PathVariable String id,
                                        @Valid @RequestBody ReporEstoqueRequest request) {
        return ProdutoResponse.from(produtoService.reporEstoque(id, request));
    }
}
