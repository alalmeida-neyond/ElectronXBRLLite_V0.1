
package com.example.demo.controller.Objects.Validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.example.demo.Resources.Constants;

/**
 *
 * @author njesus
 */
public class ValKey {
    
    private ValMLKey indexs;
    private Map<String, String> dpmKeys;
    
    public ValKey(){}
    
    public ValKey(ValMLKey indexs, Map<String, String> dpmKeys){
        this.indexs = indexs;
        this.dpmKeys = dpmKeys;
    }
    
    public void addPropertyValue(String property, String value){
        if(this.dpmKeys == null){
            this.dpmKeys = new HashMap<>();
        }
        this.dpmKeys.put(property, value);
    }
    
    public boolean hasKeysPropertiesIndexsNull(){
        return (this.dpmKeys == null || this.dpmKeys.isEmpty()) && (this.indexs == null || this.indexs.isNull());
    }
    
    
    public boolean hasOnlyKeyOfSheetCode() {
        return (this.dpmKeys != null && (this.dpmKeys.isEmpty() || (this.dpmKeys.size() == 1 && this.dpmKeys.containsKey(Constants.SHEETCODE)))) 
                && (this.indexs == null || this.indexs.isNull());
    }

    public ValMLKey getIndexs() {
        return indexs;
    }

    public void setIndexs(ValMLKey indexs) {
        this.indexs = indexs;
    }

    public Map<String, String> getDpmKeys() {
        return dpmKeys;
    }

    public void setDpmKeys(Map<String, String> dpmKeys) {
        this.dpmKeys = dpmKeys;
    }  

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.indexs);
        hash = 97 * hash + Objects.hashCode(this.dpmKeys);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ValKey other = (ValKey) obj;

        if (this.indexs == null) {
            if (other.indexs != null) {
                if (!other.indexs.equals(this.indexs)) {
                    return false;
                }
            }
        } else {
            if (!this.indexs.equals(other.indexs)) {
                return false;
            }
        }

        if (this.dpmKeys != null && !this.dpmKeys.isEmpty() && this.dpmKeys.containsKey(Constants.SHEETCODE)) {
            this.dpmKeys.remove(Constants.SHEETCODE);
        }
        if (other.getDpmKeys() != null && !other.getDpmKeys().isEmpty() && other.getDpmKeys().containsKey(Constants.SHEETCODE)) {
            other.getDpmKeys().remove(Constants.SHEETCODE);
        }

        return ((this.dpmKeys == null || this.dpmKeys.isEmpty()) && (other.dpmKeys == null || other.dpmKeys.isEmpty())) || Objects.equals(this.dpmKeys, other.dpmKeys);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (indexs != null && (indexs.getxIndex() != null || indexs.getyIndex() != null || indexs.getzIndex() != null)) {
            sb.append("Índices -> ");
            String indexX = (indexs.getxIndex() != null) ? "x: " + indexs.getxIndex() + " " : "x: null ";
            String indexY = (indexs.getyIndex() != null) ? "y: " + indexs.getyIndex() + " " : "y: null ";
            String indexZ = (indexs.getzIndex() != null) ? "z: " + indexs.getzIndex() + " " : "z: null ";
            sb.append(indexX).append(indexY).append(indexZ);
        } else {
            sb.append("Índices -> null ");
        }

        sb.append(" | ");

        if (dpmKeys != null && !dpmKeys.isEmpty()) {
            sb.append("Propriedades -> ");
            dpmKeys.forEach((key, value) -> sb.append(key).append(": ").append(value).append(", "));
            
            //Remove a última vírgula e espaco adicionados
            sb.setLength(sb.length() - 2);
        } else {
            sb.append("Propriedades -> null");
        }

        return sb.toString();
    }
   
}
