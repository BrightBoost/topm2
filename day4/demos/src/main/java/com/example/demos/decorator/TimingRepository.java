package com.example.demos.decorator;

import java.util.List;
import java.util.Optional;

/**
 * Concrete Decorator: meet de uitvoertijd van elke operatie.
 */
public class TimingRepository implements Repository<Product> {
    private final Repository<Product> delegate;

    public TimingRepository(Repository<Product> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<Product> findById(long id) {
        long start = System.nanoTime();
        Optional<Product> result = delegate.findById(id);
        printDuration("findById", start);
        return result;
    }

    @Override
    public List<Product> findAll() {
        long start = System.nanoTime();
        List<Product> result = delegate.findAll();
        printDuration("findAll", start);
        return result;
    }

    @Override
    public void save(Product entity) {
        long start = System.nanoTime();
        delegate.save(entity);
        printDuration("save", start);
    }

    @Override
    public void delete(long id) {
        long start = System.nanoTime();
        delegate.delete(id);
        printDuration("delete", start);
    }

    private void printDuration(String method, long startNanos) {
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        System.out.println("[TIMING] " + method + " took " + durationMs + " ms");
    }
}
