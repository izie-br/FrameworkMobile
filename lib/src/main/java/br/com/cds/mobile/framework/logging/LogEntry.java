package br.com.cds.mobile.framework.logging;

public interface LogEntry extends Cloneable{

	boolean save();
	void setMessage(String message);
	LogEntry clone();
	void setLevel(String level);

}
