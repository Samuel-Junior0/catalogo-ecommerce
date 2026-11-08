package br.edu.ifpb.catalogo_ecommerce;

import br.edu.ifpb.catalogo_ecommerce.enumerates.MetodoPagamento;
import br.edu.ifpb.catalogo_ecommerce.exception.EntidadeNaoEncontradaException;
import br.edu.ifpb.catalogo_ecommerce.exception.NenhumaEntidadeEncontradaException;
import br.edu.ifpb.catalogo_ecommerce.model.Verificacao.CodigoVerificacao;
import br.edu.ifpb.catalogo_ecommerce.model.carrinho.Carrinho;
import br.edu.ifpb.catalogo_ecommerce.model.carrinho.ItemCarrinho;
import br.edu.ifpb.catalogo_ecommerce.model.pagamento.Pagamento;
import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import br.edu.ifpb.catalogo_ecommerce.model.usuario.Usuario;
import br.edu.ifpb.catalogo_ecommerce.model.venda.Venda;
import br.edu.ifpb.catalogo_ecommerce.repository.CodigoVerificacaoRepository;
import br.edu.ifpb.catalogo_ecommerce.service.BaseService;
import br.edu.ifpb.catalogo_ecommerce.service.CarrinhoService;
import br.edu.ifpb.catalogo_ecommerce.service.PagamentoService;
import br.edu.ifpb.catalogo_ecommerce.service.ProdutoService;
import br.edu.ifpb.catalogo_ecommerce.service.UsuarioService;
import br.edu.ifpb.catalogo_ecommerce.service.VendaService;
import br.edu.ifpb.catalogo_ecommerce.service.VerificacaoEmailService;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Menu de console para exercitar as funcionalidades do catalogo contra o emulador
 * do Firestore (localhost:8080).
 *
 * <p>Pre-requisito: {@code firebase emulators:start --only firestore}.
 * Execute com {@code .\mvnw.cmd exec:exec} ou diretamente pela IDE.</p>
 */
public final class Main {

    private static final Scanner ENTRADA = new Scanner(System.in, StandardCharsets.UTF_8);

    private static ConfigurableApplicationContext contexto;

    private static ProdutoService produtoService;
    private static UsuarioService usuarioService;
    private static CarrinhoService carrinhoService;
    private static VendaService vendaService;
    private static PagamentoService pagamentoService;
    private static CodigoVerificacaoRepository codigoVerificacaoRepository;
    private static VerificacaoEmailService verificacaoEmailService;

    public static void main(String[] args) {
        if (!iniciarContexto(args)) {
            return;
        }
        System.out.println();
        System.out.println("=== Catálogo E-commerce - Menu interativo ===");
        System.out.println("Firestore: localhost:8080 (emulador)");

        try {
            loopPrincipal();
        } catch (FimDaEntrada e) {
            System.out.println();
            System.out.println("Entrada encerrada.");
        } finally {
            contexto.close();
            ENTRADA.close();
        }
    }

