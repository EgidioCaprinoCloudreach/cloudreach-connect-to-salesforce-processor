package com.cloudreach.connect.tosalesforceprocessor.models;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ToSalesforceRecord {

    private Long idToSalesforceRecord;
    private Long parentId;
    private String salesforceObject;
    private String salesforceId;
    private String data;
    private Boolean processed;
    private Timestamp processedDate;
    private Timestamp insertDate;
    private String lastError;
    private Long priority;

}
