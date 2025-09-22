package com.example.demo.controller.Objects.Extensions;

public abstract class RunnableExtension implements Runnable{
    
    private volatile boolean canBeCanceled = false;
    
    private volatile boolean isActive = false;
    
    public void askToBeCanceled() {
        canBeCanceled = true;
    }

    public boolean isCanceled() {
        return canBeCanceled;
    }
    
    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
