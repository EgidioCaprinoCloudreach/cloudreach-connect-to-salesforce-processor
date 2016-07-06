# to-salesforce-processor

You write into the database table and this processor will push the records to Salesforce.

## Install

Add the following in your `pom.xml` file.

``` xml
<repositories>
  <repository>
      <id>cloudreach-connect</id>
      <url>https://raw.githubusercontent.com/EgidioCaprinoCloudreach/cloudreach-connect-to-salesforce-processor/mvn-repo/
      </url>
  </repository>
</repositories>

<dependencies>
  <dependency>
      <groupId>com.cloudreach.connect</groupId>
      <artifactId>to-salesforce-processor</artifactId>
      <version>1.0</version>
  </dependency>
</dependencies>
```

## Setup

Create a `to-salesforce-processor.properties` file in your classpath with these properties:

- sf.orgId
- sf.userId (optional)
- db.schema
- batchProcessor.frequencyInMinutes

## Logger

You can setup a custom LogService by calling `com.cloudreach.connect.tosalesforceprocessor.services.FeatherModule#setLogService` method.
Your custom logger must not be context specific and set up in a `PluginLifecycle` class.
