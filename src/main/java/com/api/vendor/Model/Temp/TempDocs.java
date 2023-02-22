package com.api.vendor.Model.Temp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Arrays;

@Entity
@Getter @Setter
public class TempDocs {

    @Id
    private String documentId;

    private String fileName;

    @Lob
    private String file;

    private String documentName;

    private Timestamp uploadedAt;

    @ManyToOne
    @JoinColumn(name = "vendorId")
    @JsonIgnore
    private TempVendor tempVendor;

    @Override
    public String toString() {
        return "{\n" +
                "\"documentId\":\"" + documentId + "\"" +
                ",\n\"fileName\":\"" + fileName + "\"" +
                ",\n\"documentName\":\"" + documentName + "\"" +
                ",\n\"file\":\"" + file +
                ",\n\"uploadedAt\":\"" + uploadedAt +"\"" +
                "\n}";
    }
}
