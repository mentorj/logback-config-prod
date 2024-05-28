package com.javaxpert.logging.unittesting;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.turbo.DuplicateMessageFilter;
import ch.qos.logback.core.FileAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


import static org.assertj.core.api.Assertions.*;

/**
 * tests suite used to assert configuration options work as exoected.
 * Uses some Logback speecific classes as SLF4J  is not meant to do such testing.
 * Logger class as exposed by SLF4J does not provide a setLevel method
 * @author J..MOLIERE <jerome@javaxpert.com>
 */
public class LoggingTestsSuite {

    @Test
    void whenFetchingLargeMessageFromLogsFile_AnswerShouldBeTruncated(){
        org.slf4j.Logger logger = LoggerFactory.getLogger("fileLogger");
        String largeMessage= "";
        for(int i=0;i<256;i++){
            largeMessage+= "Hello";
        }
        logger.info(largeMessage);
        Path traceFilePath = Paths.get("log-test.log");
        String firstLine= "";
        try {
            firstLine = Files.readAllLines(traceFilePath).get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertThat(firstLine.length()).isGreaterThan(16);
        assertThat(firstLine.length()).isLessThan(256);
    }

    @Test
    void avoidMessagesRepetitionWithTurboFilters(){
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingTestsSuite.class);
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

        DuplicateMessageFilter filter = new DuplicateMessageFilter();
        filter.setAllowedRepetitions(4);
        filter.setCacheSize(30);
        filter.start();
        ctx.addTurboFilter(filter);

        PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(ctx);
        logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level – %msg%n");
        logEncoder.start();

        FileAppender fileAppender = new FileAppender<>();
        fileAppender.setFile("log-test-filtered.log");
        fileAppender.setContext(ctx);
        fileAppender.setAppend(false);
        fileAppender.setEncoder(logEncoder);
        fileAppender.start();
        logger.setLevel(Level.INFO);
        logger.addAppender(fileAppender);
        logger.setAdditive(false);

        for(int i=0;i<10;i++){
            logger.info("dummy trace again...");
        }
        Path traceFilePath = Paths.get("log-test-filtered.log");
        List<String> allLogEntries =  new ArrayList<>();
        try {
            allLogEntries = Files.readAllLines(traceFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertThat(allLogEntries.size()).isLessThan(10);
        assertThat(allLogEntries.size()).isEqualTo(5);
    }


    @Test
    void largeMessagesShouldBeTruncated_ProgrammaticConfig(){
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingTestsSuite.class);
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(ctx);
        logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level – %.-128msg%n");//here is the trick!!
        logEncoder.start();

        FileAppender fileAppender = new FileAppender<>();
        fileAppender.setFile("log-test-truncated-msg.log");
        fileAppender.setContext(ctx);
        fileAppender.setAppend(false);
        fileAppender.setEncoder(logEncoder);
        fileAppender.start();
        logger.setLevel(Level.INFO);
        logger.addAppender(fileAppender);
        logger.setAdditive(false);
        StringBuffer buffer = new StringBuffer();
        for(int i=0;i<1000;i++){
            buffer.append("Useless trace again");
        }
        logger.info(buffer.toString());

        Path traceFilePath = Paths.get("log-test-truncated-msg.log");
        List<String> allLogEntries =  new ArrayList<>();
        try {
            allLogEntries = Files.readAllLines(traceFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertThat(allLogEntries.get(0).length()).isBetween(20,200);
    }

    @Test
    void stackTraceCanBeTruncated(){
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingTestsSuite.class);
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(ctx);
        logEncoder.setPattern("%ex{3}%msg%n");//here is the trick!!
        logEncoder.start();
        FileAppender fileAppender = new FileAppender<>();
        fileAppender.setFile("log-tests-stacktrace.log");
        fileAppender.setContext(ctx);
        fileAppender.setAppend(false);
        fileAppender.setEncoder(logEncoder);
        fileAppender.start();
        logger.setLevel(Level.INFO);
        logger.addAppender(fileAppender);
        logger.setAdditive(false);


        // triggers an exception
        try{
            var result = 256/0;
        }catch (RuntimeException ex){
            logger.error("Oops exploded",ex);
        }

        // asseryions
        Path traceFilePath = Paths.get("log-tests-stacktrace.log");
        List<String> allLogEntries =  new ArrayList<>();
        try {
            allLogEntries = Files.readAllLines(traceFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertThat(allLogEntries.size()).isEqualTo(5);
        assertThat(allLogEntries.get(0)).contains("ArithmeticException");
        assertThat(allLogEntries.get(4)).contains("exploded");



    }
}
