package com.api.vendor.Model.Temp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter @Setter
public class TempVendor {

    @Id
    private String vendorId;

// COMPANY DETAILS =================
    private String orgName;
    private String orgPhoneNo;
    private String orgEmail;
    private String orgAddress;
    private String orgWebSite;
    private String orgSpec;
    private String orgTin;
    private String orgRcNum;
// COMPANY DETAILS =================

// COMPANY CONTACT PERSON ===============
    private String contactPhone;
    private String contactFullName;
    private String suntrustAccountNumber;
// COMPANY CONTACT PERSON ===============


// COMPLIANCE WITH LAWS AND REGULATIONS =======
    @Lob
    private String compliance;
    private String businessType;
// COMPLIANCE WITH LAWS AND REGULATIONS =======

// FINANCIAL RESOURCES ===============
    private String orgWorkingCapital;
    private String orgBankName;
    private String orgAccName;
    private String orgAccNum;
// FINANCIAL RESOURCES ===============

    @JsonIgnore
    private String requestId;
    private String initiatorId;
    private String approverId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String action;
    private String status;
    private String approvalStatus;
    private String remark;


    @OneToMany(mappedBy = "tempVendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempDocs> documents;

    @OneToMany(mappedBy = "tempVendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempVerifiedClient> verifiedClients;

    @OneToMany(mappedBy = "tempVendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempOwnedEquip> ownedEquips;

    @Override
    public String toString() {
        return "{" +
                "\n \"vendorId\":\"" + vendorId +"\""+
                ", \n \"orgName\":\"" + orgName +"\""+
                ", \n \"orgPhoneNo\":\"" + orgPhoneNo +"\""+
                ", \n \"orgEmail\":\"" + orgEmail +"\""+
                ", \n \"orgAddress\":\"" + orgAddress +"\""+
                ", \n \"orgWebSite\":\"" + orgWebSite +"\""+
                ", \n \"orgSpec\":\"" + orgSpec +"\""+
                ", \n \"orgTin\":\"" + orgTin +"\""+
                ", \n \"orgRcNum\":\"" + orgRcNum +"\""+
                ", \n \"contactPhone\":\"" + contactPhone +"\""+
                ", \n \"contactFullName\":\"" + contactFullName +"\""+
                ", \n \"suntrustAccountNumber\":\"" + suntrustAccountNumber +"\""+
                ", \n \"compliance\":\"" + compliance +"\""+
                ", \n \"orgWorkingCapital\":\"" + orgWorkingCapital +"\""+
                ", \n \"orgBankName\":\"" + orgBankName +"\""+
                ", \n \"orgAccName\":\"" + orgAccName +"\""+
                ", \n \"orgAccNum\":\"" + orgAccNum +"\""+
                ", \n \"requestId\":\"" + requestId +"\""+
                ", \n \"initiatorId\":\"" + initiatorId +"\""+
                ", \n \"approverId\":\"" + approverId +"\""+
                ", \n \"createdAt\"=\"" + createdAt +"\""+
                ", \n \"updatedAt\"=\"" + updatedAt +"\""+
                ", \n \"action\":\"" + action +"\""+
                ", \n \"status\":\"" + status +"\""+
                "}";
    }
}
