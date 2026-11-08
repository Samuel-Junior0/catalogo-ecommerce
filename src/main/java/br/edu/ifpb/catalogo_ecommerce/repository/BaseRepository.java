package br.edu.ifpb.catalogo_ecommerce.repository;

import br.edu.ifpb.catalogo_ecommerce.exception.RepositoryException;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public abstract class BaseRepository<T> {

    protected final Firestore firestore;
    protected final CollectionReference colecao;
    private final Class<T> tipo;


    public BaseRepository(Firestore firestore, String colecao, Class<T> tipo) {
        this.firestore = firestore;
        this.colecao = firestore.collection(colecao);
        this.tipo = tipo;
    }

    public String criar(T entidade){
        try {
            return colecao.add(entidade).get().getId();
        } catch (InterruptedException | ExecutionException e) {
            throw new RepositoryException("Erro ao criar documento", e);
        }
    }

    public String salvar(String id, T entidade){
        try {
            colecao.document(id).set(entidade).get();
            return id;
        } catch (InterruptedException | ExecutionException e) {
            throw new RepositoryException("Erro ao salvar documento " + id, e);
        }
    }

    public Optional<T> buscarPorId(String id){
        try {
            DocumentSnapshot doc = colecao.document(id).get().get();
            return doc.exists() ? Optional.ofNullable(doc.toObject(tipo)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RepositoryException("Erro ao buscar documento" + id, e);
        }
    }

    public List<T> listarTodos(){
        return executar(colecao);
    }

    public void deletar(String id) {
        try {
            colecao.document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RepositoryException("Erro ao deletar documento " + id, e);
        }
    }

    protected List<T> executar(Query query) {
        try {
            return query.get().get().toObjects(tipo);
        } catch (InterruptedException | ExecutionException e) {
            throw new RepositoryException("Erro ao executar consulta", e);
        }
    }
}
