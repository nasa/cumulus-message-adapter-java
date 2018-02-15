# Example Task

This is an example Lambda Java task that uses the Cumulus Message Adapter. 

The business logic for the task is in `TaskLogic.java`. If an SNS topic arn is present in the input, a message is published to SNS. Sample JSON is returned by the function and will be included in the message adapter output. The business logic function also demonstrates using the `AdapterLogger` for logging.

## Lambda Configuration

The handler should be set to `test_task.task.Task::handleRequest`.

## SNS Configuration

To use this to send a message to SNS, first a topic must be created. 

The `topic_arn` should be in the `workflow_config` section of the cumulus message JSON string. To publish the Cumulus message to SNS, the `cumulus_message` should be in the `payload` section of the cumulus message JSON string. If no message is found, a test message will be published.

The Lambda task execution role must have the `AWSLambdaSNSPublishPolicyExecutionRole` policy assigned to be able to publish a message. Memory should be at least 256MB.
