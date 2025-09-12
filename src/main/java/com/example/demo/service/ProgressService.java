package com.example.demo.service;

import org.springframework.stereotype.Service;


@Service
public class ProgressService {

    private volatile int importProgress = 0;
    private volatile int validationProgress = 0;
    private volatile int generationProgress = 0;

    public void setImportProgress(int completedSteps, int totalSteps) {
        if (totalSteps == 0) {
            this.importProgress = 0;
        } else {
            this.importProgress = (completedSteps * 100) / totalSteps;
        }
    }

    public int getImportProgress() {
        return importProgress;
    }

    public void setGenerationProgress(int completedSteps, int totalSteps) {
        if (totalSteps == 0) {
            this.generationProgress = 0;
        } else {
            this.generationProgress = (completedSteps * 100) / totalSteps;
        }
    }

    public int getGenerationProgress() {
        return generationProgress;
    }

    public void setValidationProgress(int completedSteps, int totalSteps) {
        if (totalSteps == 0) {
            this.validationProgress = 0;
        } else {
            this.validationProgress = (completedSteps * 100) / totalSteps;
        }
    }

    public int getValidationProgress() {
        return validationProgress;
    }

    public void resetProgress() {
        this.importProgress = 0;
        this.validationProgress = 0;
        this.generationProgress = 0;
    }
}


