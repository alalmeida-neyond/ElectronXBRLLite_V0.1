package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Repository.CategoryRepository;
import com.example.demo.controller.Objects.Entities.DPMOrigin.Category;

@Service
public class CategoryService {

   @Autowired
   private CategoryRepository categoryRepository;

   public Category createCategory (Category category) {
       return categoryRepository.save(category);
   }

   public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(int categoryID) {
        return categoryRepository.findById(categoryID).orElse(null);
    }
}