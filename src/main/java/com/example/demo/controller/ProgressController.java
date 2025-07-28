package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ProgressService;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/importProgress")
    public int getImportProgress() {
        return progressService.getImportProgress();
    }

    @GetMapping("/validationProgress")
    public int getValidationProgress() {
        return progressService.getValidationProgress();
    }

    @GetMapping("/generationProgress")
    public int getGenerationProgress() {
        return progressService.getGenerationProgress();
    }
}

