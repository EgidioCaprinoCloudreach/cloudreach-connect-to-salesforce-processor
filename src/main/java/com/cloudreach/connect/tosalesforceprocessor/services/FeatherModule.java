package com.cloudreach.connect.tosalesforceprocessor.services;

import com.cloudreach.connect.api.LogService;
import com.cloudreach.connect.api.context.PluginContext;
import com.cloudreach.connect.api.integration.Salesforce;
import com.cloudreach.connect.api.integration.SalesforceException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.apache.commons.dbutils.QueryRunner;
import org.codejargon.feather.Provides;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

public class FeatherModule {

    @Setter
    private static LogService logService;

    private final PluginContext pluginContext;
    private final Connection connection;

    public FeatherModule() {
        this(null, null);
    }

    public FeatherModule(PluginContext pluginContext, Connection connection) {
        this.pluginContext = pluginContext;
        this.connection = connection;
    }

    @Provides
    @Singleton
    public PluginContext pluginContext() {
        return pluginContext;
    }

    @Provides
    @Singleton
    public Connection connection() {
        return connection;
    }

    @Provides
    @Inject
    public LogService logService(PluginContext pluginContext) {
        if (logService == null) {
            return pluginContext.getLogService();
        } else {
            return logService;
        }
    }

    @Provides
    @Singleton
    public QueryRunner queryRunner() {
        return new QueryRunner();
    }

    @Provides
    @Singleton
    public Properties properties() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = FeatherModule.class.getResourceAsStream("/to-salesforce-processor.properties")) {
            properties.load(inputStream);
        }
        return properties;
    }

    @Provides
    @Singleton
    @Inject
    public Salesforce salesforce(PluginContext pluginContext, Properties properties) throws SalesforceException {
        Salesforce salesforce = pluginContext.getIntegration(Salesforce.class);
        salesforce.setScope(properties.getProperty("sf.orgId"), properties.getProperty("sf.userId"));
        return salesforce;
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Named("mapTypeReference")
    @Singleton
    public TypeReference<Map<String, Object>> mapTypeReference() {
        return new TypeReference<Map<String, Object>>() {
        };
    }

}
