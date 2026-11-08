package br.edu.ifpb.catalogo_ecommerce;

import br.edu.ifpb.catalogo_ecommerce.enumerates.MetodoPagamento;
import br.edu.ifpb.catalogo_ecommerce.exception.EntidadeNaoEncontradaException;
import br.edu.ifpb.catalogo_ecommerce.model.Verificacao.CodigoVerificacao;
import br.edu.ifpb.catalogo_ecommerce.model.carrinho.Carrinho;
import br.edu.ifpb.catalogo_ecommerce.model.carrinho.ItemCarrinho;
import br.edu.ifpb.catalogo_ecommerce.model.pagamento.Pagamento;
import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import br.edu.ifpb.catalogo_ecommerce.model.usuario.Usuario;
import br.edu.ifpb.catalogo_ecommerce.model.venda.Venda;
import br.edu.ifpb.catalogo_ecommerce.repository.CodigoVerificacaoRepository;
import br.edu.ifpb.catalogo_ecommerce.service.CarrinhoService;
import br.edu.ifpb.catalogo_ecommerce.service.PagamentoService;
import br.edu.ifpb.catalogo_ecommerce.service.ProdutoService;
import br.edu.ifpb.catalogo_ecommerce.service.UsuarioService;
import br.edu.ifpb.catalogo_ecommerce.service.VendaService;
import com.google.cloud.Timestamp;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Bateria automatizada de testes de fumaca, executada pela opcao 7 do menu.
 * Cria documentos no Firestore, valida cada operacao e remove tudo ao final.
 */
public class BateriaAutomatica {

    private final List<String> falhas = new ArrayList<>();
    private final List<Limpeza> limpezas = new ArrayList<>();

    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;
    private final CarrinhoService carrinhoService;
    private final VendaService vendaService;
    private final PagamentoService pagamentoService;
    private final CodigoVerificacaoRepository codigoVerificacaoRepository;

    public BateriaAutomatica(ConfigurableApplicationContext contexto) {
        this.produtoService = contexto.getBean(ProdutoService.class);
        this.usuarioService = contexto.getBean(UsuarioService.class);
        this.carrinhoService = contexto.getBean(CarrinhoService.class);
        this.vendaService = contexto.getBean(VendaService.class);
        this.pagamentoService = contexto.getBean(PagamentoService.class);
        this.codigoVerificacaoRepository = contexto.getBean(CodigoVerificacaoRepository.class);
    }

    /** Executa todos os cenarios e devolve {@code true} se nenhum falhou. */
    public boolean executar() {
        try {
            cenarioProduto();
            cenarioUsuario();
            cenarioCarrinho();
            cenarioVendaEPagamento();
            cenarioCodigoVerificacao();
        } finally {
            executarLimpeza();
        }
        if (!falhas.isEmpty()) {
            System.out.println("=== RESUMO ===");
            System.out.println(falhas.size() + " falha(s):");
            falhas.forEach(falha -> System.out.println(" - " + falha));
            System.out.println();
            System.out.println("Dica: confirme se o emulador do Firestore esta rodando "
                    + "(firebase emulators:start --only firestore).");
        }
        return falhas.isEmpty();
    }

    // ------------------------------------------------------------------ cenarios

    private void cenarioProduto() {
        cenario("PRODUTO - criar, consultar, atualizar e listar", () -> {
            Produto produto = new Produto(
                    "Notebook IFPB",
                    "Notebook usado na bateria de testes",
                    349_990L,
                    10,
                    true,
                    "informatica",
                    null);
            produto.setTags(List.of("teste", "notebook"));
            produto.setAtributos(Map.of("memoria", "16GB", "ssd", "512GB"));

            String id = produtoService.criar(produto);
            exigir(id != null && !id.isBlank(), "a criacao deveria gerar um id");
            agendarLimpeza("produto " + id, () -> produtoService.deletar(id));
            System.out.println("  criado: id=" + id);

            Produto encontrado = produtoService.buscarPorId(id).orElseThrow();
            exigirIgual("Notebook IFPB", encontrado.getNome(), "nome");
            exigirIgual(349_990L, encontrado.getPrecoEmCentavos(), "precoEmCentavos");
            exigirIgual(10, encontrado.getEstoque(), "estoque");
            exigirIgual("informatica", encontrado.getCatregoriaId(), "categoriaId");
            exigirIgual(List.of("teste", "notebook"), encontrado.getTags(), "tags");
            exigir(encontrado.getCriadoEm() != null, "criadoEm deveria ser preenchido pelo @ServerTimestamp");
            System.out.println("  lido: " + encontrado.getNome() + " / estoque=" + encontrado.getEstoque());

            encontrado.setNome("Notebook IFPB (atualizado)");
            encontrado.setEstoque(7);
            String idSalvo = produtoService.salvar(id, encontrado);
            exigirIgual(id, idSalvo, "id retornado por salvar");

            Produto atualizado = produtoService.buscarPorId(id).orElseThrow();
            exigirIgual("Notebook IFPB (atualizado)", atualizado.getNome(), "nome apos atualizacao");
            exigirIgual(7, atualizado.getEstoque(), "estoque apos atualizacao");
            System.out.println("  atualizado: " + atualizado.getNome() + " / estoque=" + atualizado.getEstoque());

            List<Produto> todos = produtoService.buscarTodos();
            exigir(todos.stream().anyMatch(p -> id.equals(p.getId())),
                    "buscarTodos deveria conter o produto criado");
            System.out.println("  listados: " + todos.size() + " produto(s) na colecao");
        });
    }

