CREATE SCHEMA IF NOT EXISTS __SCHEMA__;

SET SCHEMA '__SCHEMA__';

CREATE TABLE IF NOT EXISTS to_salesforce_record (
  id_to_salesforce_record BIGSERIAL NOT NULL PRIMARY KEY,
  parent_id               BIGINT REFERENCES to_salesforce_record (id_to_salesforce_record) ON UPDATE CASCADE,
  salesforce_object       VARCHAR   NOT NULL,
  salesforce_id           VARCHAR,
  data                    JSON      NOT NULL,
  processed               BOOLEAN   NOT NULL,
  processed_date          TIMESTAMP,
  insert_date             TIMESTAMP NOT NULL,
  last_error              TEXT,
  priority                BIGINT    NOT NULL
);
