package br.com.cds.mobile.framework;

import br.com.cds.mobile.framework.query.QuerySet;

public interface LogEntry extends Cloneable{

	boolean save();
	LogEntry cloneFor(String message);
	void setLevel(String level);
	String getFilePath();
	QuerySet<LogEntry> objects();

}
