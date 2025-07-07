package com.example.demo.Verification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

import org.jboss.logging.Logger;

import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LicenseVerification {

    private static final File PUBLIC_KEY_FILE = new File(Constants.publicKeyDirectory);
    private static String cachedLicense = null;
    private static boolean licenseValidated = false;
    private final Logger LOG = Logger.getLogger(LicenseVerification.class);

    public void licenseVerification() throws Exception {

        File f = new File(Constants.licenseStringDirectory);
        if (f.exists() && !f.isDirectory()) {
            Scanner scanningLicenseString = new Scanner(f);

            while (scanningLicenseString.hasNextLine()) {
                String licenseStringPrevious = scanningLicenseString.nextLine();
                if (validateLicense(licenseStringPrevious)) {
                    licenseValidated = true;

                }
            }
            scanningLicenseString.close();
        }

        if (!licenseValidated) {
            if (cachedLicense == null) {
                Scanner scanner = new Scanner(System.in);
                cachedLicense = scanner.nextLine();
                scanner.close();
            }

            if (validateLicense(cachedLicense)) {
                licenseValidated = true;
                OutputStream out = new FileOutputStream(Constants.licenseStringDirectory);
                try {
                    Writer writer = new OutputStreamWriter(out, Constants.charSet);
                    writer.write(cachedLicense);
                    writer.close();
                } finally {
                    out.close();
                }
            } else {
                cachedLicense = null;
            }
        }

    }

    private boolean validateLicense(String base64) {
        String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> full;
        try {
            full = mapper.readValue(json, Map.class);

            Map<String, String> license = (Map<String, String>) full.get(Constants.licenseString);
            String signatureBase64 = (String) full.get(Constants.signatureString);

            byte[] licenseBytes = mapper.writeValueAsBytes(license);
            byte[] signature = Base64.getDecoder().decode(signatureBase64);

            PublicKey publicKey;

            publicKey = loadPublicKey(PUBLIC_KEY_FILE);

            if (publicKey == null) {
                LOG.error("The Public Key Does Not Exist");
                return false;
            }

            Signature verifier;
            try {
                verifier = Signature.getInstance(Constants.algoritmString);
                try {
                    verifier.setParameter(new PSSParameterSpec(Constants.mdNameString, Constants.mgfNameString,
                            MGF1ParameterSpec.SHA256, 32, 1));
                    try {
                        verifier.initVerify(publicKey);
                        try {
                            verifier.update(licenseBytes);
                            boolean verified = verifier.verify(signature);
                            if (!verified)
                                return false;

                            String lei = license.get(Constants.LEICodeKeyString);
                            String BDPID = license.get (Constants.BDPIDKeyString);
                            String hwid = license.get(Constants.hardwareIDKeyString);
                            String expiry = license.get(Constants.expirationDateString);

                            LocalDate expiryDate = LocalDate.parse(expiry);
                            if (expiryDate.isBefore(LocalDate.now())) {
                                return false;
                            }

                            JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        
                            StringBuilder query = new StringBuilder(" DELETE FROM CONF_ENTITIES ");
                            
                            try {
                                jpa.executeNativeQuery(query.toString());
                                try {
                                    jpa.executeFileQuery("SQL_Queries/CONFENTITIESInsertion.sql",
                                        "leicode", lei,
                                        "bdpid", BDPID
                                    );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    jpa.rollback();
                                }
                                
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                jpa.rollback();
                            }

                            return true;
                        } catch (SignatureException e) {
                            LOG.error("Error with the Signature:" + e.getMessage());
                            e.printStackTrace();
                            return false;
                        }
                    } catch (InvalidKeyException e) {
                        LOG.error("Key Invalid:" + e.getMessage());
                        e.printStackTrace();
                        return false;
                    }

                } catch (InvalidAlgorithmParameterException e) {
                    LOG.error("Invalid Algorithm:" + e.getMessage());
                    e.printStackTrace();
                    return false;
                }

            } catch (NoSuchAlgorithmException e) {
                LOG.error("Algorithm Does Not Exist:" + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static PublicKey loadPublicKey(File pemFile) {
        final Logger LOGMETHOD = Logger.getLogger(LicenseVerification.class);
        byte[] pem;
        try {
            pem = Files.readAllBytes(pemFile.toPath());
            String key = new String(pem)
                    .replaceAll(Constants.beginRegex, "")
                    .replaceAll(Constants.endRegex, "")
                    .replaceAll(Constants.whiteSpaceRegex, "");

            byte[] decoded = Base64.getDecoder().decode(key);
            try {
                return KeyFactory.getInstance(Constants.RSAAlgoritmString)
                        .generatePublic(new X509EncodedKeySpec(decoded));
            } catch (InvalidKeySpecException e) {
                LOGMETHOD.error("Key Invalid:" + e.getMessage());
                e.printStackTrace();
                return null;
            } catch (NoSuchAlgorithmException e) {
                LOGMETHOD.error("Algorithm Does Not Exist:" + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            LOGMETHOD.error("Problem Accessing File:" + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }
}
