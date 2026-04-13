package com.example.demos.decorator;

import java.util.*;

/**
 * Concrete Decorator: cachet findById-resultaten in een Map.
 * Laat zien hoe caching als decorator werkt zonder de originele code te wijzigen.
 */
public class CachingRepository implements Repository<Product> {
    private final Repository<Product> delegate;
    private final Map<Long, Product> cache = new HashMap<>();

    public CachingRepository(Repository<Product> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<Product> findById(long id) {
        if (cache.containsKey(id)) {
            System.out.println("[CACHE] HIT voor id " + id);
            return Optional.of(cache.get(id));
        }
        System.out.println("[CACHE] MISS voor id " + id);
        Optional<Product> result = delegate.findById(id);
        result.ifPresent(p -> cache.put(id, p));
        return result;
    }

    @Override
    public List<Product> findAll() {
        // findAll gaat altijd naar de delegate (te complex om te cachen)
        return delegate.findAll();
    }

    @Override
    public void save(Product entity) {
        delegate.save(entity);
        // Cache invalideren bij wijzigingen
        cache.put(entity.getId(), entity);
        System.out.println("[CACHE] Updated cache voor id " + entity.getId());
    }

    @Override
    public void delete(long id) {
        delegate.delete(id);
        cache.remove(id);
        System.out.println("[CACHE] Removed id " + id + " from cache");
    }
}
