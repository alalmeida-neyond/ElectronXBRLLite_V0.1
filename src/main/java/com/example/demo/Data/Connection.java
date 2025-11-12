package com.example.demo.Data;

import java.time.LocalDateTime;
import java.util.List;


import jakarta.persistence.*;

import org.jboss.logging.Logger;

import com.example.demo.Resources.Constants;


public final class Connection {
    private static final String PERSISTENCE_UNIT_NAME_MD = "default";
    public final static Logger LOG = Logger.getLogger(Connection.class);

    private static EntityManagerFactory factory = null;

    public synchronized static EntityManager getEm() {
        initialize();
        return factory.createEntityManager();
    }
    
    protected static java.sql.Connection conn;
    public static void initialize() {
        if (factory == null) {
            try {
                factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME_MD);
                System.out.println("EntityManagerFactory Initialized");
            } catch (Exception e) {
                LOG.error("Failed to initialize EntityManagerFactory", e);
                System.out.println("Failed to initialize EntityManagerFactory");
                throw e;
            }
        }
    }

    public static Boolean persist(Object obj) {
        return persist(new ConnectionManager(), obj);
    }

    public static Boolean persist(ConnectionManager em, Object obj) {
        try {
            return persist(em, obj, true);
        } catch (Exception e) {
            LOG.error(LocalDateTime.now(Constants.LISBON) +  "Erro no persist: " + obj.getClass().getSimpleName(), e);
            return false;
        }
    }

    public static Boolean persist(ConnectionManager em, Object obj, Boolean processException) throws Exception {
        try {
            EntityTransaction tx = em.em.getTransaction();
            persist(em, tx, obj);
            tx.commit();
            return true;
        } catch (Exception e) {
            em.reset();
            if (processException) {
                System.out.println(e.getCause());
                e.printStackTrace();
                return false;
            } else {
                throw e;
            }
        }
    }

    public static void persist(ConnectionManager em, EntityTransaction tx, Object obj) throws Exception {
        if (tx == null) {
            tx = em.em.getTransaction();
        }

        if (!tx.isActive()) {
            tx.begin();
        }
        em.em.persist(obj);
    }

    public static Boolean merge(Object obj) {
        return merge(new ConnectionManager(), obj);
    }

    public static Boolean merge(ConnectionManager em, Object obj) {
        try {
            return merge(em, obj, true);
        } catch (Exception e) {
            System.out.println(e.getCause());
            return false;
        }
    }

    public static Boolean merge(ConnectionManager em, Object obj, Boolean processException) throws Exception {
        try {
            EntityTransaction tx = em.em.getTransaction();
            merge(em, tx, obj);
            tx.commit();
            return true;
        } catch (Exception e) {
            em.reset();
            if (processException) {
                System.out.println(e.getCause());
                return false;
            } else {
                throw e;
            }
        }
    }

    public static void merge(ConnectionManager em, EntityTransaction tx, Object obj) throws Exception {
        if (!tx.isActive()) {
            tx.begin();
        }
        em.em.merge(obj);
    }

    public static Boolean remove(Object obj) {
        return remove(new ConnectionManager(), obj);
    }

    public static Boolean remove(ConnectionManager em, Object obj) {
        try {
            EntityTransaction tx = em.em.getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            em.em.remove(obj);
            tx.commit();
            return true;
        } catch (Exception e) {
            System.out.println(e.getCause());
            return false;
        }
    }

    public static <T> Boolean persistList(List<T> lst) {
        return persistList(new ConnectionManager(), lst);
    }

    public static <T> Boolean persistList(ConnectionManager em, List<T> lst) {
        try {
            EntityTransaction tx = em.em.getTransaction();
            for (Object obj : lst) {
                persist(em, tx, obj);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            em.reset();
            System.out.println(e.getCause());
            return false;
        }
    }

    public static <T> Boolean mergeList(ConnectionManager em, List<T> lst) {
        try {
            EntityTransaction tx = em.em.getTransaction();
            for (Object obj : lst) {
                merge(em, tx, obj);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            em.reset();
            System.out.println(e.getCause());
            return false;
        }
    }

    public static <T> Boolean removeList(ConnectionManager em, List<T> lst) {
        try {
            EntityTransaction tx = em.em.getTransaction();
            if (!tx.isActive()) {
                tx.begin();
            }
            for (Object obj : lst) {
                em.em.remove(obj);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            em.reset();
            System.out.println(e.getCause());
            return false;
        }
    }

    public static Integer getIdFromEntity(Object entity) {
        initialize();
        if (entity != null && !(entity instanceof String) && !(entity instanceof Long)) {
            return (Integer) factory.getPersistenceUnitUtil().getIdentifier(entity);
        }
        return 0;
    }

    public static int createNativeQuery(String query) {
        EntityManager em = getEm();
        try {
            return createNativeQuery(em, query);
        } finally {
            em.close();
        }
    }

    public static int createNativeQuery(String query, String... parameters) {
        EntityManager em = getEm();
        try {
            return createNativeQuery(em, query, parameters);
        } finally {
            em.close();
        }
    }

    public static int createNativeQuery(EntityManager em, String query, String... parameters) {
        return createNativeQuery(em, true, query, parameters);
    }

    @SuppressWarnings("null")
    public static int createNativeQuery(EntityManager em, Boolean commit, String query, String... parameters) {

        int affectedRows = 0;
        EntityTransaction tx = null;
        try {
            if (commit) {
                tx = em.getTransaction();
                tx.begin();
            }

            Query q = em.createNativeQuery(query);
            if (parameters != null && parameters.length > 0 && parameters.length % 2 == 0) {
                int i = 0;
                while (i < parameters.length) {
                    if (parameters[i] != null && !"".equals(parameters[i]) && parameters[i + 1] != null && !"".equals(parameters[i + 1])) {
                        q.setParameter(parameters[i], parameters[i + 1]);
                    }
                    i = i + 2;
                }
            }
            affectedRows = q.executeUpdate();

            if (commit) {
                tx.commit();
            }
        } catch (Exception e) {
            affectedRows = -1;
            if (commit && tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.out.println(e.getCause());
        }

        return affectedRows;
    }

    public static int createNativeQuery(ConnectionManager em, Boolean commit, String query, String... parameters) {

        int affectedRows = 0;
        try {
            if (em.tx == null) {
                em.createNewTransaction();
            }

            if (!em.tx.isActive()) {
                em.tx.begin();
            }

            Query q = em.em.createNativeQuery(query);
            if (parameters != null && parameters.length > 0 && parameters.length % 2 == 0) {
                int i = 0;
                while (i < parameters.length) {
                    if (parameters[i] != null && !"".equals(parameters[i]) && parameters[i + 1] != null && !"".equals(parameters[i + 1])) {
                        q.setParameter(parameters[i], parameters[i + 1]);
                    }
                    i = i + 2;
                }
            }
            affectedRows = q.executeUpdate();

            if (commit) {
                em.tx.commit();
            }
        } catch (Exception e) {
            affectedRows = -1;
            if (commit && em.tx != null && em.tx.isActive()) {
                em.tx.rollback();
            }
            System.out.println(e.getCause());
        }

        return affectedRows;
    }

    public static void close(EntityManager em) {
        try {
            if (em != null) {
                em.close();
            }
        } catch (Exception e) {
        }
    }
}
