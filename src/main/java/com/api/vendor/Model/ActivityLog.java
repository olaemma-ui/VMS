package com.api.vendor.Model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter @Setter
public class ActivityLog {

    @Id
    private String id;

    private String initiatorId;

    private String approverId;

    private String action;

    private String vendorId;

    private String status;

    private Timestamp createdAt;

    private String initiatorRemark;

    private String approverRemark;
}
