package com.cloudreach.connect.tosalesforceprocessor.batchprocessors;

import com.cloudreach.connect.api.LogService;
import com.cloudreach.connect.api.Result;
import com.cloudreach.connect.api.batch.BatchProcessor;
import com.cloudreach.connect.api.context.PluginContext;
import com.cloudreach.connect.api.integration.SalesforceException;
import com.cloudreach.connect.api.integration.SalesforceObject;
import com.cloudreach.connect.api.persistence.Transaction;
import com.cloudreach.connect.tosalesforceprocessor.dao.ToSalesforceRecordDao;
import com.cloudreach.connect.tosalesforceprocessor.models.ToSalesforceRecord;
import com.cloudreach.connect.tosalesforceprocessor.services.FeatherModule;
import com.cloudreach.connect.tosalesforceprocessor.services.SalesforceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codejargon.feather.Feather;
import org.codejargon.feather.Key;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ToSalesforceBatchProcessor implements BatchProcessor {

    @Override
    public int getFrequency() {
        FeatherModule featherModule = new FeatherModule();
        Feather feather = Feather.with(featherModule);
        Properties properties = feather.instance(Properties.class);
        int frequency = Integer.parseInt(properties.getProperty("batchProcessor.frequencyInMinutes"));
        return frequency;
    }

    @Override
    public UnitOfTime getUnitOfTime() {
        return UnitOfTime.MINUTES;
    }

    @Override
    public Result implement(final PluginContext pluginContext) throws Exception {
        pluginContext.getPersistence().runTransaction(new Transaction() {
            @Override
            public void implement(Connection db) throws Exception {
                FeatherModule featherModule = new FeatherModule(pluginContext, db);
                Feather feather = Feather.with(featherModule);
                ToSalesforceRecordDao toSalesforceRecordDao = feather.instance(ToSalesforceRecordDao.class);
                SalesforceService salesforceService = feather.instance(SalesforceService.class);
                ObjectMapper objectMapper = feather.instance(ObjectMapper.class);
                TypeReference<Map<String, Object>> mapTypeReference = feather.instance(Key.of(TypeReference.class, "mapTypeReference"));
                LogService logService = feather.instance(LogService.class);
                sendRecordsToSalesforce(toSalesforceRecordDao, salesforceService, db, objectMapper, mapTypeReference, logService);
            }
        });
        return null;
    }

    private void sendRecordsToSalesforce(ToSalesforceRecordDao toSalesforceRecordDao, SalesforceService salesforceService, Connection db, ObjectMapper objectMapper, TypeReference<Map<String, Object>> mapTypeReference, LogService logService) throws Exception {
        List<ToSalesforceRecord> toSalesforceRecords = toSalesforceRecordDao.findAllByProcessed(false);
        for (ToSalesforceRecord toSalesforceRecord : toSalesforceRecords) {
            try {
                String salesforceId = sendToSalesforce(toSalesforceRecord, salesforceService, objectMapper, mapTypeReference);
                logService.info("Salesforce ID: " + salesforceId);
                toSalesforceRecord.setSalesforceId(salesforceId);
                toSalesforceRecord.setLastError(null);
            } catch (Exception e) {
                db.rollback();
                toSalesforceRecord.setLastError(ExceptionUtils.getStackTrace(e));
            } finally {
                toSalesforceRecord.setProcessed(true);
                toSalesforceRecord.setProcessedDate(new Timestamp(System.currentTimeMillis()));
                toSalesforceRecordDao.save(toSalesforceRecord);
                db.commit();
            }
        }
    }

    private String sendToSalesforce(ToSalesforceRecord toSalesforceRecord, SalesforceService salesforceService, ObjectMapper objectMapper, TypeReference<Map<String, Object>> mapTypeReference) throws IOException, SalesforceException {
        Map<String, Object> data = objectMapper.readValue(toSalesforceRecord.getData(), mapTypeReference);
        if (toSalesforceRecord.getSalesforceId() != null) {
            data.put("Id", toSalesforceRecord.getSalesforceId());
        }
        SalesforceObject salesforceObject = salesforceService.createObject(toSalesforceRecord.getSalesforceObject(), data);
        salesforceService.save(Arrays.asList(salesforceObject));
        for (String field : salesforceObject.getFields()) {
            if ("Id".equalsIgnoreCase(field)) {
                String id = StringUtils.trimToNull(salesforceObject.getAsString(field));
                if (id != null && !id.equalsIgnoreCase("null")) {
                    return id;
                }
            }
        }
        throw new RuntimeException("Field not found after save operation: Id");
    }

}
