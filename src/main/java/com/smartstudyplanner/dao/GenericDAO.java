package com.smartstudyplanner.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object interface demonstrating Abstraction and Generics in Java OOP.
 *
 * @param <T>  Entity type
 * @param <ID> Primary key identifier type
 */
public interface GenericDAO<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean update(T entity);

    boolean deleteById(ID id);
}

