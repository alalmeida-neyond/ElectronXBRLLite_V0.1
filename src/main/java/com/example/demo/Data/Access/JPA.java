package com.example.demo.Data.Access;

import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

import com.example.demo.Data.Connection;
import com.example.demo.Data.ConnectionManager;
import com.example.demo.Resources.*;

public class JPA<T> {
    private final Logger LOG = Logger.getLogger(JPA.class);

    private Class<T> type = null;
    private ConnectionManager em = null;
    private String fixedSortField;
    private SortOrder fixedSortOrder;

    public JPA() {
    }

    public JPA(Class<T> type) {
        this(new ConnectionManager(), type);
    }

    public JPA(ConnectionManager em, Class<T> type) {
        this.type = type;
        if (em == null) {
            this.em = new ConnectionManager();
        } else {
            this.em = em;
        }
    }

    public JPA(EntityManager entityManager, Class<T> type) {
        this.type = type;
        if (entityManager == null) {
            this.em = new ConnectionManager();
        } else {
            this.em = new ConnectionManager(entityManager);
        }
    }

    public void detach(T item) {
        if (item != null) {
            em.em.detach(item);
        }
    }

    public void detach(List<T> lst) {
        if (lst != null) {
            for (T item : lst) {
                if (item != null) {
                    em.em.detach(item);
                }
            }
        }
    }

    public List<T> getResultList() {
        return getResultList(0, 0, null, null, null);
    }

