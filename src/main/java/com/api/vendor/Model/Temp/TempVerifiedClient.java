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
public class TempVerifiedClient {

    @Id
    private String id = UUID.randomUUID().toString();

    private String compName;

    private String compNumb;

    private String compEmail;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "vendorId")
    private TempVendor tempVendor;

    @Override
    public String toString() {
        return "{\n" +
                "\"id\":\"" + id + "\"" +
                ",\n \"compName\": \"" + compName + "\"" +
                ",\n \"compEmail\": \"" + compEmail + "\"" +
                ",\n \"compNumb\": \"" + compNumb + "\"" +
            "\n}";
    }
}
