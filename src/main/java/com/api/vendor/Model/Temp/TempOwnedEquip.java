package com.api.vendor.Model.Temp;

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
public class TempOwnedEquip {

    @Id
    private String id = UUID.randomUUID().toString();

    private String description;

    private String number;

    private String model;

    private String age;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "vendorId")
    private TempVendor tempVendor;

}
