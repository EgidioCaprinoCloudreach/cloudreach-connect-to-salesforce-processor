package com.cloudreach.connect.tosalesforceprocessor.pluginLifecycle;

import com.cloudreach.connect.api.LogService;
import com.cloudreach.connect.api.PluginLifecycle;
import com.cloudreach.connect.api.context.PluginContext;
import com.cloudreach.connect.api.persistence.PersistenceException;
import com.cloudreach.connect.api.persistence.Transaction;
import com.cloudreach.connect.tosalesforceprocessor.services.FeatherModule;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.IOUtils;
import org.codejargon.feather.Feather;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DbPluginLifecycle implements PluginLifecycle {

    @Override
    public void onStartup(PluginContext pluginContext) throws Exception {
        setUpDb(pluginContext);
    }

    @Override
    public void onInstallOrUpdate(PluginContext pluginContext) throws Exception {
        setUpDb(pluginContext);
    }

    private void setUpDb(final PluginContext pluginContext) throws PersistenceException {
        pluginContext.getPersistence().runTransaction(new Transaction() {
            @Override
            public void implement(Connection db) throws Exception {
                FeatherModule featherModule = new FeatherModule(pluginContext, db);
                Feather feather = Feather.with(featherModule);
                QueryRunner queryRunner = feather.instance(QueryRunner.class);
                Properties properties = feather.instance(Properties.class);
                try {
                    createTable(db, queryRunner, properties);
                } catch (Exception e) {
                    LogService logService = feather.instance(LogService.class);
                    logService.error(e.getMessage(), e);
                }
            }
        });
    }

    private void createTable(Connection db, QueryRunner queryRunner, Properties properties) throws IOException, SQLException {
        String ddl;
        try (InputStream inputStream = DbPluginLifecycle.class.getResourceAsStream("/to-salesforce-processor-db.ddl")) {
            ddl = IOUtils.toString(inputStream);
        }
        ddl = ddl.replace("__SCHEMA__", properties.getProperty("db.schema"));
        queryRunner.update(db, ddl);
    }

}
