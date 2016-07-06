# to-salesforce-processor

You write into the database table and this processor will push the records to Salesforce.

## Install



## Setup

Create a `to-salesforce-processor.properties` file in your classpath with these properties:

- sf.orgId
- sf.userId (optional)
- db.schema
- batchProcessor.frequencyInMinutes

## Logger

You can setup a custom LogService by calling `com.cloudreach.connect.tosalesforceprocessor.services.FeatherModule#setLogService` method.
Your custom logger must not be context specific and set up in a `PluginLifecycle` class.
