package com.api.vendor.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
@Getter @Setter
public class VerifiedClient {

    @Id
    private String id;

    private String compName;

    private String compNumb;

    private String compEmail;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "vendorId")
    private Vendor vendor;
}
