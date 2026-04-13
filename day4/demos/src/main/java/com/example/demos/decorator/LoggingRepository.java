package com.example.demos.decorator;

import java.util.List;
import java.util.Optional;

/**
 * Concrete Decorator: voegt logging toe aan elke repository-operatie.
 * Delegeert alle calls naar de gewrapte repository.
 */
public class LoggingRepository implements Repository<Product> {
    private final Repository<Product> delegate;

    public LoggingRepository(Repository<Product> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<Product> findById(long id) {
        System.out.println("[LOG] findById(" + id + ")");
        Optional<Product> result = delegate.findById(id);
        System.out.println("[LOG] findById result: " + result.orElse(null));
        return result;
    }

    @Override
    public List<Product> findAll() {
        System.out.println("[LOG] findAll()");
        List<Product> result = delegate.findAll();
        System.out.println("[LOG] findAll returned " + result.size() + " items");
        return result;
    }

    @Override
    public void save(Product entity) {
        System.out.println("[LOG] save(" + entity + ")");
        delegate.save(entity);
        System.out.println("[LOG] save completed");
    }

    @Override
    public void delete(long id) {
        System.out.println("[LOG] delete(" + id + ")");
        delegate.delete(id);
        System.out.println("[LOG] delete completed");
    }
}
