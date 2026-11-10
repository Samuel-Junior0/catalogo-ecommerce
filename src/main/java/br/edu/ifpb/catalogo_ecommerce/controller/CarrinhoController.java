package br.edu.ifpb.catalogo_ecommerce.controller;

import br.edu.ifpb.catalogo_ecommerce.dto.CarrinhoRequest;
import br.edu.ifpb.catalogo_ecommerce.dto.CarrinhoResponse;
import br.edu.ifpb.catalogo_ecommerce.dto.CarrinhoUpdateRequest;
import br.edu.ifpb.catalogo_ecommerce.dto.ItemCarrinhoRequest;
import br.edu.ifpb.catalogo_ecommerce.service.CarrinhoService;
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
@Tag(name = "Carrinhos", description = "CRUD de carrinhos")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    public CarrinhoController(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    @GetMapping("/listar/carrinhos")
    @Operation(summary = "Lista todos os carrinhos")
    public List<CarrinhoResponse> listar() {
        return carrinhoService.buscarTodos().stream()
                .map(CarrinhoResponse::from)
                .toList();
    }

    @GetMapping("/buscar/carrinhos/{id}")
    @Operation(summary = "Busca um carrinho por id")
    public CarrinhoResponse buscar(@PathVariable String id) {
        return CarrinhoResponse.from(carrinhoService.buscarPorId(id).orElseThrow());
    }

    @PostMapping("/criar/carrinhos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um carrinho a partir de produtos e quantidades")
    public Map<String, String> criar(@Valid @RequestBody CarrinhoRequest request) {
        return Map.of("id", carrinhoService.criar(request));
    }

    @PutMapping("/atualizar/carrinhos/{id}")
    @Operation(summary = "Substitui os itens do carrinho")
    public CarrinhoResponse atualizar(@PathVariable String id,
                                      @Valid @RequestBody CarrinhoUpdateRequest request) {
        carrinhoService.atualizar(id, request);
        return CarrinhoResponse.from(carrinhoService.buscarPorId(id).orElseThrow());
    }

    @PostMapping("/adicionar-item/carrinhos/{id}")
    @Operation(summary = "Adiciona um item ao carrinho",
            description = "Se o produto já estiver no carrinho, a quantidade é somada à existente.")
    public CarrinhoResponse adicionarItem(@PathVariable String id,
                                          @Valid @RequestBody ItemCarrinhoRequest request) {
        carrinhoService.adicionarItem(id, request);
        return CarrinhoResponse.from(carrinhoService.buscarPorId(id).orElseThrow());
    }

    @DeleteMapping("/deletar/carrinhos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um carrinho")
    public void deletar(@PathVariable String id) {
        carrinhoService.deletar(id);
    }
}
