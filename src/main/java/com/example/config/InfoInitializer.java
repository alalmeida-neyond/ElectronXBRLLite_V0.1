package com.example.config;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.demo.Data.Access.Info;

import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class InfoInitializer {

    /*@EventListener(ApplicationReadyEvent.class)
    public void loadDataAfterStartup() {
        Info.getInstance().loadRefData(true);
    }*/
}
