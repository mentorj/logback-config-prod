package com.javaxpert.logging.unittesting;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.turbo.DuplicateMessageFilter;
import com.javaxpert.logging.unittetsing.MemoryAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import static org.assertj.core.api.Assertions.*;

/**
 * tests suite used to assert configuration options work as exoected.
 * Uses some Logback speecific classes as SLF4J  is not meant to do such testing.
 * Logger class as exposed by SLF4J does not provide a setLevel method
 * @author J..MOLIERE <jerome@javaxpert.com>
 */
public class LoggingTestsSuite {
    private MemoryAppender memoryAppender;
    private Logger logger;
    private final static String message = "Very useful trace indeed";

    @BeforeEach
    public void setup() {
        logger = (Logger) LoggerFactory.getLogger(LoggingTestsSuite.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.INFO);
        logger.addAppender(memoryAppender);
        logger.setAdditive(true);
        memoryAppender.start();
    }

    @Test
    /**
     *  filtering by level and how to assert expected messages to be logged
     */
    void whenSendingTraceEventsWithLevelInferiorToINFO_ThoseTRacesShouldDropped(){
        // sends a couple of messages, same message but with different levels
        logger.trace(message);
        logger.info(message);
        logger.debug(message);
        logger.info(message);
        logger.warn(message);
        logger.error(message);


        // assertions

        assertThat(memoryAppender.countEventsForLogger(LoggingTestsSuite.class.getName())).isEqualTo(4);
        assertThat(memoryAppender.search(message,Level.INFO).size()).isEqualTo(2);
        assertThat(memoryAppender.search(message,Level.TRACE).size()).isEqualTo(0);
    }

    @Test
    /*
     * shows TurboFilter in action, how to avoid duplicae messages
     */
    void sendingMultipleMessagesWithDuplicateFilterInPlace(){
        // adds Turbo filter tp the logger
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        DuplicateMessageFilter filter = new DuplicateMessageFilter();
        filter.setAllowedRepetitions(1);
        context.addTurboFilter(filter);
        memoryAppender.reset();
        filter.start();
        // send messages
        logger.info(message);
        logger.info(message);
        logger.info(message);
        logger.info(message);
        logger.warn(message);
        logger.warn(message);

        // assertions
        assertThat(memoryAppender.search(message).size()).isEqualTo(2);

    }

    @Test
    void largeMessagePayloadsShouldBeTruncated(){
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%d{HH:mm:ss.SSS} %-5level %logger{36} -   %.0-512msg%n");
        memoryAppender.setPatternLayoutEncoder(encoder);

        String largeMessage= "";
        for(int i=0;i<256;i++){
            largeMessage+= "Hello";
        }
        int origSize = largeMessage.length();
        logger.info(largeMessage);
        assertThat(memoryAppender.countEventsForLogger(LoggingTestsSuite.class.getName())).isEqualTo(1);
        assertThat(memoryAppender.search("Hello",Level.INFO).get(0).getMessage().length()).isLessThan(origSize);
    }
}
