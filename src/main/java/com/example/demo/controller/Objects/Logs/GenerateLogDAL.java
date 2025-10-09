/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Logs;

import java.time.LocalDateTime;
import com.example.demo.Data.*;
import com.example.demo.controller.Objects.Generation.OutXBRLGenerated;

public class GenerateLogDAL {
    public static boolean createNewGenerationLog(String log, int xbrlID, ConnectionManager cm) {
        try {
            cm.em.getTransaction().begin();
            GenerateLog generationLog = new GenerateLog();
            generationLog.setDescription(log);
            generationLog.setXbrlGenerates(cm.em.getReference(OutXBRLGenerated.class, xbrlID));
            generationLog.setTimeStampCreated(LocalDateTime.now());
            cm.em.persist(generationLog);
            cm.em.flush();
            cm.em.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
