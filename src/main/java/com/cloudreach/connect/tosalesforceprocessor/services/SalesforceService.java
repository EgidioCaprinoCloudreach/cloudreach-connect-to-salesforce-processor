package com.cloudreach.connect.tosalesforceprocessor.services;

import com.cloudreach.connect.api.integration.Salesforce;
import com.cloudreach.connect.api.integration.SalesforceError;
import com.cloudreach.connect.api.integration.SalesforceException;
import com.cloudreach.connect.api.integration.SalesforceObject;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

@Singleton
public class SalesforceService {

    private final Salesforce salesforce;

    @Inject
    public SalesforceService(Salesforce salesforce) {
        this.salesforce = salesforce;
    }

    public SalesforceObject createObject(String name, Map<String, Object> data) {
        Map<String, Object> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(data);
        return salesforce.createObject(name, map);
    }

    public void upsert(Collection<SalesforceObject> salesforceObjects) throws SalesforceException {
        try {
            salesforce.upsert(salesforceObjects);
        } catch (SalesforceException e) {
            throw toMeaningfulException(e);
        }
    }

    public SalesforceException toMeaningfulException(SalesforceException e) {
        StringBuilder stringBuilder = new StringBuilder();
        append(stringBuilder, e.getMessage());
        for (SalesforceError error : e.getErrors()) {
            append(stringBuilder, String.format("%s [%s] (%s)", error.getMessage(), error.getCode(), StringUtils.join(error.getFields(), ", ")));
        }
        return new SalesforceException(stringBuilder.toString(), e);
    }

    private void append(StringBuilder stringBuilder, String string) {
        if (string != null) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" - ");
            }
            stringBuilder.append(string);
        }
    }

}
