
package com.example.demo.controller.Objects.Entities.Logs;

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
