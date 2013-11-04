package org.celllife.report.framework;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

/**
 * User: Kevin W. Sewell
 * Date: 2013-03-17
 * Time: 12h14
 */
public class MockPagingAndSortingRepository<T, ID extends Serializable> implements PagingAndSortingRepository<T, ID> {
    
    @Override
    public <S extends T> S save(S entity) {
        return null;  
    }

    @Override
    public <S extends T> Iterable<S> save(Iterable<S> entities) {
        return null;  
    }

    @Override
    public T findOne(ID id) {
        return null;  
    }

    @Override
    public boolean exists(ID id) {
        return false;  
    }

    @Override
    public Iterable<T> findAll() {
        return null;  
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids) {
        return null;  
    }

    @Override
    public long count() {
        return 0;  
    }

    @Override
    public void delete(ID id) {
        
    }

    @Override
    public void delete(T entity) {
        
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        
    }

    @Override
    public void deleteAll() {
        
    }

    @Override
    public Iterable<T> findAll(Sort sort) {
        return null;  
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return null;  
    }
}
