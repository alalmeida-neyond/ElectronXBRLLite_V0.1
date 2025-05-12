package com.example.demo.Data;

import jakarta.persistence.*;


public class ConnectionManager {

    public EntityManager em;
    public EntityTransaction tx;

    public ConnectionManager() {
        em = Connection.getEm();
    }

    public ConnectionManager(EntityManager em) {
        this.em = em;
    }

    public void reset() {
        close();
        em = Connection.getEm();
    }

    public void close() {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    public boolean isOpen() {
        if (em == null) {
            return false;
        }
        return em.isOpen();
    }

    public void createNewTransaction() {
        tx = em.getTransaction();
    }
}