    public T getSimpleResult(Map<String, Object> filters) {
        List<T> list = getResultList(0, 1, null, null, filters);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public T getSimpleResult(String filterColumn, String filterValue, String... selectColumns) {
        List<T> list = getSimpleResultList(filterColumn, filterValue, selectColumns);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public T getSimpleResult(String filterColumn1, Object filterValue1, String filterColumn2, Object filterValue2, String... selectColumns) {
        List<T> list = getSimpleResultList(filterColumn1, filterValue1, filterColumn2, filterValue2, selectColumns);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public T getSimpleResult(String filterColumn1, Object filterValue1, String filterColumn2, Object filterValue2, String filterColumn3, Object filterValue3, String... selectColumns) {
        List<T> list = getSimpleResultList(filterColumn1, filterValue1, filterColumn2, filterValue2, filterColumn3, filterValue3, selectColumns);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @SuppressWarnings("serial")
    public List<T> getSimpleResultList(String filterColumn1, Object filterValue1, String filterColumn2, Object filterValue2, String... selectColumns) {
        if (filterColumn1 != null && !"".equals(filterColumn1) && filterValue1 != null && !"".equals(filterValue1) && filterColumn2 != null && !"".equals(filterColumn2) && filterValue2 != null && !"".equals(filterValue2)) {
            return getResultListWithoutPagination(new HashMap<String, Object>() {
                {
                    this.put(filterColumn1, filterValue1);
                    this.put(filterColumn2, filterValue2);
                }
            }, new HashMap<String, String>() {
                {
                    this.put(filterColumn1, "=");
                    this.put(filterColumn2, "=");
                }
            }, selectColumns);
        } else {
            return getResultListWithoutPagination(null, null, selectColumns);
        }
    }

    @SuppressWarnings("serial")
    public List<T> getSimpleResultList(String filterColumn1, Object filterValue1, String filterColumn2,
            Object filterValue2, String filterColumn3, Object filterValue3, String... selectColumns) {
        if (filterColumn1 != null && !"".equals(filterColumn1) && filterValue1 != null && !"".equals(filterValue1)
                && filterColumn2 != null && !"".equals(filterColumn2) && filterValue2 != null
                && !"".equals(filterValue2) && filterColumn3 != null && !"".equals(filterColumn3)
                && filterValue3 != null && !"".equals(filterValue3)) {
            return getResultListWithoutPagination(new HashMap<String, Object>() {
                {
                    this.put(filterColumn1, filterValue1);
                    this.put(filterColumn2, filterValue2);
                    this.put(filterColumn3, filterValue3);
                }
            }, new HashMap<String, String>() {
                {
                    this.put(filterColumn1, "=");
                    this.put(filterColumn2, "=");
                    this.put(filterColumn3, "=");
                }
            }, selectColumns);
        } else {
            return getResultListWithoutPagination(null, null, selectColumns);
        }
    }

    @SuppressWarnings("serial")
    public List<T> getSimpleResultList(String filterColumn, Object filterValue, String... selectColumns) {
        if (filterColumn != null && !"".equals(filterColumn) && filterValue != null && !"".equals(filterValue)) {
            return getResultListWithoutPagination(new HashMap<String, Object>() {
                {
                    this.put(filterColumn, filterValue);
                }
            }, new HashMap<String, String>() {
                {
                    this.put(filterColumn, "=");
                }
            }, selectColumns);
        } else {
            return getResultListWithoutPagination(null, null, selectColumns);
        }
    }

    @SuppressWarnings("serial")
    public List<T> getSimpleResultList(String filterColumn, Object filterValue, String sortField, String sortOrder) {
        SortOrder sortOrderType = sortOrder == null ? null : (sortOrder.matches("(?i)asc") ? SortOrder.ASCENDING : SortOrder.DESCENDING);
        if (filterColumn != null && !"".equals(filterColumn) && filterValue != null && !"".equals(filterValue)) {
            return getResultList(0, 0, sortField, sortOrderType, new HashMap<String, Object>() {
                {
                    this.put(filterColumn, filterValue);
                }
            });
        } else {
            return getResultList(0, 0, sortField, sortOrderType, null);
        }
    }

    public List<T> getResultListWithoutPagination(Map<String, Object> filters, Map<String, String> filterType, String... selectColumns) {
        return getResultList(0, 0, null, null, filters, filterType, selectColumns);
    }

    public List<T> getResultList(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        return getResultList(first, pageSize, sortField, sortOrder, filters, null);
    }

    @SuppressWarnings("unchecked")
    public List<T> getResultList(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters, Map<String, String> filterType, String... selectColumns) {
        TypedQuery<T> tq = (TypedQuery<T>) getResultListGeneric(first, pageSize, sortField, sortOrder, filters, filterType, false, selectColumns);
        if (tq != null) {
            return tq.getResultList();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Long getResultAggregate(Map<String, Object> filters, Map<String, String> filterType, String... selectColumns) {
        TypedQuery<Long> tq = (TypedQuery<Long>) getResultListGeneric(0, 0, null, null, filters, filterType, true, selectColumns);
        if (tq != null) {
            return tq.getSingleResult();
        }
        return 0L;
    }

    public TypedQuery<?> getResultListGeneric(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters, Map<String, String> filterType, Boolean isAggregate, String... selectColumns) {
        try {
            CriteriaBuilder cb = em.em.getCriteriaBuilder();
            CriteriaQuery<T> query = null;
            CriteriaQuery<Long> queryAgg = null;
            Root<T> from;
            From<?, ?> f;
            List<Order> orderList = new ArrayList<Order>();
            if (isAggregate) {
                queryAgg = cb.createQuery(Long.class);
                from = queryAgg.from(type);
            } else {
                query = cb.createQuery(type);
                from = query.from(type);
            }

            //select
            if (selectColumns != null) {
                List<Selection<?>> select = new ArrayList<>();
                for (String column : selectColumns) {
                    if (!"".equals(column)) {
                        if (isAggregate) {
                            if ("count".equals(column)) {
                                select.add(cb.count(from));
                            } else if (column.matches("(?i)sum\\(.+\\)")) //sum
                            {
                                select.add(cb.sum(from.get(column.replaceAll("sum\\(|\\)", ""))));
                            } else if (column.matches("(?i)count\\(.+\\)")) //count
                            {
                                select.add(cb.count(from.get(column.replaceAll("count\\(|\\)", ""))));
                            } else if (column.matches("(?i)max\\(.+\\)")) //max
                            {
                                select.add(cb.max(from.get(column.replaceAll("max\\(|\\)", ""))));
                            }
                        } else {
                            select.add(from.get(column));
                        }
                    }
                }
                if (!select.isEmpty()) {
                    if (isAggregate) {
                        queryAgg.multiselect(select);
                    } else {
                        query.multiselect(select);
                    }
                } else {
                    query.select(from);
                }
            } else {
                query.select(from);
            }

            //filter
            if (filters != null) {
                Predicate filterCondition = cb.conjunction();
                String key;
                for (Entry<String, Object> filter : filters.entrySet()) {
                    if (!filter.getKey().startsWith("#") && (filter.getValue() == null || (filter.getValue() != null && !"".equals(filter.getValue())))) {

                        //join
                        int dot = filter.getKey().indexOf('.');
                        if (dot > 0) { //com join?
                            key = (filter.getKey().substring(dot + 1));
                            if (type.getSimpleName().toLowerCase().equals(filter.getKey().substring(0, dot).toLowerCase())) {
                                f = from;
                            } else {
                                f = (From<?, ?>) from.join(filter.getKey().substring(0, dot), JoinType.LEFT);
                            }
                        } else {
                            key = filter.getKey();
                            f = from;
                        }
//		            	@SuppressWarnings("unused")
//						Set<Join<T, ?>> set = from.getJoins();
//		            	f.join("areaBalanco");
//		            	Set<?> set2 = f.getJoins();

                        if (filter.getValue() == null) //caso null
                        {
                            filterCondition = cb.and(filterCondition, cb.isNull(f.get(key)));
                        } //filtro manualmente definido
                        else if (filterType != null && !filterType.isEmpty() && filterType.containsKey(key)) {
                            //filtro em múltiplas colunas
                            if (filterType.get(key).contains(",")) {
                                Predicate ou = cb.disjunction();
                                for (String type : filterType.get(key).split(",")) {
                                    ou = cb.or(ou, cb.like(f.get(type), "%" + filter.getValue() + "%"));
                                }
                                filterCondition = cb.and(filterCondition, ou);
                            } else {
                                LocalDate dia = null;
                                if (filterType.get(key).contains("hora")) {
                                    if (filters.containsKey("io.diaMercado")) {
                                        dia = (LocalDate) filters.get("io.diaMercado");
                                    } else if (filters.containsKey("diaMercado")) {
                                        dia = (LocalDate) filters.get("diaMercado");
                                    } else if (filters.containsKey("#auxiliarVariable")) //o prefixo "#" indica uma variável auxiliar e não uma coluna
                                    {
                                        dia = LocalDate.parse((String) filters.get("#auxiliarVariable"));
                                    } else {
                                        dia = LocalDate.now();
                                    }
                                }
                                switch (filterType.get(key)) {
                                    case "=":
                                        filterCondition = cb.and(filterCondition, (filter.getValue() == null ? cb.isNull(f.get(key)) : cb.equal(f.get(key), filter.getValue())));
                                        break;
                                    case "in":
                                        String[] splitToIn = filter.getValue().toString().split(",");
                                        In<String> inClause = cb.in(f.get(key));
                                        for (int i = 0; i < splitToIn.length; i++) {
                                            filterCondition = inClause.value(splitToIn[i]);
                                        }
                                        break;

                                    case "=dia":
                                        filterCondition = cb.and(filterCondition, cb.greaterThanOrEqualTo(f.get(key), ZonedDateTime.of(LocalDateTime.of((LocalDate) filter.getValue(), LocalTime.MIDNIGHT), ZoneId.of("Europe/Lisbon"))));
                                        filterCondition = cb.and(filterCondition, cb.lessThan(f.get(key), ZonedDateTime.of(LocalDateTime.of((LocalDate) filter.getValue(), LocalTime.MIDNIGHT).plusDays(1L), ZoneId.of("Europe/Lisbon"))));
                                        break;
                                    case ">=dia":
                                        filterCondition = cb.and(filterCondition, cb.greaterThanOrEqualTo(f.get(key), ZonedDateTime.of(LocalDateTime.of((LocalDate) filter.getValue(), LocalTime.MIDNIGHT), ZoneId.of("Europe/Lisbon"))));
                                        break;
                                    case "<=dia":
                                        filterCondition = cb.and(filterCondition, cb.lessThan(f.get(key), ZonedDateTime.of(LocalDateTime.of((LocalDate) filter.getValue(), LocalTime.MIDNIGHT).plusDays(1L), ZoneId.of("Europe/Lisbon"))));
                                        break;
                                    case "=horas":
                                        String[] split = filter.getValue().toString().split(",");
                                        if (split.length != 2 || ("0".equals(split[0]) && "23".equals(split[1]))) {
                                            break;
                                        }
                                        int horaIni = Integer.parseInt(split[0]) - 1;
                                        if (horaIni == -1) {
                                            horaIni = 23;
                                        }
                                        int horaFim = Integer.parseInt(split[1]) - 1;
                                        if (horaFim == -1) {
                                            horaFim = 23;
                                        }
                                        LocalDate diaIni;
                                        if (horaIni == 23) {
                                            diaIni = dia.minusDays(1);
                                        } else {
                                            diaIni = dia;
                                        }
                                        if (horaFim == 23) {
                                            dia = dia.minusDays(1);
                                        }
                                        filterCondition = cb.and(filterCondition, cb.greaterThanOrEqualTo(f.get(key), ZonedDateTime.of(LocalDateTime.of(diaIni, LocalTime.of(horaIni, 0)), ZoneId.of("Europe/Lisbon"))));
                                        filterCondition = cb.and(filterCondition, cb.lessThanOrEqualTo(f.get(key), ZonedDateTime.of(LocalDateTime.of(dia, LocalTime.of(horaFim, 0)), ZoneId.of("Europe/Lisbon"))));
                                        break;

//													if(!"0".equals(split[0]))
//														filterCondition = cb.and(filterCondition, cb.greaterThanOrEqualTo(cb.function("to_char", LocalTime.class, f.get(key), cb.literal("hh24:mi:ss")), LocalTime.of(horaIni, 0)));
//													if(!"23".equals(split[1]))
//														filterCondition = cb.and(filterCondition, cb.lessThanOrEqualTo(cb.function("to_char", LocalTime.class, f.get(key), cb.literal("hh24:mi:ss")), LocalTime.of(horaFim, 0))); break;
                                    case ">=horaInteger":
                                        LocalDate diaInicial = dia;
                                        LocalDate diaFinal = dia;
                                        int horaInicial = 0;
                                        int horaFinal = 24;
                                        if ((filter.getKey().equals("inicioTimestamp") || filter.getKey().equals("fimTimestamp")) && filters.containsKey("inicioTimestamp") && filters.containsKey("fimTimestamp") && !filter.getValue().toString().contains(",")) {
                                            horaInicial = Integer.parseInt(filters.get("inicioTimestamp").toString()) - 1 == -1 ? 23 : Integer.parseInt(filters.get("inicioTimestamp").toString()) - 1;
                                            horaFinal = Integer.parseInt(filters.get("fimTimestamp").toString()) - 1 == -1 ? 23 : Integer.parseInt(filters.get("fimTimestamp").toString()) - 1;
                                            diaInicial = horaInicial == 23 ? diaInicial.minusDays(1) : diaInicial;
                                            diaFinal = horaFinal == 23 ? diaFinal.minusDays(1) : diaFinal;

                                        } else if ((filter.getKey().equals("inicioTimestamp") || filter.getKey().equals("fimTimestamp")) && filters.containsKey("inicioTimestamp") && !filters.containsKey("fimTimestamp") && !filter.getValue().toString().contains(",")) {
                                            horaInicial = Integer.parseInt(filters.get("inicioTimestamp").toString()) - 1 == -1 ? 23 : Integer.parseInt(filters.get("inicioTimestamp").toString()) - 1;
                                            horaFinal = horaFinal - 1;
                                            diaInicial = horaInicial == 23 ? diaInicial.minusDays(1) : diaInicial;

                                        } else if ((filter.getKey().equals("inicioTimestamp") || filter.getKey().equals("fimTimestamp")) && !filters.containsKey("inicioTimestamp") && filters.containsKey("fimTimestamp") && !filter.getValue().toString().contains(",")) {
                                            horaInicial = 23;
                                            horaFinal = Integer.parseInt(filters.get("fimTimestamp").toString()) - 1 <= 0 ? 23 : Integer.parseInt(filters.get("fimTimestamp").toString()) - 1;
                                            diaInicial = horaInicial == 23 ? diaInicial.minusDays(1) : diaInicial;
                                            diaFinal = horaFinal == 23 ? diaFinal.minusDays(1) : diaFinal;

                                        } else {
                                            horaFinal = horaFinal - 1;
                                            diaInicial.minusDays(1);
                                        }
                                        filterCondition = cb.and(filterCondition, cb.greaterThanOrEqualTo(f.get(key), ZonedDateTime.of(LocalDateTime.of(diaInicial, LocalTime.of(horaInicial, 0)), ZoneId.of("Europe/Lisbon"))));
                                        filterCondition = cb.and(filterCondition, cb.lessThanOrEqualTo(f.get(key), ZonedDateTime.of(LocalDateTime.of(dia, LocalTime.of(horaFinal, 0)), ZoneId.of("Europe/Lisbon"))));
                                        break;
                                    case ">=hora":
                                        filterCondition = cb.and(filterCondition, cb.greaterThanOrEqualTo(f.get(key), (ZonedDateTime) filter.getValue()));
                                        break;
                                    case "<=hora":
                                        filterCondition = cb.and(filterCondition, cb.lessThanOrEqualTo(f.get(key), (ZonedDateTime) filter.getValue()));
                                        break;
                                    case "!=":
                                    case "<>":
                                        filterCondition = cb.and(filterCondition, (filter.getValue() == null ? cb.isNotNull(f.get(key)) : cb.notEqual(f.get(key), filter.getValue())));
                                        break;
                                    case ">":
                                        filterCondition = cb.and(filterCondition, cb.greaterThan(f.get(key), "" + filter.getValue().toString()));
                                        break;
                                    case ">=":
                                        if ("LocalDate".equals(filter.getValue().getClass().getSimpleName())) {
                                            filterCondition = cb.and(filterCondition, cb.greaterThanOrEqualTo(f.get(key), (LocalDate) filter.getValue()));
                                        } else {
                                            filterCondition = cb.and(filterCondition, cb.greaterThanOrEqualTo(f.get(key), "" + filter.getValue()));
                                        }
                                        break;
                                    case "<":
                                        filterCondition = cb.and(filterCondition, cb.lessThan(f.get(key), "" + filter.getValue()));
                                        break;
                                    case "<=":
                                        if ("LocalDate".equals(filter.getValue().getClass().getSimpleName())) {
                                            filterCondition = cb.and(filterCondition, cb.lessThanOrEqualTo(f.get(key), (LocalDate) filter.getValue()));
                                        } else {
                                            filterCondition = cb.and(filterCondition, cb.lessThanOrEqualTo(f.get(key), "" + filter.getValue()));
                                        }
                                        break;
                                    case "=OuNull":
                                        filterCondition = cb.and(filterCondition, cb.or(cb.equal(f.get(key), filter.getValue()), cb.isNull(f.get(key))));
                                        break;
                                }
                            }
                        }
                        
                        //sort
                        if (!isAggregate && sortField != null && !"".equals(sortField)
                                && dot > 0 && sortField.length() > dot && '.' == (sortField.charAt(dot)) && sortField.substring(0, dot).matches("(?i)" + f.getJavaType().getSimpleName())) { //com o join correcto?

                            key = (sortField.substring(dot + 1));
                            if (sortOrder == null || sortOrder.equals(SortOrder.ASCENDING)) {
                                orderList.add(cb.asc(f.get(key)));
                            } else if (sortOrder.equals(SortOrder.DESCENDING)) {
                                orderList.add(cb.desc(f.get(key)));
                            }

                            sortField = null;
                        }
                        //fixed sort
                        if (!isAggregate && fixedSortField != null && !"".equals(fixedSortField)
                                && dot > 0 && fixedSortField.length() > dot && '.' == (fixedSortField.charAt(dot)) && fixedSortField.substring(0, dot).matches("(?i)" + f.getJavaType().getSimpleName())) { //com o join correcto?

                            key = (fixedSortField.substring(dot + 1));
                            if (fixedSortOrder == null || fixedSortOrder.equals(SortOrder.ASCENDING)) {
                                orderList.add(cb.asc(f.get(key)));
                            } else if (fixedSortOrder.equals(SortOrder.DESCENDING)) {
                                orderList.add(cb.desc(f.get(key)));
                            }

                            fixedSortField = null;
                        }
                    }
                }
                if (isAggregate) {
                    queryAgg.where(filterCondition);
                } else {
                    query.where(filterCondition);
                }
            }

            //sort (se o campo a ordenar for da entidade base ou não corresponder a nenhum filtro com join)
            if (!isAggregate && sortField != null && !"".equals(sortField)) {
                String key;
                int dot = sortField.indexOf('.');
                if (dot > 0) { //com join?
                    key = (sortField.substring(dot + 1));
                    if (type.getSimpleName().toLowerCase().equals(sortField.substring(0, dot).toLowerCase())) {
                        f = from;
                    } else {
                        f = (From<?, ?>) from.join(sortField.substring(0, dot), JoinType.LEFT);
                    }
                } else {
                    key = sortField;
                    f = from;
                }
                if (sortOrder == null || sortOrder.equals(SortOrder.ASCENDING)) {
                    orderList.add(cb.asc(f.get(key)));
                } else if (sortOrder.equals(SortOrder.DESCENDING)) {
                    orderList.add(cb.desc(f.get(key)));
                }
            }
            //sort fixo (se o campo a ordenar for da entidade base ou não corresponder a nenhum filtro com join)
            if (!isAggregate && fixedSortField != null && !"".equals(fixedSortField)) {
                String key;
                int dot = fixedSortField.indexOf('.');
                if (dot > 0) { //com join?
                    key = (fixedSortField.substring(dot + 1));
                    if (type.getSimpleName().toLowerCase().equals(fixedSortField.substring(0, dot).toLowerCase())) {
                        f = from;
                    } else {
                        f = (From<?, ?>) from.join(fixedSortField.substring(0, dot), JoinType.LEFT);
                    }
                } else {
                    key = fixedSortField;
                    f = from;
                }
                if (fixedSortOrder == null || fixedSortOrder.equals(SortOrder.ASCENDING)) {
                    orderList.add(cb.asc(f.get(key)));
                } else if (fixedSortOrder.equals(SortOrder.DESCENDING)) {
                    orderList.add(cb.desc(f.get(key)));
                }
            }
            //aplicar ordenação
            if (orderList != null && !orderList.isEmpty()) {
                query.orderBy(orderList);
            }

            //create query
            if (isAggregate) {
                return em.em.createQuery(queryAgg);
            } else {
                TypedQuery<T> tq = em.em.createQuery(query);
                if (pageSize > 0) {
                    tq.setMaxResults(pageSize);
                }
                if (first >= 0) {
                    tq.setFirstResult(first);
                }

                return tq;
            }

        } catch (Exception e) {
        }
        return null;
    }

    public int count(Map<String, Object> filters, Map<String, String> filterType) {
        return getResultAggregate(filters, filterType, "count").intValue();
    }

    public int max(Map<String, Object> filters, Map<String, String> filterType, String column) {
        return getResultAggregate(filters, filterType, "max(" + column + ")").intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getNativeResultList(String queryStr, Object... parameters) {
        try {
            Query query = em.em.createNativeQuery(queryStr);
            if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
            {
                for (int i = 0; i < parameters.length; i = i + 2)
					try {
                    query.setParameter((String) parameters[i], parameters[i + 1]);
                } catch (Exception e) {
                }
            }
            return query.getResultList();
        } catch (Exception e) {
            LOG.error("Erro  GetNatve: " + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<T> getTypedNativeResultList(String queryStr, Object... parameters) {
        try 
        {
            Query query = em.em.createNativeQuery(queryStr, this.type);
            //Query query = em.em.createNativeQuery(queryStr);
             Set<String> availableParameters = query.getParameters().stream()
                    .map(Parameter::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            LOG.info(availableParameters);

            for (String stringAvailable : availableParameters) {
                LOG.info("StringAvailable:" + stringAvailable);
            }
            if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
            {
                LOG.info("Query parameters being set");
                for (int i = 0; i < parameters.length; i = i + 2)
                {
                    try 
                    {
                        LOG.info("Parameter:" + (String) parameters[i]);
                        LOG.info("Parameter:" + parameters[i+1]);

                        String name = (String) parameters[i];
                        Object value = parameters[i + 1];
                        query.setParameter(name, value);
                        
                    } 
                    catch (Exception e) 
                    {
                        LOG.error("Error Adding Parameter:" + e);
                    }
                }
            }
            /*LOG.info("------------------Break Line ------------------");
            for (Parameter<?> object : query.getParameters()) {
                LOG.info("Position:" + object.getClass());
                //LOG.info("ParameterType:" + object.getParameterType().getName());
                LOG.info("Result:" + object.getName());
            }*/
                        
            return query.getResultList();
        } 
        catch (Exception e) 
        {
            LOG.error("Error in query:" + e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> getTypedNativeResultList(String queryStr, Class<T> resultClass, Object... parameters) {
        try 
        {
            Query query = em.em.createNativeQuery(queryStr, resultClass);
            //Query query = em.em.createNativeQuery(queryStr);
            
            if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
            {
                LOG.info("Query parameters being set");
                for (int i = 0; i < parameters.length; i = i + 2)
                {
                    try 
                    {
                        LOG.info("Parameter:" + (String) parameters[i]);
                        LOG.info("Parameter:" + parameters[i+1]);
                        query.setParameter((String) parameters[i], parameters[i+1]);
                    } 
                    catch (Exception e) 
                    {
                        LOG.warn("Error Adding Parameter:" + e);
                    }
                }
            }
            LOG.info("------------------Break Line2 ------------------");
            for (Parameter<?> object : query.getParameters()) {
                LOG.info("Position:" + object.getPosition());
                LOG.info("ParameterType:" + object.getParameterType().getName());
                LOG.info("Result:" + object.getName());
            }
            
            return query.getResultList();
        } 
        catch (Exception e) 
        {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public T getTypedNativeResult(String queryStr, Object... parameters) {
        try {
            Query query = em.em.createNativeQuery(queryStr);
            if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
            {
                for (int i = 0; i < parameters.length; i = i + 2)
					try {
                    query.setParameter((String) parameters[i], parameters[i + 1]);
                } catch (Exception e) {
                    
                }
            }

            return (T) query.getSingleResult();
        } catch (Exception e) {

        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<T> getNativeEntityList(String query) throws Exception {
        return em.em.createNativeQuery(query, this.type).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getFileQueryResultList(String file, Object... parameters) throws Exception {
        String queryStr = Utils.getResource(file);
        if (queryStr == null || queryStr.isEmpty()) {
            return null;
        }

        Query query = em.em.createNativeQuery(queryStr);
        if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
        {
            for (int i = 0; i < parameters.length; i = i + 2) {
                if (parameters[i + 1] != null)
					try {
                    query.setParameter((String) parameters[i], parameters[i + 1]);
                } catch (Exception e) {
                    
                }
            }
        }

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<T> getMappedFileQueryResultList(String file, String mapping, Object... parameters) throws Exception {
        String queryStr = Utils.getResource(file);
        if (queryStr == null || queryStr.isEmpty()) {
            return null;
        }

        Query query = em.em.createNativeQuery(queryStr, mapping);

        if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
        {
            for (int i = 0; i < parameters.length; i = i + 2) {
                if (parameters[i + 1] != null)
					try {
                    query.setParameter((String) parameters[i], parameters[i + 1]);
                } catch (Exception e) {
                    
                }
            }
        }

        return query.getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public List<T> getMappedFileQueryResultList(String file, Class<T> mapping, Object... parameters) throws Exception {
        String queryStr = Utils.getResource(file);
        if (queryStr == null || queryStr.isEmpty()) {
            return null;
        }

        Query query = em.em.createNativeQuery(queryStr, mapping);

        if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
        {
            for (int i = 0; i < parameters.length; i = i + 2) {
                if (parameters[i + 1] != null)
					try {
                    query.setParameter((String) parameters[i], parameters[i + 1]);
                } catch (Exception e) {
                    
                }
            }
        }

        return query.getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public List<T> getMappedQueryResultList(String queryStr, Class<T> mapping, Object... parameters) throws Exception {
        if (queryStr == null || queryStr.isEmpty()) {
            return null;
        }

        Query query = em.em.createNativeQuery(queryStr, mapping);

        if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
        {
            for (int i = 0; i < parameters.length; i = i + 2) {
                if (parameters[i + 1] != null)
					try {
                    query.setParameter((String) parameters[i], parameters[i + 1]);
                } catch (Exception e) {
                    
                }
            }
        }

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<T> getTypedFileQueryResultList(String file, Object... parameters) throws Exception {
        String queryStr = Utils.getResource(file);
        if (queryStr == null || queryStr.isEmpty()) {
            return null;
        }

        Query query = em.em.createNativeQuery(queryStr, this.type);

        if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
        {
            for (int i = 0; i < parameters.length; i = i + 2) {
                if (parameters[i + 1] != null)
					try {
                    query.setParameter((String) parameters[i], parameters[i + 1]);
                } catch (Exception e) {
                    
                }
            }
        }

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<T> getNativeResultListWithMapping(String queryStr, String mapping, Object... parameters) throws Exception {
        Query query = em.em.createNativeQuery(queryStr, mapping);
        if (parameters != null && (parameters.length & 1) == 0) // número par de parâmetros
        {
            for (int i = 0; i < parameters.length; i = i + 2) {
                if (parameters[i + 1] != null)
					try {
                    query.setParameter((String) parameters[i], parameters[i + 1]);
                } catch (Exception e) {
                    
                }
            }
        }

        return query.getResultList();
    }

    public int executeFileQuery(String file, String... parameters) throws Exception {
        String queryStr = Utils.getResource(file);
        if (queryStr == null || queryStr.isEmpty()) {
            return 0;
        }

        return Connection.createNativeQuery(em.em, queryStr, parameters);
    }

    public int executeNativeQuery(String queryStr, String... parameters) throws Exception {
        if (queryStr == null || queryStr.isEmpty()) {
            return 0;
        }

        return Connection.createNativeQuery(em.em, queryStr, parameters);
    }
    
    public Object executeNativeQueryWithoutCommit(String queryStr, String... parameters) throws Exception {
        if (queryStr == null || queryStr.isEmpty()) {
            return 0;
        }
        
        return Connection.createNativeQuery(em.em, false, queryStr, parameters);
    }

    public ConnectionManager getEm() {
        if (em == null) {
            em = new ConnectionManager();
        }

        return em;
    }

    public void setEm(ConnectionManager em) {
        this.em = em;
    }

    public String getFixedSortField() {
        return fixedSortField;
    }

    public void setFixedSortField(String fixedSortField) {
        this.fixedSortField = fixedSortField;
    }

    public SortOrder getFixedSortOrder() {
        return fixedSortOrder;
    }

    public void setFixedSortOrder(SortOrder fixedSortOrder) {
        this.fixedSortOrder = fixedSortOrder;
    }

    public void close() {
        getEm().close();
    }

    public void rollback() {
        getEm().tx.rollback();
    }
}