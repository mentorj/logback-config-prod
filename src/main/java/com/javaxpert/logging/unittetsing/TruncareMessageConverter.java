package com.javaxpert.logging.unittetsing;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.List;

/**
 * truncate message if NEEDED adding a suffix message indicating truncation.
 * @uathor J.MOLIERE - jerome@javaxpert.com
 */
public class TruncareMessageConverter extends ClassicConverter {

    private static final String TRUNCATION_SUFFIX = "... [truncated]";

    // parameter set while defining encoder
    private int msgLength =128 ;

    @Override
    public void start() {
        List<String> options = getOptionList();
        if(options!=null && !options.isEmpty()) {
            msgLength = Integer.parseInt(getFirstOption());
        }
        super.start();
    }
    @Override
    public String convert(ILoggingEvent event) {
        String formattedMessage = event.getFormattedMessage();
        if (formattedMessage == null ||
                formattedMessage.length() < msgLength) {
            return formattedMessage;
        }
        return new StringBuilder(TRUNCATION_SUFFIX+msgLength)
                .append(formattedMessage.substring(0, msgLength))
                .append(TRUNCATION_SUFFIX)
                .toString();
    }
}