/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Entities.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.DTOs.HeaderDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;

public class TableVersionHeaderDAL {

    public static List<Object[]> getGenerationHeader(int tableVID, LocalDate referenceDate) {
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        List<Object[]> resultList = new ArrayList<>();

        try {
            StringBuilder queryString = new StringBuilder("SELECT IC.CODE, HV.CODE, h.direction ");
            queryString.append(" FROM TABLEVERSIONHEADER TVH ");
            queryString.append(" INNER JOIN HEADERVERSION HV ON TVH.HEADERVID = HV.HEADERVID ");
            queryString.append(" RIGHT JOIN VARIABLEVERSION VV ON VV.VARIABLEVID = HV.KEYVARIABLEVID ");
            queryString.append(" INNER JOIN ITEMCATEGORY IC ON IC.ITEMID = HV.PROPERTYID ");
            queryString.append(" Inner JOIN Header h ON h.HEADERID = HV.HEADERID ");
            queryString.append(" left join release sr on sr.releaseid = ic.startreleaseid ");
            queryString.append(" left join release er on er.releaseid = ic.endreleaseid ");
            queryString.append(" where (sr.\"Date\" <= TO_DATE(:referenceDate, 'YYYY-MM-DD') and (er.\"Date\" >= TO_DATE(:referenceDate, 'YYYY-MM-DD') or er.releaseid is null)) ");
            queryString.append("     and h.iskey = '1' and TVH.TABLEVID = :tableVID ");
            queryString.append(" ORDER BY TVH.\"Order\",TVH.TABLEVID");
            resultList = jpa.getNativeResultList(queryString.toString(),
                    "tableVID", tableVID,
                    "referenceDate", referenceDate.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return resultList;
    }
    
    
    public static List<Object[]> getAltGenerationHeader(int tableVID, LocalDate referenceDate) {
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        List<Object[]> resultList = new ArrayList<>();

        try {
            resultList = jpa.getNativeResultList(Utils.getResource("SQL_Queries/altHeaderGenerationXBRL.sql"),
                    "tableVId", tableVID,
                    "referenceDate", referenceDate.format(Constants.DATEFORMATISO8601),
                    "format",Constants.ISOBASEFORMAT8601SQLite);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return resultList;
    }

    public static List<HeaderDTO> getListOfHeaderForATableVid(int tableVid){
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        List<Object[]> listTemp = new ArrayList<>();
        List<HeaderDTO> listOfModules = new ArrayList<>();
        try {
            StringBuilder query = new StringBuilder("SELECT H.HEADERID, HV.CODE, H.DIRECTION ");
            query.append(" FROM TABLEVERSIONHEADER TVH ");
            query.append(" Inner JOIN HEADERVERSION HV ON TVH.HEADERVID = HV.HEADERVID ");
            query.append(" Inner JOIN HEADER H ON H.HEADERID = HV.HEADERID ");
            query.append(" WHERE TVH.TABLEVID = :tableVid");
            
            listTemp = jpa.getNativeResultList(query.toString(),
                    "tableVid", tableVid);
            for(Object[] obj: listTemp){
               listOfModules.add(new HeaderDTO(Integer.valueOf(obj[0].toString()),obj[1].toString(), obj[2].toString().toCharArray()[0]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            jpa.close();
        }
        return listOfModules;
    }
    
    public static String getCodeForAPropertyAndTableVID(int tableVID, String propertyCode){
        JPA<String> jpa = new JPA<String>(String.class);
        String result  = null;
        try {
            StringBuilder query = new StringBuilder("Select hv.code from itemcategory ic ");
                query.append(" inner join HeaderVersion hv on hv.propertyid = ic.itemid ");
                query.append(" inner join tableversionheader tvh on tvh.headervid = hv.headervid ");
                query.append(" where ic.code = :PropertyCode ");
                query.append(" and tvh.tablevid = :tableVID");
        Object aux = jpa.getNativeResultList(query.toString(),
                    "PropertyCode", propertyCode,"tableVID",tableVID).get(0);
        result = aux.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            jpa.close();
        }
        
        return result;
    
    }
}
