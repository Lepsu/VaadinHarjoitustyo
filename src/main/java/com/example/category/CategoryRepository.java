package com.example.category;

import com.example.category.Category;
import com.vaadin.copilot.shaded.guava.base.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.events")
    List<Category> findAllWithEvents();

    Optional<Category> findByNameIgnoreCase(String name);
    List<Category> findByNameContainingIgnoreCase(String name);
}