    private static boolean iniciarContexto(String[] args) {
        try {
            contexto = new SpringApplicationBuilder(CatalogoEcommerceApplication.class)
                    .web(WebApplicationType.NONE)
                    .bannerMode(Banner.Mode.OFF)
                    .run(args);
        } catch (RuntimeException e) {
            System.err.println("Não foi possível iniciar o contexto Spring: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        produtoService = contexto.getBean(ProdutoService.class);
        usuarioService = contexto.getBean(UsuarioService.class);
        carrinhoService = contexto.getBean(CarrinhoService.class);
        vendaService = contexto.getBean(VendaService.class);
        pagamentoService = contexto.getBean(PagamentoService.class);
        codigoVerificacaoRepository = contexto.getBean(CodigoVerificacaoRepository.class);
        verificacaoEmailService = contexto.getBean(VerificacaoEmailService.class);
        return true;
    }

    // ------------------------------------------------------------------ menus

    private static void loopPrincipal() {
        while (true) {
            System.out.println();
            System.out.println("================ MENU PRINCIPAL ================");
        System.out.println(" 1 - Produtos");
            System.out.println(" 2 - Usuários");
            System.out.println(" 3 - Carrinhos");
            System.out.println(" 4 - Vendas");
            System.out.println(" 5 - Pagamentos");
            System.out.println(" 6 - Códigos de verificação");
            System.out.println(" 7 - Executar bateria automática de testes");
            System.out.println(" 0 - Sair");

            int opcao = lerOpcao("Opção: ");
            switch (opcao) {
                case 1 -> menuEntidade("PRODUTOS", produtoService,
                        Main::novoProduto, Main::editarProduto, Main::resumoProduto);
                case 2 -> menuEntidade("USUÁRIOS", usuarioService,
                        Main::novoUsuario, Main::editarUsuario, Main::resumoUsuario);
                case 3 -> menuEntidade("CARRINHOS", carrinhoService,
                        Main::novoCarrinho, Main::editarCarrinho, Main::resumoCarrinho);
                case 4 -> menuEntidade("VENDAS", vendaService,
                        Main::novaVenda, Main::editarVenda, Main::resumoVenda);
                case 5 -> menuEntidade("PAGAMENTOS", pagamentoService,
                        Main::novoPagamento, Main::editarPagamento, Main::resumoPagamento);
                case 6 -> menuCodigos();
                case 7 -> executarBateria();
                case 0 -> {
                    System.out.println("Até logo!");
                    return;
                }
                default -> System.out.println("  Opção inválida.");
            }
        }
    }

    private static <T> void menuEntidade(String titulo,
                                         BaseService<T> servico,
                                         Supplier<T> criacao,
                                         Consumer<T> edicao,
                                         Function<T, String> resumo) {
        while (true) {
            System.out.println();
            System.out.println("----------- " + titulo + " -----------");
            System.out.println(" 1 - Criar");
            System.out.println(" 2 - Listar todos");
            System.out.println(" 3 - Buscar por id");
            System.out.println(" 4 - Atualizar");
            System.out.println(" 5 - Deletar");
            System.out.println(" 0 - Voltar");

            int opcao = lerOpcao("Opção: ");
            switch (opcao) {
                case 1 -> executar(() -> {
                    T entidade = criacao.get();
                    if (entidade == null) {
                        System.out.println("  Operação cancelada.");
                        return;
                    }
                    String id = servico.criar(entidade);
                    System.out.println("  Criado com id " + id);
                });
                case 2 -> executar(() -> {
                    List<T> registros = servico.buscarTodos();
                    System.out.println("  " + registros.size() + " registro(s):");
                    for (T registro : registros) {
                        System.out.println("   - " + resumo.apply(registro));
                    }
                });
                case 3 -> executar(() -> {
                    String id = lerObrigatorio("  Id: ");
                    T encontrado = servico.buscarPorId(id).orElseThrow();
                    System.out.println("  " + resumo.apply(encontrado));
                });
                case 4 -> executar(() -> {
                    String id = lerObrigatorio("  Id: ");
                    T entidade = servico.buscarPorId(id).orElseThrow();
                    edicao.accept(entidade);
                    servico.salvar(id, entidade);
                    System.out.println("  Atualizado.");
                });
                case 5 -> executar(() -> {
                    String id = lerObrigatorio("  Id: ");
                    servico.deletar(id);
                    System.out.println("  Deletado.");
                });
                case 0 -> {
                    return;
                }
                default -> System.out.println("  Opção inválida.");
            }
        }
    }

    private static void menuCodigos() {
        while (true) {
            System.out.println();
            System.out.println("----------- CÓDIGOS DE VERIFICAÇÃO -----------");
            System.out.println(" 1 - Salvar/sobrescrever código");
            System.out.println(" 2 - Consultar código por e-mail");
            System.out.println(" 3 - Verificar código informado");
            System.out.println(" 4 - Enviar código por e-mail");
            System.out.println(" 5 - Deletar código");
            System.out.println(" 0 - Voltar");

            int opcao = lerOpcao("Opção: ");
            switch (opcao) {
                case 1 -> executar(Main::salvarCodigo);
                case 2 -> executar(Main::consultarCodigo);
                case 3 -> executar(Main::verificarCodigo);
                case 4 -> executar(Main::enviarCodigo);
                case 5 -> executar(Main::deletarCodigo);
                case 0 -> {
                    return;
                }
                default -> System.out.println("  Opção inválida.");
            }
        }
    }

    private static void executarBateria() {
        System.out.println();
        boolean sucesso = new BateriaAutomatica(contexto).executar();
        System.out.println();
        System.out.println(sucesso
                ? "Bateria concluída com sucesso."
                : "Bateria concluída com falhas (veja acima).");
    }

    // ------------------------------------------------------------------ produtos

    private static Produto novoProduto() {
        String nome = lerObrigatorio("  Nome: ");
        String descricao = ler("  Descrição: ");
        long preco = lerPreco("  Preço (ex.: 34,90)", null);
        int estoque = lerInt("  Estoque", 0);
        String categoria = ler("  Categoria [geral]: ");
        Produto produto = new Produto(nome, descricao, preco, estoque, true,
                categoria.isBlank() ? "geral" : categoria, null);
        return produto;
    }

    private static void editarProduto(Produto produto) {
        produto.setNome(lerOuManter("Nome", produto.getNome()));
        produto.setDescricao(lerOuManter("Descrição", produto.getDescricao()));
        produto.setPrecoEmCentavos(lerPreco("Preço", produto.getPrecoEmCentavos()));
        produto.setEstoque(lerInt("Estoque", produto.getEstoque()));
        produto.setAtivo(lerSimNao("Ativo?", produto.isAtivo()));
        produto.setCatregoriaId(lerOuManter("Categoria", produto.getCatregoriaId()));
    }

    private static String resumoProduto(Produto produto) {
        return produto.getId()
                + " | " + produto.getNome()
                + " | " + formatarPreco(produto.getPrecoEmCentavos())
                + " | estoque=" + produto.getEstoque()
                + " | categoria=" + textoOuTraco(produto.getCatregoriaId())
                + (produto.isAtivo() ? "" : " | INATIVO");
    }

    // ------------------------------------------------------------------ usuarios

    private static Usuario novoUsuario() {
        String email = lerObrigatorio("  E-mail: ");
        String senha = lerObrigatorio("  Senha: ");
        return new Usuario(null, email, senha);
    }

    private static void editarUsuario(Usuario usuario) {
        usuario.setEmail(lerOuManter("E-mail", usuario.getEmail()));
        String senha = ler("  Nova senha (Enter mantém): ");
        if (!senha.isEmpty()) {
            usuario.setSenha(senha);
        }
    }

    private static String resumoUsuario(Usuario usuario) {
        return usuario.getId() + " | " + usuario.getEmail();
    }

    // ------------------------------------------------------------------ carrinhos

    private static Carrinho novoCarrinho() {
        System.out.println("  Produtos disponíveis:");
        listarProdutosDisponiveis();

        String usuarioId = ler("  Id do usuário (opcional): ");
        List<ItemCarrinho> itens = new ArrayList<>();

        while (true) {
            String produtoId = ler("  Id do produto (Enter para finalizar): ");
            if (produtoId.isEmpty()) {
                break;
            }

            Produto produto;
            try {
                produto = produtoService.buscarPorId(produtoId).orElseThrow();
            } catch (EntidadeNaoEncontradaException e) {
                System.out.println("  Produto não encontrado.");
                continue;
            }

            int quantidade = lerInt("  Quantidade", 1);
            itens.add(new ItemCarrinho(produto, quantidade));
            System.out.println("  Item: " + quantidade + "x " + produto.getNome());
        }

        if (itens.isEmpty()) {
            return null;
        }

        long total = itens.stream().mapToLong(ItemCarrinho::getSubtotal).sum();
        Carrinho carrinho = new Carrinho(null, itens, total, itens);
        if (!usuarioId.isEmpty()) {
            carrinho.setUsuarioId(usuarioId);
        }
        return carrinho;
    }

    private static void editarCarrinho(Carrinho carrinho) {
        System.out.println("  Adicionar item ao carrinho:");
        listarProdutosDisponiveis();

        String produtoId = lerObrigatorio("  Id do produto: ");
        Produto produto = produtoService.buscarPorId(produtoId).orElseThrow();
        int quantidade = lerInt("  Quantidade", 1);

        List<ItemCarrinho> itens = new ArrayList<>(
                carrinho.getProdutos() == null ? List.of() : carrinho.getProdutos());
        itens.add(new ItemCarrinho(produto, quantidade));

        carrinho.setProdutos(itens);
        carrinho.setProdutosSelecionados(itens);
        carrinho.setTotalEmCentavos(itens.stream().mapToLong(ItemCarrinho::getSubtotal).sum());
    }

    private static String resumoCarrinho(Carrinho carrinho) {
        int quantidadeItens = carrinho.getProdutos() == null ? 0 : carrinho.getProdutos().size();
        return carrinho.getId()
                + " | usuário=" + textoOuTraco(carrinho.getUsuarioId())
                + " | itens=" + quantidadeItens
                + " | total=" + formatarPreco(carrinho.getTotalEmCentavos());
    }

    private static void listarProdutosDisponiveis() {
        try {
            for (Produto produto : produtoService.buscarTodos()) {
                System.out.println("   - " + produto.getId()
                        + " | " + produto.getNome()
                        + " | " + formatarPreco(produto.getPrecoEmCentavos())
                        + " | estoque=" + produto.getEstoque());
            }
        } catch (NenhumaEntidadeEncontradaException e) {
            System.out.println("   (nenhum produto cadastrado)");
        }
    }

    // ------------------------------------------------------------------ vendas

    private static Venda novaVenda() {
        String carrinhoId = lerObrigatorio("  Id do carrinho: ");
        Carrinho carrinho = carrinhoService.buscarPorId(carrinhoId).orElseThrow();

        List<Produto> itens = new ArrayList<>();
        for (ItemCarrinho item : carrinho.getProdutos()) {
            try {
                itens.add(produtoService.buscarPorId(item.getProdutoId()).orElseThrow());
            } catch (EntidadeNaoEncontradaException e) {
                System.out.println("  Aviso: produto " + item.getProdutoId() + " não existe mais.");
            }
        }

        Venda venda = new Venda();
        venda.setCarrinhoId(carrinhoId);
        venda.setTotalEmCentavos(carrinho.getTotalEmCentavos());
        venda.setItens(itens);
        venda.setPago(false);
        return venda;
    }

    private static void editarVenda(Venda venda) {
        if (!lerSimNao("Marcar esta venda como paga?", true)) {
            return;
        }
        String pagamentoId = ler("  Id do pagamento (opcional): ");
        if (!pagamentoId.isEmpty()) {
            venda.setPagamentoId(pagamentoId);
        }
        venda.setPago(true);
    }

    private static String resumoVenda(Venda venda) {
        return venda.getId()
                + " | carrinho=" + textoOuTraco(venda.getCarrinhoId())
                + " | total=" + formatarPreco(venda.getTotalEmCentavos())
                + " | " + (venda.isPago() ? "PAGA" : "PENDENTE");
    }

    // ------------------------------------------------------------------ pagamentos

    private static Pagamento novoPagamento() {
        String vendaId = lerObrigatorio("  Id da venda: ");

        Long valorPadrao = null;
        try {
            valorPadrao = vendaService.buscarPorId(vendaId).orElseThrow().getTotalEmCentavos();
        } catch (EntidadeNaoEncontradaException e) {
            System.out.println("  Aviso: venda não encontrada; informe o valor manualmente.");
        }

        MetodoPagamento metodo = escolherMetodoPagamento();
        long valor = lerPreco("  Valor", valorPadrao);
        return new Pagamento(false, null, vendaId, metodo, valor);
    }

    private static void editarPagamento(Pagamento pagamento) {
        pagamento.setPago(lerSimNao("Pago?", pagamento.isPago()));
        if (lerSimNao("Alterar método de pagamento?", false)) {
            pagamento.setMetodoPagamento(escolherMetodoPagamento());
        }
    }

    private static String resumoPagamento(Pagamento pagamento) {
        String metodo = pagamento.getMetodoPagamento() == null
                ? "-"
                : pagamento.getMetodoPagamento().getDescricao();
        return pagamento.getId()
                + " | venda=" + textoOuTraco(pagamento.getVendaId())
                + " | " + metodo
                + " | " + formatarPreco(pagamento.getPrecoEmCentavos())
                + " | " + (pagamento.isPago() ? "PAGO" : "PENDENTE");
    }

    private static MetodoPagamento escolherMetodoPagamento() {
        System.out.println("  Métodos: 1-Pix  2-Cartão de crédito  3-Cartão de débito  4-Boleto");
        int opcao = lerInt("  Método", 1);
        return switch (opcao) {
            case 2 -> MetodoPagamento.CARTAO_CREDITO;
            case 3 -> MetodoPagamento.CARTAO_DEBITO;
            case 4 -> MetodoPagamento.BOLETO_BANCARIO;
            default -> MetodoPagamento.PIX;
        };
    }

    // ------------------------------------------------------------------ codigos de verificacao

    private static void salvarCodigo() {
        String email = lerObrigatorio("  E-mail: ").toLowerCase(Locale.ROOT);
        String codigo = lerObrigatorio("  Código: ");

        CodigoVerificacao registro = new CodigoVerificacao();
        registro.setEmail(email);
        registro.setCodigoHash(hash(email, codigo));
        registro.setExpiraEmMillis(System.currentTimeMillis() + 10 * 60 * 1_000L);
        registro.setTentativas(0);

        codigoVerificacaoRepository.salvarComId(email, registro);
        System.out.println("  Código salvo para " + email + " (validade de 10 minutos).");
    }

    private static void consultarCodigo() {
        String email = lerObrigatorio("  E-mail: ").toLowerCase(Locale.ROOT);
        CodigoVerificacao registro = codigoVerificacaoRepository.buscarPorId(email).orElse(null);
        if (registro == null) {
            System.out.println("  Nenhum código cadastrado para " + email + ".");
            return;
        }
        System.out.println("  Hash:       " + registro.getCodigoHash());
        System.out.println("  Expira em:  " + new Date(registro.getExpiraEmMillis()));
        System.out.println("  Tentativas: " + registro.getTentativas());
    }

    private static void verificarCodigo() {
        String email = lerObrigatorio("  E-mail: ");
        String codigo = lerObrigatorio("  Código: ");
        boolean valido = verificacaoEmailService.verificarCodigo(email, codigo);
        System.out.println(valido
                ? "  Código válido (e já consumido)."
                : "  Código inválido, expirado ou bloqueado por tentativas.");
    }

    private static void enviarCodigo() {
        String email = lerObrigatorio("  E-mail: ");
        System.out.println("  Atenção: o envio usa as credenciais SMTP de MAIL_USER/MAIL_PASSWORD.");
        if (!lerSimNao("Confirmar envio para " + email + "?", false)) {
            return;
        }
        verificacaoEmailService.enviarCodigo(email);
        System.out.println("  Código enviado.");
    }

    private static void deletarCodigo() {
        String email = lerObrigatorio("  E-mail: ").toLowerCase(Locale.ROOT);
        codigoVerificacaoRepository.deletar(email);
        System.out.println("  Código de " + email + " removido.");
    }

    private static String hash(String email, String codigo) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest((email + ":" + codigo).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ------------------------------------------------------------------ utilidades

    private static void executar(Runnable acao) {
        try {
            acao.run();
        } catch (FimDaEntrada e) {
            throw e;
        } catch (EntidadeNaoEncontradaException | NenhumaEntidadeEncontradaException e) {
            System.out.println("  " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("  Erro: " + e.getMessage());
        }
    }

    private static String ler(String mensagem) {
        System.out.print(mensagem);
        System.out.flush();
        if (!ENTRADA.hasNextLine()) {
            throw new FimDaEntrada();
        }
        return ENTRADA.nextLine().trim();
    }

    private static String lerObrigatorio(String mensagem) {
        while (true) {
            String valor = ler(mensagem);
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("  Valor obrigatório.");
        }
    }

    private static String lerOuManter(String campo, String atual) {
        String valor = ler("  " + campo + " [" + textoOuTraco(atual) + "]: ");
        return valor.isEmpty() ? atual : valor;
    }

    private static int lerOpcao(String mensagem) {
        String valor = ler(mensagem);
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int lerInt(String mensagem, Integer padrao) {
        while (true) {
            String sufixo = padrao == null ? "" : " [" + padrao + "]";
            String valor = ler("  " + mensagem + sufixo + ": ");
            if (valor.isEmpty() && padrao != null) {
                return padrao;
            }
            try {
                int numero = Integer.parseInt(valor);
                if (numero >= 0) {
                    return numero;
                }
                System.out.println("  Informe um número maior ou igual a zero.");
            } catch (NumberFormatException e) {
                System.out.println("  Informe um número inteiro.");
            }
        }
    }

    private static long lerPreco(String mensagem, Long padraoEmCentavos) {
        while (true) {
            String sufixo = padraoEmCentavos == null ? "" : " [" + formatarPreco(padraoEmCentavos) + "]";
            String valor = ler("  " + mensagem + sufixo + ": ");
            if (valor.isEmpty() && padraoEmCentavos != null) {
                return padraoEmCentavos;
            }
            try {
                String limpo = valor.replace("R$", "").trim();
                if (limpo.contains(",")) {
                    limpo = limpo.replace(".", "").replace(",", ".");
                }
                double reais = Double.parseDouble(limpo);
                if (reais < 0) {
                    throw new NumberFormatException();
                }
                return Math.round(reais * 100);
            } catch (NumberFormatException e) {
                System.out.println("  Valor inválido. Use, por exemplo, 34,90.");
            }
        }
    }

    private static boolean lerSimNao(String mensagem, boolean padrao) {
        while (true) {
            String valor = ler("  " + mensagem + (padrao ? " (S/n): " : " (s/N): "));
            if (valor.isEmpty()) {
                return padrao;
            }
            if (valor.equalsIgnoreCase("s") || valor.equalsIgnoreCase("sim")) {
                return true;
            }
            if (valor.equalsIgnoreCase("n") || valor.equalsIgnoreCase("nao")
                    || valor.equalsIgnoreCase("não")) {
                return false;
            }
            System.out.println("  Responda s ou n.");
        }
    }

    private static String formatarPreco(long centavos) {
        return "R$ " + String.format(Locale.ROOT, "%.2f", centavos / 100.0).replace('.', ',');
    }

    private static String textoOuTraco(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    /** Lancada quando a entrada padrao e encerrada (Ctrl+D / fim do stream). */
    private static final class FimDaEntrada extends RuntimeException {
    }

    private Main() {
    }
}
