package com.example.demos.decorator;

import java.util.*;

/**
 * Concrete Component: een simpele in-memory repository.
 * Dit is de "echte" implementatie zonder extra gedrag.
 */
public class SimpleRepository implements Repository<Product> {
    private final Map<Long, Product> store = new HashMap<>();

    @Override
    public Optional<Product> findById(long id) {
        // Simuleer een trage database-operatie
        simulateDelay(50);
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Product> findAll() {
        simulateDelay(100);
        return new ArrayList<>(store.values());
    }

    @Override
    public void save(Product entity) {
        simulateDelay(30);
        store.put(entity.getId(), entity);
    }

    @Override
    public void delete(long id) {
        simulateDelay(30);
        store.remove(id);
    }

    private void simulateDelay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
