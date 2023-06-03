package com.api.vendor.Model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter @Setter
public class Vendor {

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
    private String orgFax;
// COMPANY DETAILS =================

// COMPANY CONTACT PERSON ===============
    private String contactPhone;
    private String contactFullName;
    private String suntrustAccountNumber;
// COMPANY CONTACT PERSON ===============


// COMPLIANCE WITH LAWS AND REGULATIONS =======
    @Lob
    private String compliance;
// COMPLIANCE WITH LAWS AND REGULATIONS =======

// FINANCIAL RESOURCES ===============
    private String orgWorkingCapital;
    private String orgBankName;
    private String orgAccName;
    private String orgAccNum;
// FINANCIAL RESOURCES ===============

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorDocuments> documents;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerifiedClient> verifiedClients;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OwnedEquip> ownedEquips;

    private String initiatorId;
    private String initiatorName;

    private String approverId;
    private String approverName;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private String status;

    private String action;

    private String approvalStatus;
}
