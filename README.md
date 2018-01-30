# cumulus-sled-java

[![CircleCI](https://circleci.com/gh/cumulus-nasa/cumulus-message-adapter-java.svg?style=svg)](https://circleci.com/gh/cumulus-nasa/cumulus-message-adapter-java)

Java handler for the Cumulus Message Adapter. 

## Building and Packaging

Maven must be installed as a prerequisite. To build the uber-jar, run

```mvn -B package``` 

## Usage in Lambda Task code  

Message Parser contains the Java library that invokes the Cumulus Message Adapter. The message parser takes the string input to the AWS Lambda task, the AWS Lambda context, and an instance of a callback class. The input is passed to the message adapter. The message adapter output is passed to the callback function as a string and the output is passed back to the message adapter and used to create the next task in the workflow.

### Using the Message Parser

Add the message parser uber-jar as a dependency in your project.

Create a Java class that implements the ITask interface and  

```String PerformFunction(String input);```

```PerformFunction``` should contain all of the business logic for the Lambda task. This is the callback function used by the message parser.

Create an instance of ```MessageParser``` and call ```HandleMessage(String input, Context context, ITask task)``` with the input to the Lambda task, the AWS Lambda Context, and an instance of the class that implements ITask.

An example Lambda task is located in the task directory.

## Testing the Message Parser

### Manually with Lambda

An example task is located in the repository that can be used to test the message parser in Lambda.

The message parser uber-jar should be added as a dependency to the task. The compiled task code, the message parser uber-jar, the cumulus message adapter zip, and any other dependencies should all be included in a zip file and uploaded to lambda. 

Instructions on the zip file folder structure are [here](https://docs.aws.amazon.com/lambda/latest/dg/create-deployment-pkg-zip-java.html)

### Integration Tests

Integration tests are located in the test folder in ```MessageParserTest.java```. To build and run the tests, run 

```mvn -B verify```
