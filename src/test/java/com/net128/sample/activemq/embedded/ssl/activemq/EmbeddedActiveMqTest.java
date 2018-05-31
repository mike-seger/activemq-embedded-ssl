package com.net128.sample.activemq.embedded.ssl.activemq;

import static org.junit.Assert.*;

import com.net128.sample.activemq.embedded.ssl.configuration.JmsTestConfiguration;
import com.net128.sample.activemq.embedded.ssl.configuration.ReadTestPropertiesConfiguration;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.Message;
import javax.jms.TextMessage;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ReadTestPropertiesConfiguration.class, JmsTestConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EmbeddedActiveMqTest {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedActiveMqTest.class);

    protected final static String TEST_QUEUE = "testQueue";
    protected final static String TEST_MESSAGE_STRING = "This is a text message!";

    @Autowired
    protected JmsTemplate mJmsTestTemplate;

    @Test
    public void testSendAndReceiver() throws Exception {
        logger.debug("About to send JMS message");
        mJmsTestTemplate.send(TEST_QUEUE,
            (inSession) -> {
                final TextMessage theTextMessage = new ActiveMQTextMessage();
                theTextMessage.setText(TEST_MESSAGE_STRING);
                return theTextMessage;
            });
        logger.debug("JMS message sent");

        logger.debug("About to receive JMS message");
        final Message theMessage = mJmsTestTemplate.receive(TEST_QUEUE);
        logger.debug("JMS message received");

        assertNotNull("There should be a message on the queue", theMessage);
        assertTrue("The message should be a text message", theMessage instanceof  TextMessage);
        final TextMessage theTextMessage = (TextMessage)theMessage;
        logger.debug("Contents of received message: {}", theTextMessage.getText());
        assertEquals("Message contents should match", TEST_MESSAGE_STRING, theTextMessage.getText());
    }
}
