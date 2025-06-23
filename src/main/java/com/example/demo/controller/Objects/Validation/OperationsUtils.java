package com.example.demo.controller.Objects.Validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.demo.Data.Connection;
import com.example.demo.Data.Access.*;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.DPMOrigin.DataType;
import com.example.demo.controller.Objects.Import.*;

import java.util.AbstractMap;
import org.jboss.logging.Logger;


public class OperationsUtils {
    
    private final static Logger LOG = Logger.getLogger(ValidationOperators.class.getName());
    private final static OperationExpressionBuilder expressionBuilder = new OperationExpressionBuilder();
    private final static OperationDomainBuilder domainBuilder = new OperationDomainBuilder();
    
    protected static Map<Integer, List<ValResult>> getResultsByNodeForTimeShift(int operationVID, LocalDate refDate, String entityID, int nodeId, String domain) {
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        List<Object[]> results = new ArrayList<>();
        try {
            results = jpa.getMappedFileQueryResultList("SQL_Queries/GetValuesForTimeShiftUpdate.sql", "ValuesForOperationMapping",
                    "operationVId", String.valueOf(operationVID),
                    "entityId", entityID,
                    "refdate", refDate.format(Constants.DATEFORMATISO8601),
                    "format",Constants.ISOBASEFORMAT8601,
                    "nodeId", String.valueOf(nodeId),
                    "domain", domain,
                    "actionId", String.valueOf(Constants.actionImport),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "stateOk", String.valueOf(Constants.processoOk),
                    "desagregationTypeFixed", String.valueOf(Constants.DESAGREGATIONCODEFIXEDTYPE),
                    "directionZ", String.valueOf(Constants.SheetCoordinate),
                    "stateOk", String.valueOf(Constants.processoOk),
                    "desagregationCodeType", String.valueOf(Constants.DESAGREGATIONCODETYPE)
            );
        } catch (Exception e) {
            LOG.error("Erro na query getResultsByNodeForTimeShift:" + e.getMessage());
        } finally {
            Connection.close(jpa.getEm().em);
        }

        return OperationsUtils.mapResultsFromDatabase(results, refDate.format(Constants.DATEFORMATISO8601));
    }
    
    /**
     * Mapeamento dos valores dos nós folha por ID do nó.
     *
     * @param results Resultados da Query "XBRLArvore.sql"
     * @param refDate Data de referência dos valores obtidos.
     * @return Lista de valores mapeados por nós
     */
    protected static Map<Integer, List<ValResult>> mapResultsFromDatabase(List<Object[]> results, String refDate) {
        Map<Integer, List<ValResult>> valuesOfNodes = new HashMap<>();
        try {
            valuesOfNodes = results.stream().collect(Collectors.groupingBy(
                    //Result[3] -> ID do Nó
                    result -> Integer.valueOf(result[3].toString()),
                    Collectors.mapping(result -> {
                        ValResult newResult = new ValResult();
                        //Result[8] -> Tipo do registo (Se é um valor, uma property ou um Item)
                        switch (result[8].toString()) {
                            case Constants.VALUE: {
                                //Result[0] -> DataType do Valor
                                //Result[4] -> Valor bruto importado
                                ValValue value = new ValValue((DataType) result[0], (result[4] != null) ? result[4].toString() : null);

                                ValKey key = new ValKey();
                                ValMLKey mLKey = new ValMLKey();
                                //Result[5] -> Valor do Indíce X na operacao
                                if (result[5] != null) {
                                    mLKey.setxIndex(Integer.valueOf(result[5].toString()));
                                }
                                //Result[6] -> Valor do Indíce Y na operacao
                                if (result[6] != null) {
                                    mLKey.setyIndex(Integer.valueOf(result[6].toString()));
                                }
                                //Result[7] -> Valor do Indíce Z na operacao
                                if (result[7] != null) {
                                    mLKey.setzIndex(Integer.valueOf(result[7].toString()));
                                }
                                key.setIndexs(mLKey);

                                Map<String, String> dpmKeys = new HashMap<>();

                                //"-1" é usado como Dummy para indicar que o valor não tem chave.
                                //Result[1] -> RowKey
                                if (((InImportKey) result[1]).getImportKeyID() != -1) {
                                    InImportKey rowKey = new InImportKey();
                                    rowKey = (InImportKey) result[1];

                                    for (InKeyAssociation keyAssociation : rowKey.getListPropertyValues()) {
                                        dpmKeys.put(keyAssociation.getPropertyName(), keyAssociation.getPropertyValue());
                                    }
                                }

                                //"-1" é usado como Dummy para indicar que o valor não tem chave.
                                //Result[2] -> DesagregationCode
                                if (((InImportKey) result[2]).getImportKeyID() != -1) {
                                    InImportKey desagregationCode = new InImportKey();
                                    desagregationCode = (InImportKey) result[2];

                                    for (InKeyAssociation keyAssociation : desagregationCode.getListPropertyValues()) {
                                        dpmKeys.put(keyAssociation.getPropertyName(), keyAssociation.getPropertyValue());
                                    }
                                }
                                key.setDpmKeys(dpmKeys);
                                
                                String domain = null;
                                if(result[11] != null){
                                    domain = result[11].toString();
                                }
                                
                                return new ValResult(key, value, refDate, domain);
                            }
                            case Constants.PROPERTY: 
                            case Constants.ITEM:
                            case Constants.REFPERIOD: {
                                //Result[0] -> DataType do Valor
                                //Result[4] -> Valor do Item ou Propriedade
                                ValValue value = new ValValue((DataType) result[0], result[4].toString());
                                newResult.setResult(value);

                                return newResult;
                            }
                            default:
                                break;
                        }
                        return newResult;
                    },
                            Collectors.toList())
            ));
        } catch (Exception e) {
            LOG.error("Erro no mapeamento dos dados da query getResultsByNodeForTimeShift:" + e.getMessage());
        }

        return valuesOfNodes;
    }

