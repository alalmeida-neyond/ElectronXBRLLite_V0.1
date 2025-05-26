package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import com.example.config.ReplacementDB;
import com.example.demo.controller.Objects.ActionPhases.DeleteAction;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		DeleteAction deleteAction = new DeleteAction();
		deleteAction.deleteRecords();
		ReplacementDB replacementDB = new ReplacementDB();
		replacementDB.replacementDBEvent();
		System.out.println("Replacement Called");
		if (!replacementDB.isValidSQLiteFile()) {
			System.err.println("The SQLite file is invalid. Application will not start.");
			System.exit(1);
		}
		SpringApplication.run(DemoApplication.class, args);
	}

	

}
