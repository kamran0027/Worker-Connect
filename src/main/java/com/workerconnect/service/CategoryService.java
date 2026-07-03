package com.workerconnect.service;

import com.workerconnect.model.Category;
import com.workerconnect.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Transactional
    public Category createCategory(String name, String description, String icon) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Category already exists");
        }
        return categoryRepository.save(Category.builder()
                .name(name).description(description).icon(icon).active(true).build());
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description, String icon) {
        Category cat = findById(id);
        cat.setName(name);
        cat.setDescription(description);
        cat.setIcon(icon);
        return categoryRepository.save(cat);
    }

    @Transactional
    public void toggleStatus(Long id) {
        Category cat = findById(id);
        cat.setActive(!cat.isActive());
        categoryRepository.save(cat);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
