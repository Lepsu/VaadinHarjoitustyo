package com.example.category;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<Category> findAll() {
        return repository.findAllWithEvents(); // ← vaihda tämä
    }

    public Optional<Category> findById(Long id) {
        return repository.findById(id);
    }

    public Category save(Category category) {
        return repository.save(category);
    }

    public void delete(Category category) {
        repository.delete(category);
    }
}