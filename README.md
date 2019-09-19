# cumulus-message-adapter-java

[![CircleCI](https://circleci.com/gh/nasa/cumulus-message-adapter-java.svg?style=svg)](https://circleci.com/gh/nasa/cumulus-message-adapter-java)

## What is Cumulus?

Cumulus is a cloud-based data ingest, archive, distribution and management
prototype for NASA's future Earth science data streams.

Read the [Cumulus Documentation](https://cumulus-nasa.github.io/)

## What is the Cumulus Message Adapter?

The Cumulus Message Adapter is a library that adapts incoming messages in the
Cumulus protocol to a format more easily consumable by Cumulus tasks, invokes
the tasks, and then adapts their response back to the Cumulus message protocol
to be sent to the next task.

## Installation

Add the Cumulus Message Adapter as a dependency in your project. The current version and dependency installation code can be found [here](https://clojars.org/gov.nasa.earthdata/cumulus-message-adapter).

## Task definition

In order to use the Cumulus Message Adapter, you will need to create two
methods in your task module: a handler function and a business logic function.

The handler function is a standard Lambda handler function.

The business logic function is where the actual work of your task occurs. The class containing this work should implement the `ITask` interface and the ```String PerformFunction(String input, Context context);``` function. `input` is the simplified JSON from the message adapter and `context` is the AWS Lambda Context.

## Cumulus Message Adapter interface

Create an instance of `MessageParser` and call 

`RunCumulusTask(String input, Context context, ITask task)` 

or 

`RunCumulusTask(String input, Context context, ITask task, String inputSchemaLocation, String outputSchemaLocation, String configSchemaLocation)`

with the following parameters:
  
  * `input` - the input to the Lamda function
  * `context` - the Lambda context
  * `task` - an instance of the class that implements `ITask`
  
  And optionally:
  * `inputSchemaLocation` - file location of the input JSON schema, can be null 
  * `outputSchemaLocation` - file location of the output JSON schema, can be null
  * `configSchemaLocation` - file location of the config JSON schema, can be null
  
If the schema locations are not specified, the message adapter will look for schemas in a schemas directory at the root level for the files: input.json, output.json, or config.json. If the schema is not specified or missing, schema validation will not be peformed.
  
 ```RunCumulusTask``` throws a ```MessageAdapterException``` when there is an error.
  
## Example Cumulus task

```java
public class TaskLogic implements ITask
{
    public String PerformFunction(String input, Context context)
    {
        return "{\"status\":\"complete\"}";
    }
}
```

```java
public class Task implements RequestHandler<String, String> 
{
    public String handleRequest(String input, Context context) {
        MessageParser parser = new MessageParser();

        try
        {
            return parser.RunCumulusTask(input, context, new TaskLogic());
        }
        catch(MessageAdapterException e)
        {
            return e.getMessage();
        }
    }
}
```

For a full example see the [task folder](./task).

## Creating a deployment package

The compiled task code, the message parser uber-jar, the cumulus message adapter zip, and any other dependencies should all be included in a zip file and uploaded to lambda. Information on the zip file folder structure is located [here](https://docs.aws.amazon.com/lambda/latest/dg/create-deployment-pkg-zip-java.html).

## Usage in Cumulus Deployment

For documenation on how to utilize this package in a Cumulus Deployment, view the [Cumulus Workflow Documenation](https://nasa.github.io/cumulus/docs/workflows/input_output).

## Logging

The message adapter library contains a logging class `AdapterLogger` that standardizes the log format for Cumulus. Static functions are provided to log error, fatal, warning, debug, info, and trace.

For example, to log an error, call:

```AdapterLogger.LogError("Error message");```

## Development

### Prerequisites

  * [Apache Maven](https://maven.apache.org/install.html)
  
### Building

Build with ```mvn -B package```

### Integration Tests

Integration tests are located in the test folder in ```MessageParserTest.java```. By default, the latest [Cumulus Message Adapter](https://github.com/cumulus-nasa/cumulus-message-adapter) will be downloaded and used for the tests. To build and run the tests using the latest version of `Cumulus Message Adapter', run

```mvn -B test```

To build and run the tests using a different version of `Cumulus Message Adapter`, run

```MESSAGE_ADAPTER_VERSION=vx.x.xx mvn -B test```

### Running the example task

Follow the installation instructions above for the example task.

If updating the version of the message parser, make sure to update the pom.xml in the task code. To build the task with this dependency, run:

```mvn clean install -U```

then ```mvn -B package```

## Why?

This approach has a few major advantages:

1. It explicitly prevents tasks from making assumptions about data structures
   like `meta` and `cumulus_meta` that are owned internally and may therefore
   be broken in future updates. To gain access to fields in these structures,
   tasks must be passed the data explicitly in the workflow configuration.
1. It provides clearer ownership of the various data structures. Operators own
   `meta`. Cumulus owns `cumulus_meta`. Tasks define their own `config`,
   `input`, and `output` formats.
1. The Cumulus Message Adapter greatly simplifies running Lambda functions not
   explicitly created for Cumulus.
1. The approach greatly simplifies testing for tasks, as tasks don't need to
   set up cumbersome structures to emulate the message protocol and can just
   test their business function.
