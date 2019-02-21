package com.joe.utils.log.logback;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.status.StatusUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于在logback加载完毕后重新配置logback
 *
 * @author joe
 * @version 2018.07.11 21:19
 */
@Slf4j
public class LogbackReconfigure {
    private static final String        RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION = "Re-registering previous fallback "
                                                                                    + "configuration once more as a fallback configuration point";
    private static final String        FALLING_BACK_TO_SAFE_CONFIGURATION         = "Given previous errors, falling back to "
                                                                                    + "previously registered safe configuration.";
    private final long                 birthdate                                  = System
        .currentTimeMillis();

    private final static LoggerContext CONTEXT;

    static {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (factory instanceof LoggerContext) {
            CONTEXT = (LoggerContext) factory;
        } else {
            CONTEXT = null;
        }
    }

    private LogbackReconfigure() {
    }

    /**
     * 重新配置当前logback
     *
     * @param config logback的xml配置文件
     * @param context 要配置的context
     */
    public static void reconfigure(InputStream config, LoggerContext context) {
        if (context == null) {
            throw new NullPointerException("LoggerContext must not be null");
        }
        LoggerContext lc = context;
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(lc);
        StatusUtil statusUtil = new StatusUtil(lc);
        List<SaxEvent> eventList = jc.recallSafeConfiguration();

        URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(lc);
        lc.reset();
        long threshold = System.currentTimeMillis();
        try {
            jc.doConfigure(config);
            if (statusUtil.hasXMLParsingErrors(threshold)) {
                fallbackConfiguration(lc, eventList, mainURL);
            }
        } catch (JoranException e) {
            fallbackConfiguration(lc, eventList, mainURL);
        }
    }

    /**
     * 重新配置当前logback
     *
     * @param config logback的xml配置文件
     */
    public static void reconfigure(InputStream config) {
        if (CONTEXT == null) {
            log.warn("当前日志上下文不是logback，不能使用该配置器重新配置");
            return;
        }
        reconfigure(config, CONTEXT);
    }

    private static List<SaxEvent> removeIncludeEvents(List<SaxEvent> unsanitizedEventList) {
        List<SaxEvent> sanitizedEvents = new ArrayList<SaxEvent>();
        if (unsanitizedEventList == null) {
            return sanitizedEvents;
        }

        for (SaxEvent e : unsanitizedEventList) {
            if (!"include".equalsIgnoreCase(e.getLocalName())) {
                sanitizedEvents.add(e);
            }

        }
        return sanitizedEvents;
    }

    private static void fallbackConfiguration(LoggerContext lc, List<SaxEvent> eventList,
                                              URL mainURL) {
        // failsafe events are used only in case of errors. Therefore, we must *not*
        // quickInvoke file inclusion since the included files may be the cause of the error.

        List<SaxEvent> failsafeEvents = removeIncludeEvents(eventList);
        JoranConfigurator joranConfigurator = new JoranConfigurator();
        joranConfigurator.setContext(CONTEXT);
        ConfigurationWatchList oldCWL = ConfigurationWatchListUtil
            .getConfigurationWatchList(CONTEXT);
        ConfigurationWatchList newCWL = oldCWL.buildClone();

        if (failsafeEvents == null || failsafeEvents.isEmpty()) {
            log.warn("No previous configuration to fall back on.");
        } else {
            log.warn(FALLING_BACK_TO_SAFE_CONFIGURATION);
            try {
                lc.reset();
                ConfigurationWatchListUtil.registerConfigurationWatchList(CONTEXT, newCWL);
                joranConfigurator.doConfigure(failsafeEvents);
                log.info(RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);
                joranConfigurator.registerSafeConfiguration(eventList);

                log.info("after registerSafeConfiguration: " + eventList);
            } catch (JoranException e) {
                log.error("Unexpected exception thrown by a configuration considered safe.", e);
            }
        }
    }

    @Override
    public String toString() {
        return "LogbackReconfigure(born:" + birthdate + ")";
    }
}