    private void cenarioUsuario() {
        cenario("USUARIO - criar, consultar, atualizar e erro esperado", () -> {
            Usuario usuario = new Usuario(null, "teste.main@ifpb.edu.br", "senha-segura");

            String id = usuarioService.criar(usuario);
            exigir(id != null && !id.isBlank(), "a criacao deveria gerar um id");
            agendarLimpeza("usuario " + id, () -> usuarioService.deletar(id));
            System.out.println("  criado: id=" + id);

            Usuario encontrado = usuarioService.buscarPorId(id).orElseThrow();
            exigirIgual("teste.main@ifpb.edu.br", encontrado.getEmail(), "email");
            exigirIgual("senha-segura", encontrado.getSenha(), "senha");
            System.out.println("  lido: " + encontrado.getEmail());

            encontrado.setSenha("nova-senha");
            usuarioService.salvar(id, encontrado);
            exigirIgual("nova-senha", usuarioService.buscarPorId(id).orElseThrow().getSenha(),
                    "senha apos salvar");
            System.out.println("  senha atualizada");

            boolean lancou = false;
            try {
                usuarioService.buscarPorId("id-que-nao-existe");
            } catch (EntidadeNaoEncontradaException e) {
                lancou = true;
            }
            exigir(lancou, "buscarPorId inexistente deveria lancar EntidadeNaoEncontradaException");
            System.out.println("  excecao esperada confirmada para id inexistente");
        });
    }

    private void cenarioCarrinho() {
        cenario("CARRINHO - criacao, itens e totais", () -> {
            Produto produto = new Produto("Caneca IFPB", "Caneca de ceramica", 4_500L, 20, true,
                    "utilidades", null);
            String produtoId = produtoService.criar(produto);
            agendarLimpeza("produto " + produtoId, () -> produtoService.deletar(produtoId));
            produto.setId(produtoId);

            ItemCarrinho item = new ItemCarrinho(produto, 3);
            exigirIgual(13_500L, item.getSubtotal(), "subtotal do item");

            List<ItemCarrinho> itens = List.of(item);
            Carrinho carrinho = new Carrinho(null, itens, item.getSubtotal(), itens);
            String carrinhoId = carrinhoService.criar(carrinho);
            agendarLimpeza("carrinho " + carrinhoId, () -> carrinhoService.deletar(carrinhoId));
            System.out.println("  criado: id=" + carrinhoId + " total=" + carrinho.getTotalEmCentavos());

            Carrinho salvo = carrinhoService.buscarPorId(carrinhoId).orElseThrow();
            exigirIgual(13_500L, salvo.getTotalEmCentavos(), "totalEmCentavos");
            exigirIgual(1, salvo.getProdutos().size(), "itens do carrinho");

            ItemCarrinho itemSalvo = salvo.getProdutos().getFirst();
            exigirIgual(produtoId, itemSalvo.getProdutoId(), "produtoId do item");
            exigirIgual("Caneca IFPB", itemSalvo.getNome(), "nome do item");
            exigirIgual(3, itemSalvo.getQuantidade(), "quantidade do item");
            exigirIgual(13_500L, itemSalvo.getSubtotal(), "subtotal lido");
            System.out.println("  lido: " + itemSalvo.getQuantidade() + "x " + itemSalvo.getNome()
                    + " = " + itemSalvo.getSubtotal() + " centavos");
        });
    }

