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

    @JsonIgnore
    private String requestId;

    private String initiatorId;

    private String approverId;

    private String orgName;

    private String tin;

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

    private String action;

    private String status;

    private String approvalStatus;

    private String remark;


    @OneToMany(mappedBy = "tempVendor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<TempDocs> docsList;

    @OneToMany(mappedBy = "tempVendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempVerifiedClient> verifiedClients;

    @OneToMany(mappedBy = "tempVendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TempOwnedEquip> ownedEquips;

    @Override
    public String toString() {
        return "{\n" +
                "\"vendorId\":\"" +vendorId+"\""+
                ", \n \"initiatorId\":\"" +initiatorId+"\""+
                ", \n \"approverId\":\"" +approverId+"\""+
                ", \n \"orgName\":\"" +orgName+"\""+
                ", \n \"tin\":\"" +tin+"\""+
                ", \n \"orgAddress\":\"" +orgAddress+"\""+
                ", \n \"orgOfficeNo\":\"" +orgOfficeNo+"\""+
                ", \n \"orgWebSite\":\"" +orgWebSite+"\""+
                ", \n \"orgEmail\":\"" +orgEmail+"\""+
                ", \n \"status\":\"" +status+"\""+
                ", \n \"vendorPhone\":\"" +vendorPhone+"\""+
                ", \n \"vendorFullName\":\"" +vendorFullName+"\""+
                ", \n \"suntrustAccountNumber\":\"" +suntrustAccountNumber+"\""+
                ", \n \"orgWorkingCapital\":\"" +orgWorkingCapital+"\""+
                ", \n \"createdAt\":\"" +createdAt+"\""+
                ", \n \"updatedAt\":\"" +updatedAt+"\""+
                ", \n \"action\":\"" +action+"\""+
                ", \n \"remark\":\"" +remark+"\""+
                ", \n \"verifiedClients\":" +verifiedClients+""+
                ", \n \"ownedEquips\":" +ownedEquips+""+
//                ", \n \"approverRemark\":\"" +approverRemark+"\""+
                ", \n \"approvalStatus\":\"" +approvalStatus+"\""+
                ", \n \"docsList\":" +docsList+""+
                "\n}";
    }
}
