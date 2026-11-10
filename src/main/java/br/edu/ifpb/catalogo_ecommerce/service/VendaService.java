package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.dto.VendaRequest;
import br.edu.ifpb.catalogo_ecommerce.exception.ConflitoException;
import br.edu.ifpb.catalogo_ecommerce.exception.EntidadeNaoEncontradaException;
import br.edu.ifpb.catalogo_ecommerce.model.carrinho.Carrinho;
import br.edu.ifpb.catalogo_ecommerce.model.carrinho.ItemCarrinho;
import br.edu.ifpb.catalogo_ecommerce.model.pagamento.Pagamento;
import br.edu.ifpb.catalogo_ecommerce.model.produto.Produto;
import br.edu.ifpb.catalogo_ecommerce.model.venda.Venda;
import br.edu.ifpb.catalogo_ecommerce.repository.PagamentoRepository;
import br.edu.ifpb.catalogo_ecommerce.repository.VendaRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VendaService extends BaseService<Venda> {

    private final Firestore firestore;
    private final CarrinhoService carrinhoService;
    private final PagamentoRepository pagamentoRepository;

    public VendaService(VendaRepository repository, Firestore firestore,
                        CarrinhoService carrinhoService, PagamentoRepository pagamentoRepository) {
        super(repository);
        this.firestore = firestore;
        this.carrinhoService = carrinhoService;
        this.pagamentoRepository = pagamentoRepository;
    }

    public String criar(VendaRequest request) {
        String carrinhoId = request.carrinhoId();
        Carrinho carrinho = carrinhoService.buscarPorId(carrinhoId).orElseThrow();
        List<ItemCarrinho> itensCarrinho = itensDoCarrinho(carrinho);
        if (itensCarrinho.isEmpty()) {
            throw new IllegalArgumentException("Carrinho não possui produtos.");
        }

        Map<String, Integer> quantidades = agruparQuantidades(itensCarrinho);

        return TransacaoFirestore.executar(firestore, transaction -> {
            DocumentReference vendaRef = firestore.collection("vendas").document(carrinhoId);
            Query vendasDoCarrinho = firestore.collection("vendas")
                    .whereEqualTo("carrinhoId", carrinhoId);

            if (transaction.get(vendaRef).get().exists()
                    || !transaction.get(vendasDoCarrinho).get().isEmpty()) {
                throw new ConflitoException("O carrinho " + carrinhoId + " já possui uma venda.");
            }

            List<ItemCarrinho> itens = new ArrayList<>();
            Map<String, Produto> produtos = new LinkedHashMap<>();
            long total = 0;

            for (Map.Entry<String, Integer> entrada : quantidades.entrySet()) {
                DocumentReference produtoRef = firestore.collection("produtos")
                        .document(entrada.getKey());
                DocumentSnapshot documento = transaction.get(produtoRef).get();
                if (!documento.exists()) {
                    throw new EntidadeNaoEncontradaException("Produto", entrada.getKey());
                }

                Produto produto = documento.toObject(Produto.class);
                if (produto == null) {
                    throw new EntidadeNaoEncontradaException("Produto", entrada.getKey());
                }
                produto.setId(entrada.getKey());

                if (!produto.isAtivo()) {
                    throw new IllegalArgumentException("Produto indisponível: " + produto.getNome());
                }

                int quantidade = entrada.getValue();
                if (quantidade > produto.getEstoque()) {
                    throw new IllegalArgumentException(
                            "Estoque insuficiente para " + produto.getNome() + ".");
                }
                produtos.put(entrada.getKey(), produto);
            }

            for (Map.Entry<String, Integer> entrada : quantidades.entrySet()) {
                Produto produto = produtos.get(entrada.getKey());
                ItemCarrinho item = new ItemCarrinho(produto, entrada.getValue());
                itens.add(item);
                total += item.getSubtotal();
                DocumentReference produtoRef = firestore.collection("produtos")
                        .document(entrada.getKey());
                transaction.update(produtoRef, "estoque", produto.getEstoque() - entrada.getValue());
            }

            Venda venda = new Venda();
            venda.setCarrinhoId(carrinhoId);
            venda.setItens(itens);
            venda.setTotalEmCentavos(total);
            venda.setPago(false);
            venda.setEstoqueBaixado(true);
            transaction.create(vendaRef, venda);
            return carrinhoId;
        }, "Erro ao criar venda", "O carrinho " + carrinhoId + " já possui uma venda.");
    }

    @Override
    public void deletar(String id) {
        Venda venda = buscarPorId(id).orElseThrow();
        Map<String, Integer> quantidades = venda.getItens() == null
                ? Map.of()
                : agruparQuantidades(venda.getItens());

        TransacaoFirestore.executar(firestore, transaction -> {
            DocumentReference vendaRef = firestore.collection("vendas").document(id);
            DocumentSnapshot vendaAtual = transaction.get(vendaRef).get();
            if (!vendaAtual.exists()) {
                throw new EntidadeNaoEncontradaException("Venda", id);
            }
            List<QueryDocumentSnapshot> pagamentos = transaction.get(firestore.collection("pagamentos")
                    .whereEqualTo("vendaId", id)).get().getDocuments();

            Map<String, DocumentSnapshot> produtos = new LinkedHashMap<>();
            if (Boolean.TRUE.equals(vendaAtual.getBoolean("estoqueBaixado"))) {
                for (Map.Entry<String, Integer> entrada : quantidades.entrySet()) {
                    DocumentReference produtoRef = firestore.collection("produtos")
                            .document(entrada.getKey());
                    produtos.put(entrada.getKey(), transaction.get(produtoRef).get());
                }
                for (Map.Entry<String, Integer> entrada : quantidades.entrySet()) {
                    if (produtos.get(entrada.getKey()).exists()) {
                        DocumentReference produtoRef = firestore.collection("produtos")
                                .document(entrada.getKey());
                        transaction.update(produtoRef, "estoque", FieldValue.increment(entrada.getValue()));
                    }
                }
            }

            pagamentos.forEach(pagamento -> transaction.delete(pagamento.getReference()));
            transaction.delete(vendaRef);
            return null;
        }, "Erro ao deletar venda");
    }

    public void confirmarPagamento(String vendaId) {
        Venda venda = buscarPorId(vendaId).orElseThrow();
        if (venda.isPago()) {
            return;
        }
        Pagamento pagamento = pagamentoRepository.buscarPorVendaId(vendaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "A venda " + vendaId + " não possui pagamento."));
        confirmarPagamento(vendaId, pagamento.getId());
    }

    public void confirmarPagamento(String vendaId, String pagamentoId) {
        DocumentReference vendaRef = firestore.collection("vendas").document(vendaId);
        DocumentReference pagamentoRef = firestore.collection("pagamentos").document(pagamentoId);

        TransacaoFirestore.executar(firestore, transaction -> {
            DocumentSnapshot venda = transaction.get(vendaRef).get();
            DocumentSnapshot pagamento = transaction.get(pagamentoRef).get();

            if (!venda.exists()) {
                throw new EntidadeNaoEncontradaException("Venda", vendaId);
            }
            if (!pagamento.exists()) {
                throw new EntidadeNaoEncontradaException("Pagamento", pagamentoId);
            }

            String vendaDoPagamento = pagamento.getString("vendaId");
            if (!vendaId.equals(vendaDoPagamento)) {
                throw new IllegalArgumentException("O pagamento " + pagamentoId
                        + " não pertence à venda " + vendaId + ".");
            }

            Long valorPagamento = pagamento.getLong("precoEmCentavos");
            Long totalVenda = venda.getLong("totalEmCentavos");
            if (valorPagamento != null && totalVenda != null && !valorPagamento.equals(totalVenda)) {
                throw new IllegalArgumentException("O valor do pagamento não corresponde ao total da venda.");
            }

            transaction.update(pagamentoRef, "pago", true,
                    "pagoEm", FieldValue.serverTimestamp());
            transaction.update(vendaRef, "pago", true, "pagamentoId", pagamentoId);
            return null;
        }, "Erro ao confirmar pagamento");
    }

    private List<ItemCarrinho> itensDoCarrinho(Carrinho carrinho) {
        List<ItemCarrinho> itens = carrinho.getProdutosSelecionados();
        if (itens == null || itens.isEmpty()) {
            itens = carrinho.getProdutos();
        }
        return itens == null ? List.of() : itens;
    }

    private Map<String, Integer> agruparQuantidades(List<ItemCarrinho> itens) {
        Map<String, Integer> quantidades = new LinkedHashMap<>();
        for (ItemCarrinho item : itens) {
            if (item.getProdutoId() == null || item.getProdutoId().isBlank()) {
                throw new IllegalArgumentException("Item de carrinho sem produtoId.");
            }
            if (item.getQuantidade() <= 0) {
                throw new IllegalArgumentException(
                        "Quantidade inválida para o produto " + item.getProdutoId() + ".");
            }
            quantidades.merge(item.getProdutoId(), item.getQuantidade(), Integer::sum);
        }
        return quantidades;
    }
}
