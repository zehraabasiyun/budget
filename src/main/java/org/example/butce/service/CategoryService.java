package org.example.butce.service;

import org.example.butce.entity.Category;
import org.example.butce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getCategoriesByType(String type) {
        return categoryRepository.findByType(type);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @PostConstruct
    public void initializeCategories() {
        if (categoryRepository.count() == 0) {
            // Harcama kategorileri
            categoryRepository.save(new Category("Kira", "EXPENSE"));
            categoryRepository.save(new Category("Gıda", "EXPENSE"));
            categoryRepository.save(new Category("Ulaşım", "EXPENSE"));
            categoryRepository.save(new Category("Faturalar", "EXPENSE"));
            categoryRepository.save(new Category("Eğlence", "EXPENSE"));
            categoryRepository.save(new Category("Sağlık", "EXPENSE"));
            categoryRepository.save(new Category("Diğer", "EXPENSE"));

            // Gelir kategorileri
            categoryRepository.save(new Category("Maaş", "INCOME"));
            categoryRepository.save(new Category("Ek Gelir", "INCOME"));
            categoryRepository.save(new Category("Yatırım", "INCOME"));
            categoryRepository.save(new Category("Diğer", "INCOME"));
        }
    }
}

