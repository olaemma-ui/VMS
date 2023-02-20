package com.api.vendor.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter @Setter
public class VendorDocuments {

    @Id private String documentId;

    private String fileName;

    private String title;

    @Lob
    private byte[] base64;

    private Timestamp uploadedAt;

    @ManyToOne
    @JoinColumn(name = "vendorId")
    @JsonIgnore
    private Vendor vendor;
}
