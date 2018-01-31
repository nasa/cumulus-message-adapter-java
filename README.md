# cumulus-sled-java

[![CircleCI](https://circleci.com/gh/cumulus-nasa/cumulus-message-adapter-java.svg?style=svg)](https://circleci.com/gh/cumulus-nasa/cumulus-message-adapter-java)

Java handler for the Cumulus Message Adapter. 

## Prerequisites

  - Maven

## Building and Packaging

To build the uber-jar, run

```mvn -B package``` 

## Usage in Lambda Task code  

Message Parser contains the Java library that invokes the Cumulus Message Adapter. The message parser takes the string input to the AWS Lambda task, the AWS Lambda context, and an instance of a callback class. The input is passed to the message adapter. The message adapter output is passed to the callback function as a string and the output is passed back to the message adapter and used to create the next task in the workflow.

### Using the Message Parser

Add the message parser uber-jar as a dependency in your project.

Create a Java class that implements the ITask interface and  

```String PerformFunction(String input, Context context);```

```PerformFunction``` should contain all of the business logic for the Lambda task. This is the callback function used by the message parser. Context is the AWS Lambda Context.

Create an instance of ```MessageParser``` and call ```HandleMessage(String input, Context context, ITask task)``` with the input to the Lambda task, the AWS Lambda Context, and an instance of the class that implements ITask.

An example Lambda task is located in the task directory.

## Testing the Message Parser

### Integration Tests

Integration tests are located in the test folder in ```MessageParserTest.java```. To build and run the tests, run 

```mvn -B test```

### Manually with Lambda

An example task is located in the task folder of the repository that can be used to test the message parser in Lambda.

The message parser uber-jar should be added as a dependency to the task in the lib folder. To deploy the message parser to the task, build and package the message parser and run the following from the message_parser directory: 

```mvn deploy:deploy-file -Durl=file:<path to task lib folder> -Dfile=target/message_parser-1.8.jar -DgroupId=cumulus_message_adapter.message_parser -DartifactId=message_parser -Dpackaging=jar -Dversion=1.8```

If updating the version of the message parser, make sure to update the pom.xml in the task code. To build the task with this dependency, run:

```mvn clean install -U```

then ```mvn -B package```

The compiled task code, the message parser uber-jar, the cumulus message adapter zip, and any other dependencies should all be included in a zip file and uploaded to lambda. Information on the zip file folder structure is located [here](https://docs.aws.amazon.com/lambda/latest/dg/create-deployment-pkg-zip-java.html).

## Usage in a Cumulus Deployment

During deployment, Cumulus will automatically obtain and inject the Cumulus Message Adapter zip into the compiled code and create a zip file to be deployed to Lambda.

The test task in the 'task' folder of this repository would be configured in lambdas.yml as follows:

```
JavaTest:
  handler: test_task.task.Task::handleRequest
  timeout: 300
  source: '../cumulus-message-adapter-java/deploy/'
  useSled: true
  runtime: java8
  memory: 256
```

The source points to a folder with the compiled .class files and dependency libraries in the Lambda Java zip folder structure (details [here](https://docs.aws.amazon.com/lambda/latest/dg/create-deployment-pkg-zip-java.html)), not an uber-jar.

The deploy folder referenced here would contain a folder 'test_task/task/' which contains Task.class and TaskLogic.class as well as a lib folder containing dependency jars. The Cumulus Message Adapter zip would be added at the top level by the deployment step and that folder zipped and deployed to Lambda. 




