package test_task.task;

import com.amazonaws.services.lambda.runtime.Context;

import cumulus_message_adapter.message_parser.AdapterLogger;
import cumulus_message_adapter.message_parser.ITask;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.*;
import com.amazonaws.regions.*;


public class TaskLogic implements ITask
{
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
        try
        {
            String topicArn = System.getenv("TOPIC_ARN");

            if(topicArn != null)
            {
                AmazonSNSClient snsClient = new AmazonSNSClient(new DefaultAWSCredentialsProviderChain());		                           
                snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));

                PublishRequest publishRequest = new PublishRequest(topicArn, "Test Message");
                PublishResult publishResult = snsClient.publish(publishRequest);
                AdapterLogger.LogInfo("Published message: " + publishResult.getMessageId());
            }
            else
            {
                AdapterLogger.LogInfo("No topic arn found. Skipping publishing to SNS.");
            }
        }
        catch(Exception e)
        {
            AdapterLogger.LogError(e.getMessage());
        }


        return "{\"status\":\"complete\"}";
    }
}