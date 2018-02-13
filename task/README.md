# Example Task

This is an example Lambda Java task that uses the Cumulus Message Adapter. 

The business logic for the task is in `TaskLogic.java`. If the `TOPIC_ARN` environment variable is configured, a sample message is published to SNS. Sample JSON is returned by the function and will be included in the message adapter output. The business logic function also demonstrates using the `AdapterLogger` for logging.

## Lambda Configuration

The handler should be set to `test_task.task.Task::handleRequest`.

## SNS Configuration

To use this to send a message to SNS, first a topic must be created. 

Configure the topic to publish to by setting an environment variable `TOPIC_ARN` to the topic Arn.

The Lambda task execution role must have the `AWSLambdaSNSPublishPolicyExecutionRole` policy assigned to be able to publish a message. Memory should be at least 256MB.
