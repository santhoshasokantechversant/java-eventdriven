package com.techversant.common_lib.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger("com.techversant.security");

    // Private constructor to prevent instantiation
    private AuditLogger() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void log(String user, String ip, String entity, String action, String status) {
        try {
            MDC.put("user", user != null ? user : "anonymous");
            MDC.put("ip", ip != null ? ip : "unknown");
            MDC.put("entity", entity != null ? entity : "unknown");
            MDC.put("status", status != null ? status : "SUCCESS");
            logger.info(action);
        } finally {
            MDC.clear();
        }
    }
}
