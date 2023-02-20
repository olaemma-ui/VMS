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


    private String tin;

    private String fax;

    @Lob
    private String litigation;


    private String orgName;

    private String initiatorId;

    private String approverId;

    private String orgAddress;

    private String orgOfficeNo;

    private String orgWebSite;

    private String orgEmail;

    private String vendorPhone;

    private String vendorFullName;

    private String suntrustAccountNumber;

    private String orgWorkingCapital;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private String status;

    private String action;

    private String approvalStatus;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VendorDocuments> documents;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerifiedClient> verifiedClients;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OwnedEquip> ownedEquips;
}
