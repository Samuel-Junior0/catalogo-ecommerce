package br.edu.ifpb.catalogo_ecommerce.controller;

import br.edu.ifpb.catalogo_ecommerce.dto.VendaRequest;
import br.edu.ifpb.catalogo_ecommerce.dto.VendaResponse;
import br.edu.ifpb.catalogo_ecommerce.service.VendaService;
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
@Tag(name = "Vendas", description = "CRUD de vendas e confirmação de pagamento")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @GetMapping("/listar/vendas")
    @Operation(summary = "Lista todas as vendas")
    public List<VendaResponse> listar() {
        return vendaService.buscarTodos().stream()
                .map(VendaResponse::from)
                .toList();
    }

    @GetMapping("/buscar/vendas/{id}")
    @Operation(summary = "Busca uma venda por id")
    public VendaResponse buscar(@PathVariable String id) {
        return VendaResponse.from(vendaService.buscarPorId(id).orElseThrow());
    }

    @PostMapping("/criar/vendas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma venda a partir do carrinho")
    public Map<String, String> criar(@Valid @RequestBody VendaRequest request) {
        return Map.of("id", vendaService.criar(request));
    }

    @PostMapping("/confirmar-pagamento/vendas/{vendaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Confirma o pagamento da venda em uma transação")
    public void confirmarPagamento(@PathVariable String vendaId) {
        vendaService.confirmarPagamento(vendaId);
    }

    @DeleteMapping("/deletar/vendas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma venda")
    public void deletar(@PathVariable String id) {
        vendaService.deletar(id);
    }
}
