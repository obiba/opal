package org.obiba.opal.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 *
 */
public class MessageCollectorTest {

    private MessageCollector collector;

    @Before
    public void setUp() {
        collector = new MessageCollector();
    }

    @Test
    public void testTaskMessagesAreCollected() throws Exception {
        String tableRef = "foo.bar";
        String text1 = "foo";
        String text2 = "bar";

        long time1 = System.currentTimeMillis();
        Thread.sleep(5);

        //verify no messages
        Assert.assertEquals(0, collector.getMessages(tableRef).size());

        MessageCollector.Task task = collector.createTask(tableRef);
        task.info("%s", text1);
        Thread.sleep(5);
        task.info(text2);

        Thread.sleep(5);
        long time2 = System.currentTimeMillis();

        //verify still no messages
        Assert.assertEquals(0, collector.getMessages(tableRef).size());

        task.close();
        List<Message> messages = collector.getMessages(tableRef);
        Assert.assertEquals(2, messages.size());

        Message msg1 = messages.get(0);
        Assert.assertEquals(text1, msg1.getMessage());
        Assert.assertTrue(time1 < msg1.getTimestamp());

        Message msg2 = messages.get(1);
        Assert.assertEquals(text2, msg2.getMessage());
        Assert.assertTrue(msg1.getTimestamp() < msg2.getTimestamp());
        Assert.assertTrue(msg2.getTimestamp() < time2);
    }

}
