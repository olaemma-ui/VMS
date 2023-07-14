package com.api.vendor.Model;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class DocumentTypeTable {

    @Id
    private String id = UUID.randomUUID().toString();
    private String documentName;
}
