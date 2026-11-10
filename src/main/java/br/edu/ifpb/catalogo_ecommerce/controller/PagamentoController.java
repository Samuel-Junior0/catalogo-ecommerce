package br.edu.ifpb.catalogo_ecommerce.controller;

import br.edu.ifpb.catalogo_ecommerce.dto.PagamentoRequest;
import br.edu.ifpb.catalogo_ecommerce.dto.PagamentoResponse;
import br.edu.ifpb.catalogo_ecommerce.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Pagamentos", description = "CRUD de pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/listar/pagamentos")
    @Operation(summary = "Lista todos os pagamentos")
    public List<PagamentoResponse> listar() {
        return pagamentoService.buscarTodos().stream()
                .map(PagamentoResponse::from)
                .toList();
    }

    @GetMapping("/buscar/pagamentos/{id}")
    @Operation(summary = "Busca um pagamento por id")
    public PagamentoResponse buscar(@PathVariable String id) {
        return PagamentoResponse.from(pagamentoService.buscarPorId(id).orElseThrow());
    }

    @PostMapping("/criar/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria o pagamento de uma venda")
    public Map<String, String> criar(@Valid @RequestBody PagamentoRequest request) {
        return Map.of("id", pagamentoService.criar(request));
    }

    @DeleteMapping("/deletar/pagamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um pagamento")
    public void deletar(@PathVariable String id) {
        pagamentoService.deletar(id);
    }
}
