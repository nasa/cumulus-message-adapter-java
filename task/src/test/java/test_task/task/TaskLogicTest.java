package test_task.task;

//import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TaskLogicTest
{
    static TaskLogic taskLogicObject;

    @BeforeAll
    public static void InitializeTaskTest()
    {
        taskLogicObject = new TaskLogic();
    }

    @Test
    public void taskLogicGetTopicArnIsNullReturn()
    {
        assertThrows(NullPointerException.class, () -> TaskLogicTest.taskLogicObject.PerformFunction(null, null));
    }
}
