package br.com.cds.mobile.framework.logging;

public interface LogEntry extends Cloneable{

	boolean save();
	LogEntry cloneFor(String message);
	void setLevel(String level);

}
