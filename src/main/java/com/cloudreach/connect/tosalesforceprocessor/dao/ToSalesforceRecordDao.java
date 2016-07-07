package com.cloudreach.connect.tosalesforceprocessor.dao;

import com.cloudreach.connect.orm.Dao;
import com.cloudreach.connect.tosalesforceprocessor.models.ToSalesforceRecord;
import com.cloudreach.connect.tosalesforceprocessor.services.FeatherModule;
import org.codejargon.feather.Feather;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

@Singleton
public class ToSalesforceRecordDao extends Dao<ToSalesforceRecord> {

    @Inject
    public ToSalesforceRecordDao(Connection db) throws SQLException {
        super(db, ToSalesforceRecord.class);
        addColumnCast("data", "JSON");
    }

    @Override
    public String getSchema() {
        FeatherModule featherModule = new FeatherModule();
        Feather feather = Feather.with(featherModule);
        Properties properties = feather.instance(Properties.class);
        return properties.getProperty("db.schema");
    }

    @Override
    public String getTable() {
        return "to_salesforce_record";
    }

    public List<ToSalesforceRecord> findAllByProcessed(boolean processed) throws SQLException {
        String query = "SELECT * " +
                "FROM " + getSchema() + "." + getTable() + " " +
                "WHERE processed = ? " +
                "ORDER BY priority ASC, salesforce_object ASC";
        Object[] params = {processed};
        return queryRunner.query(db, query, listHandler, params);
    }

    public List<ToSalesforceRecord> findAllBySalesforceObjectAndDataField(String salesforceObject, String field, String value) throws SQLException {
        String query = "SELECT * " +
                "FROM " + getSchema() + "." + getTable() + " " +
                "WHERE salesforce_object = ? " +
                "AND data->>? = ?";
        Object[] params = {salesforceObject, field, value};
        return queryRunner.query(db, query, listHandler, params);
    }

}
