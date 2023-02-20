package com.api.vendor.Message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Response {
    private Boolean success;
    private String responseCode;
    private String responseDescription;
    private Object data;
    private Object error;
}
