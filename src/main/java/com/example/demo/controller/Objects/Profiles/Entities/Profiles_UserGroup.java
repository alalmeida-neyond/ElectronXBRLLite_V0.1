package com.example.demo.controller.Objects.Profiles.Entities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import jakarta.persistence.*;

import jakarta.validation.constraints.*;

public class Profiles_UserGroup {
    
    @Id
    @Column(name = "ID_USER_GROUP")
    private Integer idUserGroup;
    
    @Column(name = "ID_PROFILE")
    @NotNull
    private Integer idProfile;
    
    @Column(name = "ID_PROFILE_GROUP_PRIVILEGES")
    @NotNull
    private Integer idProfileGroupPrivileges;
    
    @Column(name = "FROMDATE")
    @NotNull
    private String fromDate;

    @Column(name = "TODATE")
    private String toDate;

    @Column(name = "GRANTEDBY")
    @NotNull
    private String grantedBy;

    @Column(name = "REVOKEDBY")
    private String revokedBy;

    public Integer getIdUserGroup() {
        return idUserGroup;
    }

    public void setIdUserGroup(Integer idUserGroup) {
        this.idUserGroup = idUserGroup;
    }

    public Integer getIdProfile() {
        return idProfile;
    }

    public void setIdProfile(Integer idProfile) {
        this.idProfile = idProfile;
    }

    public Integer getIdProfileGroupPrivileges() {
        return idProfileGroupPrivileges;
    }

    public void setIdProfileGroupPrivileges(Integer idProfileGroupPrivileges) {
        this.idProfileGroupPrivileges = idProfileGroupPrivileges;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        if (fromDate.split(" ").length > 1) {
            try {
                System.out.println("Attempt to Parse Date Number 1");
                DateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                Date d = df.parse(fromDate);
                fromDate = new SimpleDateFormat("yyyyMMdd").format(d);

            } catch (ParseException ex) {
            }
        }
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        if (toDate.split(" ").length > 1) {
            try {
                System.out.println("Attempt to Parse Date Number 2");
                DateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                Date d = df.parse(toDate);
                toDate = new SimpleDateFormat("yyyyMMdd").format(d);

            } catch (ParseException ex) {
            }
        }
        this.toDate = toDate;
    }

    public String getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(String grantedBy) {
        this.grantedBy = grantedBy;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }
    
    
}