    private void cenarioVendaEPagamento() {
        cenario("VENDA + PAGAMENTO - fluxo de compra ate a confirmacao", () -> {
            Produto produto = new Produto("Curso de Spring Boot", "Curso de teste", 19_900L, 100,
                    true, "cursos", null);
            String produtoId = produtoService.criar(produto);
            agendarLimpeza("produto " + produtoId, () -> produtoService.deletar(produtoId));
            produto.setId(produtoId);
            // Timestamp concreto evita o sentinela de @ServerTimestamp dentro de um array.
            produto.setCriadoEm(Timestamp.now());

            ItemCarrinho item = new ItemCarrinho(produto, 1);
            List<ItemCarrinho> itens = List.of(item);
            String carrinhoId = carrinhoService.criar(new Carrinho(null, itens, item.getSubtotal(), itens));
            agendarLimpeza("carrinho " + carrinhoId, () -> carrinhoService.deletar(carrinhoId));

            Venda venda = new Venda();
            venda.setCarrinhoId(carrinhoId);
            venda.setTotalEmCentavos(item.getSubtotal());
            venda.setItens(List.of(produto));
            venda.setPago(false);
            String vendaId = vendaService.criar(venda);
            agendarLimpeza("venda " + vendaId, () -> vendaService.deletar(vendaId));
            System.out.println("  venda criada: id=" + vendaId + " total=" + venda.getTotalEmCentavos());

            Pagamento pagamento = new Pagamento(false, null, vendaId, MetodoPagamento.PIX,
                    venda.getTotalEmCentavos());
            String pagamentoId = pagamentoService.criar(pagamento);
            agendarLimpeza("pagamento " + pagamentoId, () -> pagamentoService.deletar(pagamentoId));
            System.out.println("  pagamento criado: id=" + pagamentoId);

            Pagamento pagamentoSalvo = pagamentoService.buscarPorId(pagamentoId).orElseThrow();
            exigirIgual(MetodoPagamento.PIX, pagamentoSalvo.getMetodoPagamento(), "metodoPagamento");
            exigirIgual(vendaId, pagamentoSalvo.getVendaId(), "vendaId do pagamento");
            exigir(!pagamentoSalvo.isPago(), "pagamento recem-criado deveria estar pendente");

            pagamentoSalvo.setPago(true);
            pagamentoService.salvar(pagamentoId, pagamentoSalvo);
            exigir(pagamentoService.buscarPorId(pagamentoId).orElseThrow().isPago(),
                    "pagamento deveria estar pago apos salvar");

            Venda vendaSalva = vendaService.buscarPorId(vendaId).orElseThrow();
            exigir(!vendaSalva.isPago(), "venda deveria comecar nao paga");
            vendaSalva.setPagamentoId(pagamentoId);
            vendaSalva.setPago(true);
            vendaService.salvar(vendaId, vendaSalva);

            Venda vendaFinal = vendaService.buscarPorId(vendaId).orElseThrow();
            exigir(vendaFinal.isPago(), "venda deveria estar paga");
            exigirIgual(pagamentoId, vendaFinal.getPagamentoId(), "pagamentoId da venda");
            exigirIgual(1, vendaFinal.getItens().size(), "itens da venda");
            System.out.println("  fluxo concluido: venda paga com "
                    + pagamentoSalvo.getMetodoPagamento().getDescricao());
        });
    }

    private void cenarioCodigoVerificacao() {
        cenario("CODIGO DE VERIFICACAO - repositorio", () -> {
            String email = "josewagner4jdjd@gmail.com";

            CodigoVerificacao codigo = new CodigoVerificacao();
            codigo.setEmail(email);
            codigo.setCodigoHash("hash-de-teste");
            codigo.setExpiraEmMillis(System.currentTimeMillis() + 60_000);
            codigo.setTentativas(0);

            codigoVerificacaoRepository.salvarComId(email, codigo);
            agendarLimpeza("codigo de " + email, () -> codigoVerificacaoRepository.deletar(email));

            CodigoVerificacao salvo = codigoVerificacaoRepository.buscarPorId(email).orElseThrow();
            exigirIgual(email, salvo.getEmail(), "email");
            exigirIgual("hash-de-teste", salvo.getCodigoHash(), "codigoHash");
            exigirIgual(0, salvo.getTentativas(), "tentativas");
            System.out.println("  gravado e lido: " + salvo.getEmail());

            codigoVerificacaoRepository.deletar(email);
            exigir(codigoVerificacaoRepository.buscarPorId(email).isEmpty(),
                    "codigo deveria ter sido removido");
            System.out.println("  removido com sucesso");
        });
    }

    // ------------------------------------------------------------------ infraestrutura

    private void cenario(String nome, Runnable roteiro) {
        System.out.println("== " + nome + " ==");
        try {
            roteiro.run();
            System.out.println("  [OK] cenario concluido");
        } catch (AssertionError e) {
            registrarFalha(nome, e.getMessage());
        } catch (RuntimeException e) {
            registrarFalha(nome, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();
    }

    private void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private void exigirIgual(Object esperado, Object obtido, String campo) {
        exigir(Objects.equals(esperado, obtido),
                campo + ": esperado=" + esperado + ", obtido=" + obtido);
    }

    private void registrarFalha(String cenario, String mensagem) {
        falhas.add(cenario + ": " + mensagem);
        System.out.println("  [FALHA] " + mensagem);
    }

    private void agendarLimpeza(String descricao, Runnable acao) {
        limpezas.add(new Limpeza(descricao, acao));
    }

    private void executarLimpeza() {
        if (limpezas.isEmpty()) {
            return;
        }
        System.out.println("== LIMPEZA ==");
        for (int i = limpezas.size() - 1; i >= 0; i--) {
            Limpeza limpeza = limpezas.get(i);
            try {
                limpeza.acao().run();
                System.out.println("  [OK] removido: " + limpeza.descricao());
            } catch (RuntimeException e) {
                System.out.println("  [AVISO] falha ao remover " + limpeza.descricao()
                        + ": " + e.getMessage());
            }
        }
        System.out.println();
    }

    private record Limpeza(String descricao, Runnable acao) {
    }
}
