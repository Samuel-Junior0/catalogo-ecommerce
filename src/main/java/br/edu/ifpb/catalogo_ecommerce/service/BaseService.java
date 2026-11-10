package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.exception.EntidadeNaoEncontradaException;
import br.edu.ifpb.catalogo_ecommerce.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T> {

    private final BaseRepository<T> repository;

    protected BaseService(BaseRepository<T> repository) {
        this.repository = repository;
    }

    public String criar(T entidade){
        return repository.criar(entidade);
    }

    public String salvar(String id, T entidade){
        return repository.salvar(id, entidade);
    }

    public Optional<T> buscarPorId(String id){
        Optional<T> entidadeEncontrada = repository.buscarPorId(id);
        if(entidadeEncontrada.isEmpty()){
            throw new EntidadeNaoEncontradaException("Entidade", id);
        }
        return entidadeEncontrada;
    }

    public List<T> buscarTodos(){
        return repository.listarTodos();
    }

    public void deletar(String id){
        buscarPorId(id);
        repository.deletar(id);
    }
}
