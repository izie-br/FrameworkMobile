package com.quantium.mobile.geradores.util;

import org.apache.log4j.Logger;

public class LoggerUtil {

    private static Logger log;

    public static Logger getLog () {
        if (log == null)
            log = Logger.getLogger("com.quantium.mobile.geradores");
        return log;
    }

}
