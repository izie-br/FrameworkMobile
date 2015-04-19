package com.quantium.mobile.geradores.util;

import org.apache.log4j.Logger;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class LoggerUtil implements LogChute {

    public static final String LOG_NAME = "com.quantium.mobile.geradores";
    private static Logger log;

    public static Logger getLog() {
        if (log == null)
            log = Logger.getLogger(LOG_NAME);
        return log;
    }

    @Override
    public void init(RuntimeServices arg0) throws Exception {
        //pass
    }

    @Override
    public boolean isLevelEnabled(int arg0) {
        return true;
    }

    @Override
    public void log(int level, String message) {
        log(level, message, null);
    }

    @Override
    public void log(int level, String message, Throwable t) {
        Logger logInst = getLog();
        switch (level) {
            case LogChute.DEBUG_ID:
                //TODO O velocity parece ter um mau habito de logar tudo
                //     em DEBUG.
                //     Redirecionei tudo para INFO.
//			logInst.debug(message, t);
//			break;
            case LogChute.INFO_ID:
                logInst.info(message, t);
                break;
            case LogChute.TRACE_ID:
                logInst.trace(message, t);
                break;
            case LogChute.WARN_ID:
                logInst.warn(message, t);
                break;
            case LogChute.ERROR_ID:
                logInst.error(message, t);
                break;
            default:
                logInst.error("error level unknown " + message, t);
        }
    }

}
