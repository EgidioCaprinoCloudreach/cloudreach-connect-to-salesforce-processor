package com.cloudreach.connect.tosalesforceprocessor.services;

import com.cloudreach.connect.api.integration.Salesforce;
import com.cloudreach.connect.api.integration.SalesforceError;
import com.cloudreach.connect.api.integration.SalesforceException;
import com.cloudreach.connect.api.integration.SalesforceObject;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

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

    public void save(Collection<SalesforceObject> salesforceObjects) throws SalesforceException {
        List<SalesforceObject> toInsert = new ArrayList<>(salesforceObjects.size());
        List<SalesforceObject> toUpdate = new ArrayList<>(salesforceObjects.size());

        for (SalesforceObject salesforceObject : salesforceObjects) {
            if (salesforceObject.get("Id") == null) {
                toInsert.add(salesforceObject);
            } else {
                toUpdate.add(salesforceObject);
            }
        }

        try {
            salesforce.insert(toInsert);
            salesforce.update(toUpdate);
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