    protected static Map<ValKey, List<ValResult>> groupValues(List<ValResult> values, ValKey groupingKey) {
        Map<String, String> propertiesToGroup = new HashMap<>();
        Map<ValKey, List<ValResult>> groupedItems = new HashMap<>();
        Map<ValKey, List<ValResult>> groupedItemsFiltered = new HashMap<>();

        try {
            if (groupingKey != null) {
                final ValKey keyToGroup = groupingKey;
                propertiesToGroup = keyToGroup.getDpmKeys();
                final Set<String> setKeys = propertiesToGroup.keySet();
                if (values != null && !values.isEmpty()) {
                    groupedItems = values.stream().collect(Collectors.groupingBy(
                            valResult -> {
                                ValKey groupingKeyResult = new ValKey();

                                Map<String, String> dpmKeys = new HashMap<>();
                                for (String key : setKeys) {
                                    if (!key.equals(Constants.SHEETCODE) && valResult.getKey().getDpmKeys().get(key) != null) {
                                        dpmKeys.put(key, valResult.getKey().getDpmKeys().get(key));
                                    }
                                }
                                groupingKeyResult.setDpmKeys(dpmKeys);

                                if (keyToGroup.getIndexs() != null) {
                                    ValMLKey valMLKey = new ValMLKey();
                                    if (keyToGroup.getIndexs().getxIndex() != null) {
                                        valMLKey.setxIndex(valResult.getKey().getIndexs().getxIndex());
                                    }
                                    if (keyToGroup.getIndexs().getyIndex() != null) {
                                        valMLKey.setyIndex(valResult.getKey().getIndexs().getyIndex());
                                    }
                                    if (keyToGroup.getIndexs().getzIndex() != null) {
                                        valMLKey.setzIndex(valResult.getKey().getIndexs().getzIndex());
                                    }
                                    groupingKeyResult.setIndexs(valMLKey);
                                }

                                return groupingKeyResult;
                            }));
                    groupedItemsFiltered = groupedItems.entrySet().stream()
                        .filter(entry -> !entry.getKey().hasKeysPropertiesIndexsNull())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return groupedItemsFiltered;
    }
    
    protected static Map<ValKey, List<Map.Entry<ValNode, ValResult>>> groupValuesOfNodes(List<ValNode> nodes, ValKey groupingKey) {
        Map<String, String> propertiesToGroup = new HashMap<>();
        Map<ValKey, List<Map.Entry<ValNode, ValResult>>> groupedIems = new HashMap<>();
        List<Map.Entry<ValNode, ValResult>> allResults = new ArrayList<>();

        try {
            if (groupingKey != null) {
                final ValKey keyToGroup = groupingKey;
                propertiesToGroup = keyToGroup.getDpmKeys();
                final Set<String> setKeys = propertiesToGroup.keySet();

                nodes.stream().forEach(node -> {
                    if(node.getResults() != null && !node.getResults().isEmpty()){
                        for(ValResult result : node.getResults()){
                            allResults.add(new AbstractMap.SimpleEntry<>(node, result));
                        }
                    }
                });
                
                if (!allResults.isEmpty()) {
                    groupedIems = allResults.stream().collect(Collectors.groupingBy(
                        pair -> {
                            ValKey groupingKeyResult = new ValKey();
                            ValResult valResult = pair.getValue();

                            Map<String, String> dpmKeys = new HashMap<>();
                            for (String key : setKeys) {
                                if(key.equals(Constants.SHEETCODE)){
                                    continue;
                                }
                                dpmKeys.put(key, valResult.getKey().getDpmKeys().get(key));
                            }
                            groupingKeyResult.setDpmKeys(dpmKeys);

                            if (keyToGroup.getIndexs() != null) {
                                ValMLKey valMLKey = new ValMLKey();
                                if (keyToGroup.getIndexs().getxIndex() != null) {
                                    valMLKey.setxIndex(valResult.getKey().getIndexs().getxIndex());
                                }
                                if (keyToGroup.getIndexs().getyIndex() != null) {
                                    valMLKey.setyIndex(valResult.getKey().getIndexs().getyIndex());
                                }
                                if (keyToGroup.getIndexs().getzIndex() != null) {
                                    valMLKey.setzIndex(valResult.getKey().getIndexs().getzIndex());
                                }
                                groupingKeyResult.setIndexs(valMLKey);
                            }
                            
                            return groupingKeyResult;
                        }));
                }
            } else {
                nodes.stream().forEach(node -> {
                    if(node.getResults() == null && node.getNode().getScalar() != null){
                        allResults.add(new AbstractMap.SimpleEntry<>(node, new ValResult(new ValValue(OperationsUtils.getDataTypeByID(Constants.DECIMAL), node.getNode().getScalar()))));
                    }
                    if(node.getResults() != null && !node.getResults().isEmpty()){
                        for(ValResult result : node.getResults()){
                            allResults.add(new AbstractMap.SimpleEntry<>(node, result));
                        }
                    }
                });
                
                groupedIems.put(groupingKey, allResults);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return groupedIems;
    }

    protected static boolean isChildOfWhere(ValNode node) {
        return (node.getParentOperator() != null) ? node.getParentOperator().getOperatorID() == Constants.WHERE : false;
    }

    //Verificacao se existem chaves num dos valores
    protected static Boolean isToUseKeys(ValNode leftChild, ValNode rightChild) {
        List<ValResult> leftValues = new ArrayList<>();
        List<ValResult> rightValues = new ArrayList<>();
        boolean leftHasKeys = false;
        boolean rightHasKeys = false;

        try {
            leftValues = leftChild.getResults();
            rightValues = rightChild.getResults();

            //Caso os valores não sejam nulos, ele verifica se algum valor tem alguma chave
            if (leftValues != null && !leftValues.isEmpty()) {
                for (ValResult value : leftValues) {
                    if (value.getKey() != null && !value.getKey().hasKeysPropertiesIndexsNull() && !value.getKey().hasOnlyKeyOfSheetCode()) {
                        leftHasKeys = true;
                        break;
                    }
                }
            }

            if (rightValues != null && !rightValues.isEmpty()) {
                for (ValResult value : rightValues) {
                    if (value.getKey() != null && !value.getKey().hasKeysPropertiesIndexsNull() && !value.getKey().hasOnlyKeyOfSheetCode()) {
                        rightHasKeys = true;
                        break;
                    }
                }
            }
            
            if(leftHasKeys && rightHasKeys){
                return true;
            } else if (!leftHasKeys && !rightHasKeys){
                return false;
            } else {
                if(leftValues != null && leftValues.size() == 1 && rightValues != null && rightValues.size() == 1){
                    return false;
                } else if (leftValues != null && rightValues != null && leftValues.size() != rightValues.size()) {
                    return null;
                }
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    protected static Boolean isToUseKeys(List<ValNode> childs){
        List<ValResult> childValues = new ArrayList<>();

        try {
            if (childs != null && !childs.isEmpty()) {
                for (ValNode child : childs) {
                    childValues = child.getResults();

                    //Caso os valores não sejam nulos, ele verifica se algum valor tem alguma chave
                    if (childValues != null && !childValues.isEmpty()) {
                        for (ValResult value : childValues) {
                            if (value.getKey() != null && !value.getKey().hasKeysPropertiesIndexsNull() && !value.getKey().hasOnlyKeyOfSheetCode()) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
 
    protected static Boolean isToUseKeys(ValNode conditionChild) {
        List<ValResult> conditionValues = new ArrayList<>();

        try {
            if(conditionChild != null){
                conditionValues = conditionChild.getResults();

                //Caso os valores não sejam nulos, ele verifica se algum valor tem alguma chave
                if (conditionValues != null && !conditionValues.isEmpty()) {
                    for (ValResult value : conditionValues) {
                        if (value.getKey() != null && !value.getKey().hasKeysPropertiesIndexsNull() && !value.getKey().hasOnlyKeyOfSheetCode()) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    protected static void setOnlyOneResultToParent(ValNode parent, ValKey key, ValValue value) {
        ValResult parentResult = new ValResult(key, value);
        parent.setOnlyOneResult(parentResult);
    }

    //Consoante o ID, vai buscar o DataType ao RefData
    public static DataType getDataTypeByID(Integer dataTypeID) {
        return Info.getInstance().getDataTypeByID(dataTypeID);
    }
    
    protected static ValKey determineGroupingKey(List<ValResult> results){
        ValKey groupingKey = new ValKey();
        ValMLKey indexs = new ValMLKey();
        Map<String, String> properties = new HashMap<>();
        
        try {
            if(results != null && !results.isEmpty()){
                for(ValResult result : results){
                    if(result.getKey() != null){
                        
                        ValMLKey resultIndexs = result.getKey().getIndexs();
                        
                        if(resultIndexs != null && !resultIndexs.isNull()){
                            if (resultIndexs.getxIndex() != null && indexs.getxIndex() == null) {
                                indexs.setxIndex(0);
                            }
                            if (resultIndexs.getyIndex() != null && indexs.getyIndex() == null) {
                                indexs.setyIndex(0);
                            }
                            if (resultIndexs.getzIndex() != null && indexs.getzIndex() == null) {
                                indexs.setzIndex(0);
                            }
                        }
                        
                        Map<String, String> resultProperties = result.getKey().getDpmKeys();
                        if (resultProperties != null && !resultProperties.isEmpty()) {
                            for (Map.Entry<String, String> property : resultProperties.entrySet()) {
                                if(property.getKey().equals(Constants.SHEETCODE)){
                                    continue;
                                }
                                
                                if (!properties.containsKey(property.getKey())) {
                                    properties.put(property.getKey(), null);
                                }
                            }
                        }
                    }
                }

                if (!indexs.isNull()) {
                    groupingKey.setIndexs(indexs);
                }
                if(!properties.isEmpty()) {
                    groupingKey.setDpmKeys(properties);
                }
            }
            
            return groupingKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    //Construcao da "InnerJoin" com base nos valores
    protected static ValKey defineInnerJoinKeys(List<ValResult> leftValues, List<ValResult> rightValues) {
        ValResult leftValue = new ValResult();
        ValResult rightValue = new ValResult();
        ValKey innerJoinKey = new ValKey();

        try {
            leftValue = leftValues.get(0);
            rightValue = rightValues.get(0);

            ValKey leftKey = leftValue.getKey();
            ValKey rightKey = rightValue.getKey();

            //Caso as chaves sejam diferentes de null
            if (leftKey.getIndexs() != null && !leftKey.getIndexs().isNull() && rightKey.getIndexs() != null && !rightKey.getIndexs().isNull()) {
                ValMLKey indexsFinal = new ValMLKey();

                //Caso ambas tenham algum índice, então esse indíce deve pertencer como coluna para "InnerJoin"
                if (leftKey.getIndexs().getxIndex() != null && rightKey.getIndexs().getxIndex() != null) {
                    indexsFinal.setxIndex(0);
                }
                if (leftKey.getIndexs().getyIndex() != null && rightKey.getIndexs().getyIndex() != null) {
                    indexsFinal.setyIndex(0);
                }
                if (leftKey.getIndexs().getzIndex() != null && rightKey.getIndexs().getzIndex() != null) {
                    indexsFinal.setzIndex(0);
                }

                if (!indexsFinal.isNull()) {
                    innerJoinKey.setIndexs(indexsFinal);
                }
            }

            //Caso tenham propriedades como chaves
            if ((leftKey.getDpmKeys() != null && !leftKey.getDpmKeys().isEmpty()) && (rightKey.getDpmKeys() != null && !rightKey.getDpmKeys().isEmpty())) {
                Map<String, String> propertysFinal = new HashMap<>();
                Map<String, String> leftPropertys = leftKey.getDpmKeys();
                Map<String, String> rightPropertys = rightKey.getDpmKeys();

                //Percorre as chaves da esquerda e vai verificando se o da direita também tem a mesma chave.
                for (Map.Entry<String, String> property : leftPropertys.entrySet()) {
                    if(property.getKey().equals(Constants.SHEETCODE)){
                        continue;
                    }
                    if (rightPropertys.containsKey(property.getKey())) {
                        propertysFinal.put(property.getKey(), null);
                    }
                }

                if (!propertysFinal.isEmpty()) {
                    innerJoinKey.setDpmKeys(propertysFinal);
                }
            }
            return innerJoinKey;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //Agrupar os valores pela innerJoinKey
    protected static Map<ValResult, List<ValResult>> groupValuesByInnerJoin(List<ValResult> leftValues, List<ValResult> rightValues, ValKey innerJoinKey) {
        Map<ValResult, List<ValResult>> groupedValues = new HashMap<>();
        ValMLKey joinIndexs = new ValMLKey();
        Map<String, String> joinPropertys = new HashMap<>();
        ValMLKey leftIndexs = new ValMLKey();
        ValMLKey rightIndexs = new ValMLKey();
        Map<String, String> leftPropertys = new HashMap<>();
        Map<String, String> rightPropertys = new HashMap<>();

        try {
            if (innerJoinKey != null) {
                joinIndexs = innerJoinKey.getIndexs();
                joinPropertys = innerJoinKey.getDpmKeys();

                //Caso a innerJoin não seja null
                if (!innerJoinKey.hasKeysPropertiesIndexsNull()) {
                    //Percorre cada valor da esquerda, para verificar se algum da direita partilha a mesma chave
                    for (ValResult leftValue : leftValues) {
                        leftIndexs = leftValue.getKey().getIndexs();
                        leftPropertys = leftValue.getKey().getDpmKeys();

                        for (ValResult rightValue : rightValues) {
                            rightIndexs = rightValue.getKey().getIndexs();
                            rightPropertys = rightValue.getKey().getDpmKeys();

                            boolean isToGroup = true;

                            //Caso os indíces da innerJoinKey sejam diferentes de null, verifica se algum dos indexs entre esquerda e direita são diferentes
                            if (joinIndexs != null && !joinIndexs.isNull()) {
                                if (leftIndexs != null && rightIndexs != null) {

                                    //Caso o indíce da innerJoinKey seja == 0, logo deve agrupar por x
                                    //Caso a esquerda e a direita tenham indíce diferente, então não deve agrupar
                                    if (joinIndexs.getxIndex() != null
                                            && joinIndexs.getxIndex() == 0
                                            && leftIndexs.getxIndex() != null && rightIndexs.getxIndex() != null
                                            && leftIndexs.getxIndex().compareTo(rightIndexs.getxIndex()) != 0) {
                                        isToGroup = false;
                                        continue;
                                    }

                                    if (joinIndexs.getyIndex() != null
                                            && joinIndexs.getyIndex() == 0
                                            && leftIndexs.getyIndex() != null && rightIndexs.getyIndex() != null
                                            && leftIndexs.getyIndex().compareTo(rightIndexs.getyIndex()) != 0) {
                                        isToGroup = false;
                                        continue;
                                    }

                                    if (joinIndexs.getzIndex() != null
                                            && joinIndexs.getzIndex() == 0
                                            && leftIndexs.getzIndex() != null && rightIndexs.getzIndex() != null
                                            && leftIndexs.getzIndex().compareTo(rightIndexs.getzIndex()) != 0) {
                                        isToGroup = false;
                                        continue;
                                    }
                                }

                            }

                            //Caso as propertys da "innerJoinKey" sejam diferentes de null
                            if (joinPropertys != null && !joinPropertys.isEmpty()) {
                                if((leftPropertys == null || leftPropertys.isEmpty()) || (rightPropertys == null || rightPropertys.isEmpty())){
                                    continue;
                                }
                                for (Map.Entry<String, String> property : joinPropertys.entrySet()) {
                                    if(property.getKey().equals(Constants.SHEETCODE)){
                                        continue;
                                    }
                                    
                                    if ((leftPropertys.get(property.getKey()) == null || rightPropertys.get(property.getKey()) == null )|| 
                                            (leftPropertys.get(property.getKey()).compareTo(rightPropertys.get(property.getKey())) != 0)) {
                                        isToGroup = false;
                                        break;
                                    }
                                }
                            }

                            //Caso se mantenha como true, então agrupa os valores
                            if (isToGroup) {
                                groupedValues.computeIfAbsent(leftValue, x -> new ArrayList<>()).add(rightValue);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<ValResult, List<ValResult>>();
        }

        return groupedValues;
    }

    //Construir a chave
    protected static ValKey buildSharedKey(ValResult leftValue, ValResult rightValue) {
        ValKey resultKey = new ValKey();
        ValKey leftKey = new ValKey();
        ValKey rightKey = new ValKey();
        ValMLKey indexsFinalKey = new ValMLKey();
        Map<String, String> propertysFinalKey = new HashMap<>();
        ValMLKey leftIndexs = new ValMLKey();
        ValMLKey rightIndexs = new ValMLKey();
        Map<String, String> leftPropertys = new HashMap<>();
        Map<String, String> rightPropertys = new HashMap<>();

        try {
            leftKey = leftValue.getKey();
            rightKey = rightValue.getKey();

            if (leftKey != null) {
                leftIndexs = leftKey.getIndexs();
                leftPropertys = leftKey.getDpmKeys();
            }

            if (rightKey != null) {
                rightIndexs = rightKey.getIndexs();
                rightPropertys = rightKey.getDpmKeys();
            }

            //Se os indexs da esquerda forem diferentes de null, ele constrói a chave final com esses índices
            if (leftIndexs != null && !leftIndexs.isNull()) {
                if (leftIndexs.getxIndex() != null) {
                    indexsFinalKey.setxIndex(leftIndexs.getxIndex());
                }
                if (leftIndexs.getyIndex() != null) {
                    indexsFinalKey.setyIndex(leftIndexs.getyIndex());
                }
                if (leftIndexs.getzIndex() != null) {
                    indexsFinalKey.setzIndex(leftIndexs.getzIndex());
                }

                if (!indexsFinalKey.isNull()) {
                    resultKey.setIndexs(indexsFinalKey);
                }
            }

            //Se os indexs da direita forem diferentes de null, ele constrói a chave final com esses índices
            if (rightIndexs != null && !rightIndexs.isNull()) {
                if (rightIndexs.getxIndex() != null) {
                    indexsFinalKey.setxIndex(rightIndexs.getxIndex());
                }
                if (rightIndexs.getyIndex() != null) {
                    indexsFinalKey.setyIndex(rightIndexs.getyIndex());
                }
                if (rightIndexs.getzIndex() != null) {
                    indexsFinalKey.setzIndex(rightIndexs.getzIndex());
                }

                if (!indexsFinalKey.isNull()) {
                    resultKey.setIndexs(indexsFinalKey);
                }
            }

            //Se as propertys da esquerda forem diferentes de null, ele constrói a chave final com esses índices
            if (leftPropertys != null && !leftPropertys.isEmpty()) {
                for (Map.Entry<String, String> property : leftPropertys.entrySet()) {
                    if(property.getKey().equals(Constants.SHEETCODE)){
                        continue;
                    }
                    propertysFinalKey.put(property.getKey(), property.getValue());
                }

                if (!propertysFinalKey.isEmpty()) {
                    resultKey.setDpmKeys(propertysFinalKey);
                }
            }

            //Se as propertys da direita forem diferentes de null, ele constrói a chave final com esses índices
            if (rightPropertys != null && !rightPropertys.isEmpty()) {
                for (Map.Entry<String, String> property : rightPropertys.entrySet()) {
                    if(property.getKey().equals(Constants.SHEETCODE)){
                        continue;
                    }
                    propertysFinalKey.put(property.getKey(), property.getValue());
                }

                if (!propertysFinalKey.isEmpty()) {
                    resultKey.setDpmKeys(propertysFinalKey);
                }
            }

            return resultKey;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public static ValResult applyDefaultValue(ValNode node, ValResult nullResult, List<String> domain) {
        String defaultValue = node.getNode().getFallbackValue();
        ValKey key = (nullResult != null && nullResult.getKey() != null) ? nullResult.getKey() : null;
        DataType dataType = (nullResult != null && nullResult.getResult() != null) ? nullResult.getResult().getDatatype(): null;
        BigDecimal margin = (nullResult != null && nullResult.getMargin() != null) ? nullResult.getMargin() : BigDecimal.ZERO;
        String refDate = (nullResult != null && nullResult.getRefDate()!= null) ? nullResult.getRefDate(): null;
        
        ValValue result = new ValValue(dataType, defaultValue);
        return new ValResult(key, result, refDate, margin, domain);
    }

    public static BigDecimal setMarginValue(ValNode node, ValResult result) {
        BigDecimal fixMargin = new BigDecimal(Info.getInstance().getConfigValueByKey(Constants.TOLERANCE));
        boolean isLeaf = (node != null) ? node.getNode().isLeaf() : false;

        //Caso estejam a zero (valor idêntico a null vindo da base de dados), deve ser para utilizar margem de erro parametrizada
        if (node != null && (node.getNode().isUseIntervalArithmetics() || (result.getMargin() != null && result.getMargin() != BigDecimal.ZERO))){
            if(node.getNode().isUseIntervalArithmetics() && isLeaf){
                return (node.getNode().getRelativeTolerance() == 0.0) ? fixMargin : new BigDecimal(node.getNode().getRelativeTolerance());
            } else if (!isLeaf && result.getMargin() != null && result.getMargin() != BigDecimal.ZERO) {
                return result.getMargin();
            }          
        } 
        
        return BigDecimal.ZERO;
    }
    
    public static boolean isToUseMargin(ValNode node, ValResult result){
        if(node != null && node.getNode() != null && result != null)
            return (node.getNode().isLeaf() && node.getNode().isUseIntervalArithmetics()) || (!node.getNode().isLeaf() && result.getMargin()!= null && result.getMargin() != BigDecimal.ZERO);
        return false;
    }

    protected static DataType determineDataType(ValResult leftValue, ValResult rightValue, boolean leftIsScalar, boolean rightIsScalar, ValNode node) {
        DataType valueDataType = new DataType();

        try {
            if (leftIsScalar && rightIsScalar) {
                // Decidir DataType quando ambos são scalars
            } else if (leftIsScalar) {
                valueDataType = rightValue.getResult().getDatatype();
            } else if (rightIsScalar) {
                valueDataType = leftValue.getResult().getDatatype();
            } else if (leftValue.getResult().getDatatype() != null) {
                valueDataType = leftValue.getResult().getDatatype();
            } else if (rightValue.getResult().getDatatype() != null) {
                valueDataType = rightValue.getResult().getDatatype();
            }

            switch (valueDataType.getDataTypeId()) {
                case Constants.INTEGER:
                case Constants.DECIMAL:
                case Constants.MONETARY:
                case Constants.PERCENTAGE:
                    return getDataTypeByID(Constants.DECIMAL);

                case Constants.STRINGNONEMPTY:
                case Constants.ENUMERATION:
                case Constants.URI:
                case Constants.ORDINALS:
                case Constants.STRINGINCLUDINGEMPTY:
                    return getDataTypeByID(Constants.STRINGINCLUDINGEMPTY);

                case Constants.BOOLEAN:
                case Constants.TRUE:
                    return getDataTypeByID(Constants.BOOLEAN);

                case Constants.DATE:
                case Constants.DATETIME:
                    return getDataTypeByID(Constants.DATETIME);

                default:
                    LOG.error("DetermineDataType a dar erro");
                    return null;
            }

        } catch (Exception e) {
            LOG.error("DetermineDataType a dar erro" + e.getMessage());
        }
        return null;
    }

    protected static Object transformValue(ValResult value, DataType dataType, ValNode node) {
        try {
            switch (dataType.getDataTypeId()) {
                case Constants.INTEGER:
                case Constants.DECIMAL:
                case Constants.MONETARY:
                case Constants.PERCENTAGE:
                    return (value.getResult() != null) ? new BigDecimal(value.getResult().getValue()) : new BigDecimal(node.getNode().getFallbackValue());

                case Constants.STRINGNONEMPTY:
                case Constants.ENUMERATION:
                case Constants.URI:
                case Constants.ORDINALS:
                case Constants.STRINGINCLUDINGEMPTY:
                    return (value.getResult() != null) ? value.getResult().getValue() : node.getNode().getFallbackValue();

                case Constants.BOOLEAN:
                case Constants.TRUE:
                    return (value.getResult() != null) ? Boolean.valueOf(value.getResult().getValue()) : Boolean.valueOf(node.getNode().getFallbackValue());

                case Constants.DATETIME:
                case Constants.DATE:
                    return (value.getResult() != null) ? LocalDate.parse(value.getResult().getValue(), Constants.DATEFORMATUSEDBYVALIDATIONS) : LocalDate.parse(node.getNode().getFallbackValue(), Constants.DATEFORMATUSEDBYVALIDATIONS);
                
                    
                default:
                    LOG.error("TransformValue a dar erro");
                    return null;
            }
        } catch (Exception e) {
            LOG.error("TransformValue a dar erro" + e.getMessage());
        }
        return null;
    }

    protected static ValNode getChildNode(List<ValNode> childs, String argument) {
        return childs.stream().filter(node -> node.getNode().getArgument().getName().equalsIgnoreCase(argument)).findFirst().orElse(null);
    }

    protected static boolean applyChildWhereLogic(ValNode parent, ValNode leftNode, ValNode rightNode) {
        List<ValResult> resultsList = new ArrayList<>();
        List<ValResult> leftResults = new ArrayList<>();
        List<ValResult> rightResults = new ArrayList<>();

        try {
            if (leftNode != null) {
                leftResults = leftNode.getResults();
            }
            if (rightNode != null) {
                rightResults = rightNode.getResults();
            }

            if (!leftResults.isEmpty() && !rightResults.isEmpty()) {
                //vai criar as combinacoes de propriedades com os único/vários valores possíveis
                String expression = expressionBuilder.childWhereOperationBuilder(parent, leftNode, rightNode);
                List<String> domain = domainBuilder.childWhereDomainBuilder(leftNode, rightNode);
                for (ValResult left : leftResults) {
                    for (ValResult right : rightResults) {
                        //resultLog = "Filho de Where: " + left.getRawValue() + " " + parent.getNode().getOperator().getSymbol() + " " + right.getRawValue();
                        //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, null, "Operacao");
                        
                        ValKey keyFromCombination = new ValKey();
                        keyFromCombination.addPropertyValue(left.getRawValue(), right.getRawValue());

                        ValResult resultFromCombination = new ValResult(keyFromCombination);
                        resultFromCombination.setExpression(expression);
                        resultFromCombination.setDomain(domain);
                        resultsList.add(resultFromCombination);
                    }
                }

                parent.setResults(resultsList);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
        
    protected static String generateIfThenElseLog(ValResult ifResult, ValResult thenResult, ValResult elseResult, Integer ifNodeId, Integer thenNodeId, Integer elseNodeId){
        String resultIf = (ifResult != null && !ifResult.valueIsNull()) ? ifResult.getRawValue() : "null";
        String resultThen = (thenResult != null && !thenResult.valueIsNull()) ? thenResult.getRawValue() : "null";
        String resultElse = (elseResult != null && !elseResult.valueIsNull()) ? elseResult.getRawValue() : "null";
        return "if(" + resultIf + "-NodeId:"+ifNodeId+") then (" + resultThen + "-NodeId:"+thenNodeId+") else (" + resultElse + "-NodeId:"+elseNodeId+")";
    }

    protected static boolean isToCompareWithItem(ValNode rightChild) {
        try {
            if (rightChild != null && rightChild.getResults() != null && !rightChild.getResults().isEmpty()) {
                ValResult rightResult = rightChild.getResults().get(Constants.FIRSTRESULT);

                if (rightResult != null && rightResult.getResult() != null){
                    if(!rightResult.isItem()) return false;
                } else {
                    LOG.error("Ocorreu um erro no método isToCompareWithItem. O resultado veio a null.");
                    return false;
                }

                return true;
            }
        } catch (Exception e) {
            LOG.error("Ocorreu um erro no método isToCompareWithItem.");
        }
        return false;
    }
    
    protected static boolean resultsIsNotEmpty(ValNode node){
        return node.getResults() != null && !node.getResults().isEmpty();
    }
    
    protected static boolean nodeIsNotNull(ValNode node) {
        return node != null && node.getNode() != null;
    }

}
