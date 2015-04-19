package com.quantium.mobile.framework.logging;

import android.content.Context;
import android.util.Log;
import com.quantium.mobile.framework.BaseApplication;
import com.quantium.mobile.framework.utils.StringUtil;

public class AndroidLogImpl extends LogPadrao {

    private String tag;

    private LogEntry logEntryPrototype;

    @Override
    protected void setLogEntryPrototype(LogEntry logEntryPrototype) {
        this.logEntryPrototype = logEntryPrototype;
    }

    @Override
    protected void info(String message, Object... args) {
        if (args != null && args.length > 0)
            message = String.format(message, args);
        Log.i(getTag(), message);
        if (logEntryPrototype != null) {
            LogEntry logEntry = logEntryPrototype.clone();
            logEntry.log(LogEntry.LEVEL_INFO, message);
            logEntry.save();
        }
    }

    @Override
    protected void error(String message, Object... args) {
        if (message == null) {
            message = "Erro sem mensagem";
            if (args != null && args.length > 0) {
                message += " com argumentos";
                for (int i = 0; i < args.length; i++)
                    message += " %s ";
            }

        }
        if (args != null && args.length > 0)
            message = String.format(message, args);
        Log.e(getTag(), message);
        if (logEntryPrototype != null) {
            LogEntry logEntry = logEntryPrototype.clone();
            logEntry.log(LogEntry.LEVEL_ERROR, message);
            logEntry.save();
        }
    }

    @Override
    protected void error(Throwable t) {
        String stackTrace = StringUtil.getStackTrace(t);
        error(
                (stackTrace == null) ?
                        //
                        "Exception sem stack trace" :
                        //
                        stackTrace
        );
    }

    @Override
    public void debug(String message, Object... args) {
        if (args != null && args.length > 0)
            message = String.format(message, args);
        Log.d(getTag(), message);
    }

    private String getTag() {
        if (tag == null) {
            Context context = BaseApplication.getContext();
            if (context == null)
                return "Log";
            tag = context.getPackageName();
        }
        return tag;
    }

}
