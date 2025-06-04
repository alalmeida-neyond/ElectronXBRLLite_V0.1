/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Entities.DAL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.Data.ConnectionManager;
import com.example.demo.Data.Access.Info;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.Conf.*;


public class ConfAppConfigsDAL {

    public static List<ConfAppConfigs> getListOfAppConfiguration(){
        return Info.getInstance().refDataGet(Constants.AppConfigsAll);
    }
    
    /**
     * method use to get the value based on a key
     * @param key
     * @return 
     */
    public static String getValueOfAppConfigurationKey(String key){  
        List<ConfAppConfigs> appConfigs = new ArrayList<>();
        String configResult = "";
        try {
            appConfigs = Info.getInstance().refDataGet(Constants.AppConfigsAll);
            configResult = appConfigs.stream().filter(x -> x.getKey().equals(key)).collect(Collectors.toList()).get(0).getValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
        }
        return configResult;
    }
    
    /**
     * method used to create a AppConfigs in the Database
     * @return if it was able to insert the new value
     */
    public static boolean createNewAppConfig(String key, String value) {
        ConnectionManager cm = new ConnectionManager();
        try {
            cm.em.getTransaction().begin();
            ConfAppConfigs appConfig = new ConfAppConfigs();
            appConfig.setKey(key.toUpperCase());
            appConfig.setValue(value);
            cm.em.persist(appConfig);
            cm.em.flush();
            cm.em.getTransaction().commit();
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally{
            cm.em.close();
        }
        
        return true;
    }    
    
    /**
     * method used to update a AppConfigs in the Database
     * @param oldKey
     * @param newkey
     * @param newValue
     * @return if was able to do the update
     */
    public static boolean createUpdateAppConfig(String oldKey, String newkey, String newValue) {
        JPA<Object> jpa = new JPA<Object>(Object.class);
        try {
            jpa.executeNativeQuery("Update CONF_APPCONFIGS set CONFIGKEY = :newkey, CONFIGVALUE = :newValue where CONFIGKEY = :oldKey",
                    "newkey",newkey,
                    "newValue",newValue,
                    "oldKey",oldKey);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }finally {
            jpa.close();
        }
        return true;
    }
}
