package com.example.demos.decorator;

import java.util.List;
import java.util.Optional;

/**
 * De Component-interface: definieert alle repository-operaties.
 */
public interface Repository<T> {
    Optional<T> findById(long id);
    List<T> findAll();
    void save(T entity);
    void delete(long id);
}
