package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.dto.PagamentoRequest;
import br.edu.ifpb.catalogo_ecommerce.exception.ConflitoException;
import br.edu.ifpb.catalogo_ecommerce.exception.EntidadeNaoEncontradaException;
import br.edu.ifpb.catalogo_ecommerce.model.pagamento.Pagamento;
import br.edu.ifpb.catalogo_ecommerce.repository.PagamentoRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import org.springframework.stereotype.Service;

@Service
public class PagamentoService extends BaseService<Pagamento> {

    private final Firestore firestore;

    public PagamentoService(PagamentoRepository repository, Firestore firestore) {
        super(repository);
        this.firestore = firestore;
    }

    public String criar(PagamentoRequest request) {
        String vendaId = request.vendaId();
        String pagamentoId = vendaId;

        return TransacaoFirestore.executar(firestore, transaction -> {
            DocumentReference vendaRef = firestore.collection("vendas").document(vendaId);
            DocumentReference pagamentoRef = firestore.collection("pagamentos").document(pagamentoId);
            DocumentSnapshot venda = transaction.get(vendaRef).get();

            if (!venda.exists()) {
                throw new EntidadeNaoEncontradaException("Venda", vendaId);
            }
            if (Boolean.TRUE.equals(venda.getBoolean("pago"))) {
                throw new ConflitoException("A venda " + vendaId + " já está paga.");
            }

            Query pagamentosDaVenda = firestore.collection("pagamentos")
                    .whereEqualTo("vendaId", vendaId);
            if (transaction.get(pagamentoRef).get().exists()
                    || !transaction.get(pagamentosDaVenda).get().isEmpty()) {
                throw new ConflitoException("A venda " + vendaId + " já possui um pagamento.");
            }

            Long total = venda.getLong("totalEmCentavos");
            Pagamento pagamento = new Pagamento();
            pagamento.setVendaId(vendaId);
            pagamento.setMetodoPagamento(request.metodoPagamento());
            pagamento.setPrecoEmCentavos(total == null ? 0 : total);
            pagamento.setPago(false);
            transaction.create(pagamentoRef, pagamento);
            return pagamentoId;
        }, "Erro ao criar pagamento", "A venda " + vendaId + " já possui um pagamento.");
    }

    @Override
    public void deletar(String id) {
        TransacaoFirestore.executar(firestore, transaction -> {
            DocumentReference pagamentoRef = firestore.collection("pagamentos").document(id);
            DocumentSnapshot pagamento = transaction.get(pagamentoRef).get();
            if (!pagamento.exists()) {
                throw new EntidadeNaoEncontradaException("Pagamento", id);
            }

            String vendaId = pagamento.getString("vendaId");
            if (vendaId != null) {
                DocumentReference vendaRef = firestore.collection("vendas").document(vendaId);
                DocumentSnapshot venda = transaction.get(vendaRef).get();
                if (venda.exists() && id.equals(venda.getString("pagamentoId"))) {
                    transaction.update(vendaRef, "pago", false,
                            "pagamentoId", FieldValue.delete());
                }
            }

            transaction.delete(pagamentoRef);
            return null;
        }, "Erro ao deletar pagamento");
    }
}
