package com.example.demo.Verification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

import org.jboss.logging.Logger;
import org.springframework.stereotype.Component;

import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LicenseVerification {

    private static final File PUBLIC_KEY_FILE = new File(Constants.publicKeyDirectory);
    private static boolean licenseValidated = false;
    private final Logger LOG = Logger.getLogger(LicenseVerification.class);
    private boolean expired;
    private String lei,BDPID,hwid,expiry;

    public boolean isExpired() {
        return expired;
    }

    public void setIsExpired (boolean expired)
    {
        this.expired = expired;
    }

    public void licenseVerificationString(String licenseString) throws Exception {

        if (validateLicense(licenseString)) {
            licenseValidated = true;
            OutputStream out = new FileOutputStream(Constants.licenseStringDirectory);
            try {
                Writer writer = new OutputStreamWriter(out, Constants.charSet);
                writer.write(licenseString);
                writer.close();
            } catch (Exception e){
                LOG.error("Error writing in the .dat file:" + e.getMessage());
            } finally {
                out.close();
            } 
        }
    }

    public boolean licenseValidationFile() throws FileNotFoundException {
        File f = new File(Constants.licenseStringDirectory);
        if (f.exists() && !f.isDirectory()) {
            Scanner scanningLicenseString = new Scanner(f);

            while (scanningLicenseString.hasNextLine()) {
                String licenseStringPrevious = scanningLicenseString.nextLine();
                if (validateLicense(licenseStringPrevious)) {
                    scanningLicenseString.close();
                    return true;
                }
            }
            scanningLicenseString.close();
        }
        return false;
    }
    
    public String getSystemUUID() {
        String uuid = null;
        try {
            String command = "powershell Get-WmiObject Win32_ComputerSystemProduct | Select-Object -ExpandProperty UUID";

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    uuid = line.trim();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uuid;
    }

    public boolean validateLicense(String base64) {
        expired = false;
        String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> full;
        try {
            full = mapper.readValue(json, Map.class);

            Map<String, String> license = (Map<String, String>) full.get(Constants.licenseString);
            String signatureBase64 = (String) full.get(Constants.signatureString);

            byte[] licenseBytes = mapper.writeValueAsBytes(license);
            byte[] signature = Base64.getDecoder().decode(signatureBase64);

            PublicKey publicKey = loadPublicKey(PUBLIC_KEY_FILE);
            if (publicKey == null) {
                LOG.error("Public key does not exist");
                return false;
            }

            Signature verifier;
            verifier = Signature.getInstance(Constants.algoritmString);

            verifier.setParameter(new PSSParameterSpec(Constants.mdNameString, Constants.mgfNameString,
                    MGF1ParameterSpec.SHA256, 32, 1));

            verifier.initVerify(publicKey);

            verifier.update(licenseBytes);
            boolean verified = verifier.verify(signature);
            if (!verified)
                return false;

            String machinehwid = getSystemUUID();
            lei = license.get(Constants.LEICodeKeyString);
            BDPID = license.get(Constants.BDPIDKeyString);
            hwid = license.get(Constants.hardwareIDKeyString);
            expiry = license.get(Constants.expirationDateString);
            
            setBDPID(BDPID);
            setLEICode(lei);
            setHardwareID(hwid);
            
            LocalDate expiryDate = LocalDate.parse(expiry);
            if (expiryDate.isBefore(LocalDate.now())) {
                expired = true;
                setIsExpired(expired);

                return false;
            }

            if (!hwid.equalsIgnoreCase(machinehwid))
            {
                return false;
            }
            
            setExpirationDate(expiry);

            JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);

            // ATIVAR EM PROD

            /*StringBuilder query = new StringBuilder(" DELETE FROM CONF_ENTITIES ");

            try {
                jpa.executeNativeQuery(query.toString());
                try {
                    jpa.executeFileQuery("SQL_Queries/CONFENTITIESInsertion.sql",
                            "leicode", lei,
                            "bdpid", BDPID);
                } catch (Exception e) {
                    LOG.error("Error performing Insertion in Table:" + e.getMessage());
                    jpa.rollback();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                jpa.rollback();
            }*/

            return true;
        } catch (Exception e) {
            LOG.error("Error gathering information of license:" + e.getMessage());
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
            return KeyFactory.getInstance(Constants.RSAAlgoritmString)
                    .generatePublic(new X509EncodedKeySpec(decoded));

        } catch (Exception e) {
            LOGMETHOD.error("Problem Loading Key:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String getLEICode()
    {
        return lei;
    }

    private void setLEICode(String LEICode)
    {
        this.lei = LEICode;
    }

    public String getHardwareID()
    {
        return hwid;
    }

    private void setHardwareID(String HardwareID)
    {
        this.hwid = HardwareID;
    }

    public String getBDPID()
    {
        return BDPID;
    }

    private void setBDPID(String BDPID)
    {
        this.BDPID = BDPID;
    }

    public String getExpirationDate()
    {
        return expiry;
    }

    private void setExpirationDate(String ExpirationDate)
    {
        this.expiry = ExpirationDate;
    }
}
