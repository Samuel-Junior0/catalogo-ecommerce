package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.dto.CarrinhoRequest;
import br.edu.ifpb.catalogo_ecommerce.dto.ItemCarrinhoRequest;
import br.edu.ifpb.catalogo_ecommerce.model.carrinho.Carrinho;
import br.edu.ifpb.catalogo_ecommerce.model.carrinho.ItemCarrinho;
import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import br.edu.ifpb.catalogo_ecommerce.model.usuario.Usuario;
import br.edu.ifpb.catalogo_ecommerce.repository.CarrinhoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CarrinhoServiceTest {

    private CarrinhoRepository repository;
    private ProdutoService produtoService;
    private UsuarioService usuarioService;
    private CarrinhoService service;

    @BeforeEach
    void setUp() {
        repository = mock(CarrinhoRepository.class);
        produtoService = mock(ProdutoService.class);
        usuarioService = mock(UsuarioService.class);
        service = new CarrinhoService(repository, produtoService, usuarioService);
    }

    @Test
    void criaCarrinhoVazioQuandoItensNaoSaoInformados() {
        when(usuarioService.buscarPorId("u1")).thenReturn(Optional.of(new Usuario()));
        when(repository.criar(any(Carrinho.class))).thenReturn("c1");

        String id = service.criar(new CarrinhoRequest("u1", null));

        ArgumentCaptor<Carrinho> captor = ArgumentCaptor.forClass(Carrinho.class);
        verify(repository).criar(captor.capture());

        Carrinho salvo = captor.getValue();
        assertEquals("c1", id);
        assertEquals("u1", salvo.getUsuarioId());
        assertEquals(List.of(), salvo.getProdutos());
        assertEquals(0, salvo.getTotalEmCentavos());
    }

    @Test
    void adicionaPrimeiroItemAoCarrinho() {
        Carrinho carrinho = carrinhoVazio();
        Produto produto = produto("p1", 1000, 10);
        when(repository.buscarPorId("c1")).thenReturn(Optional.of(carrinho));
        when(produtoService.buscarPorId("p1")).thenReturn(Optional.of(produto));
        when(repository.salvar(eq("c1"), any(Carrinho.class))).thenReturn("c1");

        service.adicionarItem("c1", new ItemCarrinhoRequest("p1", 2));

        assertEquals(1, carrinho.getProdutos().size());
        ItemCarrinho item = carrinho.getProdutos().getFirst();
        assertEquals("p1", item.getProdutoId());
        assertEquals(2, item.getQuantidade());
        assertEquals(2000, carrinho.getTotalEmCentavos());
        assertSame(carrinho.getProdutos(), carrinho.getProdutosSelecionados());
    }

    @Test
    void somaQuantidadeQuandoProdutoJaEstaNoCarrinho() {
        Produto produtoAntigo = produto("p1", 1000, 10);
        ItemCarrinho existente = new ItemCarrinho(produtoAntigo, 2);

        Carrinho carrinho = carrinhoVazio();
        carrinho.setProdutos(List.of(existente));
        carrinho.setProdutosSelecionados(List.of(existente));

        Produto produtoAtualizado = produto("p1", 1200, 10);
        when(repository.buscarPorId("c1")).thenReturn(Optional.of(carrinho));
        when(produtoService.buscarPorId("p1")).thenReturn(Optional.of(produtoAtualizado));
        when(repository.salvar(eq("c1"), any(Carrinho.class))).thenReturn("c1");

        service.adicionarItem("c1", new ItemCarrinhoRequest("p1", 3));

        assertEquals(1, carrinho.getProdutos().size());
        ItemCarrinho item = carrinho.getProdutos().getFirst();
        assertEquals(5, item.getQuantidade());
        assertEquals(1200, item.getPrecoEmCentavos());
        assertEquals(6000, carrinho.getTotalEmCentavos());
    }

    @Test
    void rejeitaSomaQueUltrapassaOEstoque() {
        Produto produto = produto("p1", 1000, 10);
        ItemCarrinho existente = new ItemCarrinho(produto, 5);

        Carrinho carrinho = carrinhoVazio();
        carrinho.setProdutos(List.of(existente));
        carrinho.setProdutosSelecionados(List.of(existente));

        when(repository.buscarPorId("c1")).thenReturn(Optional.of(carrinho));
        when(produtoService.buscarPorId("p1")).thenReturn(Optional.of(produto));

        assertThrows(IllegalArgumentException.class,
                () -> service.adicionarItem("c1", new ItemCarrinhoRequest("p1", 6)));

        verify(repository, never()).salvar(eq("c1"), any(Carrinho.class));
    }

    private Carrinho carrinhoVazio() {
        Carrinho carrinho = new Carrinho();
        carrinho.setUsuarioId("u1");
        carrinho.setProdutos(List.of());
        carrinho.setProdutosSelecionados(List.of());
        carrinho.setTotalEmCentavos(0);
        return carrinho;
    }

    private Produto produto(String id, long precoEmCentavos, int estoque) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome("Produto " + id);
        produto.setPrecoEmCentavos(precoEmCentavos);
        produto.setEstoque(estoque);
        produto.setAtivo(true);
        return produto;
    }
}
