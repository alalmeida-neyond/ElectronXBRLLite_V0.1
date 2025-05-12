package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.controller.Objects.Entities.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // Custom query method example:
    Optional<Category> findById(int username);
}
