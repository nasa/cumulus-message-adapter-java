package test_task.task;

import com.amazonaws.services.lambda.runtime.Context;

import cumulus_message_adapter.message_parser.AdapterLogger;
import cumulus_message_adapter.message_parser.ITask;

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

        Map inputMap = gson.fromJson(input, Map.class);

        Object config = inputMap.get("config");

        if(config != null)
        {
            Map configMap = gson.fromJson(gson.toJson(config), Map.class);
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

        Map inputMap = gson.fromJson(input, Map.class);

        Object inputJson = inputMap.get("input");

        if(inputJson != null)
        {
            Map messageInputMap = gson.fromJson(gson.toJson(inputJson), Map.class);
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
