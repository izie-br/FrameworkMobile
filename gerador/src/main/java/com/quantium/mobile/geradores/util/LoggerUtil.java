package com.quantium.mobile.geradores.util;

import org.apache.log4j.Logger;

public class LoggerUtil {

    public static final String LOG_NAME = "com.quantium.mobile.geradores";
	private static Logger log;

    public static Logger getLog () {
        if (log == null)
            log = Logger.getLogger(LOG_NAME);
        return log;
    }

}
