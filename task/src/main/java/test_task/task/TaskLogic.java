package test_task.task;

import com.amazonaws.services.lambda.runtime.Context;

import cumulus_message_adapter.message_parser.AdapterLogger;
import cumulus_message_adapter.message_parser.ITask;
import cumulus_message_adapter.message_parser.Json;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.*;
import com.amazonaws.regions.*;

import com.google.gson.Gson;
import java.util.Map;


public class TaskLogic implements ITask
{
    /**
     * Get the SNS Topic Arn to publish to from the input JSON
     *
     * @param input - input JSON
     * @return Object containing the Topic Arn, null if not found
     */
    private Object GetTopicArn(String input)
    {
        Gson gson = new Gson();

        Map<String, Object> inputMap = Json.toMap(input);
        Object config = inputMap.get("config");

        if(config != null)
        {
            String configJson = gson.toJson(config);
            Map<String, Object> configMap = Json.toMap(configJson);
            return configMap.get("topic_arn");
        }

        return null;
    }

    /**
     * Get the Cumulus message to publish to SNS from the input JSON
     *
     * @param input - input JSON
     * @return Object containing the Cumulus message, null if not found
     */
    private Object GetCumulusMessage(String input)
    {
        Gson gson = new Gson();

        Map<String, Object> inputMap = Json.toMap(input);
        Object messageInput = inputMap.get("input");

        if(messageInput != null)
        {
            String messageInputJson = gson.toJson(messageInput);
            Map<String, Object> messageInputMap = Json.toMap(messageInputJson);
            return messageInputMap.get("full_cumulus_message");
        }

        return null;
    }

    /**
     * Sample business logic. Log an info message. Publish a message to an SNS
     * topic configured by an environment variable if the variable is present.
     *
     * @param input - input string
     * @param context - AWS Lambda context
     * @return JSON string
     */
    public String PerformFunction(String input, Context context)
    {
        AdapterLogger.LogInfo("Business logic input: " + input);

        Object topicArn = GetTopicArn(input);

        if(topicArn != null)
        {
            AmazonSNS snsClient =  AmazonSNSClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

            Object cumulusMessage = GetCumulusMessage(input);
            String message = "Test Message";

            if(cumulusMessage != null)
            {
                message = cumulusMessage.toString();
            }
            else
            {
                AdapterLogger.LogInfo("No cumulus message found in input, publishing test message to SNS.");
            }

            PublishRequest publishRequest = new PublishRequest(topicArn.toString(), message);
            PublishResult publishResult = snsClient.publish(publishRequest);
            AdapterLogger.LogInfo("Published message to SNS: " + publishResult.getMessageId());
        }
        else
        {
            AdapterLogger.LogInfo("No topic arn found. Skipping publishing to SNS.");
        }

        return "{\"status\":\"complete\"}";
    }
}
