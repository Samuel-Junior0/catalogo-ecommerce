package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.dto.CarrinhoRequest;
import br.edu.ifpb.catalogo_ecommerce.dto.CarrinhoUpdateRequest;
import br.edu.ifpb.catalogo_ecommerce.dto.ItemCarrinhoRequest;
import br.edu.ifpb.catalogo_ecommerce.model.carrinho.Carrinho;
import br.edu.ifpb.catalogo_ecommerce.model.carrinho.ItemCarrinho;
import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import br.edu.ifpb.catalogo_ecommerce.repository.CarrinhoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CarrinhoService extends BaseService<Carrinho> {

    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;

    public CarrinhoService(CarrinhoRepository repository,
                           ProdutoService produtoService,
                           UsuarioService usuarioService) {
        super(repository);
        this.produtoService = produtoService;
        this.usuarioService = usuarioService;
    }

    public String criar(CarrinhoRequest request) {
        usuarioService.buscarPorId(request.usuarioId()).orElseThrow();

        Carrinho carrinho = new Carrinho();
        carrinho.setUsuarioId(request.usuarioId());
        aplicarItens(carrinho, montarItens(request.itens()));
        return criar(carrinho);
    }

    public String atualizar(String id, CarrinhoUpdateRequest request) {
        Carrinho carrinho = buscarPorId(id).orElseThrow();
        aplicarItens(carrinho, montarItens(request.itens()));
        return salvar(id, carrinho);
    }

    public String adicionarItem(String id, ItemCarrinhoRequest request) {
        Carrinho carrinho = buscarPorId(id).orElseThrow();
        Produto produto = produtoService.buscarPorId(request.produtoId()).orElseThrow();
        validarProduto(produto, request.quantidade());

        List<ItemCarrinho> itens = new ArrayList<>(itensDoCarrinho(carrinho));
        ItemCarrinho existente = itens.stream()
                .filter(item -> request.produtoId().equals(item.getProdutoId()))
                .findFirst()
                .orElse(null);

        int quantidadeAtual = existente == null ? 0 : existente.getQuantidade();
        int novaQuantidade = quantidadeAtual + request.quantidade();
        if (novaQuantidade > produto.getEstoque()) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente para " + produto.getNome() + ".");
        }

        if (existente == null) {
            itens.add(new ItemCarrinho(produto, request.quantidade()));
        } else {
            existente.setNome(produto.getNome());
            existente.setPrecoEmCentavos(produto.getPrecoEmCentavos());
            existente.setQuantidade(novaQuantidade);
        }

        aplicarItens(carrinho, itens);
        return salvar(id, carrinho);
    }

    private List<ItemCarrinho> montarItens(List<ItemCarrinhoRequest> itens) {
        if (itens == null || itens.isEmpty()) {
            return List.of();
        }

        Set<String> produtosVistos = new HashSet<>();
        List<ItemCarrinho> resultado = new ArrayList<>();

        for (ItemCarrinhoRequest item : itens) {
            if (!produtosVistos.add(item.produtoId())) {
                throw new IllegalArgumentException("Produto repetido no carrinho: " + item.produtoId());
            }

            Produto produto = produtoService.buscarPorId(item.produtoId()).orElseThrow();
            validarProduto(produto, item.quantidade());
            resultado.add(new ItemCarrinho(produto, item.quantidade()));
        }

        return resultado;
    }

    private void validarProduto(Produto produto, int quantidade) {
        if (!produto.isAtivo()) {
            throw new IllegalArgumentException("Produto indisponível: " + produto.getNome());
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (quantidade > produto.getEstoque()) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente para " + produto.getNome() + ".");
        }
    }

    private List<ItemCarrinho> itensDoCarrinho(Carrinho carrinho) {
        List<ItemCarrinho> itens = carrinho.getProdutosSelecionados();
        if (itens == null || itens.isEmpty()) {
            itens = carrinho.getProdutos();
        }
        return itens == null ? List.of() : itens;
    }

    private void aplicarItens(Carrinho carrinho, List<ItemCarrinho> itens) {
        carrinho.setProdutos(itens);
        carrinho.setProdutosSelecionados(itens);
        carrinho.setTotalEmCentavos(itens.stream()
                .mapToLong(ItemCarrinho::getSubtotal)
                .sum());
    }
}
