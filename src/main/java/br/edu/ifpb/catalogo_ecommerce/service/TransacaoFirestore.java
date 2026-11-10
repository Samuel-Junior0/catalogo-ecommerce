package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.exception.ConflitoException;
import br.edu.ifpb.catalogo_ecommerce.exception.RepositoryException;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import com.google.api.gax.rpc.ApiException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.ExecutionException;

final class TransacaoFirestore {

    private TransacaoFirestore() {
    }

    static <R> R executar(Firestore firestore, Transaction.Function<R> funcao, String mensagem) {
        return executar(firestore, funcao, mensagem, null);
    }

    static <R> R executar(Firestore firestore, Transaction.Function<R> funcao,
                          String mensagem, String mensagemConflito) {
        try {
            return firestore.runTransaction(funcao).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RepositoryException(mensagem + " interrompida", e);
        } catch (ExecutionException e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            if (mensagemConflito != null && documentoJaExiste(causa)) {
                throw new ConflitoException(mensagemConflito);
            }
            if (causa instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RepositoryException(mensagem, e);
        }
    }

    private static boolean documentoJaExiste(Throwable causa) {
        for (Throwable atual = causa; atual != null; atual = atual.getCause()) {
            if (atual instanceof StatusRuntimeException status
                    && status.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
                return true;
            }
            if (atual instanceof ApiException apiException
                    && apiException.getStatusCode().getCode()
                    == com.google.api.gax.rpc.StatusCode.Code.ALREADY_EXISTS) {
                return true;
            }
        }
        return false;
    }
}
