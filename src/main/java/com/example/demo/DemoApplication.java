package com.example.demo;

import org.jboss.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import com.example.config.ReplacementDB;
import com.example.demo.Verification.LicenseVerification;
import com.example.demo.controller.Objects.ActionPhases.DeleteAction;

@SpringBootApplication
public class DemoApplication {
	

	public static void main(String[] args) {
		final Logger LOG = Logger.getLogger(DemoApplication.class);
		LicenseVerification licenseVerification = new LicenseVerification();

        try {
			licenseVerification.licenseValidationFile();
		} catch (Exception e) {
			LOG.error("Error verifying license:" + e.getMessage());
			e.printStackTrace();
		}
		DeleteAction deleteAction = new DeleteAction();
		deleteAction.deleteRecords();
		ReplacementDB replacementDB = new ReplacementDB();
		replacementDB.replacementDBEvent();
		if (!replacementDB.isValidSQLiteFile()) {
			System.err.println("The SQLite file is invalid. Application will not start.");
			System.exit(1);
		}
		SpringApplication.run(DemoApplication.class, args);
	}

	

}
