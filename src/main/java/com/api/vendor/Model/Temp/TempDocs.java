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
//    @Basic(fetch = FetchType.EAGER)
    private String base64;

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
                ",\n\"base64\":\"" + fileName + "\"" +
                ",\n\"documentName\":\"" + documentName + "\"" +
                ",\n\"file\":\"" + base64 +
                ",\n\"uploadedAt\":\"" + uploadedAt +"\"" +
                "\n}";
    }
}
